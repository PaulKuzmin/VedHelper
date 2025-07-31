package com.alternadv.vedhelper.ui.screen.tnved

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alternadv.vedhelper.model.TnvedNodeModel
import com.alternadv.vedhelper.ui.navigation.BottomNavItem

@Composable
fun TnvedScreen(
    navController: NavController,
    viewModel: TnvedViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    /*
    LaunchedEffect(Unit) {
        viewModel.loadNodes()
    }
     */

    LaunchedEffect(Unit) {
        viewModel.initIfNeeded()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 3.dp)
    ) {
        if (state.path.isNotEmpty() && state.path.count() > 1) {
            BreadcrumbsVerticalCard(
                path = state.path,
                onClick = { index -> viewModel.navigateToPath(index) }
            )
            Spacer(Modifier.height(4.dp))
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.nodes.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет данных", style = MaterialTheme.typography.bodyMedium)
            }

            else -> LazyColumn(contentPadding = PaddingValues(bottom = 8.dp)) {
                items(state.nodes) { node ->
                    NodeCard(node) {
                        if (node.hasChilds == true) {
                            val displayName = buildString {
                                append(node.kod ?: "")
                                if (!node.kodplus.isNullOrBlank()) append(" ${node.kodplus}")
                                append(" ${node.name}")
                            }.trim()
                            viewModel.loadNodes(node.idx ?: "0", displayName)
                        } else {
                            val code = if (!node.kodplus.isNullOrBlank()) {
                                "${node.kod}_${node.kodplus}"
                            } else node.kod
                            navController.navigate(BottomNavItem.TnvedCode.route + "/$code")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeCard(node: TnvedNodeModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            val codeText = buildString {
                append(node.kod ?: "")
                if (!node.kodplus.isNullOrBlank()) append(" ${node.kodplus}")
                append(" ${node.name}")
            }
            Text(codeText.trim())
        }
    }
}

@Composable
private fun BreadcrumbsVerticalCard(
    path: List<TnvedPathItem>,
    onClick: (Int) -> Unit
) {
    if (path.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Дерево", // (${path.size})
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    path.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClick(index) }
                                .padding(vertical = 4.dp)
                        ) {
                            if (index == 0) {
                                Icon(
                                    imageVector = Icons.Default.FormatListNumbered,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SubdirectoryArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (index < path.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 24.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerDefaults.color
                            )
                        }
                    }
                }
            }
        }
    }
}