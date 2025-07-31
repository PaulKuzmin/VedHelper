package com.alternadv.vedhelper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.alternadv.vedhelper.model.CalcDocumentModel

@Composable
fun DocumentSection(document: CalcDocumentModel) {
    if (document.data.isNullOrEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "mainArrow")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            // Заголовок всего блока
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = document.name ?: "Документы",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 8.dp)) {
                    // Группируем по direction
                    val grouped = document.data.groupBy { it.direction ?: "" }
                    val order = listOf("I", "E", "")
                    order.forEach { key ->
                        val rows = grouped[key] ?: return@forEach
                        val directionTitle = when (key) {
                            "I" -> "Импорт"
                            "E" -> "Экспорт"
                            else -> "Общее"
                        }
                        ExpandableDirectionSection(directionTitle, rows.map { row ->
                            val authority = if ((!row.authority.isNullOrBlank() && row.authority != "false")
                                || (!row.authorityLicense.isNullOrBlank() && row.authorityLicense != "false")
                            ) {
                                listOfNotNull(row.authority, row.authorityLicense)
                                    .filter { it.isNotBlank() && it != "false" }
                                    .joinToString(" / ")
                                    .let { " // Орган: $it" }
                            } else ""

                            val orderStr = if (!row.order.isNullOrBlank() && row.order != "false") {
                                " // Основание: ${row.order}"
                            } else ""

                            buildString {
                                if (!row.document.isNullOrBlank() && row.document != "false") append(row.document)
                                if (!row.description.isNullOrBlank()) append(" ${row.description}")
                                append(authority)
                                append(orderStr)
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableDirectionSection(title: String, items: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "directionArrow")

    Column(Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(start = 8.dp, top = 4.dp)) {
                items.forEach { desc ->
                    Text("• $desc", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}