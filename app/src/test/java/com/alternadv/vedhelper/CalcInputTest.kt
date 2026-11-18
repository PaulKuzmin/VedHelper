package com.alternadv.vedhelper

import com.alternadv.vedhelper.ui.screen.calc.CalcState
import com.alternadv.vedhelper.ui.screen.calc.withAddonInput
import com.alternadv.vedhelper.ui.screen.calc.withCostInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Поля «Стоимость» и доп. параметры калькулятора товаров (найдено на эмуляторе 18.11.2026):
 * стоимость «1234.56» показывалась как «1235», количество «100» — как «100.0»,
 * а очистить количество было нельзя (оставалось «0.0»).
 */
class CalcInputTest {

    private val empty = CalcState(cost = null, isLoading = false, errorMessage = null)

    private fun CalcState.typeCost(text: String): CalcState =
        text.indices.fold(this) { s, i -> s.withCostInput(text.substring(0, i + 1)) }

    @Test
    fun `стоимость с копейками вводится посимвольно и не округляется`() {
        val s = empty.typeCost("1234.56")
        assertEquals("1234.56", s.costInput)
        assertEquals(1234.56, s.cost!!, 0.0)
    }

    @Test
    fun `стоимость - запятая становится точкой, пустое поле даёт null`() {
        assertEquals("12.5", empty.withCostInput("12,5").costInput)
        val cleared = empty.withCostInput("100").withCostInput("")
        assertEquals("", cleared.costInput)
        assertNull(cleared.cost)
    }

    @Test
    fun `количество показывается как введено, без хвоста точка ноль`() {
        val s = empty.withAddonInput("qty", "1").withAddonInput("qty", "10").withAddonInput("qty", "100")
        assertEquals("100", s.addonInputs["qty"])
        assertEquals(100.0, s.chosenParams.addons!!["qty"]!!, 0.0)
    }

    @Test
    fun `количество можно стереть полностью`() {
        val s = empty.withAddonInput("qty", "100")
            .withAddonInput("qty", "10")
            .withAddonInput("qty", "1")
            .withAddonInput("qty", "")
        assertNull(s.addonInputs["qty"])
        assertNull(s.chosenParams.addons!!["qty"])
    }

    @Test
    fun `промежуточный ввод с точкой на конце сохраняется в поле`() {
        val s = empty.withAddonInput("qty", "2.")
        assertEquals("2.", s.addonInputs["qty"])
        assertEquals(2.0, s.chosenParams.addons!!["qty"]!!, 0.0)
    }
}
