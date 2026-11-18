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
    val resultsU: List<ReportRowModel>,
    /** Вместо таблицы физлица: F_denied или сообщения сервера, если F не посчитан. */
    val fNote: String? = null,
    /** То же для юрлица. */
    val uNote: String? = null,
    /** Заголовок второй денежной колонки (fourth): «Сумма, $» или валюта стоимости, «Сумма, EUR». */
    val amountHeader: String = "Сумма, $"
)