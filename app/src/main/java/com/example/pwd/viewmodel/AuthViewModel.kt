package com.example.pwd.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pwd.data.AppDatabase
import com.example.pwd.repository.UserRepository
import com.example.pwd.utils.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState
    
    suspend fun isUserLoggedIn(): Boolean {
        val userId = userPreferences.userId.first()
        return userId != null && userId > 0
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            try {
                val result = userRepository.login(username, password)
                result.fold(
                    onSuccess = { user ->
                        userPreferences.saveUserId(user.id)
                        _loginState.value = LoginState.Success
                    },
                    onFailure = { exception ->
                        _loginState.value = LoginState.Error(exception.message ?: "登录失败")
                    }
                )
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "登录时发生错误")
            }
        }
    }
    
    fun register(username: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("两次输入的密码不一致")
            return
        }
        
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            
            try {
                val result = userRepository.register(username, password)
                result.fold(
                    onSuccess = { userId ->
                        userPreferences.saveUserId(userId)
                        _registerState.value = RegisterState.Success
                    },
                    onFailure = { exception ->
                        _registerState.value = RegisterState.Error(exception.message ?: "注册失败")
                    }
                )
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "注册时发生错误")
            }
        }
    }
    
    fun resetStates() {
        _loginState.value = LoginState.Initial
        _registerState.value = RegisterState.Initial
    }
    
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUserId()
            _loginState.value = LoginState.Initial
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = UserRepository(database.userDao())
            val userPreferences = UserPreferences(context)
            return AuthViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("未知的ViewModel类")
    }
} 