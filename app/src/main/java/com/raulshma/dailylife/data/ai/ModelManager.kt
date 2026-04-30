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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ModelManager"
private const val PREFS_NAME = "ai_models_prefs"
private const val KEY_DEFAULT_MODEL = "default_model_id"
private const val KEY_CATALOG_CACHE = "catalog_cache_json"
private const val CATALOG_URL = "https://gist.githubusercontent.com/raulshma/658a6e1d3e313b82a1996bba25c74acd/raw/models"
private const val MODELS_DIR = "ai_models"

@Singleton
class ModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val catalogType = Types.newParameterizedType(List::class.java, AIModel::class.java)
    private val catalogAdapter = moshi.adapter<List<AIModel>>(catalogType)

    private val _catalog = MutableStateFlow<List<AIModel>>(emptyList())
    val catalog = _catalog.asStateFlow()

    private val _isLoadingCatalog = MutableStateFlow(false)
    val isLoadingCatalog = _isLoadingCatalog.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError = _catalogError.asStateFlow()

    private val downloadStates = ConcurrentHashMap<String, MutableStateFlow<ModelDownloadState>>()
    private val downloadJobs = ConcurrentHashMap<String, Job>()

    private val modelsDir: File
        get() = File(context.filesDir, MODELS_DIR).also { it.mkdirs() }

    init {
        val cached = loadCachedCatalog()
        if (cached.isNotEmpty()) {
            _catalog.value = cached
            initDownloadStates(cached)
        }
    }

    fun getDownloadState(modelId: String): Flow<ModelDownloadState> {
        val state = downloadStates.getOrPut(modelId) {
            MutableStateFlow(
                if (modelFile(modelId).exists()) ModelDownloadState.Downloaded
                else ModelDownloadState.NotDownloaded
            )
        }
        return state.asStateFlow()
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
                MutableStateFlow(
                    if (modelFile(model.id).exists()) ModelDownloadState.Downloaded
                    else ModelDownloadState.NotDownloaded
                )
            }
        }
    }

    fun downloadModel(model: AIModel): Flow<Float> {
        val stateFlow = downloadStates.getOrPut(model.id) {
            MutableStateFlow(ModelDownloadState.NotDownloaded)
        }
        val progressFlow = MutableStateFlow(0f)
        stateFlow.value = ModelDownloadState.Downloading(0f)

        downloadJobs[model.id]?.cancel()
        downloadJobs[model.id] = scope.launch {
            try {
                val targetFile = modelFile(model.id)
                val tmpFile = File(targetFile.parent, "${targetFile.name}.tmp")

                val request = Request.Builder().url(model.downloadUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    stateFlow.value = ModelDownloadState.DownloadFailed("HTTP ${response.code}")
                    return@launch
                }

                val body = response.body ?: run {
                    stateFlow.value = ModelDownloadState.DownloadFailed("Empty response")
                    return@launch
                }

                val contentLength = body.contentLength()
                var bytesDownloaded = 0L

                var wasCancelled = false
                body.byteStream().use { input ->
                    tmpFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            if (downloadJobs[model.id]?.isActive != true) {
                                wasCancelled = true
                                break
                            }
                            output.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead
                            if (contentLength > 0) {
                                val progress = (bytesDownloaded.toFloat() / contentLength).coerceIn(0f, 1f)
                                progressFlow.value = progress
                                stateFlow.value = ModelDownloadState.Downloading(progress)
                            }
                        }
                    }
                }

                if (!wasCancelled) {
                    tmpFile.renameTo(targetFile)
                    stateFlow.value = ModelDownloadState.Downloaded
                } else {
                    tmpFile.delete()
                    stateFlow.value = ModelDownloadState.NotDownloaded
                }
            } catch (e: CancellationException) {
                File(modelFile(model.id).parent, "${modelFile(model.id).name}.tmp").delete()
                stateFlow.value = ModelDownloadState.NotDownloaded
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for ${model.id}", e)
                stateFlow.value = ModelDownloadState.DownloadFailed(e.message ?: "Download failed")
            } finally {
                downloadJobs.remove(model.id)
            }
        }

        return progressFlow.asStateFlow()
    }

    fun cancelDownload(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        downloadStates[modelId]?.value = ModelDownloadState.NotDownloaded
    }

    fun deleteModel(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
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
}
