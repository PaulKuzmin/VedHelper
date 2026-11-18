package com.alternadv.vedhelper

import com.alternadv.vedhelper.datasource.DataSource
import com.alternadv.vedhelper.model.CarCalcParamsModel
import com.alternadv.vedhelper.model.CarCalcResultModel
import com.alternadv.vedhelper.model.CarCalcVehiclesModel
import com.alternadv.vedhelper.model.ResponseModel
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Разбор реальных ответов dev-сервера (эндпоинты widget/calcauto).
 *
 * Фикстуры в `src/test/resources/fixtures` сняты с dev 16.11.2026 — это настоящие ответы,
 * а не придуманные. Тесты ловят расхождение моделей с API: если сервер начнёт отдавать
 * поле иначе (числом вместо строки, пустым объектом вместо массива), тест упадёт.
 */
class CarCalcJsonTest {

    private inline fun <reified T> parse(body: String): T {
        val type = Types.newParameterizedType(ResponseModel::class.java, T::class.java)
        return requireNotNull(DataSource.moshi.adapter<ResponseModel<T>>(type).fromJson(body)) {
            "не удалось разобрать ответ"
        }.data
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResource("fixtures/$name")!!.readText()

    // ---------------------------------------------------------------- виды ТС

    @Test
    fun `список видов ТС разбирается и содержит новые типы`() {
        val model = parse<CarCalcVehiclesModel>(fixture("vehicles.json"))

        val ids = model.vehicles.map { it.id }
        assertEquals(19, ids.size)
        assertTrue("пикап должен быть отдельным видом", ids.contains("pickup"))
        assertTrue("самосвал должен быть отдельным видом", ids.contains("dumptruck"))
        assertTrue("гольф-кар должен быть отдельным видом", ids.contains("golfcar"))
        assertTrue(model.vehicles.all { it.name.isNotBlank() })
    }

    // ------------------------------------------------------------------ params

    @Test
    fun `параметры легкового содержат choices для состояния`() {
        val model = parse<CarCalcParamsModel>(fixture("params-car.json"))

        val state = model.calcParams.first { it.code == "state" }
        val choices = requireNotNull(state.choices) { "choices должны разбираться" }
        assertEquals(listOf("used", "new"), choices.map { it.id })
        assertTrue(choices.all { it.name.isNotBlank() })

        // поля-числа не должны получить choices
        val capacity = model.calcParams.first { it.code == "capacity" }
        assertNull(capacity.choices)
        assertEquals("куб.см.", capacity.dimension)
    }

    @Test
    fun `параметры тягача содержат два вида и шесть двигателей`() {
        val model = parse<CarCalcParamsModel>(fixture("params-tractor.json"))

        val kind = model.calcParams.first { it.code == "kind" }
        assertEquals(listOf("eco4", "other"), kind.choices!!.map { it.id })

        assertTrue("гибриды и «без двигателя» приходят с сервера",
            model.calcEngines.map { it.id }.containsAll(listOf("f", "d", "e", "pe", "de", "n")))
    }

    // ------------------------------------------------------------------ result

    @Test
    fun `результат в евро содержит суммы в валюте расчёта`() {
        val model = parse<CarCalcResultModel>(fixture("result-car-eur.json"))

        val currency = requireNotNull(model.calculation.currency) { "блок currency обязателен" }
        assertEquals("978", currency.code)
        assertEquals("EUR", currency.short)

        val u = requireNotNull(model.calculation.u)
        val duty = requireNotNull(u.payments?.firstOrNull { it.type == "import_tax" })
        // сервер отдаёт summa_cur — приложение показывает именно её, ничего не пересчитывая
        assertEquals(2250.0, duty.sumCur!!, 0.01)
        assertEquals(15119.29, u.paymentsSumCur!!, 0.01)

        // и рубли с долларами тоже на месте
        assertTrue(duty.sumRub > 200_000)
        assertTrue(duty.sumUsd > 0)
    }

    @Test
    fun `электромобиль 60 кВт - льготный утильсбор 3400`() {
        val model = parse<CarCalcResultModel>(fixture("result-electric-kvt.json"))

        val f = requireNotNull(model.calculation.f)
        val perk = requireNotNull(f.payments?.firstOrNull { it.type == "recycle_madness_perk" })
        assertEquals(3400.0, perk.sumRub, 0.01)

        // справочный коммерческий утильсбор показан отдельной строкой
        assertEquals(true, f.hasReference)
        val ref = requireNotNull(f.payments?.firstOrNull { it.type == "recycle_madness" })
        assertEquals(800_800.0, ref.sumRub, 0.01)

        // 60 кВт сервер пересчитал сам (60 / 0.75 = 80 л.с.) — в chosen лежит исходное значение
        assertEquals("60", model.chosen.power)
    }

    @Test
    fun `грузовик приходит без блока физлица`() {
        val model = parse<CarCalcResultModel>(fixture("result-cargo-noF.json"))

        assertNull("у грузовика блока F нет", model.calculation.f)
        val u = requireNotNull(model.calculation.u)
        assertEquals(true, u.success)
        assertNotNull(u.tnved)
    }

    @Test
    fun `гольф-кар считается без утильсбора`() {
        val model = parse<CarCalcResultModel>(fixture("result-golfcar.json"))

        val u = requireNotNull(model.calculation.u)
        val recycle = u.payments?.filter { it.type.startsWith("recycle") }.orEmpty()
        assertTrue("у гольф-кара утильсбора быть не должно", recycle.isEmpty())
        assertEquals("8703101800", u.tnved)
    }

    // --------------------------------------------------------- устойчивость

    @Test
    fun `отсутствующий необязательный блок currency не ломает разбор`() {
        // старый сервер (до доработок) не отдавал calculation.currency и summa_cur
        val legacy = """
            {"success":true,"data":{"chosen":{"vehicle":"car","cost":"15000","engine":"f",
            "capacity":"2000","power":"150","seats":null,"weight":null,"bag":null,
            "month":"6","year":"2022","json":true,"ondate":null},
            "calculation":{"age":4.4,"F":null,
            "U":{"success":true,"cost_usd":15000,"cost_rub":1232274,
            "payments_summa_rub":2005828.14,"payments_summa_usd":24416.18,
            "payments":[{"type":"import_tax","name":"Пошлина","rate":"20 %",
            "summa_rub":246454.8,"summa_usd":3000}],"messages":[],"tnved":"8703239082"},
            "currencies":{"840":{"value":82.1516,"dated":"14.11.2026","name":"Доллар США"}}}}}
        """.trimIndent()

        val model = parse<CarCalcResultModel>(legacy)

        assertNull(model.calculation.currency)
        val duty = requireNotNull(model.calculation.u!!.payments!!.firstOrNull())
        assertNull("summa_cur может отсутствовать — экран покажет прочерк, а не USD", duty.sumCur)
        assertNull(model.calculation.u!!.paymentsSumCur)
        assertEquals(246454.8, duty.sumRub, 0.01)
    }

    @Test
    fun `ответ с success=false и сообщениями разбирается без исключения`() {
        val errorBody = """
            {"success":true,"data":{"chosen":{"vehicle":"cargo","cost":"","engine":"d",
            "capacity":"6000","power":"300","seats":null,"weight":"10000","bag":null,
            "month":"6","year":"2022","json":true,"ondate":null},
            "calculation":{"age":4.4,"U":{"success":false,
            "messages":[{"type":"e","message":"Укажите стоимость числом, в выбранной валюте"}]},
            "currencies":[]}}}
        """.trimIndent()

        val model = parse<CarCalcResultModel>(errorBody)

        val u = requireNotNull(model.calculation.u)
        assertEquals(false, u.success)
        assertEquals("Укажите стоимость числом, в выбранной валюте", u.messages!!.single().message)
        assertEquals("e", u.messages!!.single().type)
        assertFalse(model.calculation.u!!.success!!)
    }
}
