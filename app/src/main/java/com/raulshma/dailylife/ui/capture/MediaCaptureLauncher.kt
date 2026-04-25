package com.raulshma.dailylife.ui.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class MediaCaptureLauncher(
    private val context: Context,
) {
    var onPhotoCaptured: ((Uri) -> Unit)? = null
    var onVideoCaptured: ((Uri) -> Unit)? = null
    var onPhotoPicked: ((Uri) -> Unit)? = null
    var onVideoPicked: ((Uri) -> Unit)? = null
    var onFilePicked: ((Uri) -> Unit)? = null
    var onCameraPermissionResult: ((Boolean) -> Unit)? = null
    var onAudioPermissionResult: ((Boolean) -> Unit)? = null

    private var pendingPhotoUri: Uri? = null
    private var pendingVideoUri: Uri? = null

    private var takePictureCallback: ((Uri) -> Unit)? = null
    private var captureVideoCallback: ((Uri) -> Unit)? = null
    private var requestCameraPermissionCallback: (() -> Unit)? = null
    private var requestAudioPermissionCallback: (() -> Unit)? = null
    private var pickPhotoCallback: (() -> Unit)? = null
    private var pickVideoCallback: (() -> Unit)? = null
    private var pickFileCallback: (() -> Unit)? = null

    internal fun registerTakePicture(callback: (Uri) -> Unit) {
        takePictureCallback = callback
    }

    internal fun registerCaptureVideo(callback: (Uri) -> Unit) {
        captureVideoCallback = callback
    }

    internal fun registerRequestCameraPermission(callback: () -> Unit) {
        requestCameraPermissionCallback = callback
    }

    internal fun registerRequestAudioPermission(callback: () -> Unit) {
        requestAudioPermissionCallback = callback
    }

    internal fun registerPickPhoto(callback: () -> Unit) {
        pickPhotoCallback = callback
    }

    internal fun registerPickVideo(callback: () -> Unit) {
        pickVideoCallback = callback
    }

    internal fun registerPickFile(callback: () -> Unit) {
        pickFileCallback = callback
    }

    fun launchCamera() {
        pendingPhotoUri = context.createMediaFileUri("photos", "jpg")
        pendingPhotoUri?.let { uri ->
            takePictureCallback?.invoke(uri)
        }
    }

    fun launchVideoCamera() {
        pendingVideoUri = context.createMediaFileUri("videos", "mp4")
        pendingVideoUri?.let { uri ->
            captureVideoCallback?.invoke(uri)
        }
    }

    fun launchPhotoPicker() {
        pickPhotoCallback?.invoke()
    }

    fun launchVideoPicker() {
        pickVideoCallback?.invoke()
    }

    fun launchFilePicker() {
        pickFileCallback?.invoke()
    }

    fun requestCameraPermissionIfNeeded() {
        if (hasCameraPermission(context)) {
            onCameraPermissionResult?.invoke(true)
        } else {
            requestCameraPermissionCallback?.invoke()
        }
    }

    fun requestAudioPermissionIfNeeded() {
        if (hasAudioPermission(context)) {
            onAudioPermissionResult?.invoke(true)
        } else {
            requestAudioPermissionCallback?.invoke()
        }
    }

    internal fun onTakePictureResult(success: Boolean) {
        if (success) {
            pendingPhotoUri?.let { onPhotoCaptured?.invoke(it) }
        }
        pendingPhotoUri = null
    }

    internal fun onCaptureVideoResult(success: Boolean) {
        if (success) {
            pendingVideoUri?.let { onVideoCaptured?.invoke(it) }
        }
        pendingVideoUri = null
    }
}

@Composable
fun rememberMediaCaptureLauncher(
    context: Context,
    onPhotoCaptured: (Uri) -> Unit,
    onVideoCaptured: (Uri) -> Unit,
    onPhotoPicked: (Uri) -> Unit,
    onVideoPicked: (Uri) -> Unit,
    onFilePicked: (Uri) -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit = {},
    onAudioPermissionResult: (Boolean) -> Unit = {},
): MediaCaptureLauncher {
    val launcher = remember { MediaCaptureLauncher(context) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        launcher.onTakePictureResult(success)
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
    ) { success ->
        launcher.onCaptureVideoResult(success)
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { onPhotoPicked(it) }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { onVideoPicked(it) }
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { onFilePicked(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onCameraPermissionResult(granted)
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onAudioPermissionResult(granted)
    }

    launcher.onPhotoCaptured = onPhotoCaptured
    launcher.onVideoCaptured = onVideoCaptured
    launcher.onPhotoPicked = onPhotoPicked
    launcher.onVideoPicked = onVideoPicked
    launcher.onFilePicked = onFilePicked
    launcher.onCameraPermissionResult = onCameraPermissionResult
    launcher.onAudioPermissionResult = onAudioPermissionResult
    launcher.registerTakePicture { uri: Uri -> takePictureLauncher.launch(uri) }
    launcher.registerCaptureVideo { uri: Uri -> captureVideoLauncher.launch(uri) }
    launcher.registerPickPhoto { pickPhotoLauncher.launch(androidx.activity.result.PickVisualMediaRequest()) }
    launcher.registerPickVideo { pickVideoLauncher.launch(androidx.activity.result.PickVisualMediaRequest()) }
    launcher.registerPickFile { pickFileLauncher.launch("*/*") }
    launcher.registerRequestCameraPermission { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
    launcher.registerRequestAudioPermission { audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }

    return launcher
}

fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
}

fun hasAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.createMediaFileUri(subDir: String, extension: String): Uri {
    val dir = File(filesDir, "media/$subDir").apply { mkdirs() }
    val file = File(dir, "${System.currentTimeMillis()}.$extension")
    return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
}
