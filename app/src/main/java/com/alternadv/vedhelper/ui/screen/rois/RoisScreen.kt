package com.alternadv.vedhelper.ui.screen.rois

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.alternadv.vedhelper.model.OisModel

@Composable
fun RoisScreen(viewModel: RoisViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 3.dp)
    ) {
        OutlinedTextField(
            value = state.searchTerm,
            onValueChange = { viewModel.onSearchInput(it) },
            label = { Text("Введите ключевое слово (мин. 3 символа)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            trailingIcon = {
                if (state.searchTerm.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchInput("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.isShowHint -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "РОИС — Реестр объектов интеллектуальной собственности",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "С помощью РОИС вы можете найти и проверить статус патентов, " +
                                    "товарных знаков и других объектов интеллектуальной собственности, " +
                                    "чтобы убедиться в их правовой охране и уникальности.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            state.isShowNotFound -> {
                Text("Ничего не найдено.", style = MaterialTheme.typography.bodyMedium)
            }

            else -> {
                LazyColumn {
                    items(state.items) { item ->
                        RoisItem(item)
                    }
                }
            }
        }
    }
}

/*
@Composable
fun RoisItem(item: OisModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name ?: "", style = MaterialTheme.typography.titleMedium)

            item.image?.let {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter("https://alternadv.com/img/ois/$it"),
                    contentDescription = "Нет изображения",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }

            item.regnom?.let { Text("Рег. номер: $it") }
            item.document?.let { Text("Документ: $it") }
            item.dateend?.let { Text("Дата окончания: $it") }
        }
    }
}
 */
@Composable
fun RoisItem(item: OisModel) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Основные поля (всегда видны)
            item.regnom?.takeIf { it.isNotBlank() }?.let { LabeledValue("Рег. номер", it) }
            item.g3112?.takeIf { it.isNotBlank() }?.let { LabeledValue("Наименование", it) }
            item.dateend?.takeIf { it.isNotBlank() }?.let { LabeledValue("Срок внесения в реестр", it) }

            val imageUrl = "https://alternadv.com/img/ois/${item.image}"
            val painter = rememberAsyncImagePainter(model = imageUrl)
            val state = painter.state

            if (state !is AsyncImagePainter.State.Error) {
                Image(
                    painter = painter,
                    contentDescription = "Изображение",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Кнопка "Данные по объекту ИС"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Данные по объекту ИС",
                    style = MaterialTheme.typography.titleSmall
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Скрыть" else "Показать",
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    item.note?.takeIf { it.isNotBlank() }?.let { LabeledValue("Описание", it) }
                    item.document?.takeIf { it.isNotBlank() }?.let { LabeledValue("Документ об охраноспособности ОИС", it) }
                    item.name?.takeIf { it.isNotBlank() }?.let { LabeledValue("Сведения о правообладателе", it) }
                    item.namet?.takeIf { it.isNotBlank() }?.let { LabeledValue("Наименование товаров", it) }
                    item.agent?.takeIf { it.isNotBlank() }?.let { LabeledValue("Доверенные лица правообладателя", it) }
                    item.mktu?.takeIf { it.isNotBlank() }?.let { LabeledValue("Класс товаров по МКТУ", it) }
                    item.letter?.takeIf { it.isNotBlank() }?.let { LabeledValue("Письма ФТС", it) }
                    item.g33?.takeIf { it.isNotBlank() }?.let { LabeledValue("Коды ТНВЭД", it) }
                    item.comm?.takeIf { it.isNotBlank() }?.let { LabeledValue("Примечание", it) }
                }
            }
        }
    }
}

@Composable
fun LabeledValue(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}