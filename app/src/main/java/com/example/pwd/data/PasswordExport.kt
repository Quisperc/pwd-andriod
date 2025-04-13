package com.example.pwd.data

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class PasswordExportItem(
    val username: String,
    val password: String,
    val phone: String? = null,
    val email: String? = null,
    val platform: String,
    val note: String? = null,
    val createTime: Long,
    val updateTime: Long
)

@Serializable
data class PasswordExportData(
    val passwords: List<PasswordExportItem>,
    val exportTime: Long = System.currentTimeMillis(),
    val version: Int = 1
)

// 转换函数
fun PasswordEntity.toExportItem(): PasswordExportItem {
    return PasswordExportItem(
        username = username,
        password = password,
        phone = phone,
        email = email,
        platform = platform,
        note = note,
        createTime = createTime.time,
        updateTime = updateTime.time
    )
}

fun PasswordExportItem.toEntity(userId: Long): PasswordEntity {
    return PasswordEntity(
        id = 0, // 新记录ID为0，会自动生成
        username = username,
        password = password,
        phone = phone,
        email = email,
        platform = platform,
        note = note,
        createTime = Date(createTime),
        updateTime = Date(updateTime),
        userId = userId
    )
} 