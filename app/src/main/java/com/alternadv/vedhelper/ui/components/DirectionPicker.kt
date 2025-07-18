package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable

@Composable
fun DirectionPicker(selected: String, onChange: (String) -> Unit) {
    val options = listOf("I" to "Импорт", "E" to "Экспорт")
    DropdownSelector(label = "Направление перемещения", options, selected, onChange)
}