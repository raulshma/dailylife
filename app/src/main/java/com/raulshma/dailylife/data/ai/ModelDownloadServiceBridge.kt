package com.raulshma.dailylife.data.ai

import com.raulshma.dailylife.domain.ModelDownloadState
import kotlinx.coroutines.flow.Flow

internal object ModelDownloadServiceBridge {
    private var _manager: ModelManager? = null

    fun attach(manager: ModelManager) {
        _manager = manager
    }

    fun detach(manager: ModelManager) {
        if (_manager === manager) _manager = null
    }

    fun cancelDownload(modelId: String) {
        _manager?.cancelDownload(modelId)
    }

    fun getDownloadState(modelId: String): ModelDownloadState {
        val flow = _manager?.getDownloadState(modelId) ?: return ModelDownloadState.NotDownloaded
        return (flow as? kotlinx.coroutines.flow.StateFlow)?.value ?: ModelDownloadState.NotDownloaded
    }

    fun getDownloadProgress(modelId: String): Float {
        return when (val state = getDownloadState(modelId)) {
            is ModelDownloadState.Downloading -> state.progress
            is ModelDownloadState.Resuming -> state.progress
            else -> 0f
        }
    }
}
