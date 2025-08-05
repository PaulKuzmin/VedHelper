package com.alternadv.vedhelper.ui.screen.calcresult

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.alternadv.vedhelper.model.CalcMessage
import com.alternadv.vedhelper.model.CalcResultPaymentModel
import com.alternadv.vedhelper.ui.navigation.BottomNavItem
import com.alternadv.vedhelper.utils.PdfGenerator
import com.alternadv.vedhelper.utils.buildReportRows
import java.util.Locale

@Composable
fun CalcResultScreen(
    viewModel: CalcResultViewModel
) {
    val params by viewModel.calcParams.collectAsState()
    val result by viewModel.calcResult.collectAsState()

    var selectedCurrency by remember { mutableStateOf("rubles") }
    var rateHint by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (result == null) {
        Text("Нет данных для отображения", modifier = Modifier.padding(16.dp))
        return
    }

    val calc = result!!.calculation
    val chosen = result!!.chosen

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { selectedCurrency = "rubles" },
                    enabled = selectedCurrency != "rubles"
                ) {
                    Text("₽")
                }
                Button(
                    onClick = { selectedCurrency = "usd" },
                    enabled = selectedCurrency != "usd"
                ) {
                    Text("$")
                }
            }
        }

        if (calc?.success == true) {
            item {
                ResultSection(
                    payments = calc.payments,
                    total = if (selectedCurrency == "rubles") calc.paymentsSummaRub else calc.paymentsSummaUsd,
                    currency = selectedCurrency,
                    onRateClick = { name, rate -> rateHint = name to rate },
                    messages = calc.messages
                )
            }
        }

        val currencies = calc?.currencies?.values?.toList().orEmpty()
        if (currencies.isNotEmpty()) {
            item {
                Text("Курсы валют", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                currencies.forEach {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(it.name)
                        Text(it.value.toString())
                    }
                }
            }
        }

        item {
            val context = LocalContext.current

            Button(
                onClick = {

                    val (parametersData, resultsData) = buildReportRows(result)
                    val reportFile = PdfGenerator.generateCalcResultPdf(
                        context = context,
                        outputPath = "Результаты_расчета_таможенной_пошлины_Альтерна.pdf",
                        title = "Результаты расчета таможенной пошлины",
                        parameters = parametersData,
                        resultTitle = "При оформлении на юридическое лицо",
                        results = resultsData
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
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Поделиться расчетом")
            }
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

@Composable
fun ResultSection(
    payments: List<CalcResultPaymentModel>?,
    total: Double?,
    currency: String,
    onRateClick: (String, String) -> Unit,
    messages: List<CalcMessage>?
) {
    Card(modifier = Modifier.padding(vertical = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Итог", style = MaterialTheme.typography.titleMedium)
            payments.orEmpty().forEach {
                if (it.summaRub != null && it.summaRub > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRateClick(it.name ?: "", it.rate ?: "") }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(it.name ?: "")
                        Text(String.format(Locale.US, "%.2f", it.getAmount(currency)))
                    }
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
                Text(
                    String.format(Locale.US, "%.2f", total),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (!messages.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Примечание:", style = MaterialTheme.typography.titleSmall)
                messages.forEach {
                    Text(it.message)
                }
            }
        }
    }
}

private fun CalcResultPaymentModel.getAmount(currency: String): Double? =
    if (currency == "rubles") summaRub else summaUsd