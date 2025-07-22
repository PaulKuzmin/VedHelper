package com.alternadv.vedhelper.ui.screen.carcalc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.CarCalcSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.text.toInt

class CarCalcViewModel : ViewModel() {

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
                    it.copy(isLoading = false, errorMessage = e.message ?: "Ошибка загрузки параметров")
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

    fun onCostChanged(v: String) {
        val value = v.toDoubleOrNull()
        _uiState.update {
            it.copy(cost = value)
        }
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
    }

    fun calcClick() {

    }

    fun dismissCalcError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun CarCalcState.toParamMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["vehicle"] = vehicle
        cost?.let { map["cost"] = it.toString() }
        map["engine"] = engine ?: ""
        chosenParams.forEach { (key, value) -> map[key] = value.toString() }
        map["month"] = month.toString()
        map["year"] = year.toString()
        map["json"] = "1"
        return map
    }
}