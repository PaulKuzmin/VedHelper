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
import com.alternadv.vedhelper.model.CarPartCalculation
import com.alternadv.vedhelper.ui.components.CalcDisclaimer
import com.alternadv.vedhelper.ui.components.ContactOrderSheet
import com.alternadv.vedhelper.ui.components.OrderCtaButton
import com.alternadv.vedhelper.ui.components.SegmentedButton
import com.alternadv.vedhelper.utils.PdfGenerator
import com.alternadv.vedhelper.utils.REFERENCE_FOOTNOTE
import com.alternadv.vedhelper.utils.REFERENCE_NOTE
import com.alternadv.vedhelper.utils.buildCarReportRows
import com.alternadv.vedhelper.utils.formatSum
import com.alternadv.vedhelper.utils.hasCurAmounts
import com.alternadv.vedhelper.utils.needsReferenceFootnote
import com.alternadv.vedhelper.utils.pickAmount
import com.alternadv.vedhelper.utils.referencePaymentName
import com.alternadv.vedhelper.utils.referencePayments
import com.alternadv.vedhelper.utils.regularPayments
import java.util.Locale

@Composable
fun CarCalcResultScreen(
    viewModel: CarCalcResultViewModel
) {
    val result by viewModel.carCalcResult.collectAsState()
    val params by viewModel.carCalcParams.collectAsState()

    var calcCurrs by remember { mutableStateOf("rubles") }
    var rateHint by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showOrderSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    result?.let { data ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Переключатель валют. Третий вариант — валюта, в которой введена стоимость:
            // сервер возвращает суммы в ней (summa_cur), приложение ничего не пересчитывает.
            val currencyShort = data.calculation.currency?.short
            val hasCurAmounts = data.calculation.hasCurAmounts()

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
                if (hasCurAmounts) {
                    Spacer(modifier = Modifier.width(8.dp))
                    SegmentedButton(
                        selected = calcCurrs == "currency",
                        text = "В $currencyShort",
                        onClick = { calcCurrs = "currency" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // FAB на результатах скрыт; снизу нужен обычный воздух.
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Физ. лицо: расчёт, либо причина отказа (F_denied), либо сообщения сервера
                val fPart = data.calculation.f
                val fDenied = data.calculation.fDenied?.takeIf { it.isNotBlank() }
                when {
                    fPart?.success == true -> item {
                        PaymentsCard(
                            title = "При оформлении на физ.лицо",
                            part = fPart,
                            calcCurrs = calcCurrs,
                            onRateClick = { name, rate -> rateHint = name to rate }
                        )
                    }
                    fDenied != null -> item {
                        NoteCard(title = "При оформлении на физ.лицо", lines = listOf(fDenied))
                    }
                    fPart != null -> item {
                        NoteCard(title = "При оформлении на физ.лицо", lines = partMessages(fPart))
                    }
                }

                // Юр. лицо: показываем, даже если физлицо не посчиталось
                data.calculation.u?.let { part ->
                    item {
                        if (part.success == true) {
                            PaymentsCard(
                                title = "При оформлении на юр.лицо",
                                part = part,
                                calcCurrs = calcCurrs,
                                onRateClick = { name, rate -> rateHint = name to rate }
                            )
                        } else {
                            NoteCard(title = "При оформлении на юр.лицо", lines = partMessages(part))
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
            Spacer(Modifier.height(8.dp))
            OrderCtaButton(text = "Заказать автомобиль", onClick = { showOrderSheet = true })
            CalcDisclaimer(Modifier.padding(top = 8.dp, bottom = 4.dp))
            OutlinedButton(
                onClick = {
                    val messages = collectCarMessages(data)
                    val comments = messages.joinToString(separator = "\n") { "* $it" }

                    val reportData = buildCarReportRows(data, params)
                    val reportFile = PdfGenerator.generateCalcResultPdf(
                        context = context,
                        outputPath = "Результаты_расчета_таможенной_пошлины_тс_Альтерна.pdf",
                        title = "Результаты расчета таможенной пошлины на ТС",
                        parameters = reportData.parameters,
                        resultTitle = sectionTitle("При оформлении на физическое лицо", reportData.resultsF, reportData.fNote),
                        results = reportData.resultsF,
                        result2Title = sectionTitle("При оформлении на юридическое лицо", reportData.resultsU, reportData.uNote),
                        results2 = reportData.resultsU,
                        comments = comments,
                        amountHeader = reportData.amountHeader
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

    if (showOrderSheet) {
        ContactOrderSheet(
            onDismiss = { showOrderSheet = false },
            serviceLabel = "Авто",
            whatsAppUrl = com.alternadv.vedhelper.ui.components.AUTO_WHATSAPP_URL,
            telegramUrl = com.alternadv.vedhelper.ui.components.AUTO_TELEGRAM_URL
        )
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

/** Заголовок раздела PDF: с таблицей — как есть, без неё — с пояснением, без раздела — пусто. */
private fun sectionTitle(title: String, rows: List<*>, note: String?): String = when {
    rows.isNotEmpty() -> title
    note != null -> "$title: $note"
    else -> ""
}

/**
 * Примечания: сноска о перепродаже — только при справочном утильсборе (has_reference);
 * сообщения не посчитанного блока уже в его карточке и здесь не дублируются.
 */
private fun collectCarMessages(data: CarCalcResultModel): List<String> {
    val result = mutableListOf<String>()
    if (data.calculation.needsReferenceFootnote()) result += REFERENCE_FOOTNOTE
    listOfNotNull(data.calculation.f, data.calculation.u)
        .filter { it.success == true }
        .forEach { part -> result += part.messages.orEmpty().map { it.message } }
    return result
}

private fun partMessages(part: CarPartCalculation): List<String> =
    part.messages.orEmpty().map { it.message }.ifEmpty { listOf("Расчёт не выполнен") }

@Composable
private fun NoteCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun PaymentsCard(
    title: String,
    part: CarPartCalculation,
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

            part.regularPayments().forEach { p ->
                PaymentRow(
                    name = p.name,
                    value = pickAmount(calcCurrs, p.sumRub, p.sumUsd, p.sumCur),
                    onClick = { if (p.name.isNotBlank() && p.rate.isNotBlank()) onRateClick(p.name, p.rate) }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "Итого к уплате",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatSum(pickAmount(calcCurrs, part.paymentsSumRub, part.paymentsSumUsd, part.paymentsSumCur)),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Справочно: коммерческий утильсбор (при перепродаже), в итог не входит
            val references = part.referencePayments()
            if (references.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                Text(
                    "Справочно",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                references.forEach { p ->
                    val name = referencePaymentName(p.name)
                    PaymentRow(
                        name = name,
                        subtitle = REFERENCE_NOTE,
                        value = pickAmount(calcCurrs, p.sumRub, p.sumUsd, p.sumCur),
                        muted = true,
                        onClick = { if (p.rate.isNotBlank()) onRateClick(name, p.rate) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(
    name: String,
    value: Double?,
    onClick: () -> Unit,
    subtitle: String? = null,
    muted: Boolean = false
) {
    val color = if (muted) Color.Gray else Color.Unspecified
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = color, modifier = Modifier.weight(1f, fill = false))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Подробнее о ставке",
                    tint = Color.Gray,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp)
                )
            }
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Text(formatSum(value), color = color, modifier = Modifier.padding(start = 8.dp))
    }
}