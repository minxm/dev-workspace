package com.redbookclone.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey
    val id: String,
    val noteId: String,
    val userId: String,
    val username: String,
    val userAvatar: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
