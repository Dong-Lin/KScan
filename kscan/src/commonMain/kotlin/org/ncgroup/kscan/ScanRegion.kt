package org.ncgroup.kscan

import androidx.compose.runtime.Immutable

/**
 * Represents a region of interest (ROI) for barcode scanning.
 *
 * The scan region defines a rectangular area within the camera preview where barcode
 * detection should occur. Coordinates are specified as fractions (0.0 to 1.0) of the
 * preview dimensions to ensure consistency across different screen sizes and orientations.
 *
 * @property left The left edge of the scan region as a fraction of the preview width (0.0 to 1.0).
 * @property top The top edge of the scan region as a fraction of the preview height (0.0 to 1.0).
 * @property width The width of the scan region as a fraction of the preview width (0.0 to 1.0).
 * @property height The height of the scan region as a fraction of the preview height (0.0 to 1.0).
 *
 * @throws IllegalArgumentException if any value is outside the range [0.0, 1.0] or if width/height are 0.
 *
 * Example:
 * ```kotlin
 * // Center square region covering 50% of width and height
 * val centerRegion = ScanRegion(
 *     left = 0.25f,
 *     top = 0.25f,
 *     width = 0.5f,
 *     height = 0.5f
 * )
 * ```
 */
@Immutable
data class ScanRegion(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
) {
    init {
        require(left in 0.0f..1.0f) { "left must be between 0.0 and 1.0, got $left" }
        require(top in 0.0f..1.0f) { "top must be between 0.0 and 1.0, got $top" }
        require(width in 0.0f..1.0f) { "width must be between 0.0 and 1.0, got $width" }
        require(height in 0.0f..1.0f) { "height must be between 0.0 and 1.0, got $height" }
        require(left + width <= 1.0f) { "left + width must not exceed 1.0, got ${left + width}" }
        require(top + height <= 1.0f) { "top + height must not exceed 1.0, got ${top + height}" }
        require(width > 0.0f) { "width must be greater than 0.0, got $width" }
        require(height > 0.0f) { "height must be greater than 0.0, got $height" }
    }

    /**
     * The right edge of the scan region as a fraction of the preview width.
     */
    val right: Float get() = left + width

    /**
     * The bottom edge of the scan region as a fraction of the preview height.
     */
    val bottom: Float get() = top + height

    companion object {
        /**
         * Creates a centered scan region with the specified size.
         *
         * @param width The width of the region as a fraction of the preview width (0.0 to 1.0).
         * @param height The height of the region as a fraction of the preview height (0.0 to 1.0).
         * @return A [ScanRegion] centered in the preview.
         */
        fun centered(
            width: Float,
            height: Float,
        ): ScanRegion {
            require(width in 0.0f..1.0f) { "width must be between 0.0 and 1.0" }
            require(height in 0.0f..1.0f) { "height must be between 0.0 and 1.0" }
            return ScanRegion(
                left = (1.0f - width) / 2.0f,
                top = (1.0f - height) / 2.0f,
                width = width,
                height = height,
            )
        }

        /**
         * Full screen scan region (default behavior when no region is specified).
         */
        val FullScreen: ScanRegion =
            ScanRegion(
                left = 0.0f,
                top = 0.0f,
                width = 1.0f,
                height = 1.0f,
            )
    }
}
