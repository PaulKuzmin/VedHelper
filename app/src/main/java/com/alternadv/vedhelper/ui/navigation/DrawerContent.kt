package com.alternadv.vedhelper.ui.navigation

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.alternadv.vedhelper.BuildConfig
import com.alternadv.vedhelper.R

@Composable
fun DrawerContent(
    currentRoute: String?,
    onNavigate: (route: String) -> Unit
) {
    val context = LocalContext.current
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
            Image(
                painter = painterResource(id = R.drawable.logotextline),
                contentDescription = "Логотип",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Альтерна",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "таможенный представитель",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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

        Spacer(Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        DrawerExternalItem(
            title = "Сайт компании",
            icon = Icons.Default.Web,
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, "https://alternadv.com/".toUri())) }
        )
        DrawerExternalItem(
            title = "Политика конфиденциальности",
            icon = Icons.Default.Description,
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, "https://alternadv.com/uslugi/pd-policy".toUri())) }
        )
        DrawerExternalItem(
            title = "Оценить приложение",
            icon = Icons.Default.Star,
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, "https://www.rustore.ru/catalog/app/com.alternadv.vedhelper".toUri())) }
        )
        Text(
            text = "Версия ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun DrawerExternalItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(title) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = title) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

private data class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)
