package cz.tomasjanicek.bp.ui.elements

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.tomasjanicek.bp.ui.screens.examination.list.ExaminationFilterType
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelector(
    selectedFilter: ExaminationFilterType,
    onFilterSelected: (ExaminationFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    SingleChoiceSegmentedButtonRow(modifier) {
        // Použijeme enum pro dynamické vytvoření tlačítek
        ExaminationFilterType.values().forEachIndexed { index, filterType ->
            SegmentedButton(
                selected = (selectedFilter == filterType),
                onClick = { onFilterSelected(filterType) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ExaminationFilterType.values().size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.secondary,
                    activeContentColor = MyBlack, // Barva textu na aktivním
                    activeBorderColor = MyBlack,

                    inactiveContainerColor = MyWhite,
                    inactiveContentColor = MyBlack,
                    inactiveBorderColor = MyBlack
                )
            ) {
                Text(filterType.label)
            }
        }
    }
}