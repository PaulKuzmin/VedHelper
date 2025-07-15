package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class ExamplesModel(
    @param:Json(name = "searched_str")
    val searchedStr: String,
    val data: List<ExampleItem>
)

data class ExampleItem(
    val id: String,
    val code: String,
    val name: String,
    val rel: String
)