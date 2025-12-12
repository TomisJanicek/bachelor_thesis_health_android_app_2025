package cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyPink

@Composable
fun ParameterEditDialog(
    field: MeasurementCategoryField,
    error: Int?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    // Zde už přijímáme jen label a unit
    onFieldChange: (label: String, unit: String, min: String, max: String) -> Unit
) {
    var label by remember { mutableStateOf(field.label) }
    var unit by remember { mutableStateOf(field.unit.orEmpty()) }

    var min by remember { mutableStateOf(field.minValue?.toString() ?: "") }
    var max by remember { mutableStateOf(field.maxValue?.toString() ?: "") }


    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (field.id < 0) "Nový parametr" else "Upravit parametr", color = MyBlack)},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Zadejte název a jednotku parametru, který chcete měřit.", color = MyBlack)

                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        // Předáváme obě hodnoty, i když se mění jen jedna
                        onFieldChange(it, unit, min, max)
                    },
                    label = { Text("Název parametru*") },
                    placeholder = { Text("např. 'Systolický tlak'") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    // Chyba se teď váže jen k tomuto poli
                    isError = error != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MyBlack,
                        unfocusedBorderColor = MyBlack,
                        unfocusedLabelColor = MyBlack,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = {
                        unit = it
                        // Předáváme obě hodnoty
                        onFieldChange(label, it, min, max)
                    },
                    label = { Text("Jednotka (nepovinné)") },
                    placeholder = { Text("např. 'mmHg'") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MyBlack,
                        unfocusedBorderColor = MyBlack,
                        unfocusedLabelColor = MyBlack,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // POLE PRO MINIMUM
                    OutlinedTextField(
                        value = min,
                        onValueChange = { newValue ->
                            // Povolíme jen čísla a desetinnou tečku/čárku
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*[,.]?\\d*\$"))) {
                                min = newValue
                                onFieldChange(label, unit, newValue, max)
                            }
                        },
                        label = { Text("Min.") },
                        placeholder = { Text("volitelné") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = MyBlack,
                            unfocusedBorderColor = MyBlack,
                            unfocusedLabelColor = MyBlack,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // POLE PRO MAXIMUM
                    OutlinedTextField(
                        value = max,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*[,.]?\\d*\$"))) {
                                max = newValue
                                onFieldChange(label, unit, min, newValue)
                            }
                        },
                        label = { Text("Max.") },
                        placeholder = { Text("volitelné") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = MyBlack,
                            unfocusedBorderColor = MyBlack,
                            unfocusedLabelColor = MyBlack,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (error != null) {
                    Text(
                        text = stringResource(id = error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MyBlack))
                {
                    Text("Uložit")
                }
        },
        dismissButton = { TextButton(
            onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(contentColor = MyBlack))
        {
            Text("Zrušit")
        }
        },
        containerColor = MyPink,
        textContentColor = MyBlack
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}