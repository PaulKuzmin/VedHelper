package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PowerTypePicker(
    selected: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("h" to "л.с.", "k" to "кВт")
    DropdownSelector(
        label = "Ед.изм.",
        options = options,
        selected = selected,
        onSelect = onChange,
        modifier = modifier
    )
}