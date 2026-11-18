package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class CalcResultModel(
    val chosen: Chosen?,
    val calculation: CalcResultCalculationModel?
)

data class CalcResultCalculationModel(
    val success: Boolean?,

    @param:Json(name = "cost_usd")
    val costUsd: Double?,

    @param:Json(name = "cost_rub")
    val costRub: Double?,

    @param:Json(name = "payments_summa_rub")
    val paymentsSummaRub: Double?,

    @param:Json(name = "payments_summa_usd")
    val paymentsSummaUsd: Double?,

    /** Итог в валюте стоимости — считает сервер, приложение не пересчитывает. */
    @param:Json(name = "payments_summa_cur")
    val paymentsSummaCur: Double? = null,

    /** Валюта, в которой задана стоимость (bлок calculation.currency). */
    val currency: CalcCurrencyInfo? = null,

    val payments: List<CalcResultPaymentModel>?,
    val currencies: Map<String, CalcCurrencyRate>?,
    val messages: List<CalcMessage>?,
)

data class CalcResultPaymentModel (
    val type: String?,
    val name: String?,
    val rate: String?,

    @param:Json(name = "summa_rub")
    val summaRub: Double?,

    @param:Json(name = "summa_usd")
    val summaUsd: Double?,

    /** Сумма в валюте стоимости. */
    @param:Json(name = "summa_cur")
    val summaCur: Double? = null
)