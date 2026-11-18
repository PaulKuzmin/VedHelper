package com.alternadv.vedhelper.ui.screen.carcalc

import com.alternadv.vedhelper.model.CarCalcEngine
import com.alternadv.vedhelper.model.CarCalcParam
import com.alternadv.vedhelper.model.CarCalcVehicle

/**
 * Состояние формы «Авто».
 *
 * Важно: приложение НЕ считает и НЕ конвертирует ничего само — ни валюту, ни мощность.
 * Все значения хранятся ровно так, как их ввёл пользователь (строкой), и уходят на сервер.
 * Пересчёт валюты и кВт→л.с. делает API (см. summa_cur / power_edizm в ответе).
 */
data class CarCalcState(
    val vehicle: String = "car",
    val month: Int = 1,
    val year: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),

    /** Стоимость как ввёл пользователь, в валюте [currency]. */
    val costInput: String = "",
    /** Код валюты стоимости (840, 978, 156, 392, 410). */
    val currency: String = "840",

    val engine: String? = "f",

    /** Мощность как ввёл пользователь, в единицах [powerUnit]. */
    val powerInput: String = "",
    /** Единицы мощности для power_edizm: "ls" | "kvt". */
    val powerUnit: String = "ls",

    /** Тягач Евро-5/6 для международных перевозок (intl=1). Уходит только для tractor. */
    val intl: Boolean = false,

    /** Все прочие поля формы сырыми строками: capacity, seats, weight, bag, len, state, kind, intl… */
    val paramValues: Map<String, String> = emptyMap(),

    val vehicles: List<CarCalcVehicle> = emptyList(),
    val calcParams: List<CarCalcParam> = emptyList(),
    val calcEngines: List<CarCalcEngine> = emptyList(),

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
