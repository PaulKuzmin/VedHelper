package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.OisResultModel
import java.net.URLEncoder

object OisSource {
    private const val PATH = "/widget/ois/list/"

    suspend fun get(text: String): OisResultModel? {
        val fullPath = "${PATH}${URLEncoder.encode(text, "UTF-8")}"
        val url = DataSource.buildUrl(fullPath, emptyMap())
        return DataSource.getRaw<OisResultModel>(url)
    }
}