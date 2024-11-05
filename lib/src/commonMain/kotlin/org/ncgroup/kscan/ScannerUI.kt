package org.ncgroup.kscan

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
internal fun ScannerUI(
    onCancel: () -> Unit,
    torchEnabled: Boolean,
    onTorchEnabled: (Boolean) -> Unit,
    zoomRatio: Float,
    zoomRatioOnChange: (Float) -> Unit,
    maxZoomRatio: Float,
    colors: ScannerColors = scannerColors(),
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
    ) {
        val (topBar, frame, zoomController) = createRefs()

        ScannerHeader(
            modifier =
                Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            onCancel = onCancel,
            torchEnabled = torchEnabled,
            onTorchEnabled = onTorchEnabled,
            containerColor = colors.headerContainerColor,
            navigationIconColor = colors.headerNavigationIconColor,
            titleColor = colors.headerTitleColor,
            actionIconColor = colors.headerActionIconColor,
        )

        ScannerBarcodeFrame(
            modifier =
                Modifier.constrainAs(frame) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            frameColor = colors.barcodeFrameColor,
        )

        ScannerZoomAdjuster(
            modifier =
                Modifier.constrainAs(zoomController) {
                    top.linkTo(frame.bottom, margin = 30.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            zoomRatio = zoomRatio,
            zoomRatioOnChange = zoomRatioOnChange,
            maxZoomRatio = maxZoomRatio,
            containerColor = colors.zoomControllerContainerColor,
            contentColor = colors.zoomControllerContentColor,
        )
    }
}
