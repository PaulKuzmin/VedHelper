package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.TnvedCodeModel
import com.alternadv.vedhelper.model.TnvedNodeModel
import com.alternadv.vedhelper.model.TnvedNodesModel

object TnvedSource {
    private const val PATH = "widget/tnved/"

    suspend fun getNodes(id: Number): List<TnvedNodeModel>? {
        val fullPath = "${PATH}node/${id}"
        val url = DataSource.buildUrl(fullPath, emptyMap())
        val t = DataSource.getRaw<TnvedNodesModel>(url)
        return t?.nodes
    }

    suspend fun getCode(code: String): TnvedCodeModel? {
        val fullPath = "${PATH}code/${code}"
        val url = DataSource.buildUrl(fullPath, emptyMap())
        return DataSource.getRaw<TnvedCodeModel>(url)
    }
}