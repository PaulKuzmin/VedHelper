package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.CarCalcParamsModel
import com.alternadv.vedhelper.model.CarCalcResultModel
import com.alternadv.vedhelper.model.CarCalcVehicle
import com.alternadv.vedhelper.model.CarCalcVehiclesModel
import com.alternadv.vedhelper.model.VehicleTypes

object CarCalcSource {
    private const val PATH = "widget/calcauto/"

    /**
     * Список видов ТС берётся с сервера, чтобы новый вид не требовал обновления приложения.
     * Если сервер недоступен — отдаём зашитый список как аварийный вариант.
     */
    suspend fun getVehicles(): List<CarCalcVehicle> {
        val url = DataSource.buildUrl("${PATH}vehicles", emptyMap())
        return try {
            val response = DataSource.get<CarCalcVehiclesModel>(url)
            response?.data?.vehicles
                ?.takeIf { it.isNotEmpty() }
                ?: fallbackVehicles()
        } catch (e: Exception) {
            fallbackVehicles()
        }
    }

    private fun fallbackVehicles(): List<CarCalcVehicle> =
        VehicleTypes.map { (id, name) -> CarCalcVehicle(id = id, name = name) }

    suspend fun getParams(vehicle: String, queryParams: Map<String, String> = emptyMap()): CarCalcParamsModel? {
        val finalParams = if ("vehicle" in queryParams) {
            queryParams
        } else {
            queryParams + ("vehicle" to vehicle)
        }
        val url = DataSource.buildUrl("${PATH}params/$vehicle", finalParams)
        val response = DataSource.get<CarCalcParamsModel>(url)
        if (response?.success != true) {
            throw Exception("Ошибка загрузки параметров для авто $vehicle")
        }
        return response.data
    }

    suspend fun getCalc(vehicle: String, queryParams: Map<String, String> = emptyMap()): CarCalcResultModel? {
        val finalParams = if ("vehicle" in queryParams) {
            queryParams
        } else {
            queryParams + ("vehicle" to vehicle)
        }
        val url = DataSource.buildUrl("${PATH}result/$vehicle", finalParams)
        val response = DataSource.get<CarCalcResultModel>(url)
            ?: throw Exception("Ошибка расчёта для авто $vehicle")
        // При success=false не подменяем ответ общим текстом: если в data есть calculation
        // с messages, ViewModel покажет текст сервера (calcErrorText). Пустой data → null.
        return response.data
    }
}
