package com.juhyeonyu.isitgood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.juhyeonyu.isitgood.ui.screens.*
import com.juhyeonyu.isitgood.ui.theme.IsItGoodTheme

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

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
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
                onChatClick = { gameTitle, gameSummary ->
                    navController.navigate("chat/$gameTitle/$gameSummary")
                }
            )
        }

        composable(
            route = "chat/{title}/{summary}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("summary") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val summary = backStackEntry.arguments?.getString("summary") ?: ""
            ChatScreen(gameTitle = title, summary = summary)
        }
    }
}