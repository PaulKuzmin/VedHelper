package com.alternadv.vedhelper

import com.alternadv.vedhelper.datasource.DataSource
import com.alternadv.vedhelper.model.CalcResultModel
import com.alternadv.vedhelper.model.CarCalcResultModel
import com.alternadv.vedhelper.model.ResponseModel
import com.alternadv.vedhelper.ui.screen.carcalc.CarCalcState
import com.alternadv.vedhelper.ui.screen.carcalc.calcErrorText
import com.alternadv.vedhelper.utils.REFERENCE_RECYCLE_TYPE
import com.alternadv.vedhelper.utils.buildCarReportRows
import com.alternadv.vedhelper.utils.buildReportRows
import com.alternadv.vedhelper.utils.costReportValue
import com.alternadv.vedhelper.utils.PDF_USD_HEADER
import com.alternadv.vedhelper.utils.formatPdfSum
import com.alternadv.vedhelper.utils.formatSum
import com.alternadv.vedhelper.utils.hasCurAmounts
import com.alternadv.vedhelper.utils.pdfSecondAmountColumn
import com.alternadv.vedhelper.utils.needsReferenceFootnote
import com.alternadv.vedhelper.utils.pickAmount
import com.alternadv.vedhelper.utils.powerReportLabel
import com.alternadv.vedhelper.utils.referencePaymentName
import com.alternadv.vedhelper.utils.referencePayments
import com.alternadv.vedhelper.utils.regularPayments
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

/**
 * Отображение результата «Авто» и PDF: справочный утильсбор, F_denied, прочерк вместо
 * отсутствующей summa_cur, подписи PDF. Фикстуры — реальные ответы dev (16–18.11.2026).
 */
class CarCalcResultDisplayTest {

    private inline fun <reified T> parse(body: String): T {
        val type = Types.newParameterizedType(ResponseModel::class.java, T::class.java)
        return requireNotNull(DataSource.moshi.adapter<ResponseModel<T>>(type).fromJson(body)).data
    }

    private fun fixture(name: String): String =
        javaClass.classLoader!!.getResource("fixtures/$name")!!.readText()

    private fun car(name: String) = parse<CarCalcResultModel>(fixture(name))

    // ------------------------------------------------------ справочный утильсбор

    @Test
    fun `справочный утильсбор отделяется от платежей и не входит в итог`() {
        val f = requireNotNull(car("result-electric-kvt.json").calculation.f)
        assertEquals(true, f.hasReference)

        val refs = f.referencePayments()
        assertEquals(1, refs.size)
        assertEquals(REFERENCE_RECYCLE_TYPE, refs.single().type)
        assertTrue(f.regularPayments().none { it.type == REFERENCE_RECYCLE_TYPE })
        assertTrue("льготный утильсбор остаётся обычным платежом",
            f.regularPayments().any { it.type == "recycle_madness_perk" })

        // итог сервера = сумма обычных платежей, справочная строка в него не входит
        val sum = f.regularPayments().sumOf { it.sumRub }
        assertEquals(f.paymentsSumRub!!, sum, 0.01)
    }

    @Test
    fun `без has_reference строка recycle_madness - обычный платёж`() {
        val u = requireNotNull(car("result-electric-kvt.json").calculation.u)
        assertTrue(u.hasReference != true)
        assertTrue(u.referencePayments().isEmpty())
        assertEquals(u.payments.orEmpty().size, u.regularPayments().size)
    }

    @Test
    fun `подпись справочной строки без звёздочки`() {
        assertEquals("Утилизационный сбор по коммерческой ставке", referencePaymentName("Утилизационный сбор *"))
        assertEquals("Утилизационный сбор по коммерческой ставке", referencePaymentName("Утилизационный сбор"))
    }

    @Test
    fun `сноска о перепродаже только при справочном утильсборе`() {
        assertTrue(car("result-electric-kvt.json").calculation.needsReferenceFootnote())
        assertTrue(car("result-car-eur.json").calculation.needsReferenceFootnote())
        assertFalse("у гольф-кара сноски нет", car("result-golfcar.json").calculation.needsReferenceFootnote())
        assertFalse("у грузовика сноски нет", car("result-cargo-noF.json").calculation.needsReferenceFootnote())
    }

    // ------------------------------------------------------------------ F_denied

    @Test
    fun `F_denied разбирается и юрлицо посчитано (тягач, intl)`() {
        val model = car("result-tractor-intl.json")

        assertNull(model.calculation.f)
        val denied = requireNotNull(model.calculation.fDenied)
        assertTrue(denied.contains("личного пользования"))
        assertEquals(true, model.calculation.u?.success)
        assertNull("показываем результат, а не диалог ошибки", calcErrorText(model))
    }

    @Test
    fun `без F_denied поле пустое`() {
        assertNull(car("result-car-eur.json").calculation.fDenied)
    }

    // --------------------------------------------- частичный успех и ошибки сервера

    private fun result(fSuccess: Boolean?, uSuccess: Boolean, uMessage: String? = null): CarCalcResultModel {
        val f = when (fSuccess) {
            null -> "null"
            true -> """{"success":true,"payments":[],"messages":[]}"""
            false -> """{"success":false,"messages":[{"type":"e","message":"Ошибка физлица"}]}"""
        }
        val uMsgs = uMessage?.let { """[{"type":"e","message":"$it"}]""" } ?: "[]"
        return parse(
            """{"success":true,"data":{"chosen":{"vehicle":"car","cost":"1","engine":"f","capacity":null,
               "power":null,"seats":null,"weight":null,"bag":null,"month":"1","year":"2024","json":true,
               "ondate":null},"calculation":{"age":2,"F":$f,"U":{"success":$uSuccess,"payments":[],
               "messages":$uMsgs},"currencies":[]}}}"""
        )
    }

    @Test
    fun `ошибка физлица не прячет расчёт юрлица`() {
        assertNull(calcErrorText(result(fSuccess = false, uSuccess = true)))
    }

    @Test
    fun `если не посчитано ничего - показываем текст сервера`() {
        val text = requireNotNull(calcErrorText(result(fSuccess = null, uSuccess = false, uMessage = "Укажите стоимость")))
        assertTrue(text.contains("Укажите стоимость"))
        assertFalse(text.contains("Неизвестная"))
    }

    @Test
    fun `пустой ответ - понятная ошибка`() {
        assertNotNull(calcErrorText(null))
    }

    // ------------------------------------------------------- суммы и прочерк

    @Test
    fun `нет summa_cur - прочерк, а не доллары`() {
        val usd = 3000.0
        assertNull(pickAmount("currency", rub = 246454.8, usd = usd, cur = null))
        assertEquals("—", formatSum(pickAmount("currency", 246454.8, usd, null)))
        assertEquals("3000.00", formatSum(pickAmount("dollars", 246454.8, usd, null)))
        assertEquals("2250.00", formatSum(pickAmount("currency", 1.0, 2.0, 2250.0)))
    }

    // --------------------------------------------------------------------- PDF

    @Test
    fun `PDF - стоимость в валюте ввода с кодом, а не как доллары`() {
        val model = car("result-car-eur.json")
        val rows = buildCarReportRows(model, null).parameters

        assertNull(rows.firstOrNull { it.first.contains("дол. США") })
        assertEquals("15000 EUR", rows.first { it.first == "Стоимость" }.second)
        assertEquals("15000 EUR", costReportValue(model.calculation, model.chosen))
    }

    @Test
    fun `PDF - мощность подписана в тех единицах, в которых введена`() {
        assertEquals("Мощность, кВт", powerReportLabel("kvt"))
        assertEquals("Мощность, л.с.", powerReportLabel("ls"))
        assertEquals("Мощность, л.с.", powerReportLabel(null))

        val electric = car("result-electric-kvt.json")
        assertEquals("kvt", electric.chosen.powerEdizm)
        val power = buildCarReportRows(electric, null).parameters.first { it.first.startsWith("Мощность") }
        assertEquals("Мощность, кВт", power.first)
        assertEquals("60", power.second)
    }

    @Test
    fun `PDF - объём багажника в кубометрах`() {
        val model = car("result-car-eur.json")
        val withBag = model.copy(chosen = model.chosen.copy(bag = "0.5"))
        val rows = buildCarReportRows(withBag, null).parameters
        assertEquals("0.5", rows.first { it.first == "Объем багажника, куб.м." }.second)
        assertFalse(rows.any { it.first.contains("багажника, куб.см.") })
    }

    @Test
    fun `PDF - пустые параметры не выводятся`() {
        // легковой: мест, массы и багажника в запросе нет — строк с пустым значением быть не должно
        val passenger = buildCarReportRows(car("result-car-eur.json"), null).parameters
        assertTrue(passenger.none { it.second.isBlank() })
        assertFalse(passenger.any { it.first == "Количество мест" || it.first == "Масса, кг." })
        assertTrue(passenger.any { it.first == "Объем двигателя, куб.см." })

        // гольф-кар: ни объёма, ни мощности
        val golf = buildCarReportRows(car("result-golfcar.json"), null).parameters
        assertFalse(golf.any { it.first.startsWith("Объем двигателя") || it.first.startsWith("Мощность") })
    }

    @Test
    fun `PDF - справочный утильсбор после итога и помечен`() {
        val f = buildCarReportRows(car("result-electric-kvt.json"), null).resultsF
        val totalIdx = f.indexOfFirst { it.first == "Итого" }
        assertTrue(totalIdx >= 0)
        val ref = f.drop(totalIdx + 1)
        assertEquals(1, ref.size)
        assertTrue(ref.single().first.contains("по коммерческой ставке"))
        assertTrue(ref.single().first.contains("не входит в итог"))
    }

    @Test
    fun `PDF - F_denied попадает в раздел физлица`() {
        val rows = buildCarReportRows(car("result-tractor-intl.json"), null)
        assertTrue(rows.resultsF.isEmpty())
        assertTrue(requireNotNull(rows.fNote).contains("личного пользования"))
        assertTrue(rows.resultsU.isNotEmpty())
        assertNull(rows.uNote)
    }

    @Test
    fun `PDF - стоимость в EUR - колонка EUR вместо долларов, рубли остаются`() {
        val model = car("result-car-eur.json")
        assertTrue(model.calculation.hasCurAmounts())

        val column = model.calculation.pdfSecondAmountColumn()
        assertEquals("currency", column.mode)
        assertEquals("Сумма, EUR", column.header)

        val rows = buildCarReportRows(model, null)
        assertEquals("Сумма, EUR", rows.amountHeader)

        val f = requireNotNull(model.calculation.f)
        val payment = f.regularPayments().first()
        val row = rows.resultsF.first()
        assertEquals(payment.name, row.first)
        assertEquals(formatPdfSum(payment.sumRub), row.third)
        assertEquals(formatPdfSum(payment.sumCur), row.fourth)
        assertFalse("долларов в колонке быть не должно", row.fourth == formatPdfSum(payment.sumUsd))

        val total = rows.resultsF.first { it.first == "Итого" }
        assertEquals(formatPdfSum(f.paymentsSumRub), total.third)
        assertEquals(formatPdfSum(f.paymentsSumCur), total.fourth)

        val u = requireNotNull(model.calculation.u)
        val uTotal = rows.resultsU.first { it.first == "Итого" }
        assertEquals(formatPdfSum(u.paymentsSumCur), uTotal.fourth)
    }

    @Test
    fun `PDF - стоимость в долларах - колонка долларов`() {
        for (name in listOf("result-electric-kvt.json", "result-tractor-intl.json", "result-golfcar.json")) {
            val model = car(name)
            assertFalse(name, model.calculation.hasCurAmounts())
            assertEquals(name, PDF_USD_HEADER, buildCarReportRows(model, null).amountHeader)
        }
        val f = requireNotNull(car("result-electric-kvt.json").calculation.f)
        val row = buildCarReportRows(car("result-electric-kvt.json"), null).resultsF.first()
        assertEquals(formatPdfSum(f.regularPayments().first().sumUsd), row.fourth)
    }

    @Test
    fun `PDF - старый API без summa_cur - остаются доллары, а не пустая колонка`() {
        val model = car("result-car-eur.json")
        val strip = { p: com.alternadv.vedhelper.model.CarPartCalculation? ->
            p?.copy(paymentsSumCur = null, payments = p.payments?.map { it.copy(sumCur = null) })
        }
        val old = model.copy(calculation = model.calculation.copy(f = strip(model.calculation.f), u = strip(model.calculation.u)))
        assertFalse(old.calculation.hasCurAmounts())
        assertEquals(PDF_USD_HEADER, old.calculation.pdfSecondAmountColumn().header)
        assertEquals("dollars", old.calculation.pdfSecondAmountColumn().mode)
    }

    @Test
    fun `PDF - нет суммы в валюте у строки - прочерк`() {
        assertEquals("—", formatPdfSum(null))
        assertEquals("1,234.50", formatPdfSum(1234.5))
    }

    @Test
    fun `PDF товаров - итого это сумма платежей, а не таможенная стоимость`() {
        val body = """
            {"success":true,"data":{
              "chosen":{"code":"8703239082","direction":"I","country":"000","param_cost":15000,
                        "json":true,"addons":[],"specials":[]},
              "calculation":{"success":true,"cost_usd":15000,"cost_rub":1237714.5,
                "payments_summa_rub":585936.32,"payments_summa_usd":7100.28,
                "payments":[{"type":"import_tax","name":"Пошлина","rate":"20 %",
                             "summa_rub":246894.97,"summa_usd":2992.15}],
                "currencies":{},"messages":[]}}}
        """.trimIndent()
        val (_, results) = buildReportRows(parse<CalcResultModel>(body), null)

        val total = results.last()
        assertEquals("Итого", total.first)
        assertEquals(String.format(Locale.US, "%,.2f", 585936.32), total.third)
        assertEquals(String.format(Locale.US, "%,.2f", 7100.28), total.fourth)
    }

    // ------------------------------------------------------------- дефолты формы

    @Test
    fun `год по умолчанию - текущий`() {
        assertEquals(Calendar.getInstance().get(Calendar.YEAR), CarCalcState().year)
        assertFalse(CarCalcState().intl)
    }
}
