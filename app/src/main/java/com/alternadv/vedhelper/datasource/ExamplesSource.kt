package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.ExamplesModel
import com.alternadv.vedhelper.model.ResponseModel
import java.net.URLEncoder

object ExamplesSource {
    private const val PATH = "widget/calculator/examples/"

    suspend fun get(text: String): ResponseModel<ExamplesModel>? {
        val fullPath = "$PATH${URLEncoder.encode(text, "UTF-8")}"
        val url = DataSource.buildUrl(fullPath, emptyMap())
        return DataSource.get<ExamplesModel>(url)
    }
}