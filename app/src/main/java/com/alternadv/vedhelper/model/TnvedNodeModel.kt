package com.alternadv.vedhelper.model

import com.squareup.moshi.Json

data class TnvedNodesModel (
    val nodes: List<TnvedNodeModel>?
)

data class TnvedNodeModel(
    val kod: String?,
    val kodplus: String?,
    val name: String?,
    val longname: String?,
    val edi2: String?,
    val edi3: String?,
    val dsign: String?,

    @param:Json(name = "index_")
    val index: String?,

    val idx: String?,

    @param:Json(name = "parent_idx")
    val parentIdx: String?,

    @param:Json(name = "has_childs")
    val hasChilds: Boolean?
)
