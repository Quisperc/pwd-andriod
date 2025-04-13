package com.example.demo2.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demo2.data.AppDatabase
import com.example.demo2.data.PasswordEntity
import com.example.demo2.repository.PasswordRepository
import com.example.demo2.utils.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 定义搜索类型
enum class SearchType {
    ALL_FIELDS, PLATFORM, USERNAME, PASSWORD, PHONE, EMAIL, NOTE
}

class PasswordViewModel(
    private val passwordRepository: PasswordRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _passwordOperationState = MutableStateFlow<PasswordOperationState>(PasswordOperationState.Initial)
    val passwordOperationState: StateFlow<PasswordOperationState> = _passwordOperationState
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _searchType = MutableStateFlow(SearchType.ALL_FIELDS)
    val searchType: StateFlow<SearchType> = _searchType
    
    suspend fun getUserId(): Long {
        return userPreferences.userId.first() ?: 0
    }
    
    fun getAllPasswords(): Flow<List<PasswordEntity>> {
        val userId = MutableStateFlow<Long>(0)
        viewModelScope.launch {
            userId.value = getUserId()
        }
        return passwordRepository.getAllPasswordsByUserId(userId.value)
    }
    
    fun searchPasswords(query: String, type: SearchType = searchType.value): Flow<List<PasswordEntity>> {
        val userId = MutableStateFlow<Long>(0)
        viewModelScope.launch {
            userId.value = getUserId()
        }
        _searchQuery.value = query
        _searchType.value = type
        
        return when (type) {
            SearchType.ALL_FIELDS -> passwordRepository.searchPasswordsByAllFields(userId.value, query)
            SearchType.PLATFORM -> passwordRepository.searchPasswordsByPlatform(userId.value, query)
            SearchType.USERNAME -> passwordRepository.searchPasswordsByUsername(userId.value, query)
            SearchType.PASSWORD -> passwordRepository.searchPasswordsByPassword(userId.value, query)
            SearchType.PHONE -> passwordRepository.searchPasswordsByPhone(userId.value, query)
            SearchType.EMAIL -> passwordRepository.searchPasswordsByEmail(userId.value, query)
            SearchType.NOTE -> passwordRepository.searchPasswordsByNote(userId.value, query)
        }
    }
    
    fun setSearchType(type: SearchType) {
        _searchType.value = type
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    suspend fun getPasswordById(id: Long): PasswordEntity? {
        return passwordRepository.getPasswordById(id)
    }
    
    fun addPassword(
        username: String,
        password: String,
        phone: String?,
        email: String?,
        platform: String,
        note: String?
    ) {
        viewModelScope.launch {
            _passwordOperationState.value = PasswordOperationState.Loading
            
            try {
                val userId = getUserId()
                if (userId == 0L) {
                    _passwordOperationState.value = PasswordOperationState.Error("用户未登录")
                    return@launch
                }
                
                val passwordId = passwordRepository.addPassword(
                    username = username,
                    password = password,
                    phone = phone,
                    email = email,
                    platform = platform,
                    note = note,
                    userId = userId
                )
                
                _passwordOperationState.value = PasswordOperationState.Success("密码添加成功")
            } catch (e: Exception) {
                _passwordOperationState.value = PasswordOperationState.Error(e.message ?: "添加密码失败")
            }
        }
    }
    
    fun updatePassword(
        id: Long,
        username: String,
        password: String,
        phone: String?,
        email: String?,
        platform: String,
        note: String?
    ) {
        viewModelScope.launch {
            _passwordOperationState.value = PasswordOperationState.Loading
            
            try {
                passwordRepository.updatePassword(
                    id = id,
                    username = username,
                    password = password,
                    phone = phone,
                    email = email,
                    platform = platform,
                    note = note
                )
                
                _passwordOperationState.value = PasswordOperationState.Success("密码更新成功")
            } catch (e: Exception) {
                _passwordOperationState.value = PasswordOperationState.Error(e.message ?: "更新密码失败")
            }
        }
    }
    
    fun deletePassword(id: Long) {
        viewModelScope.launch {
            _passwordOperationState.value = PasswordOperationState.Loading
            
            try {
                passwordRepository.deletePassword(id)
                _passwordOperationState.value = PasswordOperationState.Success("密码删除成功")
            } catch (e: Exception) {
                _passwordOperationState.value = PasswordOperationState.Error(e.message ?: "删除密码失败")
            }
        }
    }
    
    fun resetState() {
        _passwordOperationState.value = PasswordOperationState.Initial
    }
}

sealed class PasswordOperationState {
    object Initial : PasswordOperationState()
    object Loading : PasswordOperationState()
    data class Success(val message: String) : PasswordOperationState()
    data class Error(val message: String) : PasswordOperationState()
}

class PasswordViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = PasswordRepository(database.passwordDao())
            val userPreferences = UserPreferences(context)
            return PasswordViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("未知的ViewModel类")
    }
} 