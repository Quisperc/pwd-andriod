package com.example.demo2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    val phone: String? = null,
    val email: String? = null,
    val platform: String,
    val note: String? = null,
    val createTime: Date,
    val updateTime: Date,
    val userId: Long // 关联用户ID
) 