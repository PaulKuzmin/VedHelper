package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MonthPicker(
    months: Map<Int, String>,
    selected: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = months.entries.map { it.key.toString() to it.value }
    DropdownSelector(
        label = "Месяц выпуска",
        options = options,
        selected = selected,
        onSelect = onChange,
        modifier = modifier
    )
}