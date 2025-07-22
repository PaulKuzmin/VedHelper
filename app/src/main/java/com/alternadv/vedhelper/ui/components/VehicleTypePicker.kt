package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable

@Composable
fun VehicleTypePicker(vehicleTypes: Map<String, String>, selected: String, onChange: (String) -> Unit) {
    val options = vehicleTypes.entries.map { it.key to it.value }
    DropdownSelector(label = "Тип ТС", options, selected, onChange)
}