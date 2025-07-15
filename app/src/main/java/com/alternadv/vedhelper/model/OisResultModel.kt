package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class OisResultModel(
    val ois_text: String?,
    val ois_list: List<OisModel>?,

    @param:Json(name = "ois_description")
    val oisDescription: Map<String, String>?
)

data class OisModel(
    val regnom: String?,

    @param:Json(name = "g31_12")
    val g31_12: String?,

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
