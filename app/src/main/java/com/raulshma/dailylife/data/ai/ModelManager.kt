package com.raulshma.dailylife.data.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.raulshma.dailylife.domain.AIModel
import com.raulshma.dailylife.domain.ModelDownloadState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ModelManager"
private const val PREFS_NAME = "ai_models_prefs"
private const val KEY_DEFAULT_MODEL = "default_model_id"
private const val KEY_CATALOG_CACHE = "catalog_cache_json"
private const val KEY_AI_ENABLED = "ai_enabled"
private const val CATALOG_URL = "https://gist.githubusercontent.com/raulshma/658a6e1d3e313b82a1996bba25c74acd/raw/models"
private const val MODELS_DIR = "ai_models"
private const val CHUNK_COUNT = 4
private const val BUFFER_SIZE = 65536 // 64KB

@Singleton
class ModelManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .retryOnConnectionFailure(true)
        .connectionPool(okhttp3.ConnectionPool(8, 5, TimeUnit.MINUTES))
        .protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
        .build()
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val catalogType = Types.newParameterizedType(List::class.java, AIModel::class.java)
    private val catalogAdapter = moshi.adapter<List<AIModel>>(catalogType)
    private val chunkMetaType = Types.newParameterizedType(List::class.java, ChunkMeta::class.java)
    private val chunkMetaAdapter = moshi.adapter<List<ChunkMeta>>(chunkMetaType)

    private val _catalog = MutableStateFlow<List<AIModel>>(emptyList())
    val catalog = _catalog.asStateFlow()

    private val _isLoadingCatalog = MutableStateFlow(false)
    val isLoadingCatalog = _isLoadingCatalog.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError = _catalogError.asStateFlow()

    private val downloadStates = ConcurrentHashMap<String, MutableStateFlow<ModelDownloadState>>()
    private val downloadJobs = ConcurrentHashMap<String, Job>()
    private val downloadSpeeds = ConcurrentHashMap<String, MutableStateFlow<String>>()

    fun getDownloadSpeed(modelId: String): Flow<String> {
        return downloadSpeeds.getOrPut(modelId) { MutableStateFlow("") }.asStateFlow()
    }

    private val modelsDir: File
        get() = File(context.filesDir, MODELS_DIR).also { it.mkdirs() }

    init {
        ModelDownloadServiceBridge.attach(this)
        val cached = loadCachedCatalog()
        if (cached.isNotEmpty()) {
            _catalog.value = cached
            initDownloadStates(cached)
        }
    }

    fun getDownloadState(modelId: String): Flow<ModelDownloadState> {
        val state = downloadStates.getOrPut(modelId) {
            MutableStateFlow(resolveInitialState(modelId))
        }
        return state.asStateFlow()
    }

    private fun resolveInitialState(modelId: String): ModelDownloadState {
        val targetFile = modelFile(modelId)
        if (targetFile.exists()) return ModelDownloadState.Downloaded

        val hasPartial = hasPartialDownload(modelId)
        if (hasPartial) {
            val progress = calculateResumeProgress(modelId)
            return ModelDownloadState.Resuming(progress)
        }
        return ModelDownloadState.NotDownloaded
    }

    private fun hasPartialDownload(modelId: String): Boolean {
        val metaFile = chunkMetaFile(modelId)
        if (!metaFile.exists()) return false
        val partsDir = partsDir(modelId)
        if (!partsDir.exists()) return false
        return partsDir.listFiles { f -> f.name.startsWith("part-") }?.isNotEmpty() == true
    }

    private fun calculateResumeProgress(modelId: String): Float {
        val meta = loadChunkMeta(modelId) ?: return 0f
        val totalBytes = meta.sumOf { it.end - it.start + 1 }
        val downloaded = meta.sumOf { chunk ->
            val partFile = File(partsDir(modelId), "part-${chunk.index}")
            minOf(partFile.length(), chunk.end - chunk.start + 1)
        }
        return if (totalBytes > 0) (downloaded.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
    }

    suspend fun fetchCatalog() {
        _isLoadingCatalog.value = true
        _catalogError.value = null
        try {
            val remote = fetchRemoteCatalogWithRetry()
            _catalog.value = remote
            initDownloadStates(remote)
            saveCatalogCache(remote)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch catalog", e)
            val cached = loadCachedCatalog()
            if (cached.isNotEmpty() && _catalog.value.isEmpty()) {
                _catalog.value = cached
                initDownloadStates(cached)
            }
            if (_catalog.value.isEmpty()) {
                _catalogError.value = when (e) {
                    is UnknownHostException -> "No internet connection. Please check your network and try again."
                    else -> e.message ?: "Failed to load model catalog"
                }
            }
        } finally {
            _isLoadingCatalog.value = false
        }
    }

    private suspend fun fetchRemoteCatalogWithRetry(): List<AIModel> {
        var lastException: Exception? = null
        repeat(3) { attempt ->
            try {
                return fetchRemoteCatalog()
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Catalog fetch attempt ${attempt + 1} failed", e)
                if (attempt < 2) delay(1000L * (attempt + 1))
            }
        }
        throw lastException ?: IllegalStateException("Catalog fetch failed after retries")
    }

    private suspend fun fetchRemoteCatalog(): List<AIModel> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(CATALOG_URL)
            .header("User-Agent", "DailyLife-Android/1.0")
            .header("Accept", "application/json")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("HTTP ${response.code}")
        }
        val body = response.body?.string() ?: throw IllegalStateException("Empty response body")
        parseCatalogJson(body)
    }

    private fun loadCachedCatalog(): List<AIModel> {
        return try {
            val json = prefs.getString(KEY_CATALOG_CACHE, null) ?: return emptyList()
            parseCatalogJson(json)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load cached catalog", e)
            emptyList()
        }
    }

    private fun saveCatalogCache(models: List<AIModel>) {
        try {
            val json = catalogAdapter.toJson(models)
            prefs.edit().putString(KEY_CATALOG_CACHE, json).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache catalog", e)
        }
    }

    private fun parseCatalogJson(json: String): List<AIModel> {
        return catalogAdapter.fromJson(json) ?: emptyList()
    }

    private fun initDownloadStates(models: List<AIModel>) {
        for (model in models) {
            downloadStates.getOrPut(model.id) {
                MutableStateFlow(resolveInitialState(model.id))
            }
        }
    }

    fun resumeModel(model: AIModel): Flow<Float> {
        return downloadModel(model, isResume = true)
    }

    fun downloadModel(model: AIModel): Flow<Float> {
        return downloadModel(model, isResume = false)
    }

    private fun downloadModel(model: AIModel, isResume: Boolean): Flow<Float> {
        val stateFlow = downloadStates.getOrPut(model.id) {
            MutableStateFlow(ModelDownloadState.NotDownloaded)
        }
        val progressFlow = MutableStateFlow(0f)
        val speedFlow = downloadSpeeds.getOrPut(model.id) { MutableStateFlow("") }
        speedFlow.value = ""

        if (isResume) {
            stateFlow.value = ModelDownloadState.Resuming(calculateResumeProgress(model.id))
        } else {
            stateFlow.value = ModelDownloadState.Downloading(0f)
        }

        // Start foreground service
        ModelDownloadService.start(context, model.id, model.name)

        downloadJobs[model.id]?.cancel()
        downloadJobs[model.id] = scope.launch {
            try {
                val targetFile = modelFile(model.id)
                val partsDir = partsDir(model.id)
                partsDir.mkdirs()

                val contentLength = if (isResume && hasPartialDownload(model.id)) {
                    fetchContentLength(model.downloadUrl) ?: run {
                        stateFlow.value = ModelDownloadState.DownloadFailed("Cannot determine file size")
                        return@launch
                    }
                } else {
                    // Fresh download: clear any stale partials
                    clearPartialDownload(model.id)
                    fetchContentLength(model.downloadUrl) ?: run {
                        stateFlow.value = ModelDownloadState.DownloadFailed("Cannot determine file size")
                        return@launch
                    }
                }

                val chunks = if (isResume && hasPartialDownload(model.id)) {
                    loadChunkMeta(model.id)?.takeIf { it.isNotEmpty() } ?: createChunks(contentLength)
                } else {
                    createChunks(contentLength)
                }

                // Validate chunk metadata matches content length
                if (chunks.sumOf { it.end - it.start + 1 } != contentLength) {
                    clearPartialDownload(model.id)
                }

                val validChunks = if (hasPartialDownload(model.id)) {
                    loadChunkMeta(model.id) ?: createChunks(contentLength)
                } else {
                    createChunks(contentLength)
                }

                saveChunkMeta(model.id, validChunks)

                val totalBytes = validChunks.sumOf { it.end - it.start + 1 }
                val downloadedBytes = MutableStateFlow(0L)
                val progressMutex = Mutex()
                val speedTracker = SpeedTracker()

                // Count already downloaded bytes
                val existingBytes = validChunks.sumOf { chunk ->
                    val partFile = File(partsDir, "part-${chunk.index}")
                    minOf(partFile.length(), chunk.end - chunk.start + 1)
                }
                downloadedBytes.value = existingBytes
                if (totalBytes > 0) {
                    val initialProgress = (existingBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                    progressFlow.value = initialProgress
                    stateFlow.value = ModelDownloadState.Downloading(initialProgress)
                }

                // Launch parallel chunk downloads
                val chunkJobs = validChunks.map { chunk ->
                    async(Dispatchers.IO) {
                        downloadChunk(model, chunk, partsDir, downloadedBytes, totalBytes, progressFlow, stateFlow, speedFlow, progressMutex, speedTracker)
                    }
                }

                chunkJobs.awaitAll()

                if (!isActive) {
                    stateFlow.value = ModelDownloadState.Resuming(calculateResumeProgress(model.id))
                    return@launch
                }

                // Merge chunks into final file
                val merged = mergeChunks(model.id, validChunks, targetFile)
                if (!merged) {
                    stateFlow.value = ModelDownloadState.DownloadFailed("Failed to merge download parts")
                    return@launch
                }

                clearPartialDownload(model.id)
                stateFlow.value = ModelDownloadState.Downloaded
                progressFlow.value = 1f
                speedFlow.value = ""
            } catch (e: CancellationException) {
                val progress = calculateResumeProgress(model.id)
                if (progress > 0f && progress < 1f) {
                    stateFlow.value = ModelDownloadState.Resuming(progress)
                } else {
                    stateFlow.value = ModelDownloadState.NotDownloaded
                }
                speedFlow.value = ""
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for ${model.id}", e)
                val progress = calculateResumeProgress(model.id)
                if (progress > 0f && progress < 1f) {
                    stateFlow.value = ModelDownloadState.Resuming(progress)
                } else {
                    stateFlow.value = ModelDownloadState.DownloadFailed(e.message ?: "Download failed")
                }
                speedFlow.value = ""
            } finally {
                downloadJobs.remove(model.id)
            }
        }

        return progressFlow.asStateFlow()
    }

    private suspend fun downloadChunk(
        model: AIModel,
        chunk: ChunkMeta,
        partsDir: File,
        downloadedBytes: MutableStateFlow<Long>,
        totalBytes: Long,
        progressFlow: MutableStateFlow<Float>,
        stateFlow: MutableStateFlow<ModelDownloadState>,
        speedFlow: MutableStateFlow<String>,
        progressMutex: Mutex,
        speedTracker: SpeedTracker,
    ) {
        val partFile = File(partsDir, "part-${chunk.index}")
        val chunkSize = chunk.end - chunk.start + 1
        val existingSize = partFile.length()

        if (existingSize >= chunkSize) {
            // Chunk already complete
            return
        }

        val rangeStart = chunk.start + existingSize
        val rangeEnd = chunk.end

        val request = Request.Builder()
            .url(model.downloadUrl)
            .header("Range", "bytes=$rangeStart-$rangeEnd")
            .header("User-Agent", "DailyLife-Android/1.0")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful && response.code != 206) {
            throw IllegalStateException("HTTP ${response.code} for chunk ${chunk.index}")
        }

        val body = response.body ?: throw IllegalStateException("Empty body for chunk ${chunk.index}")

        RandomAccessFile(partFile, "rwd").use { raf ->
            raf.seek(existingSize)
            body.byteStream().use { input ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (!coroutineContext.isActive) break
                    raf.write(buffer, 0, bytesRead)
                    val added = bytesRead.toLong()
                    progressMutex.withLock {
                        val newTotal = downloadedBytes.value + added
                        downloadedBytes.value = newTotal
                        if (totalBytes > 0) {
                            val progress = (newTotal.toFloat() / totalBytes).coerceIn(0f, 1f)
                            progressFlow.value = progress
                            stateFlow.value = ModelDownloadState.Downloading(progress)
                        }
                        speedTracker.bytesSinceCheck += added
                        val now = System.currentTimeMillis()
                        val elapsed = now - speedTracker.lastTimeMs
                        if (elapsed >= 1000L) {
                            val bps = (speedTracker.bytesSinceCheck * 1000.0 / elapsed).toLong()
                            speedFlow.value = formatSpeed(bps)
                            speedTracker.lastTimeMs = now
                            speedTracker.bytesSinceCheck = 0L
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchContentLength(url: String): Long? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .head()
            .header("User-Agent", "DailyLife-Android/1.0")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext null
        response.header("Content-Length")?.toLongOrNull()
    }

    private fun createChunks(contentLength: Long): List<ChunkMeta> {
        val chunkSize = contentLength / CHUNK_COUNT
        return (0 until CHUNK_COUNT).map { index ->
            val start = index * chunkSize
            val end = if (index == CHUNK_COUNT - 1) contentLength - 1 else (start + chunkSize - 1).coerceAtLeast(start)
            ChunkMeta(index, start, end)
        }
    }

    private fun mergeChunks(modelId: String, chunks: List<ChunkMeta>, targetFile: File): Boolean {
        return try {
            val partsDir = partsDir(modelId)
            targetFile.outputStream().use { output ->
                chunks.sortedBy { it.start }.forEach { chunk ->
                    val partFile = File(partsDir, "part-${chunk.index}")
                    partFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to merge chunks for $modelId", e)
            targetFile.delete()
            false
        }
    }

    private fun partsDir(modelId: String): File {
        return File(context.cacheDir, "ai_model_parts/$modelId").also { it.mkdirs() }
    }

    private fun chunkMetaFile(modelId: String): File {
        return File(partsDir(modelId), "meta.json")
    }

    private fun saveChunkMeta(modelId: String, chunks: List<ChunkMeta>) {
        try {
            val json = chunkMetaAdapter.toJson(chunks)
            chunkMetaFile(modelId).writeText(json)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save chunk meta for $modelId", e)
        }
    }

    private fun loadChunkMeta(modelId: String): List<ChunkMeta>? {
        return try {
            val file = chunkMetaFile(modelId)
            if (!file.exists()) return null
            chunkMetaAdapter.fromJson(file.readText())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load chunk meta for $modelId", e)
            null
        }
    }

    private fun clearPartialDownload(modelId: String) {
        val dir = partsDir(modelId)
        if (dir.exists()) dir.deleteRecursively()
    }

    fun cancelDownload(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        val progress = calculateResumeProgress(modelId)
        downloadStates[modelId]?.value = if (progress > 0f && progress < 1f) {
            ModelDownloadState.Resuming(progress)
        } else {
            ModelDownloadState.NotDownloaded
        }
    }

    fun clearDownload(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        clearPartialDownload(modelId)
        downloadStates[modelId]?.value = ModelDownloadState.NotDownloaded
        downloadSpeeds[modelId]?.value = ""
    }

    fun deleteModel(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        clearPartialDownload(modelId)
        val file = modelFile(modelId)
        if (file.exists()) file.delete()
        val cacheDir = File(context.cacheDir, "ai_model_cache/$modelId")
        if (cacheDir.exists()) cacheDir.deleteRecursively()
        downloadStates[modelId]?.value = ModelDownloadState.NotDownloaded
        if (getDefaultModelId() == modelId) {
            clearDefaultModel()
        }
    }

    fun getDownloadedModels(): List<AIModel> {
        return _catalog.value.filter { modelFile(it.id).exists() }
    }

    fun getDefaultModelId(): String? {
        return prefs.getString(KEY_DEFAULT_MODEL, null)
    }

    fun setDefaultModelId(modelId: String) {
        prefs.edit().putString(KEY_DEFAULT_MODEL, modelId).apply()
    }

    fun clearDefaultModel() {
        prefs.edit().remove(KEY_DEFAULT_MODEL).apply()
    }

    private val _aiEnabled = MutableStateFlow(prefs.getBoolean(KEY_AI_ENABLED, true))
    val aiEnabled = _aiEnabled.asStateFlow()

    fun isAiEnabled(): Boolean = _aiEnabled.value

    fun setAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_ENABLED, enabled).apply()
        _aiEnabled.value = enabled
    }

    fun getDefaultModel(): AIModel? {
        val id = getDefaultModelId()
        if (id != null) {
            return _catalog.value.find { it.id == id }?.takeIf { modelFile(it.id).exists() }
        }
        return getDownloadedModels().firstOrNull()
    }

    fun modelFile(modelId: String): File {
        return File(modelsDir, "$modelId.litertlm")
    }

    fun modelCacheDir(modelId: String): File {
        return File(context.cacheDir, "ai_model_cache/$modelId").also { it.mkdirs() }
    }

    fun getStorageUsage(): Long {
        return modelsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun isModelDownloaded(modelId: String): Boolean {
        return modelFile(modelId).exists()
    }

    fun canResume(modelId: String): Boolean {
        return hasPartialDownload(modelId) && !isModelDownloaded(modelId)
    }

    private data class ChunkMeta(
        val index: Int,
        val start: Long,
        val end: Long,
    )

    private class SpeedTracker {
        var lastTimeMs: Long = System.currentTimeMillis()
        var bytesSinceCheck: Long = 0L
    }
}

private fun formatSpeed(bytesPerSecond: Long): String {
    if (bytesPerSecond < 1024) return "$bytesPerSecond B/s"
    val kb = bytesPerSecond / 1024.0
    if (kb < 1024) return "${"%.1f".format(kb)} KB/s"
    val mb = kb / 1024.0
    return "${"%.1f".format(mb)} MB/s"
}
