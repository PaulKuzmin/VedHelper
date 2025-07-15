package com.alternadv.vedhelper.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(
    onNavigate: (route: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 48.dp)
    ) {
        DrawerItem("ТН ВЭД", "home", onNavigate)
        DrawerItem("Товары", "calc", onNavigate)
        DrawerItem("Авто", "autocalc", onNavigate)
        DrawerItem("Примеры", "examples", onNavigate)
        DrawerItem("РОИС", "rois", onNavigate)
    }
}

@Composable
private fun DrawerItem(text: String, route: String, onClick: (String) -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(route) }
            .padding(16.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}