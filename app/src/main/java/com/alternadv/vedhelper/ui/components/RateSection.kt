package com.alternadv.vedhelper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alternadv.vedhelper.model.CalcRateModel

@Composable
fun RateSection(title: String, rate: CalcRateModel) {
    if (rate.data.isNullOrEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = rate.name ?: title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            rate.data.forEach { row ->
                Column(modifier = Modifier.padding(bottom = 12.dp)) {

                    var rate = "-"
                    if (!row.rateString.isNullOrBlank()) {
                        rate = row.rateString
                    }

                    Text(
                        text = rate,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val description: String = if (row.description.isNullOrBlank()) {
                        "базовая ставка"
                    } else {
                        row.description
                    }

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
