package com.alternadv.vedhelper.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Calc : BottomNavItem(
        route = "calc",
        icon = Icons.Default.Inventory,
        label = "Товары"
    )

    object AutoCalc : BottomNavItem(
        route = "autocalc",
        icon = Icons.Default.DirectionsCar,
        label = "Авто"
    )

    object Examples : BottomNavItem(
        route = "examples",
        icon = Icons.Default.Description,
        label = "Примеры"
    )

    object Home : BottomNavItem(
        route = "home",
        icon = Icons.Default.FormatListNumbered,
        label = "ТН ВЭД"
    )

    object Rois : BottomNavItem(
        route = "rois",
        icon = Icons.Default.Lightbulb,
        label = "РОИС"
    )
}
