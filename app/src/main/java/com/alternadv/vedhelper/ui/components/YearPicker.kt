package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun YearPicker(
    years: List<Int>,
    selected: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = years.map { it.toString() to it.toString() }
    DropdownSelector(
        label = "Год выпуска",
        options = options,
        selected = selected,
        onSelect = onChange,
        modifier = modifier
    )
}