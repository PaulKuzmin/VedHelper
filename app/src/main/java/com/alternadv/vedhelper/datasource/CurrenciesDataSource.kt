package com.alternadv.vedhelper.datasource

import com.alternadv.vedhelper.model.CurrencyListModel
import com.alternadv.vedhelper.utils.DataSourceException

object CurrenciesDataSource {
    private const val URL = "${DataSource.BASE_URL}/widget/currencies?json=1"

    suspend fun get(): CurrencyListModel? {
        val response = DataSource.get<CurrencyListModel>(URL)
        if (response?.success != true) {
            throw DataSourceException(
                message = "Ошибка загрузки данных валют"
            )
        }
        return response.data
    }
}