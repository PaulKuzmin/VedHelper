package com.alternadv.vedhelper.ui.screen.carcalc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alternadv.vedhelper.ui.components.CarCalcParamsInput
import com.alternadv.vedhelper.ui.components.CurrencyPicker
import com.alternadv.vedhelper.ui.components.EnginePicker
import com.alternadv.vedhelper.ui.components.MonthPicker
import com.alternadv.vedhelper.ui.components.PowerTypePicker
import com.alternadv.vedhelper.ui.components.VehicleTypePickerFromServer
import com.alternadv.vedhelper.ui.components.YearPicker
import com.alternadv.vedhelper.ui.components.bringIntoViewOnFocus
import com.alternadv.vedhelper.ui.navigation.BottomNavItem
import com.alternadv.vedhelper.ui.screen.carcalcresult.CarCalcResultViewModel

@Composable
fun CarCalcScreen(
    navController: NavController,
    carCalcResultViewModel: CarCalcResultViewModel,
    viewModel: CarCalcViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onCalcSuccess = { result ->
            carCalcResultViewModel.setResult(result)
            navController.navigate(BottomNavItem.CarCalcResult.route)
        }
    }

    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    // Шапка формы прокручивается отдельно: общий ScrollState с телом формы ломал прокрутку —
    // короткая шапка выставляла maxValue = 0 и тело не листалось (в т.ч. при открытой клавиатуре).
    val topScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 3.dp)
                    .verticalScroll(topScrollState),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                VehicleTypePickerFromServer(
                    vehicles = state.vehicles.map { it.id to it.name },
                    selected = state.vehicle,
                    onChange = viewModel::onVehicleTypeChanged,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonthPicker(
                        months = state.months,
                        selected = state.month.toString(),
                        onChange = viewModel::onMonthChanged,
                        modifier = Modifier.weight(1f)
                    )
                    YearPicker(
                        years = viewModel.years,
                        selected = state.year.toString(),
                        onChange = viewModel::onYearChanged,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Стоимость уходит в валюте currency — пересчёт делает сервер
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.costInput,
                        onValueChange = viewModel::onCostChanged,
                        label = { Text("Стоимость") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .bringIntoViewOnFocus()
                    )
                    CurrencyPicker(
                        selected = state.currency,
                        onChange = viewModel::onCurrencyChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        bottomBar = {
            if (!state.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val params = viewModel.lastCarCalcParams
                            if (params != null) carCalcResultViewModel.setParams(params)
                            viewModel.calcClick()
                        },
                        enabled = !state.isCalculating,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Рассчитать")
                    }

                    if (state.isCalculating) {
                        Spacer(modifier = Modifier.width(16.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp, 5.dp)
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
                CarCalcContent(state, viewModel)
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

/**
 * Поля строятся по ответу сервера (calc_params), поэтому новый параметр появляется в форме
 * без обновления приложения. Отдельно обрабатываются engine (список двигателей) и power
 * (значение + единицы измерения, пересчёт в л.с. делает API).
 */
@Composable
private fun CarCalcContent(state: CarCalcState, viewModel: CarCalcViewModel) {
    state.calcParams.forEach { param ->
        when (param.code) {
            "engine" -> EnginePicker(
                engines = state.calcEngines,
                selected = state.engine ?: state.calcEngines.firstOrNull()?.id ?: "f",
                onChange = viewModel::onEngineChanged
            )

            "power" -> Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.powerInput,
                    onValueChange = viewModel::onPowerChanged,
                    label = { Text("Мощность") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewOnFocus()
                )
                PowerTypePicker(
                    selected = state.powerUnit,
                    onChange = viewModel::onPowerUnitChanged,
                    modifier = Modifier.weight(1f)
                )
            }

            else -> CarCalcParamsInput(
                param = param,
                value = state.paramValues[param.code],
                onChange = viewModel::onParamChanged,
                onChoiceChange = viewModel::onChoiceChanged
            )
        }
    }

    // Как на сайте (calcauto_params.twig): чекбокс только для тягача, в calc_params его нет.
    if (state.supportsIntl()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = state.intl,
                    role = Role.Checkbox,
                    onValueChange = viewModel::onIntlChanged
                )
                .padding(vertical = 4.dp)
        ) {
            Checkbox(checked = state.intl, onCheckedChange = null)
            Spacer(Modifier.width(8.dp))
            Text("Евро-5/6, международные перевозки")
        }
    }
}
