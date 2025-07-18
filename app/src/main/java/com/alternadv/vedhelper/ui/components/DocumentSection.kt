package com.alternadv.vedhelper.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alternadv.vedhelper.model.CalcDocumentModel

@Composable
fun DocumentSection(document: CalcDocumentModel) {
    Column(Modifier.padding(vertical = 4.dp)) {
        document.name?.let {
            Text(it, style = MaterialTheme.typography.titleSmall)
        }
        document.data.orEmpty().forEach { doc ->
            val desc = listOfNotNull(doc.document, doc.description, doc.authority).joinToString(" – ")
            if (desc.isNotBlank()) Text("- $desc", style = MaterialTheme.typography.bodySmall)
        }
    }
}