package com.alternadv.vedhelper

import com.alternadv.vedhelper.model.CarCalcParam
import com.alternadv.vedhelper.ui.screen.carcalc.CarCalcState
import com.alternadv.vedhelper.ui.screen.carcalc.normalizeNumber
import com.alternadv.vedhelper.ui.screen.carcalc.supportsIntl
import com.alternadv.vedhelper.ui.screen.carcalc.toParamMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Проверки сборки запроса к API (widget/calcauto/result).
 *
 * Смысл: приложение не должно считать и конвертировать ничего само — валюту и мощность
 * пересчитывает сервер (проверено на dev/TKS: 110.32 кВт → 147.09 л.с., а не 150).
 * Эти тесты фиксируют контракт, чтобы правки не вернули клиентские конвертации.
 */
class CarCalcRequestTest {

    private fun state(
        vehicle: String = "car",
        cost: String = "15000",
        currency: String = "840",
        engine: String? = "f",
        power: String = "150",
        unit: String = "ls",
        values: Map<String, String> = mapOf("capacity" to "2000", "state" to "new"),
        month: Int = 6,
        year: Int = 2022,
        // поля формы: мощность + все поля, для которых заданы значения (как при обычном вводе)
        params: List<CarCalcParam> = listOf(POWER_FIELD) + values.keys.map { CarCalcParam(it, "", it) },
        intl: Boolean = false
    ) = CarCalcState(
        vehicle = vehicle,
        month = month,
        year = year,
        costInput = cost,
        currency = currency,
        engine = engine,
        powerInput = power,
        powerUnit = unit,
        paramValues = values,
        calcParams = params,
        intl = intl
    )

    private companion object {
        val POWER_FIELD = CarCalcParam(name = "Мощность", dimension = "л.с.", code = "power")
        val CAPACITY_FIELD = CarCalcParam(name = "Объём", dimension = "куб.см.", code = "capacity")
    }

    @Test
    fun `стоимость уходит как есть вместе с валютой`() {
        val map = state(cost = "13536.68", currency = "978").toParamMap()

        assertEquals("13536.68", map["cost"])
        assertEquals("978", map["currency"])
    }

    @Test
    fun `мощность в кВт не конвертируется в приложении`() {
        val map = state(power = "60", unit = "kvt").toParamMap()

        // Ключевое: отправляем 60 кВт + power_edizm=kvt. Сервер сам даст 80 л.с. (60 / 0.75).
        // Если бы приложение конвертировало (ceil(60*1.35962)=82), льгота утильсбора терялась:
        // 3 400 ₽ вместо 991 200 ₽.
        assertEquals("60", map["power"])
        assertEquals("kvt", map["power_edizm"])
    }

    @Test
    fun `мощность в лошадиных силах помечается как ls`() {
        val map = state(power = "150", unit = "ls").toParamMap()

        assertEquals("150", map["power"])
        assertEquals("ls", map["power_edizm"])
    }

    @Test
    fun `поля из calc_params уходят как есть`() {
        val map = state(
            values = mapOf("capacity" to "2000", "power" to "150", "state" to "used")
        ).toParamMap()

        assertEquals("2000", map["capacity"])
        assertEquals("used", map["state"])
        assertEquals("f", map["engine"])
    }

    @Test
    fun `поля-списки передаются строкой, а не числом`() {
        val map = state(values = mapOf("kind" to "motor_sea")).toParamMap()

        assertEquals("motor_sea", map["kind"])
    }

    @Test
    fun `пустые значения не попадают в запрос`() {
        val map = state(cost = "", values = emptyMap()).toParamMap()

        assertFalse(map.containsKey("cost"))
        assertFalse(map.containsKey("capacity"))
        assertEquals("840", map["currency"])
    }

    @Test
    fun `новые виды ТС передаются своим id`() {
        listOf("pickup", "dumptruck", "golfcar").forEach { vehicle ->
            val map = state(vehicle = vehicle).toParamMap()
            assertEquals(vehicle, map["vehicle"])
        }
    }

    @Test
    fun `json не дублируется - его добавляет DataSource`() {
        val map = state().toParamMap()

        assertFalse("json не должен входить в параметры", map.containsKey("json"))
    }

    @Test
    fun `дробные значения сохраняются`() {
        assertEquals("1.6", normalizeNumber("1.6"))
        assertEquals("1.6", normalizeNumber("1,6"))
        assertEquals("1.75", normalizeNumber("1.75"))
        assertEquals("20", normalizeNumber("20"))
        assertEquals("1.6", normalizeNumber("1.6.3"))
        assertEquals("", normalizeNumber(""))
    }

    @Test
    fun `объем в литрах доходит до сервера как литры`() {
        // Сервер сам понимает «<20 → литры» и переводит в см3. Приложение не трогает.
        val map = state(values = mapOf("capacity" to "1.6")).toParamMap()

        assertEquals("1.6", map["capacity"])
        assertTrue(map["capacity"]!!.contains('.'))
    }

    // --------------------------------------------------- мощность: только если поле есть

    @Test
    fun `мощность не уходит, если в форме вида ТС нет поля power (гольф-кар)`() {
        // у гольф-кара calc_params пустой; мощность, введённая для легкового, не должна утечь
        val map = state(vehicle = "golfcar", power = "150", unit = "kvt", params = emptyList()).toParamMap()

        assertFalse(map.containsKey("power"))
        assertFalse("единицы без мощности тоже не нужны", map.containsKey("power_edizm"))
    }

    @Test
    fun `мощность не уходит, если поля power нет, а другие поля есть`() {
        val map = state(params = listOf(CAPACITY_FIELD)).toParamMap()

        assertFalse(map.containsKey("power"))
        assertFalse(map.containsKey("power_edizm"))
    }

    @Test
    fun `пустая мощность не уходит даже при наличии поля`() {
        val map = state(power = "").toParamMap()

        assertFalse(map.containsKey("power"))
        assertFalse(map.containsKey("power_edizm"))
    }

    // ------------------------------------------------------------ intl (тягач)

    @Test
    fun `intl=1 уходит для тягача с отмеченным чекбоксом`() {
        val map = state(vehicle = "tractor", intl = true).toParamMap()

        assertEquals("1", map["intl"])
    }

    @Test
    fun `intl не уходит, если чекбокс не отмечен`() {
        val map = state(vehicle = "tractor", intl = false).toParamMap()

        assertFalse(map.containsKey("intl"))
    }

    @Test
    fun `intl не уходит для других видов ТС, даже если флаг остался`() {
        listOf("car", "cargo", "bus").forEach { vehicle ->
            val map = state(vehicle = vehicle, intl = true).toParamMap()
            assertFalse("intl для $vehicle", map.containsKey("intl"))
        }
    }

    @Test
    fun `чекбокс intl показывается только для тягача`() {
        assertTrue(state(vehicle = "tractor").supportsIntl())
        assertFalse(state(vehicle = "car").supportsIntl())
        assertFalse(state(vehicle = "cargo").supportsIntl())
    }

    @Test
    fun `field hidden after engine change is not sent`() {
        // ввёл объём для бензина, переключил на электро: в форме электро поля объёма нет
        val st = state(engine = "e", values = mapOf("capacity" to "2000", "state" to "new"),
            params = listOf(POWER_FIELD, CarCalcParam("Состояние", "", "state")))
        val map = st.toParamMap()
        assertFalse(map.containsKey("capacity"))
        assertEquals("new", map["state"])
    }
}
