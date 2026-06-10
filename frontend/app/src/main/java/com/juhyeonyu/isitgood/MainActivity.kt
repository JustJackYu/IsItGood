package com.juhyeonyu.isitgood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.juhyeonyu.isitgood.ui.screens.*
import com.juhyeonyu.isitgood.ui.theme.IsItGoodTheme
import com.juhyeonyu.isitgood.ui.viewmodel.AuthViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.HomeViewModel
import com.juhyeonyu.isitgood.ui.viewmodel.SearchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IsItGoodTheme {
                AppNavigation()
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

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