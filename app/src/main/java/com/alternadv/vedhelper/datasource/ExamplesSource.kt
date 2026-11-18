package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.ExamplesModel
import com.alternadv.vedhelper.model.ResponseModel

object ExamplesSource {
    private const val PATH = "widget/calculator/examples/"

    suspend fun get(text: String): ResponseModel<ExamplesModel>? {
        // путь кодирует DataSource.buildUrl — сюда передаём сырой текст
        val url = DataSource.buildUrl(PATH + text, emptyMap())
        return DataSource.get<ExamplesModel>(url)
    }
}
