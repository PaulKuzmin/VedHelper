package com.alternadv.vedhelper.utils

import com.alternadv.vedhelper.model.CarCalculation
import com.alternadv.vedhelper.model.CarCustomsPayment
import com.alternadv.vedhelper.model.CarPartCalculation
import com.alternadv.vedhelper.model.ChosenVehicle
import java.util.Locale

/**
 * Чистые функции отображения результата «Авто» — общие для экрана и PDF, покрыты тестами.
 */

/** Тип строки коммерческого утильсбора, который сервер кладёт справочно рядом с льготным. */
const val REFERENCE_RECYCLE_TYPE = "recycle_madness"

const val REFERENCE_NOTE = "не входит в итог"

/** Сноска про справочный утильсбор (текст как на сайте, calcauto_result.twig). */
const val REFERENCE_FOOTNOTE = "Утилизационный сбор по коммерческой ставке указан справочно и в итог " +
        "не входит. Он уплачивается вместо льготного, если автомобиль продали в течение 12 месяцев " +
        "после ввоза, не зарегистрировали на ввозившего или это второй льготный ввоз за 12 месяцев."

/**
 * Справочная строка: has_reference=true и тип recycle_madness. В итог (payments_summa_*) она
 * не входит. У юрлица recycle_madness — обычный платёж (has_reference там нет).
 */
fun CarPartCalculation.isReferencePayment(payment: CarCustomsPayment): Boolean =
    hasReference == true && payment.type == REFERENCE_RECYCLE_TYPE

fun CarPartCalculation.regularPayments(): List<CarCustomsPayment> =
    payments.orEmpty().filterNot { isReferencePayment(it) }

fun CarPartCalculation.referencePayments(): List<CarCustomsPayment> =
    payments.orEmpty().filter { isReferencePayment(it) }

/** «Утилизационный сбор *» → «Утилизационный сбор по коммерческой ставке». */
fun referencePaymentName(name: String): String =
    name.trim().trimEnd('*', ' ') + " по коммерческой ставке"

/** Сноска про перепродажу нужна только когда есть справочный утильсбор (не для гольф-кара/судна). */
fun CarCalculation.needsReferenceFootnote(): Boolean = f?.hasReference == true

/** Сумма для выбранного режима; null — сервер её не прислал (summa_cur у старого API). */
fun pickAmount(mode: String, rub: Double?, usd: Double?, cur: Double?): Double? = when (mode) {
    "rubles" -> rub
    "dollars" -> usd
    else -> cur
}

private fun formatMoneyRu(value: Double?): String =
    value?.let { String.format(Locale.forLanguageTag("ru-RU"), "%,.2f", it).replace('\u00A0', ' ') } ?: "—"

/** Нет суммы — прочерк, а не доллары под подписью другой валюты. */
fun formatSum(value: Double?): String = formatMoneyRu(value)

private val CURRENCY_SHORT = mapOf(
    "840" to "USD",
    "978" to "EUR",
    "156" to "CNY",
    "392" to "JPY",
    "410" to "KRW"
)

/** Код валюты стоимости для подписи: из calculation.currency, иначе по chosen.currency, по умолчанию USD (как на сервере). */
fun costCurrencyShort(calculation: CarCalculation, chosen: ChosenVehicle): String =
    calculation.currency?.short?.takeIf { it.isNotBlank() }
        ?: chosen.currency?.let { CURRENCY_SHORT[it] ?: it }
        ?: "USD"

/** Значение строки «Стоимость» в PDF: сумма в валюте ввода с её кодом. */
fun costReportValue(calculation: CarCalculation, chosen: ChosenVehicle): String {
    val cost = chosen.cost?.takeIf { it.isNotBlank() } ?: return ""
    return "$cost ${costCurrencyShort(calculation, chosen)}"
}

/** Подпись мощности по единицам, в которых её ввели (power_edizm: ls | kvt). */
fun powerReportLabel(powerEdizm: String?): String =
    if (powerEdizm == "kvt") "Мощность, кВт" else "Мощность, л.с."

/** Код доллара США в calculation.currency.code. */
private const val USD_CODE = "840"

/**
 * Сервер прислал суммы в валюте стоимости (summa_cur) и эта валюта — не доллар.
 * Тогда на экране есть кнопка «В EUR» и т.п., а в PDF колонка этой валюты заменяет долларовую.
 */
fun CarCalculation.hasCurAmounts(): Boolean {
    val code = currency?.code ?: return false
    return code != USD_CODE && (u?.paymentsSumCur != null || f?.paymentsSumCur != null)
}

/** Вторая денежная колонка PDF (первая — всегда рубли): режим для [pickAmount] и заголовок. */
data class PdfAmountColumn(val mode: String, val header: String)

const val PDF_USD_HEADER = "Сумма, $"

/**
 * Как на экране: стоимость введена в EUR/CNY/... и сервер вернул summa_cur — колонка этой валюты
 * (заголовок по calculation.currency.short), долларовой нет. Иначе — доллары.
 */
fun CarCalculation.pdfSecondAmountColumn(): PdfAmountColumn =
    if (hasCurAmounts()) {
        val short = currency?.short?.takeIf { it.isNotBlank() } ?: currency?.code.orEmpty()
        PdfAmountColumn(mode = "currency", header = "Сумма, $short")
    } else {
        PdfAmountColumn(mode = "dollars", header = PDF_USD_HEADER)
    }

/** Сумма для таблицы PDF: тот же русский формат, что и на экране. */
fun formatPdfSum(value: Double?): String = formatMoneyRu(value)