package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class AutoCalcParamsModel(
    @param:Json(name = "calc_params")
    val calcParams: List<AutoCalcParam>,

    @param:Json(name = "calc_engines")
    val calcEngines: List<AutoCalcEngine>,

    @param:Json(name = "calc_engine")
    val calcEngine: AutoCalcEngine?
)

data class AutoCalcParam(
    val name: String,
    val dimension: String,
    val code: String
)

data class AutoCalcEngine(
    val id: String,
    val name: String
)