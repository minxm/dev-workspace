package com.redbookclone.app.data.repository

import com.redbookclone.app.data.local.UserDao
import com.redbookclone.app.data.local.UserPreferences
import com.redbookclone.app.data.model.User
import com.redbookclone.app.data.remote.ApiService
import com.redbookclone.app.data.remote.dto.LoginRequest
import com.redbookclone.app.data.remote.dto.RegisterRequest
import com.redbookclone.app.data.remote.toUser
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                val user = authData.user.toUser()
                
                // Save user data
                userDao.insertUser(user)
                userPreferences.saveUserData(
                    userId = user.id,
                    username = user.username,
                    email = user.email,
                    token = authData.token
                )
                
                Result.success(user)
            } else {
                val errorMessage = response.body()?.message ?: "登录失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            val response = apiService.register(RegisterRequest(username, email, password))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                val user = authData.user.toUser()
                
                // Save user data
                userDao.insertUser(user)
                userPreferences.saveUserData(
                    userId = user.id,
                    username = user.username,
                    email = user.email,
                    token = authData.token
                )
                
                Result.success(user)
            } else {
                val errorMessage = response.body()?.message ?: "注册失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userPreferences.clearUserData()
        userDao.deleteAllUsers()
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.data!!.toUser()
                userDao.insertUser(user)
                user
            } else {
                val userId = userPreferences.userId.first()
                userId?.let { userDao.getUserById(it).first() }
            }
        } catch (e: Exception) {
            val userId = userPreferences.userId.first()
            userId?.let { userDao.getUserById(it).first() }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return userPreferences.token.first() != null
    }
}
