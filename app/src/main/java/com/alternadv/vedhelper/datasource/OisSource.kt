package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.OisResultModel

object OisSource {
    private const val PATH = "widget/ois/list/"

    suspend fun get(text: String): OisResultModel? {
        // путь кодирует DataSource.buildUrl — сюда передаём сырой текст
        val url = DataSource.buildUrl(PATH + text, emptyMap())
        return DataSource.getRaw<OisResultModel>(url)
    }
}
