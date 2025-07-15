package com.alternadv.vedhelper.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrencyListModel(
    val dated: String,
    val currencies: List<CurrencyModel>
)

@JsonClass(generateAdapter = true)
data class CurrencyModel(
    val id: Int,

    @param:Json(name = "Dated")
    val dated: String,

    @param:Json(name = "NumCode")
    val numCode: String,

    @param:Json(name = "Nominal")
    val nominal: String,

    @param:Json(name = "Name")
    val name: String,

    @param:Json(name = "Value")
    val value: String,

    val loaded: String
)