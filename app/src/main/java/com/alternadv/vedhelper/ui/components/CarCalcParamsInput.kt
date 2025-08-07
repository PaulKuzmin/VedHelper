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

@Composable
fun CarCalcParamsInput(
    param: CarCalcParam,
    value: Double?,
    onChange: (String, String) -> Unit
) {
    OutlinedTextField(
        value = value?.toInt()?.toString() ?: "",
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() }
            onChange(param.code, filtered)
        },
        label = {
            Text(
                text = if (param.dimension.isNotBlank())
                    "${param.name}, ${param.dimension}"
                else
                    param.name
            )
        },
        placeholder = { Text(param.dimension) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    )
}