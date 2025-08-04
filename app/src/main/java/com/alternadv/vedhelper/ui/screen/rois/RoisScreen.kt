package com.alternadv.vedhelper.ui.screen.rois

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
            label = { Text("Введите ключевое слово") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                Text("Введите минимум 3 символа для поиска.", style = MaterialTheme.typography.bodyMedium)
            }

            state.isShowNotFound -> {
                Text("Ничего не найдено.", style = MaterialTheme.typography.bodyMedium)
            }

            else -> {
                state.oisText?.let {
                    Text(text = it, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                LazyColumn {
                    items(state.items) { item ->
                        RoisItem(item)
                    }
                }
            }
        }
    }
}

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
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
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
