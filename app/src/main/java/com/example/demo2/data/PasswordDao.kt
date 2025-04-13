package com.example.demo2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords WHERE userId = :userId ORDER BY updateTime DESC")
    fun getAllPasswordsByUserId(userId: Long): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE userId = :userId AND platform LIKE '%' || :platform || '%' ORDER BY updateTime DESC")
    fun searchPasswordsByPlatform(userId: Long, platform: String): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE userId = :userId AND username LIKE '%' || :query || '%' ORDER BY updateTime DESC")
    fun searchPasswordsByUsername(userId: Long, query: String): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE userId = :userId AND password LIKE '%' || :query || '%' ORDER BY updateTime DESC")
    fun searchPasswordsByPassword(userId: Long, query: String): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE userId = :userId AND phone LIKE '%' || :query || '%' ORDER BY updateTime DESC")
    fun searchPasswordsByPhone(userId: Long, query: String): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE userId = :userId AND email LIKE '%' || :query || '%' ORDER BY updateTime DESC")
    fun searchPasswordsByEmail(userId: Long, query: String): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE userId = :userId AND note LIKE '%' || :query || '%' ORDER BY updateTime DESC")
    fun searchPasswordsByNote(userId: Long, query: String): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE userId = :userId AND (platform LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR password LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') ORDER BY updateTime DESC")
    fun searchPasswordsByAllFields(userId: Long, query: String): Flow<List<PasswordEntity>>

    @Insert
    suspend fun insertPassword(password: PasswordEntity): Long
    
    @Insert
    suspend fun insertPasswords(passwords: List<PasswordEntity>): List<Long>

    @Update
    suspend fun updatePassword(password: PasswordEntity)

    @Delete
    suspend fun deletePassword(password: PasswordEntity)

    @Query("SELECT * FROM passwords WHERE id = :passwordId")
    suspend fun getPasswordById(passwordId: Long): PasswordEntity?
} 