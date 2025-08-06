package com.alternadv.vedhelper.ui.screen.carcalcresult

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alternadv.vedhelper.model.CarCalcResultModel
import com.alternadv.vedhelper.model.CarCustomsPayment
import com.alternadv.vedhelper.ui.components.SegmentedButton
import java.util.Locale

@Composable
fun CarCalcResultScreen(
    viewModel: CarCalcResultViewModel
) {
    val result by viewModel.carCalcResult.collectAsState()
    var calcCurrs by remember { mutableStateOf("rubles") }

    result?.let { data ->
        Column(Modifier.fillMaxSize().padding(16.dp)) {
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
                                title = "Физ.лицо, итого",
                                payments = part.payments ?: emptyList(),
                                totalRub = part.paymentsSumRub,
                                totalUsd = part.paymentsSumUsd,
                                calcCurrs = calcCurrs
                            )
                        }
                    }
                }

                // Юр. лицо
                data.calculation.u?.let { part ->
                    if (part.success == true) {
                        item {
                            PaymentsCard(
                                title = "Юр.лицо, итого",
                                payments = part.payments ?: emptyList(),
                                totalRub = part.paymentsSumRub,
                                totalUsd = part.paymentsSumUsd,
                                calcCurrs = calcCurrs
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
                        Text("Курсы валют:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
                        currencies.forEach {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(it.name)
                                Text(String.format(Locale.US, "%.4f", it.value))
                            }
                        }
                    }
                }
            }
/*
            Button(
                onClick = {
                    //onRequestClick(buildMessage(data))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Отправить запрос")
            }
 */
        }
    } ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
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
    calcCurrs: String
) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                val total = if (calcCurrs == "rubles") totalRub else totalUsd
                Text(String.format(Locale.US, "%.2f", total ?: 0.0))
            }
            Spacer(modifier = Modifier.height(8.dp))
            payments.forEach { p ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(p.name)
                    val value = if (calcCurrs == "rubles") p.sumRub else p.sumUsd
                    Text(String.format(Locale.US, "%.2f", value))
                }
            }
        }
    }
}
