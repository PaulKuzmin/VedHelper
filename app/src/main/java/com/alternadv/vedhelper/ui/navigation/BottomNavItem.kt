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

    object CarCalc : BottomNavItem(
        route = "carcalc",
        icon = Icons.Default.DirectionsCar,
        label = "Авто"
    )

    object CarCalcResult : BottomNavItem(
        route = "carcalcresult",
        icon = Icons.Default.DirectionsCar,
        label = "Результаты расчета"
    )

    object Examples : BottomNavItem(
        route = "examples",
        icon = Icons.Default.Description,
        label = "Примеры"
    )

    object Tnved : BottomNavItem(
        route = "tnved",
        icon = Icons.Default.FormatListNumbered,
        label = "ТН ВЭД"
    )

    object TnvedCode : BottomNavItem(
        route = "tnvedcode",
        icon = Icons.Default.FormatListNumbered,
        label = "Код ТН ВЭД"
    )

    object Rois : BottomNavItem(
        route = "rois",
        icon = Icons.Default.Lightbulb,
        label = "РОИС"
    )

    object CalcResult : BottomNavItem(
        route = "calcResult",
        icon = Icons.Default.Calculate,
        label = "Результаты расчета"
    )
}
