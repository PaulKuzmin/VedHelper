package com.alternadv.vedhelper.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.alternadv.vedhelper.R
import com.alternadv.vedhelper.ui.theme.Action
import com.alternadv.vedhelper.ui.theme.Ink
import com.alternadv.vedhelper.ui.theme.Muted

const val COMPANY_PHONE = "+7 (902) 050-40-50"
const val CALC_DISCLAIMER = "Расчёт ориентировочный. Не является публичной офертой."

// Контакты сервисов — те же, что на сайте alternadv.com/online.
const val GOODS_WHATSAPP_URL = "https://wa.me/79020519847"
const val GOODS_TELEGRAM_URL = "https://t.me/alternadv_goods"
const val AUTO_WHATSAPP_URL = "https://wa.me/79307371101"
const val AUTO_TELEGRAM_URL = "https://t.me/aksenovJCK"

/** Три контакта в одном стиле: звонок, WhatsApp, Telegram. */
@Composable
fun ContactActionTiles(
    modifier: Modifier = Modifier,
    phone: String = COMPANY_PHONE,
    whatsAppUrl: String = GOODS_WHATSAPP_URL,
    telegramUrl: String = GOODS_TELEGRAM_URL
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ContactTile(
            label = "Позвонить",
            icon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(22.dp)) },
            modifier = Modifier.weight(1f)
        ) {
            context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${phone.filter { it.isDigit() || it == '+' }}".toUri()))
        }
        ContactTile(
            label = "WhatsApp",
            icon = { Icon(painterResource(R.drawable.whatsapp_outline_icon), contentDescription = null, modifier = Modifier.size(22.dp)) },
            modifier = Modifier.weight(1f)
        ) {
            context.startActivity(Intent(Intent.ACTION_VIEW, whatsAppUrl.toUri()))
        }
        ContactTile(
            label = "Telegram",
            icon = { Icon(painterResource(R.drawable.telegram_outline_icon), contentDescription = null, modifier = Modifier.size(22.dp)) },
            modifier = Modifier.weight(1f)
        ) {
            context.startActivity(Intent(Intent.ACTION_VIEW, telegramUrl.toUri()))
        }
    }
}

@Composable
private fun ContactTile(
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/** Зелёная кнопка продающего действия на результате. */
@Composable
fun OrderCtaButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Action, contentColor = Ink),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CalcDisclaimer(modifier: Modifier = Modifier) {
    Text(
        text = CALC_DISCLAIMER,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        color = Muted,
        textAlign = TextAlign.Start
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactOrderSheet(
    onDismiss: () -> Unit,
    serviceLabel: String = "Товары и доставка",
    phone: String = COMPANY_PHONE,
    whatsAppUrl: String = GOODS_WHATSAPP_URL,
    telegramUrl: String = GOODS_TELEGRAM_URL
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Text("Связаться с брокером", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(serviceLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(
                "Подготовим расчёт и ответим на вопросы по вашей ситуации.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )
            Spacer(Modifier.height(16.dp))
            ContactActionTiles(phone = phone, whatsAppUrl = whatsAppUrl, telegramUrl = telegramUrl)
        }
    }
}
