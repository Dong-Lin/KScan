[![official project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Multiplatform project](https://github.com/KevinnZou/compose-multiplatform-library-template/actions/workflows/build.yml/badge.svg)](https://github.com/KevinnZou/compose-multiplatform-library-template/actions/workflows/build.yml)
[![Publish Wiki](https://github.com/KevinnZou/compose-multiplatform-library-template/actions/workflows/wiki.yml/badge.svg)](https://github.com/KevinnZou/compose-multiplatform-library-template/actions/workflows/wiki.yml)
[![Latest release](https://img.shields.io/github/v/release/ismai117/kscan?color=brightgreen&label=latest%20release)](https://github.com/ismai117/kscan/releases/latest)
[![Latest build](https://img.shields.io/github/v/release/ismai117/kscan?color=orange&include_prereleases&label=latest%20build)](https://github.com/ismai117/kscan/releases)
<br>
 
<h1 align="center">KScan</h1></br>

<p align="center">
Compose Multiplatform Barcode Scanning Library
</p>

<p align="center">
  <img alt="Platform Android" src="https://img.shields.io/badge/Platform-Android-brightgreen"/>
  <img alt="Platform iOS" src="https://img.shields.io/badge/Platform-iOS-lightgray"/>
</p>

<br>

KScan is a Compose Multiplatform library that makes it easy to scan barcodes in your apps.

#### Android - MLKit
- Uses Google’s MLKit library for barcode scanning on Android.

#### iOS - AVFoundation
- Utilizes Apple’s AVFoundation framework for camera setup and barcode scanning on iOS.

<br>

Basic Usage

To use KScan, simply add the ScannerView in your app like this:

```Kotlin
if (showScanner) {
    ScannerView(
        codeTypes =
            listOf(
                BarcodeFormats.FORMAT_QR_CODE,
                BarcodeFormats.FORMAT_EAN_13,
            ),
    ) { result ->
        when (result) {
            is BarcodeResult.OnSuccess -> {
                println("Barcode: ${result.barcode.data}, format: ${result.barcode.format}")
            }
            is BarcodeResult.OnFailed -> {
                println("error: ${result.exception.message}")
            }
            BarcodeResult.OnCanceled -> {
                showScanner = false
            }
        }
    }
}
```

To dismiss the scanner, set the showScanner variable to false in the corresponding cases within the ScannerView closure after processing the result.

```Kotlin
if (showScanner) {
    ScannerView(
        codeTypes = listOf(
            BarcodeFormats.FORMAT_QR_CODE,
            BarcodeFormats.FORMAT_EAN_13,
        )
    ) { result ->
        when (result) {
            is BarcodeResult.OnSuccess -> {
                println("Barcode: ${result.barcode.data}, format: ${result.barcode.format}")
                showScanner = false
            }
            is BarcodeResult.OnFailed -> {
                println("Error: ${result.exception.message}")
                showScanner = false
            }
            BarcodeResult.OnCanceled -> {
                showScanner = false
            }
        }
    }
}
```
