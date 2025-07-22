package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable
import com.alternadv.vedhelper.model.CarCalcEngine

@Composable
fun EnginePicker(engines: List<CarCalcEngine>, selected: String, onChange: (String) -> Unit) {
    val options = engines.map { it.id to it.name }
    DropdownSelector(label = "Тип двигателя", options, selected, onChange)
}