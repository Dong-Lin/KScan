[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Latest release](https://img.shields.io/github/v/release/ismai117/KScan?color=brightgreen&label=latest%20release)](https://github.com/ismai117/KScan/releases/latest)
[![Latest build](https://img.shields.io/github/v/release/ismai117/KScan?color=orange&include_prereleases&label=latest%20build)](https://github.com/ismai117/KScan/releases)
<br>
 
<h1 align="center">KScan</h1></br>

<p align="center">
Compose Multiplatform Barcode Scanning Library
</p>

<p align="center">
  <img alt="Platform Android" src="https://img.shields.io/badge/Platform-Android-brightgreen"/>
  <img alt="Platform iOS" src="https://img.shields.io/badge/Platform-iOS-lightgray"/>
</p>

Android|iOS
--|--
<img src="https://github.com/user-attachments/assets/eb8a72c2-ec3c-4e8c-9514-31c9a746c467"/>|<img src="https://github.com/user-attachments/assets/36900489-dea0-456b-bd17-00fcb49f9701"/>

<strong>KScan is a Compose Multiplatform library that makes it easy to scan barcodes in your apps</strong>

<br>

<strong>To integrate KScan into your project</strong>

Add the dependency in your common module's commonMain source set

<br>

```Kotlin
implementation("io.github.ismai117:KScan:0.2.0")
```

<br>

<strong>Android - MLKit</strong>
- Uses Google’s MLKit library for barcode scanning on Android

<strong>iOS - AVFoundation</strong>
- Utilizes Apple’s AVFoundation framework for camera setup and barcode scanning on iOS

<br>
Important: iOS requires you to add the "Privacy - Camera Usage Description" key to your Info.plist file inside xcode, you need to provide a reason for why you want to access the camera.
</br>

</br>

<strong>Basic Usage</strong>

To use KScan, simply add the ScannerView in your app like this:

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
            }
            is BarcodeResult.OnFailed -> {
                println("error: ${result.exception.message}")
            }
            BarcodeResult.OnCanceled -> {
                println("scan canceled")
            }
        }
    }
}
```

To dismiss the scanner, you need to manage your own state, set it to <strong>false</strong> in the right places inside the <strong>ScannerView</strong> block after you handle the results

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

If you want to remove the UI and just use the raw scanner, you can set the showUi parameter to false

```Kotlin
if (showScanner) {
    ScannerView(
        codeTypes = listOf(
            BarcodeFormats.FORMAT_QR_CODE,
            BarcodeFormats.FORMAT_EAN_13,
        ),
        showUi = false
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

To build a custom scanner UI with torch and zoom control, set showUi = false and use a ScannerController.

```Kotlin
val scannerController = remember { ScannerController() }

if (showScanner) {
    ScannerView(
        codeTypes = listOf(BarcodeFormat.FORMAT_ALL_FORMATS),
        showUi = false,
        scannerController = scannerController
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

    Column(
        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            scannerController.setTorch(!scannerController.torchEnabled)
        }) {
            Text("Torch ${if (scannerController.torchEnabled) "Off" else "On"}")
        }
        Slider(
            value = scannerController.zoomRatio,
            onValueChange = scannerController::setZoom,
            valueRange = 1f..scannerController.maxZoomRatio
        )
    }
}
```
