package com.alternadv.vedhelper.ui.screen.calcresult

import androidx.lifecycle.ViewModel
import com.alternadv.vedhelper.model.CalcParamsModel
import com.alternadv.vedhelper.model.CalcResultModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CalcResultViewModel : ViewModel() {
    private val _calcResult = MutableStateFlow<CalcResultModel?>(null)
    private val _calcParams = MutableStateFlow<CalcParamsModel?>(null)
    val calcResult: StateFlow<CalcResultModel?> = _calcResult
    val calcParams: StateFlow<CalcParamsModel?> = _calcParams

    fun setResult(result: CalcResultModel) {
        _calcResult.value = result
    }

    fun setParams(result: CalcParamsModel) {
        _calcParams.value = result
    }
}