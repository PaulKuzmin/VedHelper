package com.alternadv.vedhelper.ui.screen.tnvedcode

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alternadv.vedhelper.ui.components.DocumentSection
import com.alternadv.vedhelper.ui.components.RateSection
import com.alternadv.vedhelper.ui.navigation.BottomNavItem

@Composable
fun TnvedCodeScreen(
    code: String,
    navController: NavController,
    viewModel: TnvedCodeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(code) {
        viewModel.loadCode(code)
    }

    Scaffold(
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Button(
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    onClick = {
                        val searchCode = if (code.indexOf('_') > 8) {
                            code.substring(0, code.indexOf('_'))
                        } else {
                            code
                        }
                        navController.navigate(BottomNavItem.Examples.route + "/$searchCode")
                    }
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Примеры")
                }

                Button(
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    onClick = {
                        navController.navigate(BottomNavItem.Calc.route + "/${code}")
                    }
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Рассчитать")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.codeData != null) {
                val data = state.codeData
                Text(text = "${data?.code}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(text = "${data?.name}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))

                data?.data?.importTax?.let { RateSection("Ввозная пошлина", it) }
                data?.data?.exportTax?.let { RateSection("Вывозная пошлина", it) }
                data?.data?.vat?.let { RateSection("НДС", it) }
                data?.data?.excise?.let { RateSection("Акциз", it) }
                data?.data?.special?.let { RateSection("Особая пошлина", it) }

                if (data?.data?.documents != null) {
                    Spacer(Modifier.height(16.dp))
                    Text("Документы и особенности", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    data.data.documents.restrictions?.let { DocumentSection(it) }
                    data.data.documents.license?.let { DocumentSection(it) }
                    data.data.documents.certificates?.let { DocumentSection(it) }
                    data.data.documents.others?.let { DocumentSection(it) }
                }

            } else if (state.errorMessage != null) {
                Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
