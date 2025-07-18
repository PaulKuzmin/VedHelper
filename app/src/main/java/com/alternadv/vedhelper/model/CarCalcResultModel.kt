package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class CarCalcResultModel(
    val chosen: ChosenVehicle,
    val calculation: CarCalculation
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

data class CarCalculation(
    val age: Double,

    @param:Json(name = "F")
    val f: CarPartCalculation?,

    @param:Json(name = "U")
    val u: CarPartCalculation?,

    val currencies: Map<String, CalcCurrencyRate>?
)

data class CarPartCalculation(
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

    val payments: List<CarCustomsPayment>?,
    val messages: List<CalcMessage>?,

    val tnved: String? = null
)

data class CarCustomsPayment(
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
