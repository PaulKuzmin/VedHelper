package com.alternadv.vedhelper.utils

import com.alternadv.vedhelper.datasource.CurrenciesDataSource
import com.alternadv.vedhelper.model.CurrencyListModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.round

object CurrencyConverter {
    private var cache: CurrencyListModel? = null
    private var lastFetchTime: Long = 0
    private val cacheMutex = Mutex()
    private const val CACHE_TIMEOUT = 60 * 60 * 1000

    suspend fun convertToUsd(amount: Double, currencyCode: String): Double {
        if (currencyCode == "840") return amount

        val currencies = getCurrenciesCached() ?: return amount

        val usd = currencies.currencies.find { it.numCode == "840" }
            ?: throw IllegalArgumentException("USD курс не найден")

        val currency = currencies.currencies.find { it.numCode == currencyCode }
            ?: throw IllegalArgumentException("Курс валюты $currencyCode не найден")

        val nominal = currency.nominal.toDoubleOrNull() ?: 1.0
        val value = currency.value.toDoubleOrNull() ?: 1.0
        val usdNominal = usd.nominal.toDoubleOrNull() ?: 1.0
        val usdValue = usd.value.toDoubleOrNull() ?: 1.0

        val rate = (value / nominal) / (usdValue / usdNominal)
        return (round(amount * rate * 100) / 100.0)
    }

    private suspend fun getCurrenciesCached(): CurrencyListModel? {
        val now = System.currentTimeMillis()
        return cacheMutex.withLock {
            if (cache == null || now - lastFetchTime > CACHE_TIMEOUT) {
                cache = CurrenciesDataSource.get()
                lastFetchTime = now
            }
            cache
        }
    }
}