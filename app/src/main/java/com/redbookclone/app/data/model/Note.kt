package com.redbookclone.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String,
    val userId: String,
    val username: String,
    val userAvatar: String,
    val title: String,
    val content: String,
    val images: List<String>,
    val coverImage: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val collectsCount: Int = 0,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val location: String = "",
    val topics: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
