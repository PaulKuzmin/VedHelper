package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class CarCalcParamsModel(
    @param:Json(name = "calc_params")
    val calcParams: List<CarCalcParam>,

    @param:Json(name = "calc_engines")
    val calcEngines: List<CarCalcEngine>,

    @param:Json(name = "calc_engine")
    val calcEngine: String?
)

data class CarCalcParam(
    val name: String,
    val dimension: String,
    val code: String
)

data class CarCalcEngine(
    val id: String,
    val name: String
)