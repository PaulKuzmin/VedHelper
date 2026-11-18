package com.alternadv.vedhelper.ui.screen.carcalc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.CarCalcSource
import com.alternadv.vedhelper.model.CarCalcParam
import com.alternadv.vedhelper.model.CarCalcParamsModel
import com.alternadv.vedhelper.model.CarCalcResultModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel формы «Авто».
 *
 * Принцип: приложение — тонкая форма. Оно не конвертирует валюту и мощность, а отправляет
 * сырые значения + currency + power_edizm. Все пересчёты делает API, он же возвращает
 * суммы в валюте стоимости (summa_cur / payments_summa_cur).
 */
class CarCalcViewModel : ViewModel() {

    var onCalcSuccess: ((CarCalcResultModel) -> Unit)? = null
    var lastCarCalcParams: CarCalcParamsModel? = null

    /** Какие поля пользователь менял сам — их значения не перезаписываем дефолтами. */
    private val touched = mutableSetOf<String>()

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (currentYear downTo currentYear - 20).toList()

    private val _uiState = MutableStateFlow(CarCalcState())
    val uiState: StateFlow<CarCalcState> = _uiState

    init {
        loadVehicles()
        loadParams("car")
    }

    /** Виды ТС — с сервера (widget/calcauto/vehicles), чтобы новые типы появлялись без релиза. */
    fun loadVehicles() {
        viewModelScope.launch {
            val vehicles = CarCalcSource.getVehicles()
            _uiState.update { state ->
                val exists = vehicles.any { it.id == state.vehicle }
                state.copy(
                    vehicles = vehicles,
                    vehicle = if (exists) state.vehicle else (vehicles.firstOrNull()?.id ?: state.vehicle)
                )
            }
        }
    }

    fun loadParams(vehicle: String, chosen: CarCalcState? = null) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val paramMap = chosen?.toParamMap() ?: emptyMap()
                val data = CarCalcSource.getParams(vehicle, paramMap)
                lastCarCalcParams = data
                _uiState.update { state ->
                    val params = data?.calcParams ?: emptyList()
                    state.copy(
                        calcParams = params,
                        calcEngines = data?.calcEngines ?: emptyList(),
                        paramValues = seedDefaults(state.copy(calcParams = params), params),
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

    /** Значения по умолчанию для полей-списков (чистая функция — см. CarCalcRequest.kt). */
    private fun seedDefaults(state: CarCalcState, params: List<CarCalcParam>): Map<String, String> =
        seedChoiceDefaults(params, state.paramValues, state.year, currentYear, touched)

    fun onVehicleTypeChanged(v: String) {
        // при смене типа сбрасываем поля, зависящие от типа (capacity/seats/kind/state…)
        touched.clear()
        // мощность и intl тоже: у нового вида поля может не быть (гольф-кар), а старое значение
        // ушло бы на сервер незаметно для пользователя
        _uiState.update { it.copy(vehicle = v, paramValues = emptyMap(), powerInput = "", intl = false) }
        loadParams(v, _uiState.value)
    }

    fun onEngineChanged(id: String) {
        _uiState.update { it.copy(engine = id) }
        loadParams(_uiState.value.vehicle, _uiState.value)
    }

    fun onMonthChanged(v: String) {
        v.toIntOrNull()?.let { month -> _uiState.update { it.copy(month = month) } }
    }

    fun onYearChanged(v: String) {
        v.toIntOrNull()?.let { year ->
            _uiState.update { state ->
                val values = state.paramValues.toMutableMap()
                val hasState = state.calcParams.any { it.code == "state" }
                if (hasState && !touched.contains("state")) {
                    values["state"] = defaultState(year, currentYear)
                }
                state.copy(year = year, paramValues = values)
            }
        }
    }

    fun onCurrencyChanged(v: String) {
        _uiState.update { it.copy(currency = v) }
    }

    fun onCostChanged(v: String) {
        _uiState.update { it.copy(costInput = normalizeNumber(v)) }
    }

    fun onPowerChanged(v: String) {
        _uiState.update { it.copy(powerInput = normalizeNumber(v)) }
    }

    /** Единицы мощности: "h"/"ls" → л.с., "k"/"kvt" → кВт. В API уходит как ls | kvt. */
    fun onPowerUnitChanged(id: String) {
        val unit = when (id) {
            "k", "kvt" -> "kvt"
            else -> "ls"
        }
        _uiState.update { it.copy(powerUnit = unit) }
    }

    fun onIntlChanged(checked: Boolean) {
        _uiState.update { it.copy(intl = checked) }
    }

    /** Любое поле формы (capacity, seats, weight, bag, len, state, kind…). */
    fun onParamChanged(code: String, value: String) {
        touched.add(code)
        _uiState.update { state ->
            val updated = state.paramValues.toMutableMap()
            val normalized = normalizeNumber(value)
            if (normalized.isEmpty()) {
                updated.remove(code)
            } else {
                updated[code] = normalized
            }
            state.copy(paramValues = updated)
        }
    }

    /** Поле-список (choices): значение берётся как есть. */
    fun onChoiceChanged(code: String, id: String) {
        touched.add(code)
        _uiState.update { state ->
            val updated = state.paramValues.toMutableMap()
            if (id.isEmpty()) updated.remove(code) else updated[code] = id
            state.copy(paramValues = updated)
        }
    }

    fun calcClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true, errorMessage = null) }

            try {
                val paramMap = _uiState.value.toParamMap()
                val result = CarCalcSource.getCalc(vehicle = _uiState.value.vehicle, queryParams = paramMap)

                _uiState.update { it.copy(isCalculating = false) }

                // Показываем результат, если посчитан хотя бы один блок (F или U):
                // ошибка физлица не должна прятать готовый расчёт на юрлицо.
                val errorText = calcErrorText(result)
                if (errorText != null || result == null) {
                    _uiState.update { it.copy(isCalculating = false, errorMessage = errorText) }
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

}
