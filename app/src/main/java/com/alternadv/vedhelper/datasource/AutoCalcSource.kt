package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.AutoCalcParamsModel
import com.alternadv.vedhelper.model.AutoCalcResultModel

object AutoCalcSource {
    private const val PATH = "widget/calcauto/"

    suspend fun getParams(vehicle: String, queryParams: Map<String, String> = emptyMap()): AutoCalcParamsModel? {
        val finalParams = if ("vehicle" in queryParams) {
            queryParams
        } else {
            queryParams + ("vehicle" to vehicle)
        }
        val url = DataSource.buildUrl("${PATH}params/$vehicle", finalParams)
        val response = DataSource.get<AutoCalcParamsModel>(url)
        if (response?.success != true) {
            throw Exception("Ошибка загрузки параметров для авто $vehicle")
        }
        return response.data
    }

    suspend fun getCalc(vehicle: String, queryParams: Map<String, String> = emptyMap()): AutoCalcResultModel? {
        val finalParams = if ("vehicle" in queryParams) {
            queryParams
        } else {
            queryParams + ("vehicle" to vehicle)
        }
        val url = DataSource.buildUrl("${PATH}result/$vehicle", finalParams)
        val response = DataSource.get<AutoCalcResultModel>(url)
        if (response?.success != true) {
            throw Exception("Ошибка расчёта для авто $vehicle")
        }
        return response.data
    }
}