package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.CalcParamsModel
import com.alternadv.vedhelper.model.CalcResultModel
import com.alternadv.vedhelper.model.StatsModel

object CalcSource {
    private const val PATH = "widget/calculator/"

    suspend fun getParams(code: String, queryParams: Map<String, String> = emptyMap()): CalcParamsModel? {
        val url = DataSource.buildUrl("${PATH}params/$code", queryParams)
        val response = DataSource.get<CalcParamsModel>(url)
        return response?.data
    }

    suspend fun getStats(code: String, queryParams: Map<String, String> = emptyMap()): StatsModel? {
        val url = DataSource.buildUrl("${PATH}statsprice/$code", queryParams)
        val response = DataSource.get<StatsModel>(url)
        return response?.data
    }

    suspend fun getCalc(code: String, queryParams: Map<String, String> = emptyMap()): CalcResultModel? {
        val url = DataSource.buildUrl("${PATH}result/$code", queryParams)
        val response = DataSource.get<CalcResultModel>(url)
        return response?.data
    }
}