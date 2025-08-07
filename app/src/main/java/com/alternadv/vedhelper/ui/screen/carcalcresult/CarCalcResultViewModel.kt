package com.alternadv.vedhelper.ui.screen.carcalcresult

import androidx.lifecycle.ViewModel
import com.alternadv.vedhelper.model.CarCalcParamsModel
import com.alternadv.vedhelper.model.CarCalcResultModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CarCalcResultViewModel : ViewModel() {
    private val _carCalcResult = MutableStateFlow<CarCalcResultModel?>(null)
    private val _carCalcParams = MutableStateFlow<CarCalcParamsModel?>(null)
    val carCalcResult: StateFlow<CarCalcResultModel?> = _carCalcResult
    val carCalcParams: StateFlow<CarCalcParamsModel?> = _carCalcParams

    fun setResult(result: CarCalcResultModel) {
        _carCalcResult.value = result
    }

    fun setParams(result: CarCalcParamsModel) {
        _carCalcParams.value = result
    }
}