package com.alternadv.vedhelper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class FabMenuItem(
    val label: String,
    val iconVector: ImageVector? = null,
    val iconPainter: Painter? = null,
    val onClick: () -> Unit
)

@Composable
fun ExpandableFabMenu(
    modifier: Modifier = Modifier,
    mainIcon: ImageVector = Icons.Default.ContactPhone,
    mainIconExpanded: ImageVector = Icons.Default.Close,
    items: List<FabMenuItem>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        // Кнопка-меню (основная)
        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = if (expanded) mainIconExpanded else mainIcon,
                contentDescription = "Меню"
            )
        }

        // Раскрывающиеся элементы
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 72.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        /*
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            Text(
                                text = item.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                         */
                        FloatingActionButton(
                            onClick = {
                                item.onClick()
                                expanded = false
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            when {
                                item.iconVector != null -> Icon(
                                    imageVector = item.iconVector,
                                    contentDescription = item.label
                                )
                                item.iconPainter != null -> Icon(
                                    painter = item.iconPainter,
                                    contentDescription = item.label
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}