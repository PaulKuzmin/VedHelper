package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable

@Composable
fun VehicleTypePicker(vehicleTypes: Map<String, String>, selected: String, onChange: (String) -> Unit) {
    val options = vehicleTypes.entries.map { it.key to it.value }
    DropdownSelector(label = "Тип ТС", options, selected, onChange)
}

/** Список видов ТС приходит с сервера (widget/calcauto/vehicles), порядок — как на сервере. */
@Composable
fun VehicleTypePickerFromServer(
    vehicles: List<Pair<String, String>>,
    selected: String,
    onChange: (String) -> Unit
) {
    if (vehicles.isEmpty()) return
    DropdownSelector(label = "Тип ТС", options = vehicles, selected = selected, onSelect = onChange)
}
