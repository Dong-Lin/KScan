package org.ncgroup.kscan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScannerHeader(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    torchEnabled: Boolean,
    onTorchEnabled: (Boolean) -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Scan Code",
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    onCancel()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    if (torchEnabled) {
                        onTorchEnabled(false)
                    } else {
                        onTorchEnabled(true)
                    }
                },
            ) {
                Icon(
                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = null,
                )
            }
        },
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF291544),
                navigationIconContentColor = Color.White,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White,
            ),
        modifier = modifier,
    )
}
