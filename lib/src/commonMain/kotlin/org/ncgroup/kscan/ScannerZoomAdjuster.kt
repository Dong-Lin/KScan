package org.ncgroup.kscan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun ScannerZoomAdjuster(
    modifier: Modifier = Modifier,
    zoomRatio: Float,
    zoomRatioOnChange: (Float) -> Unit,
    maxZoomRatio: Float,
    zoomStep: Float = 0.5f,
    containerColor: Color = Color(0xFF291544),
    contentColor: Color = Color.White,
) {
    ElevatedCard(
        modifier = modifier,
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    decreaseZoom(zoomRatio, zoomStep, zoomRatioOnChange)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = contentColor,
                )
            }

            Spacer(modifier = Modifier.padding(24.dp))

            Text(
                text = "${(zoomRatio * 10).roundToInt() / 10.0}×",
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.padding(24.dp))

            IconButton(
                onClick = {
                    increaseZoom(zoomRatio, zoomStep, maxZoomRatio, zoomRatioOnChange)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = contentColor,
                )
            }
        }
    }
}

private fun decreaseZoom(
    currentZoom: Float,
    zoomStep: Float,
    zoomRatioOnChange: (Float) -> Unit,
) {
    val newZoom = (currentZoom - zoomStep).coerceAtLeast(1f)
    zoomRatioOnChange(newZoom)
}

private fun increaseZoom(
    currentZoom: Float,
    zoomStep: Float,
    maxZoom: Float,
    zoomRatioOnChange: (Float) -> Unit,
) {
    val newZoom = (currentZoom + zoomStep).coerceAtMost(maxZoom)
    zoomRatioOnChange(newZoom)
}
