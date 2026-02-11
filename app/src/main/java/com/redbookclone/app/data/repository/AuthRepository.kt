package com.redbookclone.app.data.repository

import com.redbookclone.app.data.local.UserDao
import com.redbookclone.app.data.local.UserPreferences
import com.redbookclone.app.data.model.User
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // 模拟登录逻辑
            val userId = UUID.randomUUID().toString()
            val user = User(
                id = userId,
                username = email.substringBefore("@"),
                email = email,
                avatar = "https://picsum.photos/200?random=$userId",
                bio = "这是我的个人简介"
            )
            
            userDao.insertUser(user)
            userPreferences.saveUserData(
                userId = user.id,
                username = user.username,
                email = user.email,
                token = "mock_token_$userId"
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            val userId = UUID.randomUUID().toString()
            val user = User(
                id = userId,
                username = username,
                email = email,
                avatar = "https://picsum.photos/200?random=$userId",
                bio = "新用户"
            )
            
            userDao.insertUser(user)
            userPreferences.saveUserData(
                userId = user.id,
                username = user.username,
                email = user.email,
                token = "mock_token_$userId"
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userPreferences.clearUserData()
    }

    suspend fun getCurrentUser(): User? {
        val userId = userPreferences.userId.first()
        return userId?.let { userDao.getUserById(it).first() }
    }

    suspend fun isLoggedIn(): Boolean {
        return userPreferences.userId.first() != null
    }
}
