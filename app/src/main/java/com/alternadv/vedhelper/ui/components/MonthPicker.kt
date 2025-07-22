package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable

@Composable
fun  MonthPicker(months: Map<Int, String>, selected: String, onChange: (String) -> Unit) {
    val options = months.entries.map { it.key.toString() to it.value }
    DropdownSelector(label = "Месяц выпуска", options, selected, onChange)
}