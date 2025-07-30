package com.alternadv.vedhelper.ui.screen.carcalcresult

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alternadv.vedhelper.model.CarCalcResultModel
import com.alternadv.vedhelper.model.CarCustomsPayment
import com.alternadv.vedhelper.model.CalcCurrencyRate
import com.alternadv.vedhelper.model.VehicleTypes
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

                // Курсы валют
                val currencies = data.calculation.currencies?.values?.toList().orEmpty()
                if (currencies.isNotEmpty()) {
                    item {
                        Text("Курсы валют", style = MaterialTheme.typography.titleMedium)
                    }
                    items(currencies) { rate ->
                        CurrencyRow(rate)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Дорого?\nОтправьте запрос и получите индивидуальный расчет!")
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

@Composable
private fun SegmentedButton(selected: Boolean, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )
    ) { Text(text) }
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

@Composable
private fun CurrencyRow(rate: CalcCurrencyRate) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(rate.name)
        Text(String.format(Locale.US, "%.4f", rate.value))
    }
}

private fun buildMessage(data: CarCalcResultModel): String {
    val engines = mapOf(
        "f" to "бензиновый", "d" to "дизельный",
        "h" to "гибридный", "e" to "электрический"
    )

    val chosen = data.chosen
    return buildString {
        append("Здравствуйте! Рассчитайте, пожалуйста:")
        append(" тип авто: ${VehicleTypes[chosen.vehicle]} ")
        append("; дата выпуска: ${chosen.month}.${chosen.year} ")
        append("; двигатель: ${engines[chosen.engine]} ")
        chosen.weight?.let { append("; полный вес, кг: $it ") }
        chosen.seats?.let { append("; количество мест: $it ") }
        chosen.bag?.let { append("; объем баг.отделения, куб.см.: $it ") }
        chosen.capacity?.let { append("; объем двигателя, куб.см.: $it ") }
        chosen.power?.let { append("; мощность, л.с.: $it ") }
        chosen.cost?.let { append("; стоимость, дол.США: $it ") }
    }
}
