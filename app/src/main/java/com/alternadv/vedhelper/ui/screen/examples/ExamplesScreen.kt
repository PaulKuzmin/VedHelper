package com.alternadv.vedhelper.ui.screen.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alternadv.vedhelper.ui.navigation.BottomNavItem
import com.alternadv.vedhelper.model.ExampleItem
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

@Composable
fun ExamplesScreen(
    navController: NavController,
    initialSearchTerm: String? = null,
    viewModel: ExamplesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    /*
    LaunchedEffect(initialSearchTerm) {
        initialSearchTerm?.let {
            if (it.isNotBlank()) viewModel.onSearchInput(it)
        }
    }*/
    LaunchedEffect(Unit) { viewModel.initIfNeeded() }
    LaunchedEffect(initialSearchTerm) {
        initialSearchTerm?.takeIf { it.isNotBlank() }?.let(viewModel::onSearchInput)
    }

    // Когда изменился список — скроллим в начало
    LaunchedEffect(state.items) {
        listState.scrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 3.dp)
    ) {
        OutlinedTextField(
            value = state.searchTerm,
            onValueChange = viewModel::onSearchInput,
            label = { Text("Описание или код...") },
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

        if (state.isShowHint) {
            Card(modifier = Modifier.padding(vertical = 8.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
                    Text("Не знаете код ТН ВЭД товара?")
                    Text("Подберите его по наименованию или описанию товара.")
                    Text("Или наоборот — найдите описание по коду ТН ВЭД.")
                }
            }
        }

        if (state.isShowNotFound) {
            Card(modifier = Modifier.padding(vertical = 8.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("По вашему запросу ничего не найдено!")
                    Button(onClick = { navController.navigate(BottomNavItem.Tnved.route) }) {
                        Text("ТН ВЭД")
                    }
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState
        ) {
            items(state.items.size) { index ->
                val item = state.items[index]
                ExampleCard(item = item, onCalc = {
                    navController.navigate(BottomNavItem.Calc.route + "/${item.code}")
                }, onTnved = {
                    navController.navigate(BottomNavItem.TnvedCode.route + "/${item.code}")
                })
            }
        }
    }
}

@Composable
private fun ExampleCard(item: ExampleItem, onCalc: () -> Unit, onTnved: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.code, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            // Обрезаем или показываем полностью
            Text(
                text = parseHtml(item.name),
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            // Кнопка переключения
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (expanded) "Свернуть" else "Показать полностью")
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onTnved) {
                    Icon(Icons.Default.FormatListNumbered, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ТНВЭД")
                }
                Button(onClick = onCalc) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Рассчитать")
                }
            }
        }
    }
}

fun parseHtml(input: String): AnnotatedString {
    val clean = input.replace(Regex("<span style=\"color:[^\"]*\">(.*?)</span>"), "§§§$1§§§")
    return buildAnnotatedString {
        var i = 0
        while (i < clean.length) {
            if (clean.startsWith("§§§", i)) {
                val end = clean.indexOf("§§§", i + 3)
                val text = clean.substring(i + 3, end)
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text)
                }
                i = end + 3
            } else {
                append(clean[i])
                i++
            }
        }
    }
}
