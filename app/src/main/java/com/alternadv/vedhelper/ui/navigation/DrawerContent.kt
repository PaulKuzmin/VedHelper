package com.alternadv.vedhelper.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alternadv.vedhelper.R

@Composable
fun DrawerContent(
    currentRoute: String?,
    onNavigate: (route: String) -> Unit
) {
    val items = listOf(
        DrawerMenuItem(BottomNavItem.Contacts.route, BottomNavItem.Contacts.label, BottomNavItem.Contacts.icon),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 27.dp, top = 10.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Картинка (только иконка)
            Image(
                painter = painterResource(id = R.drawable.logotextline),  // <-- только иконка
                contentDescription = "Логотип",
                modifier = Modifier
                    .size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Альтерна",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Разделитель
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            thickness = DividerDefaults.Thickness, color = DividerDefaults.color
        )

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationDrawerItem(
                label = { Text(item.title) },
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

private data class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)