package com.example.demo2.repository

import com.example.demo2.data.UserDao
import com.example.demo2.data.UserEntity
import java.util.Date

class UserRepository(private val userDao: UserDao) {
    
    suspend fun login(username: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByUsername(username)
        
        return if (user != null && user.password == password) {
            Result.success(user)
        } else {
            Result.failure(Exception("用户名或密码错误"))
        }
    }
    
    suspend fun register(username: String, password: String): Result<Long> {
        val existingUser = userDao.getUserByUsername(username)
        
        return if (existingUser != null) {
            Result.failure(Exception("用户名已存在"))
        } else {
            val userId = userDao.insertUser(
                UserEntity(
                    username = username,
                    password = password, 
                    createTime = Date()
                )
            )
            Result.success(userId)
        }
    }
    
    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }
} 