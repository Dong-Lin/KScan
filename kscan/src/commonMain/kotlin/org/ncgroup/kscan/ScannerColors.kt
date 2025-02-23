package org.ncgroup.kscan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ScannerColors(
    val headerContainerColor: Color = Color(0xFF291544),
    val headerNavigationIconColor: Color = Color.White,
    val headerTitleColor: Color = Color.White,
    val headerActionIconColor: Color = Color.White,
    val zoomControllerContainerColor: Color = Color(0xFF291544),
    val zoomControllerContentColor: Color = Color.White,
    val barcodeFrameColor: Color = Color(0xFFF050F8),
)

@Composable
fun scannerColors(
    headerContainerColor: Color = Color(0xFF291544),
    headerNavigationIconColor: Color = Color.White,
    headerTitleColor: Color = Color.White,
    headerActionIconColor: Color = Color.White,
    zoomControllerContainerColor: Color = Color(0xFF291544),
    zoomControllerContentColor: Color = Color.White,
    barcodeFrameColor: Color = Color(0xFFF050F8),
): ScannerColors {
    return ScannerColors(
        headerContainerColor = headerContainerColor,
        headerNavigationIconColor = headerNavigationIconColor,
        headerTitleColor = headerTitleColor,
        headerActionIconColor = headerActionIconColor,
        zoomControllerContainerColor = zoomControllerContainerColor,
        zoomControllerContentColor = zoomControllerContentColor,
        barcodeFrameColor = barcodeFrameColor,
    )
}
