package com.alternadv.vedhelper.model

data class ResponseModel<T>(
    val success: Boolean,
    val data: T
)