package org.ncgroup.kscan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A composable that draws a visual overlay for the scan region.
 *
 * This composable renders a semi-transparent dark overlay over the entire screen
 * with a transparent cutout for the scan region, and an optional border around
 * the scan region to guide the user.
 *
 * @param modifier The modifier to be applied to the overlay.
 * @param scanRegion The region of interest to highlight. If null, no overlay is shown.
 * @param overlayColor The color of the dimmed overlay outside the scan region.
 * @param borderColor The color of the border around the scan region.
 * @param borderWidth The width of the border stroke in pixels.
 * @param cornerRadius The radius of the rounded corners of the scan region.
 */
@Composable
internal fun ScannerRegionOverlay(
    modifier: Modifier = Modifier,
    scanRegion: ScanRegion?,
    overlayColor: Color = Color.Black.copy(alpha = 0.5f),
    borderColor: Color = Color.White,
    borderWidth: Float = 4f,
    cornerRadius: Float = 16f,
) {
    if (scanRegion == null) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Calculate the actual pixel coordinates from normalized values
        val regionLeft = scanRegion.left * canvasWidth
        val regionTop = scanRegion.top * canvasHeight
        val regionWidth = scanRegion.width * canvasWidth
        val regionHeight = scanRegion.height * canvasHeight

        // Draw the dimmed overlay over the entire screen
        drawRect(
            color = overlayColor,
            size = size,
        )

        // Cut out the scan region (make it transparent)
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(regionLeft, regionTop),
            size = Size(regionWidth, regionHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear,
        )

        // Draw the border around the scan region
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(regionLeft, regionTop),
            size = Size(regionWidth, regionHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = borderWidth),
        )
    }
}
