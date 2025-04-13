package com.example.demo2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.demo2.ui.screen.LoginScreen
import com.example.demo2.ui.screen.PasswordDetailScreen
import com.example.demo2.ui.screen.PasswordFormScreen
import com.example.demo2.ui.screen.PasswordListScreen
import com.example.demo2.ui.screen.RegisterScreen
import com.example.demo2.viewmodel.AuthViewModel
import com.example.demo2.viewmodel.AuthViewModelFactory
import com.example.demo2.viewmodel.PasswordViewModel
import com.example.demo2.viewmodel.PasswordViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object PasswordList : Screen("password_list")
    object AddPassword : Screen("add_password")
    object PasswordDetail : Screen("password_detail/{passwordId}") {
        fun createRoute(passwordId: Long) = "password_detail/$passwordId"
    }
    object EditPassword : Screen("edit_password/{passwordId}") {
        fun createRoute(passwordId: Long) = "edit_password/$passwordId"
    }
}

@Composable
fun AppNavigation(startDestination: String? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )
    
    val passwordViewModel: PasswordViewModel = viewModel(
        factory = PasswordViewModelFactory(context)
    )
    
    val initialStartDestination = startDestination ?: remember {
        runBlocking {
            if (authViewModel.isUserLoggedIn()) {
                Screen.PasswordList.route
            } else {
                Screen.Login.route
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = initialStartDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToHome = {
                    navController.navigate(Screen.PasswordList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToHome = {
                    navController.navigate(Screen.PasswordList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.PasswordList.route) {
            PasswordListScreen(
                authViewModel = authViewModel,
                passwordViewModel = passwordViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.PasswordList.route) { inclusive = true }
                    }
                },
                onNavigateToAddPassword = { navController.navigate(Screen.AddPassword.route) },
                onNavigateToPasswordDetail = { passwordId ->
                    navController.navigate(Screen.PasswordDetail.createRoute(passwordId))
                }
            )
        }
        
        composable(Screen.AddPassword.route) {
            PasswordFormScreen(
                passwordViewModel = passwordViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.PasswordDetail.route,
            arguments = listOf(
                navArgument("passwordId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getLong("passwordId") ?: 0
            PasswordDetailScreen(
                passwordViewModel = passwordViewModel,
                passwordId = passwordId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditPassword = { id ->
                    navController.navigate(Screen.EditPassword.createRoute(id))
                }
            )
        }
        
        composable(
            route = Screen.EditPassword.route,
            arguments = listOf(
                navArgument("passwordId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getLong("passwordId") ?: 0
            PasswordFormScreen(
                passwordViewModel = passwordViewModel,
                passwordId = passwordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 