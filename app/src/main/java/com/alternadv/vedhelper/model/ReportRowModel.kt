package com.alternadv.vedhelper.model

data class ReportRowModel(
    val first: String,
    val second: String,
    val third: String = "",
    val fourth: String = "",
    val bold: Boolean = false
)

data class CarReportRows(
    val parameters: List<ReportRowModel>,
    val resultsF: List<ReportRowModel>,
    val resultsU: List<ReportRowModel>
)