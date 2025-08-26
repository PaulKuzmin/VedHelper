package com.alternadv.vedhelper.ui.screen.carcalc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.CarCalcSource
import com.alternadv.vedhelper.model.CarCalcParamsModel
import com.alternadv.vedhelper.model.CarCalcResultModel
import com.alternadv.vedhelper.utils.CurrencyConverter
import com.alternadv.vedhelper.utils.PowerConverter
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.set
import kotlin.text.toInt

class CarCalcViewModel : ViewModel() {

    var onCalcSuccess: ((CarCalcResultModel) -> Unit)? = null
    var lastCarCalcParams: CarCalcParamsModel? = null

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (currentYear downTo currentYear - 20).toList()

    private val _uiState = MutableStateFlow(CarCalcState())
    val uiState: StateFlow<CarCalcState> = _uiState

    init {
        loadParams("car")
    }

    fun loadParams(vehicle: String, chosen: CarCalcState? = null) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val paramMap = chosen?.toParamMap() ?: emptyMap()
                val data = CarCalcSource.getParams(vehicle, paramMap)
                lastCarCalcParams = data
                _uiState.update {
                    it.copy(
                        calcParams = data?.calcParams ?: emptyList(),
                        calcEngines = data?.calcEngines ?: emptyList(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Ошибка загрузки параметров"
                    )
                }
            }
        }
    }

    fun onVehicleTypeChanged(v: String) {
        _uiState.update {
            it.copy(vehicle = v)
        }
        loadParams(_uiState.value.vehicle, _uiState.value)
    }

    fun onMonthChanged(v: String) {
        _uiState.update {
            it.copy(month = v.toInt())
        }
    }

    fun onYearChanged(v: String) {
        _uiState.update {
            it.copy(year = v.toInt())
        }
    }

    fun onCurrencyChanged(v: String) {
        _uiState.update {
            it.copy(currency = v)
        }
    }

    fun onRawCostChanged(v: String) {
        val value = v.toDoubleOrNull()
        _uiState.update {
            it.copy(rawCost = value)
        }
    }

    fun onRawPowerChanged(v: String) {
        /*
        val value = v.toDoubleOrNull()
        _uiState.update {
            it.copy(rawPower = value)
        }
         */
        _uiState.update { it.copy(rawPowerInput = v) }

        val parsed = v.toDoubleOrNull()
        _uiState.update { it.copy(rawPower = parsed) }
    }

    fun onCarCalcParamChanged(code: String, value: String) {
        val doubleValue = value.toDoubleOrNull()
        _uiState.update { state ->
            val updatedParams = state.chosenParams.toMutableMap()

            if (doubleValue != null) {
                updatedParams[code] = doubleValue
            } else {
                updatedParams.remove(code)
            }

            state.copy(
                chosenParams = updatedParams
            )
        }
    }

    fun onEngineChanged(id: String) {
        _uiState.update { it.copy(engine = id) }
        loadParams(_uiState.value.vehicle, _uiState.value)
    }

    fun onPowerUnitChanged(id: String) {
        _uiState.update {
            it.copy(powerUnit = id)
        }
    }

    fun calcClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true, errorMessage = null) }

            try {

                val usdCost = CurrencyConverter.convertToUsd(
                    _uiState.value.rawCost ?: 0.0,
                    _uiState.value.currency
                )

                _uiState.update {
                    it.copy(cost = usdCost)
                }

                val paramMap = _uiState.value.toParamMap()
                val calcDeferred =
                    async { CarCalcSource.getCalc(vehicle = _uiState.value.vehicle, paramMap) }
                val result = calcDeferred.await()

                _uiState.update { it.copy(isCalculating = false) }

                if (result?.calculation?.u?.success != true || result.calculation.f?.success != true) {

                    if (result?.calculation?.u?.messages?.isNotEmpty() == true || result?.calculation?.f?.messages?.isNotEmpty() == true) {
                        val fErrorText = buildString {
                            append("Ошибки при расчете на физ.лицо:\r\n")
                            result.calculation.f?.messages?.forEach { msg ->
                                append(msg.message).append("\r\n")
                            }
                        }

                        val uErrorText = buildString {
                            append("Ошибки при расчете на юр.лицо:\r\n")
                            result.calculation.u?.messages?.forEach { msg ->
                                append(msg.message).append("\r\n")
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                errorMessage = fErrorText + "\r\n" + uErrorText
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                errorMessage = "Неизвестная ошибка при расчете"
                            )
                        }
                    }
                    return@launch
                }

                onCalcSuccess?.invoke(result)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = e.message ?: "Ошибка при расчете"
                    )
                }
            }
        }
    }

    fun dismissCalcError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun CarCalcState.toParamMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["vehicle"] = vehicle
        cost?.let { map["cost"] = String.format(Locale.US, "%.4f", it) }
        map["engine"] = engine ?: ""
        chosenParams.forEach { (key, value) ->
            map[key] = value.toInt().toString() // <-- ключевое изменение
        }

        val powerValue = when {
            rawPower == null || rawPower < 0 -> 0
            powerUnit == "k" -> PowerConverter.kilowattsToHorsepower(rawPower).toInt()
            else -> rawPower.toInt()
        }
        map["power"] = powerValue.toString()

        map["month"] = month.toString()
        map["year"] = year.toString()
        map["json"] = "1"
        return map
    }
}