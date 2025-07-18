package com.alternadv.vedhelper.ui.screen.tnved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.alternadv.vedhelper.ui.navigation.BottomNavItem

@Composable
fun TnvedScreen(
    navController: NavController,
    viewModel: TnvedViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNodes()
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.nodes.isNotEmpty() -> {
                LazyColumn {
                    items(state.nodes) { node ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (node.hasChilds == true) {
                                        viewModel.loadNodes(node.idx ?: "0")
                                    } else {
                                        val code = if (!node.kodplus.isNullOrBlank()) {
                                            "${node.kod}_${node.kodplus}"
                                        } else {
                                            node.kod
                                        }

                                        navController.navigate(BottomNavItem.TnvedCode.route + "/$code")
                                    }
                                }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                val codeText = buildString {
                                    append(node.kod ?: "")
                                    if (!node.kodplus.isNullOrBlank()) {
                                        append(" ${node.kodplus}")
                                    }
                                    append(" ${node.name}")
                                }
                                Text(codeText.trim())
                            }
                        }
                    }
                }
            }

            else -> {
                Text(
                    text = "Нет данных",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}