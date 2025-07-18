package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class OisResultModel(
    @param:Json(name = "ois_text")
    val oisText: String?,

    @param:Json(name = "ois_list")
    val oisList: List<OisModel>?,

    @param:Json(name = "ois_description")
    val oisDescription: Map<String, String>?
)

data class OisModel(
    val regnom: String?,

    @param:Json(name = "g31_12")
    val g3112: String?,

    val note: String?,
    val document: String?,
    val name: String?,
    val namet: String?,
    val agent: String?,
    val mktu: String?,
    val dateend: String?,
    val comm: String?,
    val letter: String?,

    @param:Json(name = "g33")
    val g33: String?,

    val image: String?
)
