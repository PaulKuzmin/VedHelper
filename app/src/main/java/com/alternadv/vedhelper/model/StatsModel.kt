package com.alternadv.vedhelper.model

data class StatsModel(
    val statsprice: StatsPrice?
)

data class StatsPrice(
    val minimum: String,
    val average: String,
    val maximum: String
)