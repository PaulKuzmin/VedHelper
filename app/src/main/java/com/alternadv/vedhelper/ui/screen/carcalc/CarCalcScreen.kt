package com.alternadv.vedhelper.ui.screen.carcalc

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
import com.alternadv.vedhelper.model.VehicleTypes
import com.alternadv.vedhelper.ui.components.CarCalcParamsInput
import com.alternadv.vedhelper.ui.components.CurrencyPicker
import com.alternadv.vedhelper.ui.components.EnginePicker
import com.alternadv.vedhelper.ui.components.MonthPicker
import com.alternadv.vedhelper.ui.components.PowerTypePicker
import com.alternadv.vedhelper.ui.components.YearPicker
import com.alternadv.vedhelper.ui.components.VehicleTypePicker
import com.alternadv.vedhelper.ui.navigation.BottomNavItem
import com.alternadv.vedhelper.ui.screen.carcalcresult.CarCalcResultViewModel
import java.util.Locale

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

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 3.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                VehicleTypePicker(
                    vehicleTypes = VehicleTypes,
                    selected = state.vehicle,
                    onChange = viewModel::onVehicleTypeChanged,
                )
                // --- Месяц и год в одной строке ---
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

                // --- Стоимость и валюта в одной строке ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.rawCost?.let { String.format(Locale.US, "%.0f", it) } ?: "",
                        onValueChange = viewModel::onRawCostChanged,
                        label = { Text("Стоимость") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
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
                            if (viewModel.lastCarCalcParams != null) {
                                carCalcResultViewModel.setParams(viewModel.lastCarCalcParams!!)
                            }

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
                if (!state.isLoading) {
                    CarCalcContent(state, viewModel)
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
private fun CarCalcContent(state: CarCalcState, viewModel: CarCalcViewModel) {

    state.calcParams.forEach { param ->
        if (param.code != "engine" && param.code != "power") {
            val paramValue = state.chosenParams[param.code]
            CarCalcParamsInput(param, paramValue, viewModel::onCarCalcParamChanged)
        } else if (param.code == "power") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    //value = state.rawPower?.let { String.format(Locale.US, "%.0f", it) } ?: "",
                    value = state.rawPowerInput,
                    //onValueChange = viewModel::onRawPowerChanged,
                    onValueChange = { newValue ->
                        // Разрешаем только цифры и точку
                        val filtered = newValue.replace(',', '.')
                            .filter { it.isDigit() || it == '.' }

                        viewModel.onRawPowerChanged(filtered)
                    },
                    label = { Text("Мощность") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                PowerTypePicker(
                    selected = state.powerUnit,
                    onChange = viewModel::onPowerUnitChanged,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            EnginePicker(
                engines = state.calcEngines,
                selected = state.engine ?: "f",
                onChange = viewModel::onEngineChanged
            )
        }
    }
}