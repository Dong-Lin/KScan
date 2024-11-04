package org.ncgroup.kscan

enum class BarcodeFormat {
    FORMAT_CODE_128,
    FORMAT_CODE_39,
    FORMAT_CODE_93,
    FORMAT_CODABAR,
    FORMAT_EAN_13,
    FORMAT_EAN_8,
    FORMAT_ITF,
    FORMAT_UPC_A,
    FORMAT_UPC_E,
    FORMAT_QR_CODE,
    FORMAT_PDF417,
    FORMAT_AZTEC,
    FORMAT_DATA_MATRIX,
    FORMAT_ALL_FORMATS,
}

expect object BarcodeFormats {
    val FORMAT_CODE_128: BarcodeFormat
    val FORMAT_CODE_39: BarcodeFormat
    val FORMAT_CODE_93: BarcodeFormat
    val FORMAT_CODABAR: BarcodeFormat
    val FORMAT_EAN_13: BarcodeFormat
    val FORMAT_EAN_8: BarcodeFormat
    val FORMAT_ITF: BarcodeFormat
    val FORMAT_UPC_A: BarcodeFormat
    val FORMAT_UPC_E: BarcodeFormat
    val FORMAT_QR_CODE: BarcodeFormat
    val FORMAT_PDF417: BarcodeFormat
    val FORMAT_AZTEC: BarcodeFormat
    val FORMAT_DATA_MATRIX: BarcodeFormat
    val FORMAT_ALL_FORMATS: BarcodeFormat
}
