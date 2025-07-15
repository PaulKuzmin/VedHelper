package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class AutoCalcResultModel(
    val chosen: ChosenVehicle,
    val calculation: AutoCalculation
)

data class ChosenVehicle(
    val vehicle: String?,
    val cost: String?,
    val engine: String?,
    val capacity: String?,
    val power: String?,
    val seats: String?,
    val weight: String?,
    val bag: String?,
    val month: String?,
    val year: String?,
    val json: Boolean,
    val ondate: String?
)

data class AutoCalculation(
    val age: Double,

    @param:Json(name = "F")
    val f: AutoPartCalculation?,

    @param:Json(name = "U")
    val u: AutoPartCalculation?,

    val currencies: Map<String, CalcCurrencyRate>?
)

data class AutoPartCalculation(
    val success: Boolean?,

    @param:Json(name = "cost_usd")
    val costUsd: String?,

    @param:Json(name = "cost_rub")
    val costRub: Double?,

    @param:Json(name = "cost_eur")
    val costEur: Double? = null,

    @param:Json(name = "payments_summa_rub")
    val paymentsSumRub: Double?,

    @param:Json(name = "payments_summa_usd")
    val paymentsSumUsd: Double?,

    val payments: List<AutoCustomsPayment>?,
    val messages: List<CalcMessage>?,

    val tnved: String? = null
)

data class AutoCustomsPayment(
    val type: String,
    val name: String,
    val rate: String,

    @param:Json(name = "summa_rub")
    val sumRub: Double,

    @param:Json(name = "summa_usd")
    val sumUsd: Double
)

data class CalcCurrencyRate(
    val value: Double,
    val dated: String,
    val name: String
)

data class CalcMessage(
    val type: String,
    val message: String
)
