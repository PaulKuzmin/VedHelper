package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable

@Composable
fun YearPicker(years: List<Int>, selected: String, onChange: (String) -> Unit) {
    val options = years.map { it.toString() to it.toString() }
    DropdownSelector(label = "Год выпуска", options, selected, onChange)
}