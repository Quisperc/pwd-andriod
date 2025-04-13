package com.example.demo2.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.demo2.data.PasswordEntity
import com.example.demo2.viewmodel.AuthViewModel
import com.example.demo2.viewmodel.PasswordOperationState
import com.example.demo2.viewmodel.PasswordViewModel
import com.example.demo2.viewmodel.SearchType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListScreen(
    authViewModel: AuthViewModel,
    passwordViewModel: PasswordViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddPassword: () -> Unit,
    onNavigateToPasswordDetail: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf(SearchType.ALL_FIELDS) }
    var isSearchTypeMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    var passwordsFlow by remember {
        mutableStateOf<Flow<List<PasswordEntity>>>(passwordViewModel.getAllPasswords())
    }
    
    var passwords by remember { mutableStateOf<List<PasswordEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val passwordOperationState by passwordViewModel.passwordOperationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 初始加载所有密码
    LaunchedEffect(passwordsFlow) {
        isLoading = true
        passwordsFlow.collect { newPasswords ->
            passwords = newPasswords
            isLoading = false
        }
    }
    
    LaunchedEffect(passwordOperationState) {
        when (passwordOperationState) {
            is PasswordOperationState.Success -> {
                snackbarHostState.showSnackbar(
                    (passwordOperationState as PasswordOperationState.Success).message
                )
                passwordViewModel.resetState()
            }
            is PasswordOperationState.Error -> {
                snackbarHostState.showSnackbar(
                    (passwordOperationState as PasswordOperationState.Error).message
                )
                passwordViewModel.resetState()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的密码") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onNavigateToLogin()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "退出登录"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddPassword) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加密码"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索类型选择器和搜索栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 搜索类型选择器
                ExposedDropdownMenuBox(
                    expanded = isSearchTypeMenuExpanded,
                    onExpandedChange = { isSearchTypeMenuExpanded = it },
                    modifier = Modifier
                        .weight(0.3f)
                        .padding(end = 8.dp)
                ) {
                    OutlinedTextField(
                        value = getSearchTypeDisplayName(searchType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("字段") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSearchTypeMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    DropdownMenu(
                        expanded = isSearchTypeMenuExpanded,
                        onDismissRequest = { isSearchTypeMenuExpanded = false },
                        modifier = Modifier.exposedDropdownSize()
                    ) {
                        SearchType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(getSearchTypeDisplayName(type)) },
                                onClick = {
                                    searchType = type
                                    isSearchTypeMenuExpanded = false
                                    if (searchQuery.isNotBlank()) {
                                        scope.launch {
                                            passwordsFlow = passwordViewModel.searchPasswords(
                                                query = searchQuery,
                                                type = searchType
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                
                // 搜索栏
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        scope.launch {
                            passwordsFlow = if (it.isBlank()) {
                                passwordViewModel.getAllPasswords()
                            } else {
                                passwordViewModel.searchPasswords(
                                    query = it,
                                    type = searchType
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.7f),
                    placeholder = { 
                        Text(
                            "搜索${getSearchTypeDisplayName(searchType).lowercase()}"
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                scope.launch {
                                    passwordsFlow = passwordViewModel.getAllPasswords()
                                }
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            scope.launch {
                                passwordsFlow = if (searchQuery.isBlank()) {
                                    passwordViewModel.getAllPasswords()
                                } else {
                                    passwordViewModel.searchPasswords(
                                        query = searchQuery,
                                        type = searchType
                                    )
                                }
                            }
                        }
                    )
                )
            }
            
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (passwords.isEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank()) 
                            "暂无密码，点击右下角按钮添加" 
                        else 
                            "未找到匹配的密码",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(passwords) { password ->
                            PasswordItem(
                                password = password,
                                onPasswordClick = { onNavigateToPasswordDetail(password.id) },
                                onDeleteClick = {
                                    scope.launch {
                                        passwordViewModel.deletePassword(password.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 获取搜索类型的显示名称
fun getSearchTypeDisplayName(type: SearchType): String {
    return when (type) {
        SearchType.ALL_FIELDS -> "所有字段"
        SearchType.PLATFORM -> "平台"
        SearchType.USERNAME -> "用户名"
        SearchType.PASSWORD -> "密码"
        SearchType.PHONE -> "手机号"
        SearchType.EMAIL -> "邮箱"
        SearchType.NOTE -> "备注"
    }
}

@Composable
fun PasswordItem(
    password: PasswordEntity,
    onPasswordClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onPasswordClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = password.platform,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "用户名: ${password.username}",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!password.email.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "邮箱: ${password.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    Text(
                        text = "更新时间: ${dateFormat.format(password.updateTime)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除"
                    )
                }
            }
        }
    }
} 