package com.example.demo2.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
//import androidx.lint.kotlin.metadata.Visibility
import com.example.demo2.data.PasswordEntity
import com.example.demo2.viewmodel.PasswordViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    passwordViewModel: PasswordViewModel,
    passwordId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditPassword: (Long) -> Unit
) {
    var password by remember { mutableStateOf<PasswordEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(passwordId) {
        isLoading = true
        password = passwordViewModel.getPasswordById(passwordId)
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("密码详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (password != null) {
                FloatingActionButton(onClick = { onNavigateToEditPassword(passwordId) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑密码"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else if (password == null) {
                Text(
                    text = "无法找到该密码",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                PasswordDetailCard(
                    password = password!!,
                    showPassword = showPassword,
                    onTogglePasswordVisibility = { showPassword = !showPassword },
                    onCopyToClipboard = { text, label ->
                        clipboardManager.setText(AnnotatedString(text))
                        scope.launch {
                            snackbarHostState.showSnackbar("$label 已复制到剪贴板")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PasswordDetailCard(
    password: PasswordEntity,
    showPassword: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onCopyToClipboard: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = password.platform,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DetailItem(
                label = "用户名",
                value = password.username,
                onCopy = { onCopyToClipboard(password.username, "用户名") }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            DetailItem(
                label = "密码",
                value = if (showPassword) password.password else "••••••••",
                onCopy = { onCopyToClipboard(password.password, "密码") },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                        )
                    }
                }
            )
            
            if (!password.phone.isNullOrBlank()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                DetailItem(
                    label = "手机号",
                    value = password.phone,
                    onCopy = { onCopyToClipboard(password.phone!!, "手机号") }
                )
            }
            
            if (!password.email.isNullOrBlank()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                DetailItem(
                    label = "邮箱",
                    value = password.email,
                    onCopy = { onCopyToClipboard(password.email!!, "邮箱") }
                )
            }
            
            if (!password.note.isNullOrBlank()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                DetailItem(
                    label = "备注",
                    value = password.note,
                    onCopy = { onCopyToClipboard(password.note!!, "备注") }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            Text(
                text = "创建时间: ${dateFormat.format(password.createTime)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Text(
                text = "更新时间: ${dateFormat.format(password.updateTime)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String?,
    onCopy: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "复制到剪贴板"
            )
        }
        
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(4.dp))
            trailingIcon()
        }
    }
} 