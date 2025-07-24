package com.alternadv.vedhelper.ui.screen.carcalc

import com.alternadv.vedhelper.model.CarCalcEngine
import com.alternadv.vedhelper.model.CarCalcParam

data class CarCalcState(
    val vehicle: String = "car",
    val month: Int = 1,
    val year: Int = 2025,
    val cost: Double? = null,
    var engine: String? = "f",

    val calcParams: List<CarCalcParam> = emptyList(),
    val calcEngines: List<CarCalcEngine> = emptyList(),

    val chosenParams: Map<String, Double> = emptyMap(),

    val isLoading: Boolean = false,
    val isCalculating: Boolean = false,
    val errorMessage: String? = null,

    val months: Map<Int, String> = mapOf(
        1 to "Январь",
        2 to "Февраль",
        3 to "Март",
        4 to "Апрель",
        5 to "Май",
        6 to "Июнь",
        7 to "Июль",
        8 to "Август",
        9 to "Сентябрь",
        10 to "Октябрь",
        11 to "Ноябрь",
        12 to "Декабрь"
    )
)