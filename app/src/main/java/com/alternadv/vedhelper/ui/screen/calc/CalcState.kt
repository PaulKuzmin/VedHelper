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