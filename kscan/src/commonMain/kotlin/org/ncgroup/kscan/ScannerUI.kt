package org.ncgroup.kscan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScannerUI(
    onCancel: () -> Unit,
    torchEnabled: Boolean,
    onTorchEnabled: (Boolean) -> Unit,
    zoomRatio: Float,
    zoomRatioOnChange: (Float) -> Unit,
    maxZoomRatio: Float,
    colors: ScannerColors = scannerColors(),
) {
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

        ScannerBarcodeFrame(
            frameColor = colors.barcodeFrameColor,
        )

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
