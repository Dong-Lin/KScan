package org.ncgroup.kscan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerBarcodeSelectionBottomSheet(
    barcodes: List<Barcode>,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    result: (BarcodeResult) -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismissRequest() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(45.dp),
            ) {
                Text(
                    text = "More than one barcode detected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterStart),
                )
            }
            LazyColumn {
                items(
                    items = barcodes.toList(),
                ) { barcode ->
                    Card {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                text = barcode.data,
                                modifier =
                                    Modifier
                                        .padding(start = 16.dp)
                                        .align(Alignment.CenterStart),
                            )
                            TextButton(
                                onClick = {
                                    result(BarcodeResult.OnSuccess(barcode))
                                },
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterEnd),
                            ) {
                                Text(
                                    text = "Select",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
