package com.alternadv.vedhelper.ui

import android.content.Intent
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alternadv.vedhelper.ui.components.ExpandableFabMenu
import com.alternadv.vedhelper.ui.components.FabMenuItem
import com.alternadv.vedhelper.ui.navigation.BottomNavItem
import com.alternadv.vedhelper.ui.navigation.BottomNavigationBar
import com.alternadv.vedhelper.ui.navigation.DrawerContent
import com.alternadv.vedhelper.ui.screen.calc.CalcScreen
import com.alternadv.vedhelper.ui.screen.calcresult.CalcResultScreen
import com.alternadv.vedhelper.ui.screen.calcresult.CalcResultViewModel
import com.alternadv.vedhelper.ui.screen.carcalc.CarCalcScreen
import com.alternadv.vedhelper.ui.screen.carcalcresult.CarCalcResultScreen
import com.alternadv.vedhelper.ui.screen.carcalcresult.CarCalcResultViewModel
import com.alternadv.vedhelper.ui.screen.contacts.ContactsScreen
import com.alternadv.vedhelper.ui.screen.examples.ExamplesScreen
import com.alternadv.vedhelper.ui.screen.rois.RoisScreen
import com.alternadv.vedhelper.ui.screen.tnved.TnvedScreen
import com.alternadv.vedhelper.ui.screen.tnvedcode.TnvedCodeScreen
import com.alternadv.vedhelper.R

@OptIn(ExperimentalMaterial3Api::class)
//@Preview
@Composable
fun MainAppScreen(viewModel: MainAppViewModel = viewModel()) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        viewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .collect { event ->
                when (event) {
                    Event.UpdateCompleted -> {
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Скачивание завершено",
                                actionLabel = "Установить",
                                duration = SnackbarDuration.Indefinite
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.completeUpdateRequested()
                            }
                        }
                    }
                }
            }
    }

    val bottomItems = listOf(
        BottomNavItem.Calc,
        BottomNavItem.CarCalc,
        BottomNavItem.Examples,
        BottomNavItem.Tnved,
        BottomNavItem.Rois
    )

    val backOnlyScreens = listOf(
        BottomNavItem.CalcResult.route,
        BottomNavItem.CarCalcResult.route,
        BottomNavItem.TnvedCode.route,
        BottomNavItem.Contacts.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rawRoute = navBackStackEntry?.destination?.route.orEmpty()
    val currentRoute = getBottomSelectedRoute(rawRoute)

    val title = when (currentRoute) {
        BottomNavItem.Calc.route -> "Калькулятор товаров"
        BottomNavItem.CalcResult.route -> "Результаты расчета"
        BottomNavItem.CarCalc.route -> "Калькулятор авто"
        BottomNavItem.CarCalcResult.route -> "Результаты расчета"
        BottomNavItem.Examples.route -> "Примеры"
        BottomNavItem.Tnved.route -> "ТН ВЭД"
        BottomNavItem.TnvedCode.route -> "Код ТН ВЭД"
        BottomNavItem.Rois.route -> "РОИС"
        BottomNavItem.Contacts.route -> "Контакты"
        else -> "Помощник ВЭД"
    }

    val calcResultViewModel: CalcResultViewModel = viewModel()
    val carCalcResultViewModel: CarCalcResultViewModel = viewModel()

    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Surface(
                modifier = Modifier.width(250.dp)
            ) {
                DrawerContent(
                    currentRoute = rawRoute,
                    onNavigate = { route ->
                        coroutineScope.launch {
                            drawerState.close()
                            navController.navigateSingleTopTo(route)
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            //
            floatingActionButton = {
                ExpandableFabMenu(
                    modifier = Modifier.offset(y = (-70).dp),
                    items = listOf(
                        FabMenuItem(
                            "Позвонить",
                            iconVector = Icons.Default.Call
                        ) {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, "tel:+79020504050".toUri())
                            )
                        },
                        FabMenuItem(
                            "Написать",
                            iconVector = Icons.Default.Email
                        ) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:broker@alterna.ltd".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "Заявка на таможенное оформление")
                                putExtra(Intent.EXTRA_TEXT, "Здравствуйте! ")
                            }
                            context.startActivity(intent)
                        },
                        FabMenuItem(
                            "WhatsApp",
                            iconPainter = painterResource(id = R.drawable.whatsapp_outline_icon)
                        ) {
                            val url = "https://wa.me/79510275454?text=Здравствуйте!"
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        },
                        FabMenuItem(
                            "Telegram",
                            iconPainter = painterResource(id = R.drawable.telegram_outline_icon)
                        ) {
                            val url = "https://t.me/alterna_manager"
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        },
                        FabMenuItem(
                            "WWW",
                            iconVector = Icons.Default.Web
                        ) {
                            val url = "https://www.alternadv.com/"
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        }
                    )
                )
            },
            //
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (currentRoute in backOnlyScreens) {
                            // только кнопка назад
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.Filled.ChevronLeft,
                                    contentDescription = "Назад"
                                )
                            }
                        } else {
                            // меню
                            IconButton(onClick = {
                                coroutineScope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Открыть меню")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (currentRoute !in backOnlyScreens) {
                    BottomNavigationBar(
                        navController = navController,
                        items = bottomItems
                    )
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Calc.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavItem.Contacts.route) { ContactsScreen() }
                composable(BottomNavItem.Tnved.route) { TnvedScreen(navController) }
                composable(
                    "${BottomNavItem.TnvedCode.route}/{code}",
                    arguments = listOf(navArgument("code") { defaultValue = ""; nullable = false }
                    )) { backStackEntry ->
                    val code = backStackEntry.arguments?.getString("code") ?: ""
                    TnvedCodeScreen(code = code, navController = navController)
                }

                composable(BottomNavItem.Calc.route) {
                    CalcScreen(
                        navController,
                        calcResultViewModel
                    )
                }
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

                composable(BottomNavItem.CarCalc.route) {
                    CarCalcScreen(
                        navController,
                        carCalcResultViewModel
                    )
                }
                composable(BottomNavItem.CarCalcResult.route) {
                    CarCalcResultScreen(
                        carCalcResultViewModel
                    )
                }
            }
        }

        BackHandler(enabled = drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        }
    }
}

private fun getBottomSelectedRoute(currentRoute: String): String {
    return when {
        currentRoute.startsWith("${BottomNavItem.Examples.route}/") -> BottomNavItem.Examples.route
        currentRoute.startsWith("${BottomNavItem.Calc.route}/") -> BottomNavItem.Calc.route
        currentRoute.startsWith("${BottomNavItem.TnvedCode.route}/") -> BottomNavItem.TnvedCode.route
        else -> currentRoute
    }
}


fun NavHostController.navigateSingleTopTo(route: String) {
    this.navigate(route) {
        // используем currentBackStackEntry?.destination?.parent?.findStartDestination()
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { // <-- ключ
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}