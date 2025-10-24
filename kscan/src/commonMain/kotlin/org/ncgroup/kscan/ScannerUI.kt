package org.ncgroup.kscan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A Composable function that displays the scanner UI.
 *
 * @param onCancel A callback function that is invoked when the cancel button is clicked.
 * @param torchEnabled A boolean value that indicates whether the torch is enabled.
 * @param onTorchEnabled A callback function that is invoked when the torch button is clicked.
 * @param zoomRatio A float value that represents the current zoom ratio.
 * @param zoomRatioOnChange A callback function that is invoked when the zoom ratio changes.
 * @param maxZoomRatio A float value that represents the maximum zoom ratio.
 * @param scanRegion An optional region of interest to highlight visually.
 * @param colors An optional [ScannerColors] object that specifies the colors for the scanner UI.
 */
@Composable
fun ScannerUI(
    onCancel: () -> Unit,
    torchEnabled: Boolean,
    onTorchEnabled: (Boolean) -> Unit,
    zoomRatio: Float,
    zoomRatioOnChange: (Float) -> Unit,
    maxZoomRatio: Float,
    scanRegion: ScanRegion? = null,
    colors: ScannerColors = scannerColors(),
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Scanner region overlay (if specified)
        if (scanRegion != null) {
            ScannerRegionOverlay(
                scanRegion = scanRegion,
                borderColor = colors.barcodeFrameColor,
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScannerHeader(
                onCancel = onCancel,
                torchEnabled = torchEnabled,
                onTorchEnabled = onTorchEnabled,
                containerColor = colors.headerContainerColor,
                navigationIconColor = colors.headerNavigationIconColor,
                titleColor = colors.headerTitleColor,
                actionIconColor = colors.headerActionIconColor,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Only show the default barcode frame if no scan region is specified
            if (scanRegion == null) {
                ScannerBarcodeFrame(
                    frameColor = colors.barcodeFrameColor,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ScannerZoomAdjuster(
                modifier = Modifier.padding(bottom = 30.dp),
                zoomRatio = zoomRatio,
                zoomRatioOnChange = zoomRatioOnChange,
                maxZoomRatio = maxZoomRatio,
                containerColor = colors.zoomControllerContainerColor,
                contentColor = colors.zoomControllerContentColor,
            )
        }
    }
}
