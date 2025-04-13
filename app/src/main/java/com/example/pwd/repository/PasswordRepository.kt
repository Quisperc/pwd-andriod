package com.example.pwd.repository

import com.example.pwd.data.PasswordDao
import com.example.pwd.data.PasswordEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

class PasswordRepository(private val passwordDao: PasswordDao) {
    
    fun getAllPasswordsByUserId(userId: Long): Flow<List<PasswordEntity>> {
        return passwordDao.getAllPasswordsByUserId(userId)
    }
    
    fun searchPasswordsByPlatform(userId: Long, platform: String): Flow<List<PasswordEntity>> {
        return passwordDao.searchPasswordsByPlatform(userId, platform)
    }
    
    fun searchPasswordsByUsername(userId: Long, query: String): Flow<List<PasswordEntity>> {
        return passwordDao.searchPasswordsByUsername(userId, query)
    }
    
    fun searchPasswordsByPassword(userId: Long, query: String): Flow<List<PasswordEntity>> {
        return passwordDao.searchPasswordsByPassword(userId, query)
    }
    
    fun searchPasswordsByPhone(userId: Long, query: String): Flow<List<PasswordEntity>> {
        return passwordDao.searchPasswordsByPhone(userId, query)
    }
    
    fun searchPasswordsByEmail(userId: Long, query: String): Flow<List<PasswordEntity>> {
        return passwordDao.searchPasswordsByEmail(userId, query)
    }
    
    fun searchPasswordsByNote(userId: Long, query: String): Flow<List<PasswordEntity>> {
        return passwordDao.searchPasswordsByNote(userId, query)
    }
    
    fun searchPasswordsByAllFields(userId: Long, query: String): Flow<List<PasswordEntity>> {
        return passwordDao.searchPasswordsByAllFields(userId, query)
    }
    
    suspend fun addPassword(
        username: String,
        password: String,
        phone: String?,
        email: String?,
        platform: String,
        note: String?,
        userId: Long
    ): Long {
        val now = Date()
        val passwordEntity = PasswordEntity(
            username = username,
            password = password,
            phone = phone,
            email = email,
            platform = platform,
            note = note,
            createTime = now,
            updateTime = now,
            userId = userId
        )
        return passwordDao.insertPassword(passwordEntity)
    }
    
    suspend fun updatePassword(
        id: Long,
        username: String,
        password: String,
        phone: String?,
        email: String?,
        platform: String,
        note: String?
    ) {
        val currentPassword = passwordDao.getPasswordById(id)
        currentPassword?.let {
            val updatedPassword = it.copy(
                username = username,
                password = password,
                phone = phone,
                email = email,
                platform = platform,
                note = note,
                updateTime = Date()
            )
            passwordDao.updatePassword(updatedPassword)
        }
    }
    
    suspend fun deletePassword(id: Long) {
        val password = passwordDao.getPasswordById(id)
        password?.let {
            passwordDao.deletePassword(it)
        }
    }
    
    suspend fun getPasswordById(id: Long): PasswordEntity? {
        return passwordDao.getPasswordById(id)
    }
    
    suspend fun importPasswords(passwords: List<PasswordEntity>): List<Long> {
        return passwordDao.insertPasswords(passwords)
    }
} 