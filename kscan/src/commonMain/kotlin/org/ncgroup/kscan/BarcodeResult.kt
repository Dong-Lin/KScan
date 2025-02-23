package org.ncgroup.kscan

sealed interface BarcodeResult {
    data class OnSuccess(val barcode: Barcode) : BarcodeResult

    data class OnFailed(val exception: Exception) : BarcodeResult

    data object OnCanceled : BarcodeResult
}
