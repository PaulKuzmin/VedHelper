package com.alternadv.vedhelper

import com.alternadv.vedhelper.model.CarCalcChoice
import com.alternadv.vedhelper.model.CarCalcParam
import com.alternadv.vedhelper.ui.screen.carcalc.defaultState
import com.alternadv.vedhelper.ui.screen.carcalc.formatCalcErrors
import com.alternadv.vedhelper.ui.screen.carcalc.seedChoiceDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Дефолты полей-списков и текст ошибок.
 *
 * Здесь ловится класс ошибок «на экране одно, в запрос уходит другое»: раньше «Состояние»
 * показывало «бывшее в эксплуатации», а на сервер уходило пусто.
 */
class CarCalcDefaultsTest {

    private val stateParam = CarCalcParam(
        name = "Состояние", dimension = "", code = "state",
        choices = listOf(CarCalcChoice("used", "бывшее в эксплуатации"), CarCalcChoice("new", "новое"))
    )

    private val kindParam = CarCalcParam(
        name = "Вид", dimension = "", code = "kind",
        choices = listOf(CarCalcChoice("motor", "речное"), CarCalcChoice("motor_sea", "морское"))
    )

    private val capacityParam = CarCalcParam(name = "Объём", dimension = "куб.см.", code = "capacity")

    // ------------------------------------------------------------- state

    @Test
    fun `авто младше трёх лет считается новым`() {
        assertEquals("new", defaultState(2026, 2026))
        assertEquals("new", defaultState(2025, 2026))   // 1 год
        assertEquals("new", defaultState(2024, 2026))   // 2 года
    }

    @Test
    fun `три года - уже б-у (как на сервере)`() {
        // сервер: возраст < 3 лет -> коды новых; ровно 3 года -> бакет 3-5 лет
        assertEquals("used", defaultState(2023, 2026))
    }

    @Test
    fun `авто старше трёх лет считается б-у`() {
        assertEquals("used", defaultState(2022, 2026))
        assertEquals("used", defaultState(2010, 2026))
    }

    @Test
    fun `state подставляется из возраста когда поле не трогали`() {
        val result = seedChoiceDefaults(
            params = listOf(stateParam), current = emptyMap(), year = 2025, currentYear = 2026
        )

        assertEquals("new", result["state"])
    }

    @Test
    fun `старое авто получает used по умолчанию`() {
        val result = seedChoiceDefaults(
            params = listOf(stateParam), current = emptyMap(), year = 2018, currentYear = 2026
        )

        assertEquals("used", result["state"])
    }

    // -------------------------------------------------------------- kind

    @Test
    fun `вид подставляется первым из списка`() {
        val result = seedChoiceDefaults(
            params = listOf(kindParam), current = emptyMap(), year = 2022, currentYear = 2026
        )

        assertEquals("motor", result["kind"])
    }

    @Test
    fun `выбор пользователя не перезаписывается`() {
        val result = seedChoiceDefaults(
            params = listOf(kindParam, stateParam),
            current = mapOf("kind" to "motor_sea", "state" to "used"),
            year = 2025,           // возраст сказал бы "new"
            currentYear = 2026,
            touched = setOf("kind", "state")
        )

        assertEquals("выбор вида сохранён", "motor_sea", result["kind"])
        assertEquals("выбор состояния сохранён", "used", result["state"])
    }

    @Test
    fun `валидное значение из прошлого расчёта сохраняется`() {
        val result = seedChoiceDefaults(
            params = listOf(kindParam),
            current = mapOf("kind" to "motor_sea"),
            year = 2022, currentYear = 2026
            // touched не передан: значение всё равно валидно для этого вида ТС
        )

        assertEquals("motor_sea", result["kind"])
    }

    @Test
    fun `невалидное значение заменяется дефолтом`() {
        val result = seedChoiceDefaults(
            params = listOf(kindParam),
            current = mapOf("kind" to "sail"),   // парусник есть у катера, но не в этом списке
            year = 2022, currentYear = 2026
        )

        assertEquals("motor", result["kind"])
    }

    @Test
    fun `поля без choices не трогаются`() {
        val result = seedChoiceDefaults(
            params = listOf(capacityParam),
            current = mapOf("capacity" to "2000"),
            year = 2022, currentYear = 2026
        )

        assertEquals(mapOf("capacity" to "2000"), result)
        assertFalse(result.containsKey("state"))
    }

    // ------------------------------------------------------------ ошибки

    @Test
    fun `если блока физлица нет - заголовка физлица тоже нет`() {
        val text = formatCalcErrors(
            fMessages = emptyList(),
            uMessages = listOf("Укажите стоимость числом, в выбранной валюте")
        )

        assertFalse("пустого заголовка быть не должно", text.contains("физ.лицо"))
        assertTrue(text.startsWith("Ошибки при расчете на юр.лицо:"))
        assertTrue(text.contains("Укажите стоимость"))
    }

    @Test
    fun `оба блока объединяются через пустую строку`() {
        val text = formatCalcErrors(
            fMessages = listOf("ошибка Ф"),
            uMessages = listOf("ошибка Ю")
        )

        assertTrue(text.contains("физ.лицо"))
        assertTrue(text.contains("юр.лицо"))
        assertTrue("блоки разделены пустой строкой", text.contains("\r\n\r\n"))
    }

    @Test
    fun `без сообщений отдаётся понятный текст вместо пустоты`() {
        assertEquals("Неизвестная ошибка при расчете", formatCalcErrors(emptyList(), emptyList()))
    }

    @Test
    fun `несколько сообщений идут построчно`() {
        val text = formatCalcErrors(
            fMessages = emptyList(),
            uMessages = listOf("первое", "второе")
        )

        assertTrue(text.contains("\r\nпервое"))
        assertTrue(text.contains("\r\nвторое"))
    }
}
