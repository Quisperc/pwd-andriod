package com.example.pwd.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pwd.ui.screen.LoginScreen
import com.example.pwd.ui.screen.PasswordDetailScreen
import com.example.pwd.ui.screen.PasswordFormScreen
import com.example.pwd.ui.screen.PasswordListScreen
import com.example.pwd.ui.screen.RegisterScreen
import com.example.pwd.ui.screen.SplashScreen
import com.example.pwd.viewmodel.AuthViewModel
import com.example.pwd.viewmodel.AuthViewModelFactory
import com.example.pwd.viewmodel.PasswordViewModel
import com.example.pwd.viewmodel.PasswordViewModelFactory
import kotlinx.coroutines.runBlocking

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
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
    
    val initialStartDestination = startDestination ?: Screen.Splash.route
    
    NavHost(
        navController = navController,
        startDestination = initialStartDestination
    ) {
        composable(
            route = Screen.Splash.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { 
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseOut
                    )
                )
            }
        ) {
            SplashScreen(
                onSplashFinished = {
                    val route = runBlocking {
                        if (authViewModel.isUserLoggedIn()) {
                            Screen.PasswordList.route
                        } else {
                            Screen.Login.route
                        }
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Login.route,
            enterTransition = { 
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseIn
                    )
                ) 
            },
            exitTransition = { 
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOut
                    )
                )
            }
        ) {
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
        
        composable(
            route = Screen.Register.route,
            enterTransition = { 
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseIn
                    )
                ) 
            },
            exitTransition = { 
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOut
                    )
                )
            }
        ) {
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
        
        composable(
            route = Screen.PasswordList.route,
            enterTransition = { 
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseIn
                    )
                ) 
            },
            exitTransition = { 
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOut
                    )
                )
            }
        ) {
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
        
        composable(
            route = Screen.AddPassword.route,
            enterTransition = { 
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseIn
                    )
                ) 
            },
            exitTransition = { 
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOut
                    )
                )
            }
        ) {
            PasswordFormScreen(
                passwordViewModel = passwordViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.PasswordDetail.route,
            arguments = listOf(
                navArgument("passwordId") { type = NavType.LongType }
            ),
            enterTransition = { 
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseIn
                    )
                ) 
            },
            exitTransition = { 
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOut
                    )
                )
            }
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
            ),
            enterTransition = { 
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseIn
                    )
                ) 
            },
            exitTransition = { 
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOut
                    )
                )
            }
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