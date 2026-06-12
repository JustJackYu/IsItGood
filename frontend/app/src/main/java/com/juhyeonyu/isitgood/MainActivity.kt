package com.juhyeonyu.isitgood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.juhyeonyu.isitgood.data.local.TokenStore
import com.juhyeonyu.isitgood.data.local.UiPreferencesStore
import com.juhyeonyu.isitgood.data.remote.RetrofitClient
import com.juhyeonyu.isitgood.ui.screens.*
import com.juhyeonyu.isitgood.ui.theme.Cerulean
import com.juhyeonyu.isitgood.ui.theme.CoolSteel
import com.juhyeonyu.isitgood.ui.theme.IsItGoodTheme
import com.juhyeonyu.isitgood.ui.theme.Platinum
import com.juhyeonyu.isitgood.ui.theme.fontScaleFor
import com.juhyeonyu.isitgood.ui.viewmodel.AuthViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.AuthViewModelFactory
import com.juhyeonyu.isitgood.ui.viewmodel.HomeViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.SearchViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.SettingsViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val tokenStore = remember { TokenStore(context.applicationContext) }
            val uiPreferencesStore = remember { UiPreferencesStore(context.applicationContext) }

            // Locally cached font size drives the theme instantly at launch (no network needed).
            val fontSize by uiPreferencesStore.fontSizeFlow
                .collectAsState(initial = UiPreferencesStore.DEFAULT_FONT_SIZE)

            IsItGoodTheme(fontScale = fontScaleFor(fontSize)) {
                // null = still deciding; resolves to "home" (auto-login) or "login".
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val saved = tokenStore.getToken()
                    if (!saved.isNullOrBlank()) {
                        RetrofitClient.token = saved
                        startDestination = "home"
                    } else {
                        startDestination = "login"
                    }
                }

                val dest = startDestination
                if (dest == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Platinum),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Cerulean)
                    }
                } else {
                    AppNavigation(
                        startDestination = dest,
                        tokenStore = tokenStore,
                        uiPreferencesStore = uiPreferencesStore
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Filled.Home),
    BottomNavItem("settings", "Settings", Icons.Filled.Person)
)

// Routes that should display the bottom navigation bar.
private val bottomBarRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun AppNavigation(
    startDestination: String,
    tokenStore: TokenStore,
    uiPreferencesStore: UiPreferencesStore
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(tokenStore))
    val homeViewModel: HomeViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val settingsViewModel: SettingsViewModel =
        viewModel(factory = SettingsViewModelFactory(uiPreferencesStore))

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar(containerColor = Color.White) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Keep "home" as the anchor; tabs never stack on each other.
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Cerulean,
                                selectedTextColor = Cerulean,
                                unselectedIconColor = CoolSteel,
                                unselectedTextColor = CoolSteel,
                                indicatorColor = Platinum
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onSearchClick = { navController.navigate("search") },
                    onGameClick = { rawgId, title ->
                        navController.navigate("detail/$rawgId/$title")
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }

            composable("search") {
                SearchScreen(
                    viewModel = searchViewModel,
                    onGameClick = { rawgId, title ->
                        navController.navigate("detail/$rawgId/$title")
                    }
                )
            }

            composable(
                route = "detail/{rawgId}/{title}",
                arguments = listOf(
                    navArgument("rawgId") { type = NavType.IntType },
                    navArgument("title") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val rawgId = backStackEntry.arguments?.getInt("rawgId") ?: 0
                val title = backStackEntry.arguments?.getString("title") ?: ""
                GameDetailScreen(
                    rawgId = rawgId,
                    title = title,
                    onChatClick = { id -> navController.navigate("chat/$id") }
                )
            }

            composable(
                route = "chat/{rawgId}",
                arguments = listOf(
                    navArgument("rawgId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val rawgId = backStackEntry.arguments?.getInt("rawgId") ?: 0
                ChatScreen(rawgId = rawgId)
            }
        }
    }
}
