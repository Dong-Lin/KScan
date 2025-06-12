package org.ncgroup.kscan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ScannerController {
    var torchEnabled by mutableStateOf(false)

    var zoomRatio by mutableStateOf(1f)

    var maxZoomRatio by mutableStateOf(1f)
        internal set

    internal var onTorchChange: ((Boolean) -> Unit)? = null
    internal var onZoomChange: ((Float) -> Unit)? = null

    fun setTorch(enabled: Boolean) {
        onTorchChange?.invoke(enabled)
    }

    fun setZoom(ratio: Float) {
        onZoomChange?.invoke(ratio)
    }
}
