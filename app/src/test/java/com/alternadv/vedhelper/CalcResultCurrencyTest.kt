package com.alternadv.vedhelper

import com.alternadv.vedhelper.datasource.DataSource
import com.alternadv.vedhelper.model.CalcResultModel
import com.alternadv.vedhelper.model.ResponseModel
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Валюта стоимости в товарном калькуляторе (widget/calculator).
 *
 * Сервер принимает `currency` и сам возвращает суммы в этой валюте (`summa_cur`,
 * `payments_summa_cur`, блок `calculation.currency`) — приложение ничего не пересчитывает.
 * Эти тесты фиксируют разбор такой схемы и совместимость со старым ответом без неё.
 */
class CalcResultCurrencyTest {

    private inline fun <reified T> parse(body: String): T {
        val type = Types.newParameterizedType(ResponseModel::class.java, T::class.java)
        return requireNotNull(DataSource.moshi.adapter<ResponseModel<T>>(type).fromJson(body)) {
            "не удалось разобрать ответ"
        }.data
    }

    private val withCurrency = """
        {"success":true,"data":{
          "chosen":{"code":"8703239082","direction":"I","country":"000","param_cost":13536.68,
                    "json":true,"addons":[],"specials":[],"currency":"978"},
          "calculation":{"success":true,"cost_usd":14960.74,"cost_rub":1234474.83,
            "payments_summa_rub":585936.32,"payments_summa_usd":7100.28,
            "payments_summa_cur":6429.5,
            "currency":{"code":"978","name":"Евро","short":"EUR","symbol":"€"},
            "payments":[
              {"type":"import_tax","name":"Пошлина","rate":"20 %",
               "summa_rub":246894.97,"summa_usd":2992.15,"summa_cur":2707.34},
              {"type":"vat","name":"НДС","rate":"22 %",
               "summa_rub":325901.35,"summa_usd":3949.63,"summa_cur":3573.68}
            ],
            "currencies":{"840":{"value":82.5143,"dated":"16.11.2026","name":"Доллар США"},
                          "978":{"value":91.1948,"dated":"16.11.2026","name":"Евро"}},
            "messages":[]}}}
    """.trimIndent()

    private val withoutCurrency = """
        {"success":true,"data":{
          "chosen":{"code":"8703239082","direction":"I","country":"000","param_cost":15000,
                    "json":true,"addons":[],"specials":[]},
          "calculation":{"success":true,"cost_usd":15000,"cost_rub":1237714.5,
            "payments_summa_rub":585936.32,"payments_summa_usd":7100.28,
            "payments":[{"type":"import_tax","name":"Пошлина","rate":"20 %",
                         "summa_rub":246894.97,"summa_usd":2992.15}],
            "currencies":{"840":{"value":82.5143,"dated":"16.11.2026","name":"Доллар США"}},
            "messages":[]}}}
    """.trimIndent()

    @Test
    fun `валюта стоимости и суммы в ней разбираются`() {
        val model = parse<CalcResultModel>(withCurrency)
        val calc = requireNotNull(model.calculation)

        assertEquals("978", calc.currency?.code)
        assertEquals("EUR", calc.currency?.short)
        assertEquals("€", calc.currency?.symbol)
        assertEquals(6429.5, calc.paymentsSummaCur!!, 0.01)

        val duty = requireNotNull(calc.payments?.firstOrNull())
        assertEquals(2707.34, duty.summaCur!!, 0.01)
        // рубли и доллары остаются на месте
        assertEquals(246894.97, duty.summaRub!!, 0.01)
        assertEquals(2992.15, duty.summaUsd!!, 0.01)
    }

    @Test
    fun `старый ответ без валюты разбирается без падения`() {
        val model = parse<CalcResultModel>(withoutCurrency)
        val calc = requireNotNull(model.calculation)

        assertNull("блока currency быть не должно", calc.currency)
        assertNull("итога в валюте быть не должно", calc.paymentsSummaCur)
        assertNull("summa_cur у платежа отсутствует", calc.payments?.first()?.summaCur)
        assertEquals(246894.97, calc.payments?.first()?.summaRub!!, 0.01)
    }

    @Test
    fun `в USD колонка валюты не нужна - код 840`() {
        val usd = """
            {"success":true,"data":{"chosen":{"code":"1","direction":"I","country":"000",
              "param_cost":1,"json":true,"addons":[],"specials":[]},
              "calculation":{"success":true,"cost_usd":1,"cost_rub":1,
                "payments_summa_rub":1,"payments_summa_usd":1,"payments_summa_cur":1,
                "currency":{"code":"840","name":"Доллары США","short":"USD","symbol":"$"},
                "payments":[],"currencies":{},"messages":[]}}}
        """.trimIndent()

        val calc = requireNotNull(parse<CalcResultModel>(usd).calculation)
        assertNotNull(calc.currency)
        // экран показывает «В EUR» только когда код != 840 — это условие в UI
        assertEquals("840", calc.currency?.code)
    }
}
