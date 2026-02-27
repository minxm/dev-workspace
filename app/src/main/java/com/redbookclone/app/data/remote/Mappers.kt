package com.redbookclone.app.data.remote

import com.redbookclone.app.data.model.Comment
import com.redbookclone.app.data.model.Note
import com.redbookclone.app.data.model.User
import com.redbookclone.app.data.remote.dto.CommentDto
import com.redbookclone.app.data.remote.dto.NoteDto
import com.redbookclone.app.data.remote.dto.UserDto

fun UserDto.toUser(): User {
    return User(
        id = id,
        username = username,
        email = email,
        avatar = avatar,
        bio = bio,
        followersCount = followersCount,
        followingCount = followingCount,
        notesCount = notesCount
    )
}

fun NoteDto.toNote(): Note {
    return Note(
        id = id,
        userId = userId,
        username = username,
        userAvatar = userAvatar,
        title = title,
        content = content,
        images = images,
        coverImage = coverImage,
        likesCount = likesCount,
        commentsCount = commentsCount,
        collectsCount = collectsCount,
        isLiked = isLiked,
        isCollected = isCollected,
        location = location ?: "",
        topics = topics ?: emptyList(),
        createdAt = parseTimestamp(createdAt)
    )
}

fun CommentDto.toComment(): Comment {
    return Comment(
        id = id,
        noteId = noteId,
        userId = userId,
        username = username,
        userAvatar = userAvatar,
        content = content,
        likesCount = likesCount,
        isLiked = isLiked,
        createdAt = parseTimestamp(createdAt)
    )
}

private fun parseTimestamp(timestamp: String): Long {
    return try {
        // Parse ISO 8601 timestamp from MySQL
        java.time.ZonedDateTime.parse(timestamp).toInstant().toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
