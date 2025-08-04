package com.alternadv.vedhelper.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPicker(
    selected: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencies = listOf(
        "840" to "Доллар США",
        "156" to "Юань",
        "978" to "Евро",
        "392" to "Йена",
        "410" to "Вон"
    )

    var expanded by remember { mutableStateOf(false) }
    val selectedName = currencies.find { it.first == selected }?.second ?: "Выберите валюту"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Валюта") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onChange(code)
                        expanded = false
                    }
                )
            }
        }
    }
}