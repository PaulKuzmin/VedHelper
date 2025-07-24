package com.alternadv.vedhelper.ui.screen.carcalcresult

import androidx.lifecycle.ViewModel
import com.alternadv.vedhelper.model.CarCalcResultModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CarCalcResultViewModel : ViewModel() {
    private val _carCalcResult = MutableStateFlow<CarCalcResultModel?>(null)
    val carCalcResult: StateFlow<CarCalcResultModel?> = _carCalcResult

    fun setResult(result: CarCalcResultModel) {
        _carCalcResult.value = result
    }
}