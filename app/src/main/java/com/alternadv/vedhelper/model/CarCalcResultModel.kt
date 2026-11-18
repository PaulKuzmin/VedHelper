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
    val ondate: String?,
    val currency: String? = null,
    val kind: String? = null,
    val state: String? = null,
    /** Единицы введённой мощности: ls | kvt (сервер возвращает то, что пришло в запросе). */
    @param:Json(name = "power_edizm")
    val powerEdizm: String? = null,
    val len: String? = null
)

data class CarCalculation(
    val age: Double,

    @param:Json(name = "F")
    val f: CarPartCalculation?,

    @param:Json(name = "U")
    val u: CarPartCalculation?,

    val currencies: Map<String, CalcCurrencyRate>?,

    /** Валюта, в которой введена стоимость и в которой сервер вернул summa_cur. */
    val currency: CalcCurrencyInfo? = null,

    /** Почему физлицу этот вид ТС не ввезти (блока F тогда нет) — показываем вместо расчёта. */
    @param:Json(name = "F_denied")
    val fDenied: String? = null
)

/** Описание валюты расчёта из блока calculation.currency. */
data class CalcCurrencyInfo(
    val code: String?,
    val name: String?,
    val short: String?,
    val symbol: String?
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

    /** Итог в валюте стоимости (сервер считает его же, summa_cur по каждому платежу). */
    @param:Json(name = "payments_summa_cur")
    val paymentsSumCur: Double? = null,

    val payments: List<CarCustomsPayment>?,
    val messages: List<CalcMessage>?,

    val tnved: String? = null,

    /** Справочный (коммерческий) утильсбор показан рядом с льготным. */
    @param:Json(name = "has_reference")
    val hasReference: Boolean? = null
)

data class CarCustomsPayment(
    val type: String,
    val name: String,
    val rate: String,

    @param:Json(name = "summa_rub")
    val sumRub: Double,

    @param:Json(name = "summa_usd")
    val sumUsd: Double,

    /** Сумма в валюте стоимости — считает сервер, приложение не пересчитывает. */
    @param:Json(name = "summa_cur")
    val sumCur: Double? = null
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
