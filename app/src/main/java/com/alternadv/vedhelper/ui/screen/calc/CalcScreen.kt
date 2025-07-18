package com.alternadv.vedhelper.ui.screen.calc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alternadv.vedhelper.ui.components.CalcParamsInput
import com.alternadv.vedhelper.ui.components.CountryPicker
import com.alternadv.vedhelper.ui.components.DirectionPicker
import com.alternadv.vedhelper.ui.components.DropdownSelector
import com.alternadv.vedhelper.ui.navigation.BottomNavItem
import com.alternadv.vedhelper.ui.screen.calcresult.CalcResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcScreen(
    navController: NavController,
    calcResultViewModel: CalcResultViewModel,
    viewModel: CalcViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.onCalcSuccess = { result ->
            calcResultViewModel.setResult(result)
            navController.navigate(BottomNavItem.CalcResult.route)
        }
    }

    val state by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            OutlinedTextField(
                value = state.searchTerm,
                onValueChange = viewModel::onSearchInput,
                label = { Text("Введите код ТН ВЭД") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp)
            )
        },
        bottomBar = {
            if (state.isShowCalc) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.calcClick() },
                        enabled = !state.isCalculating,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Рассчитать")
                    }

                    if (state.isCalculating) {
                        Spacer(modifier = Modifier.width(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator()
                }
            } else {


                // Всё остальное рисуем, если не загрузка
                if (state.isShowHint) {
                    HintCard(viewModel)
                }

                if (state.isShowCalc) {
                    CalcContent(state, viewModel)
                }
            }
        }
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCalcError() },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissCalcError() }) {
                    Text("ОК")
                }
            },
            title = { Text("Ошибка") },
            text = { Text(state.errorMessage ?: "Неизвестная ошибка") }
        )
    }
}

@Composable
private fun HintCard(viewModel: CalcViewModel) {
    Column(modifier = Modifier.padding(top = 5.dp)) {
        Card(modifier = Modifier.padding(top = 5.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Не нашли или не знаете код?")
                Text("Воспользуйтесь справочником или примерами.")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = viewModel::tnvedClick) { Text("ТН ВЭД") }
                    Button(onClick = viewModel::examplesClick) { Text("Примеры") }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Справочник ТН ВЭД поможет вам точно подобрать код ТН ВЭД, а также получить дополнительную информацию по товару. " +
                            "В частности, в справочнике вы быстро найдете сведения о размерах ставок импортных и экспортных пошлины, акцизов, НДС и других платежей.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CalcContent(state: CalcState, viewModel: CalcViewModel) {
    Text(
        state.params?.name ?: "",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            DirectionPicker(state.chosenParams.direction, viewModel::onDirectionSelected)
            CountryPicker(
                state.availableCountries,
                state.chosenParams.country,
                viewModel::onCountrySelected
            )

            OutlinedTextField(
                value = state.chosenParams.paramCost?.toString() ?: "",
                onValueChange = viewModel::onCostChanged,
                label = { Text("Стоимость (USD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            state.calcParams.forEach { param ->
                val paramValue = state.chosenParams.addons?.get(param.code)
                CalcParamsInput(param, paramValue, viewModel::onCalcParamChanged)
            }

            if (state.specialParams.count() > 0) {
                Text(
                    "Особые условия расчета",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )

                val groupedSpecials = state.specialParams
                    .groupBy { it.typeName }

                val currentSpecials = state.chosenParams.specials.orEmpty()

                groupedSpecials.forEach { (typeName, items) ->
                    val selectedId = currentSpecials[items.first().type] ?: ""
                    val options = items.map { it.id to it.name }

                    DropdownSelector(
                        label = typeName,
                        options = options,
                        selected = selectedId,
                        onSelect = { selectedId ->
                            // вызываем метод VM
                            viewModel.onSpecialChanged(type = items.first().type, id = selectedId)
                        }
                    )
                }

            }
        }
    }

    state.statsPrice?.let { stats ->
        if (stats.average != "0.00") {
            Text(
                "Среднеконтрактные цены, $/кг",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Минимум: ${stats.minimum}")
                    Text("Средняя: ${stats.average}")
                    Text("Максимум: ${stats.maximum}")
                    Text("* Данные за полгода", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}