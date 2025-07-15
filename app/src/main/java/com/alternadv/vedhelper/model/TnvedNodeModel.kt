package com.alternadv.vedhelper.model

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
    val index_: String?,
    val idx: String?,
    val parent_idx: String?,
    val has_childs: Boolean?
)
