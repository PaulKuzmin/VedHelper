package com.alternadv.vedhelper.ui.screen.calcresult

import androidx.lifecycle.ViewModel
import com.alternadv.vedhelper.model.CalcResultModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CalcResultViewModel : ViewModel() {
    private val _calcResult = MutableStateFlow<CalcResultModel?>(null)
    val calcResult: StateFlow<CalcResultModel?> = _calcResult

    fun setResult(result: CalcResultModel) {
        _calcResult.value = result
    }
}