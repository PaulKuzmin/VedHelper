package com.alternadv.vedhelper.ui.screen.carcalcresult

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.alternadv.vedhelper.model.CarCalcResultModel
import com.alternadv.vedhelper.model.CarCustomsPayment
import com.alternadv.vedhelper.ui.components.SegmentedButton
import com.alternadv.vedhelper.utils.PdfGenerator
import com.alternadv.vedhelper.utils.buildCarReportRows
import java.util.Locale

@Composable
fun CarCalcResultScreen(
    viewModel: CarCalcResultViewModel
) {
    val result by viewModel.carCalcResult.collectAsState()
    val params by viewModel.carCalcParams.collectAsState()

    var calcCurrs by remember { mutableStateOf("rubles") }
    var rateHint by remember { mutableStateOf<Pair<String, String>?>(null) }

    val context = LocalContext.current

    result?.let { data ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Переключатель валют
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = calcCurrs == "rubles",
                    text = "В рублях",
                    onClick = { calcCurrs = "rubles" }
                )
                Spacer(modifier = Modifier.width(8.dp))
                SegmentedButton(
                    selected = calcCurrs == "dollars",
                    text = "В дол.США",
                    onClick = { calcCurrs = "dollars" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                // Физ. лицо
                data.calculation.f?.let { part ->
                    if (part.success == true) {
                        item {
                            PaymentsCard(
                                title = "При оформлении на физ.лицо",
                                payments = part.payments ?: emptyList(),
                                totalRub = part.paymentsSumRub,
                                totalUsd = part.paymentsSumUsd,
                                calcCurrs = calcCurrs,
                                onRateClick = { name, rate -> rateHint = name to rate }
                            )
                        }
                    }
                }

                // Юр. лицо
                data.calculation.u?.let { part ->
                    if (part.success == true) {
                        item {
                            PaymentsCard(
                                title = "При оформлении на юр.лицо",
                                payments = part.payments ?: emptyList(),
                                totalRub = part.paymentsSumRub,
                                totalUsd = part.paymentsSumUsd,
                                calcCurrs = calcCurrs,
                                onRateClick = { name, rate -> rateHint = name to rate }
                            )
                        }
                    }
                }

                // Примечание
                val messages = collectCarMessages(data)
                if (messages.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Примечание:", style = MaterialTheme.typography.titleSmall)
                        messages.forEach {
                            Text(" * $it")
                        }
                    }
                }

                // Курсы валют
                val currencies = data.calculation.currencies?.values?.toList().orEmpty()
                if (currencies.isNotEmpty()) {
                    item {
                        Text(
                            "Курсы валют:",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        currencies.forEach {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(it.name)
                                Text(String.format(Locale.US, "%.4f", it.value))
                            }
                        }
                    }
                }
            }
            Button(
                onClick = {
                    val messages = collectCarMessages(data)
                    val comments = messages.joinToString(separator = "\n") { "* $it" }

                    var reportData = buildCarReportRows(data, params)
                    val reportFile = PdfGenerator.generateCalcResultPdf(
                        context = context,
                        outputPath = "Результаты_расчета_таможенной_пошлины_тс_Альтерна.pdf",
                        title = "Результаты расчета таможенной пошлины на ТС",
                        parameters = reportData.parameters,
                        resultTitle = "При оформлении на физическое лицо",
                        results = reportData.resultsF,
                        result2Title = "При оформлении на юридическое лицо",
                        results2 = reportData.resultsU,
                        comments = comments
                    )
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        reportFile
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Поделиться расчетом"))

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Поделиться расчетом")
            }
        }
    } ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    rateHint?.let { (title, rate) ->
        AlertDialog(
            onDismissRequest = { rateHint = null },
            confirmButton = {
                TextButton(onClick = { rateHint = null }) {
                    Text("OK")
                }
            },
            title = { Text(title) },
            text = { Text("Ставка: $rate") }
        )
    }
}

private fun collectCarMessages(data: CarCalcResultModel): List<String> {
    val baseMessage = "Уплачивается в случае ввоза импортного автомобиля " +
            "с целью его дальнейшей перепродажи на территории РФ " +
            "в течение одного года с даты получения ПТС."

    val fMessages = data.calculation.f?.messages?.map { it.message } ?: emptyList()
    val uMessages = data.calculation.u?.messages?.map { it.message } ?: emptyList()

    return listOf(baseMessage) + fMessages + uMessages
}

@Composable
private fun PaymentsCard(
    title: String,
    payments: List<CarCustomsPayment>,
    totalRub: Double?,
    totalUsd: Double?,
    calcCurrs: String,
    onRateClick: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title)
                Text("")
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            payments.forEach { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (p.name.isNotBlank() && p.rate.isNotBlank()) {
                                onRateClick(p.name, p.rate)
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(p.name)
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Подробнее о ставке",
                            tint = Color.Gray,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(16.dp)
                        )
                    }
                    val value = if (calcCurrs == "rubles") p.sumRub else p.sumUsd
                    Text(String.format(Locale.US, "%.2f", value))
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Итого:")
                val total = if (calcCurrs == "rubles") totalRub else totalUsd
                Text(
                    String.format(Locale.US, "%.2f", total),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
