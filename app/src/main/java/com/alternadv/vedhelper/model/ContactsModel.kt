package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class ContactsRawModel(
    val success: Boolean?,
    val data: List<ContactsModel>?
)

data class ContactsModel(
    val id: Int?,
    val name: String?,

    @param:Json(name = "short_name")
    val shortName: String?,

    val address: String?,
    val map: String?,
    val even: Boolean?,

    val contacts: Map<String, List<String>>?,
)