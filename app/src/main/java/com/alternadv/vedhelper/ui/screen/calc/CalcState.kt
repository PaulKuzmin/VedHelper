package com.alternadv.vedhelper.ui.screen.calc

import com.alternadv.vedhelper.model.CalcParam
import com.alternadv.vedhelper.model.CalcSpecialParam
import com.alternadv.vedhelper.model.Chosen
import com.alternadv.vedhelper.model.CountryModel
import com.alternadv.vedhelper.model.StatsPrice

data class CalcState(
    val searchTerm: String = "",
    val isShowHint: Boolean = true,
    val isShowCalc: Boolean = false,
    val chosenParams: Chosen = Chosen(
        code = "",
        direction = "I",
        country = "000",
        paramCost = null,
        json = true,
        specials = emptyMap(),
        addons = emptyMap()
    ),
    val availableCountries: List<CountryModel> = emptyList(),
    val params: CalcMeta? = null,
    var cost: Double?,
    /** Текст поля «Стоимость» как введён (дробные, пустое) — cost считается из него. */
    val costInput: String = "",
    /** Тексты доп. полей (количество и т.п.); в addons уходят числа. */
    val addonInputs: Map<String, String> = emptyMap(),
    var currency: String = "840",
    val calcParams: List<CalcParam> = emptyList(),
    val specialParams: List<CalcSpecialParam> = emptyList(),
    val statsPrice: StatsPrice? = null,
    val isLoading: Boolean,
    val isCalculating: Boolean = false,
    var errorMessage: String?
)

data class CalcMeta(
    val name: String
)
/**
 * Ввод стоимости. Раньше поле показывало `cost` через "%.0f": дробную часть ввести было нельзя
 * («1234.56» превращалось в «1235»). Теперь текст хранится как введён, число — отдельно.
 */
fun CalcState.withCostInput(input: String): CalcState {
    val text = com.alternadv.vedhelper.ui.screen.carcalc.normalizeNumber(input)
    return copy(costInput = text, cost = text.toDoubleOrNull())
}

/**
 * Ввод доп. поля (количество и т.п.). Раньше поле показывало Double.toString(): «100» → «100.0»,
 * а стереть значение было нельзя — «100.» снова становилось «100.0», пустое поле — «0.0».
 */
fun CalcState.withAddonInput(code: String, input: String): CalcState {
    val text = com.alternadv.vedhelper.ui.screen.carcalc.normalizeNumber(input)
    val inputs = addonInputs.toMutableMap()
    val addons = chosenParams.addons?.toMutableMap() ?: mutableMapOf()
    val number = text.toDoubleOrNull()
    if (text.isEmpty()) inputs.remove(code) else inputs[code] = text
    if (number != null) addons[code] = number else addons.remove(code)
    return copy(addonInputs = inputs, chosenParams = chosenParams.copy(addons = addons))
}
