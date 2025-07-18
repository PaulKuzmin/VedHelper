package com.alternadv.vedhelper.ui.components

import androidx.compose.runtime.Composable
import com.alternadv.vedhelper.model.CountryModel

@Composable
fun CountryPicker(countries: List<CountryModel>, selected: String, onChange: (String) -> Unit) {
    val options = countries.map { it.code to it.name }
    DropdownSelector(label = "Страна", options, selected, onChange)
}