package com.alternadv.vedhelper.utils

class DataSourceException(
    message: String,
    val code: Int? = null,
    val url: String? = null
) : Exception(message)