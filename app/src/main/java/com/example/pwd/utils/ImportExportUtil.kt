package com.example.pwd.utils

import android.content.Context
import android.net.Uri
import com.example.pwd.data.PasswordEntity
import com.example.pwd.data.PasswordExportData
import com.example.pwd.data.toEntity
import com.example.pwd.data.toExportItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object ImportExportUtil {
    
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // 导出密码
    suspend fun exportPasswords(context: Context, passwords: List<PasswordEntity>, outputUri: Uri): Result<Int> {
        return try {
            val exportItems = passwords.map { it.toExportItem() }
            val exportData = PasswordExportData(
                passwords = exportItems,
                exportTime = System.currentTimeMillis()
            )
            
            val jsonContent = json.encodeToString(exportData)
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonContent)
                }
            }
            
            Result.success(passwords.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 导入密码
    suspend fun importPasswords(context: Context, inputUri: Uri, userId: Long): Result<List<PasswordEntity>> {
        return try {
            var jsonContent = ""
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    jsonContent = stringBuilder.toString()
                }
            }
            
            if (jsonContent.isBlank()) {
                return Result.failure(Exception("导入文件为空"))
            }
            
            val exportData = json.decodeFromString<PasswordExportData>(jsonContent)
            val entities = exportData.passwords.map { it.toEntity(userId) }
            
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 检查文件是否有效的密码导出文件
    suspend fun isValidPasswordFile(context: Context, uri: Uri): Boolean {
        return try {
            var jsonContent = ""
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    jsonContent = stringBuilder.toString()
                }
            }
            
            if (jsonContent.isBlank()) {
                return false
            }
            
            val exportData = json.decodeFromString<PasswordExportData>(jsonContent)
            exportData.passwords.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
} 