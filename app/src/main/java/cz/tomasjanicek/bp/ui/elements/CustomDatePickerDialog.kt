package cz.tomasjanicek.bp.ui.elements

import androidx.compose.animation.core.copy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    datePickerState: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MyBlack)
            ) { Text("Pokračvat") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MyBlack)
            ) { Text("Zrušit") }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MyGreen
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MyGreen,
                titleContentColor = MyBlack,
                headlineContentColor = MyBlack,
                weekdayContentColor = MyBlack,
                subheadContentColor = MyBlack,
                yearContentColor = MyBlack,
                currentYearContentColor = MyBlack,
                selectedYearContentColor = MyWhite,
                selectedYearContainerColor = MyPink,
                dayContentColor = MyBlack,
                disabledDayContentColor = MyBlack.copy(alpha = 0.38f),
                selectedDayContentColor = MyWhite,
                selectedDayContainerColor = MyPink,
                disabledSelectedDayContentColor = MyBlack.copy(alpha = 0.38f),
                todayContentColor = MyPink,
                todayDateBorderColor = MyPink,
                navigationContentColor = MyBlack
            )
        )
    }
}