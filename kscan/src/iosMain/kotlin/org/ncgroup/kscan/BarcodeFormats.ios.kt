@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.ncgroup.kscan

actual object BarcodeFormats {
    actual val FORMAT_CODE_128: BarcodeFormat = BarcodeFormat.FORMAT_CODE_128
    actual val FORMAT_CODE_39: BarcodeFormat = BarcodeFormat.FORMAT_CODE_39
    actual val FORMAT_CODE_93: BarcodeFormat = BarcodeFormat.FORMAT_CODE_93
    actual val FORMAT_CODABAR: BarcodeFormat = BarcodeFormat.FORMAT_CODABAR
    actual val FORMAT_EAN_13: BarcodeFormat = BarcodeFormat.FORMAT_EAN_13
    actual val FORMAT_EAN_8: BarcodeFormat = BarcodeFormat.FORMAT_EAN_8
    actual val FORMAT_ITF: BarcodeFormat = BarcodeFormat.FORMAT_ITF
    actual val FORMAT_UPC_A: BarcodeFormat = BarcodeFormat.FORMAT_UPC_A
    actual val FORMAT_UPC_E: BarcodeFormat = BarcodeFormat.FORMAT_UPC_E
    actual val FORMAT_QR_CODE: BarcodeFormat = BarcodeFormat.FORMAT_QR_CODE
    actual val FORMAT_PDF417: BarcodeFormat = BarcodeFormat.FORMAT_PDF417
    actual val FORMAT_AZTEC: BarcodeFormat = BarcodeFormat.FORMAT_AZTEC
    actual val FORMAT_DATA_MATRIX: BarcodeFormat = BarcodeFormat.FORMAT_DATA_MATRIX
    actual val FORMAT_ALL_FORMATS: BarcodeFormat = BarcodeFormat.FORMAT_ALL_FORMATS
    actual val TYPE_UNKNOWN: BarcodeFormat = BarcodeFormat.TYPE_UNKNOWN
    actual val TYPE_CONTACT_INFO: BarcodeFormat = BarcodeFormat.TYPE_CONTACT_INFO
    actual val TYPE_EMAIL: BarcodeFormat = BarcodeFormat.TYPE_EMAIL
    actual val TYPE_ISBN: BarcodeFormat = BarcodeFormat.TYPE_ISBN
    actual val TYPE_PHONE: BarcodeFormat = BarcodeFormat.TYPE_PHONE
    actual val TYPE_PRODUCT: BarcodeFormat = BarcodeFormat.TYPE_PRODUCT
    actual val TYPE_SMS: BarcodeFormat = BarcodeFormat.TYPE_SMS
    actual val TYPE_TEXT: BarcodeFormat = BarcodeFormat.TYPE_TEXT
    actual val TYPE_URL: BarcodeFormat = BarcodeFormat.TYPE_URL
    actual val TYPE_WIFI: BarcodeFormat = BarcodeFormat.TYPE_WIFI
    actual val TYPE_GEO: BarcodeFormat = BarcodeFormat.TYPE_GEO
    actual val TYPE_CALENDAR_EVENT: BarcodeFormat = BarcodeFormat.TYPE_CALENDAR_EVENT
    actual val TYPE_DRIVER_LICENSE: BarcodeFormat = BarcodeFormat.TYPE_DRIVER_LICENSE
}
