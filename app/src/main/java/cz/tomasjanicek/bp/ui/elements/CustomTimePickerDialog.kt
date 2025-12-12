package cz.tomasjanicek.bp.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.padding(top = 24.dp, bottom = 12.dp)) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MyGreen,
                            clockDialSelectedContentColor = MyWhite,
                            clockDialUnselectedContentColor = MyBlack,
                            selectorColor = MyPink,
                            periodSelectorBorderColor = MyPink,
                            periodSelectorSelectedContainerColor = MyPink,
                            periodSelectorUnselectedContainerColor = Color.Transparent,
                            periodSelectorSelectedContentColor = MyWhite,
                            periodSelectorUnselectedContentColor = MyBlack,
                            timeSelectorSelectedContainerColor = MyPink,
                            timeSelectorUnselectedContainerColor = MyGreen,
                            timeSelectorSelectedContentColor = MyWhite,
                            timeSelectorUnselectedContentColor = MyBlack,
                        )
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = MyBlack)
                    ) {
                        Text("Zrušit", color = MyGreen)
                    }
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(contentColor = MyBlack)
                    ) {
                        Text("Potvrdit", color = MyGreen)
                    }
                }
            }
        }
    }
}