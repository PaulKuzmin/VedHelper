package com.alternadv.vedhelper.ui.screen.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alternadv.vedhelper.model.ContactsModel
import androidx.core.net.toUri

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ошибка: ${state.errorMessage}")
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item { CompanyHeader() }
                items(state.contacts) { contact ->
                    ContactItem(contact)
                }
            }
        }
    }
}

@Composable
private fun CompanyHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Транспортно-логистическая компания «Альтерна» - таможенный представитель с многолетним успешным опытом работы в сфере ВЭД.",
                style = MaterialTheme.typography.bodyLarge, // увеличили размер
                textAlign = TextAlign.Justify // выравнивание по ширине
            )
            Text(
                "Мы занимаемся таможенным оформлением и доставкой грузов любой категории из всех стран Азии (Китай, Корея, Япония и т.д.) в Россию.",
                style = MaterialTheme.typography.bodyLarge, // увеличили размер
                textAlign = TextAlign.Justify // выравнивание по ширине
            )
        }
    }
}

@Composable
private fun ContactItem(contact: ContactsModel) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                ("Офис: " + (contact.shortName ?: contact.name.orEmpty())),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            contact.address?.let { address ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Адрес",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        address,
                        modifier = Modifier.clickable {
                            val gmmIntentUri = "geo:0,0?q=${Uri.encode(address)}".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        color = MaterialTheme.colorScheme.primary // выделяем кликабельность цветом
                    )
                }
            }

            contact.contacts?.forEach { (type, values) ->
                if (values.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        type.startsWith("email") -> {
                            values.forEach { email ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Email,
                                        contentDescription = "Email",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        email,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = "mailto:$email".toUri()
                                            }
                                            context.startActivity(intent)
                                        },
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        type.startsWith("phon") -> {
                            values.forEach { phone ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = "Телефон",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        phone,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = "tel:$phone".toUri()
                                            }
                                            context.startActivity(intent)
                                        },
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(type)
                            values.forEach { Text(it) }
                        }
                    }
                }
            }
        }
    }
}