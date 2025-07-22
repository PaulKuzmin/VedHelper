package com.alternadv.vedhelper.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alternadv.vedhelper.ui.navigation.BottomNavItem
import com.alternadv.vedhelper.ui.navigation.BottomNavigationBar
import com.alternadv.vedhelper.ui.navigation.DrawerContent
import com.alternadv.vedhelper.ui.screen.calc.CalcScreen
import com.alternadv.vedhelper.ui.screen.calcresult.CalcResultScreen
import com.alternadv.vedhelper.ui.screen.calcresult.CalcResultViewModel
import com.alternadv.vedhelper.ui.screen.carcalc.CarCalcScreen
import com.alternadv.vedhelper.ui.screen.examples.ExamplesScreen
import com.alternadv.vedhelper.ui.screen.rois.RoisScreen
import com.alternadv.vedhelper.ui.screen.tnved.TnvedScreen
import com.alternadv.vedhelper.ui.screen.tnvedcode.TnvedCodeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val bottomItems = listOf(
        BottomNavItem.Calc,
        BottomNavItem.CarCalc,
        BottomNavItem.Examples,
        BottomNavItem.Tnved,
        BottomNavItem.Rois
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()

    val title = when (currentRoute) {
        BottomNavItem.Calc.route -> "Калькулятор товаров"
        BottomNavItem.CalcResult.route -> "Результаты расчета"
        BottomNavItem.CarCalc.route -> "Калькулятор авто"
        BottomNavItem.Examples.route -> "Примеры"
        BottomNavItem.Tnved.route -> "ТН ВЭД"
        BottomNavItem.TnvedCode.route -> "Код ТН ВЭД"
        BottomNavItem.Rois.route -> "РОИС"
        else -> "Помощник ВЭД"
    }

    val calcResultViewModel: CalcResultViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(onNavigate = { route ->
                coroutineScope.launch {
                    drawerState.close()
                    navController.navigate(route)
                }
            })
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Открыть меню")
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    items = bottomItems
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Calc.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavItem.Tnved.route) { TnvedScreen(navController) }
                composable(
                    "${BottomNavItem.TnvedCode.route}/{code}",
                    arguments = listOf(navArgument("code") { defaultValue = ""; nullable = false}
                )) { backStackEntry ->
                    val code = backStackEntry.arguments?.getString("code") ?: ""
                    TnvedCodeScreen(code = code, navController = navController)
                }

                composable(BottomNavItem.Calc.route) { CalcScreen(navController, calcResultViewModel) }
                composable(
                    route = "${BottomNavItem.Calc.route}/{code}",
                    arguments = listOf(navArgument("code") { type = NavType.StringType })
                ) { backStackEntry ->
                    val code = backStackEntry.arguments?.getString("code") ?: ""
                    CalcScreen(navController, calcResultViewModel, initialCode = code)
                }
                composable(BottomNavItem.CalcResult.route) { CalcResultScreen(calcResultViewModel) }

                composable(BottomNavItem.Examples.route) { ExamplesScreen(navController = navController) }
                composable(
                    route = "${BottomNavItem.Examples.route}/{searchTerm}",
                    arguments = listOf(navArgument("searchTerm") { type = NavType.StringType })
                ) { backStackEntry ->
                    val searchTerm = backStackEntry.arguments?.getString("searchTerm")
                    ExamplesScreen(navController, searchTerm)
                }

                composable(BottomNavItem.Rois.route) { RoisScreen() }

                composable(BottomNavItem.CarCalc.route) { CarCalcScreen() }
            }
        }

        BackHandler(enabled = drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        }
    }
}
