package com.alternadv.vedhelper.ui.screen.calc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.CalcSource
import com.alternadv.vedhelper.model.CalcParamsModel
import com.alternadv.vedhelper.model.CalcResultModel
import com.alternadv.vedhelper.model.Chosen
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class CalcViewModel : ViewModel() {

    var onCalcSuccess: ((CalcResultModel) -> Unit)? = null
    var lastCalcParams: CalcParamsModel? = null

    private val chosenChangedTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        observeChanges()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun observeChanges() {
        viewModelScope.launch {
            chosenChangedTrigger
                .debounce(400)
                .collect {
                    loadParams(_uiState.value.searchTerm, _uiState.value.chosenParams)
                }
        }
    }

    private val _uiState = MutableStateFlow(
        CalcState(
            searchTerm = "",
            isShowHint = true,
            isShowCalc = false,
            chosenParams = Chosen(
                code = "",
                direction = "I",
                country = "000",
                paramCost = null,
                json = true,
                specials = emptyMap(),
                addons = emptyMap()
            ),
            availableCountries = emptyList(),
            params = CalcMeta(""),
            calcParams = emptyList(),
            specialParams = emptyList(),
            statsPrice = null,
            isLoading = false,
            errorMessage = null,
            cost = null
        )
    )
    val uiState: StateFlow<CalcState> = _uiState

    fun onSearchInput(input: String) {
        _uiState.update { it.copy(searchTerm = input) }
        if (input.isNotBlank()) {
            loadParams(input)
        }
    }

    private fun loadParams(code: String, chosen: Chosen? = null) {
        if (code.isBlank()) return
        if (code.length < 10) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {

                val paramMap = chosen?.toParamMap(_uiState.value.currency) ?: emptyMap()
                // runCatching внутри async: сбой одного запроса не должен ронять приложение
                // (при недоступном сервере async-ребёнок отменял viewModelScope и был FATAL EXCEPTION: main)
                val paramsDeferred = async { runCatching { CalcSource.getParams(code, paramMap) } }
                val statsDeferred = async { runCatching { CalcSource.getStats(code, paramMap) } }

                val paramsResult = paramsDeferred.await()
                val statsResult = statsDeferred.await()
                val response = paramsResult.getOrNull()
                lastCalcParams = response
                val stats = statsResult.getOrNull()

                val countries = response?.countries ?: emptyList()
                val calcParams = response?.calcParams?.values?.toList() ?: emptyList()
                val specialParams = response?.calcSpecial ?: emptyList()
                val calcInfoName = response?.calcInfo?.name ?: ""

                if (response?.calcInfo != null) {
                    _uiState.update {
                        it.copy(
                            isShowHint = false,
                            isShowCalc = true,
                            chosenParams = if (chosen == null) it.chosenParams.copy(paramCost = response.chosen?.paramCost) else it.chosenParams,
                            availableCountries = countries,
                            params = CalcMeta(calcInfoName),
                            calcParams = calcParams,
                            specialParams = specialParams,
                            statsPrice = stats?.statsprice,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = paramsResult.exceptionOrNull()?.message
                                ?: "Код ТН ВЭД не найден или сервер недоступен. Проверьте соединение и попробуйте ещё раз."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Ошибка загрузки"
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDirectionSelected(value: String) {
        _uiState.update {
            it.copy(chosenParams = it.chosenParams.copy(direction = value))
        }
        chosenChangedTrigger.tryEmit(Unit)
    }

    fun onCurrencySelected(value: String) {
        _uiState.update {
            it.copy(currency = value)
        }
    }

    fun onCountrySelected(code: String) {
        _uiState.update {
            it.copy(chosenParams = it.chosenParams.copy(country = code))
        }
        chosenChangedTrigger.tryEmit(Unit)
    }

    fun onCostChanged(input: String) {
        _uiState.update { it.withCostInput(input) }
    }

    fun onCalcParamChanged(code: String, value: String) {
        _uiState.update { it.withAddonInput(code, value) }
    }

    fun onSpecialChanged(type: String, id: String) {
        _uiState.update { state ->
            val updated = if (id.isNotEmpty()) {
                state.chosenParams.specials.orEmpty() + (type to id)
            } else {
                state.chosenParams.specials.orEmpty().filterKeys { it != type }
            }
            state.copy(
                chosenParams = state.chosenParams.copy(specials = updated)
            )
        }
        chosenChangedTrigger.tryEmit(Unit)
    }

    fun calcClick() {
        val code = _uiState.value.searchTerm
        //val chosen = _uiState.value.chosenParams

        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Код ТН ВЭД не указан") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true, errorMessage = null) }

            try {

                // Стоимость НЕ конвертируем: сервер принимает её в валюте `currency`
                // и пересчитывает сам (иначе терялась точность на округлении).
                _uiState.update {
                    it.copy(chosenParams = it.chosenParams.copy(paramCost = it.cost))
                }

                val paramMap = _uiState.value.chosenParams.toParamMap(_uiState.value.currency)
                // без async: одиночный запрос, ошибку ловит try/catch ниже (сбой child-корутины ронял приложение)
                val result = CalcSource.getCalc(code, paramMap)

                _uiState.update { it.copy(isCalculating = false) }

                if (result?.calculation?.success != true) {
                    val messages = result?.calculation?.messages.orEmpty()
                    if (messages.isNotEmpty()) {
                        val errorText = buildString {
                            append("Ошибки при расчете:\r\n")
                            messages.forEach { msg ->
                                append(msg.message).append("\r\n")
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                errorMessage = errorText
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

    fun Chosen.toParamMap(currency: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["code"] = code
        map["direction"] = direction
        map["country"] = country
        map["currency"] = currency
        paramCost?.let { map["param_cost"] = String.format(Locale.US, "%.4f", it) }
        // json=1 добавляет DataSource.buildUrl
        specials?.forEach { (key, value) -> map["special_$key"] = value }
        addons?.forEach { (key, value) -> map["param_$key"] = value.toString() }
        return map
    }
}

