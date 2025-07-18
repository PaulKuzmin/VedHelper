package com.alternadv.vedhelper.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alternadv.vedhelper.model.CalcRateModel

@Composable
fun RateSection(title: String, rate: CalcRateModel) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        rate.data.orEmpty().forEach {
            it.rateString?.let { rateText ->
                Text("- $rateText")
            }
        }
    }
}