package com.alternadv.vedhelper.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alternadv.vedhelper.model.CarCalcParam

/**
 * Поле формы.
 *
 * Если сервер прислал `choices` (например state: new/used, kind: motor/motor_sea/sail) —
 * рисуем выпадающий список: раньше такие поля рендерились как числовые и выбрать значение
 * было нельзя. Иначе — числовое поле, где разрешена точка (объём 1.6 л, масса 1.75 т).
 */
@Composable
fun CarCalcParamsInput(
    param: CarCalcParam,
    value: String?,
    onChange: (String, String) -> Unit,
    onChoiceChange: (String, String) -> Unit
) {
    val choices = param.choices.orEmpty()

    if (choices.isNotEmpty()) {
        DropdownSelector(
            label = param.name,
            options = choices.map { it.id to it.name },
            selected = value ?: choices.first().id,
            onSelect = { id -> onChoiceChange(param.code, id) }
        )
        return
    }

    val label = if (param.dimension.isNotBlank()) "${param.name}, ${param.dimension}" else param.name

    OutlinedTextField(
        value = value ?: "",
        onValueChange = { input ->
            val filtered = input.replace(',', '.').filter { it.isDigit() || it == '.' }
            onChange(param.code, filtered)
        },
        label = { Text(label) },
        placeholder = { Text(param.dimension) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .bringIntoViewOnFocus()
    )
}
