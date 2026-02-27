package com.redbookclone.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BaseResponse(
    val success: Boolean,
    val message: String?
)

// Auth
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val data: AuthData?
)

data class AuthData(
    val token: String,
    val user: UserDto
)

data class UserResponse(
    val success: Boolean,
    val data: UserDto?
)

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val avatar: String,
    val bio: String,
    @SerializedName("followers_count") val followersCount: Int,
    @SerializedName("following_count") val followingCount: Int,
    @SerializedName("notes_count") val notesCount: Int
)

// Notes
data class NotesResponse(
    val success: Boolean,
    val data: NotesData?
)

data class NotesData(
    val notes: List<NoteDto>,
    val pagination: Pagination
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class NotesListResponse(
    val success: Boolean,
    val data: List<NoteDto>?
)

data class NoteDetailResponse(
    val success: Boolean,
    val data: NoteDto?
)

data class NoteDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val username: String,
    @SerializedName("user_avatar") val userAvatar: String,
    val title: String,
    val content: String,
    @SerializedName("cover_image") val coverImage: String,
    val images: List<String>,
    val location: String?,
    val topics: List<String>?,
    @SerializedName("likes_count") val likesCount: Int,
    @SerializedName("comments_count") val commentsCount: Int,
    @SerializedName("collects_count") val collectsCount: Int,
    @SerializedName("views_count") val viewsCount: Int = 0,
    @SerializedName("is_liked") val isLiked: Boolean,
    @SerializedName("is_collected") val isCollected: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class LikeResponse(
    val success: Boolean,
    val message: String?,
    val data: LikeData?
)

data class LikeData(
    @SerializedName("is_liked") val isLiked: Boolean
)

data class CollectResponse(
    val success: Boolean,
    val message: String?,
    val data: CollectData?
)

data class CollectData(
    @SerializedName("is_collected") val isCollected: Boolean
)

// Comments
data class CommentRequest(
    val content: String
)

data class CommentsResponse(
    val success: Boolean,
    val data: List<CommentDto>?
)

data class CommentDetailResponse(
    val success: Boolean,
    val message: String?,
    val data: CommentDto?
)

data class CommentDto(
    val id: String,
    @SerializedName("note_id") val noteId: String,
    @SerializedName("user_id") val userId: String,
    val username: String,
    @SerializedName("user_avatar") val userAvatar: String,
    val content: String,
    @SerializedName("likes_count") val likesCount: Int,
    @SerializedName("is_liked") val isLiked: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class CommentLikeResponse(
    val success: Boolean,
    val message: String?,
    val data: CommentLikeData?
)

data class CommentLikeData(
    @SerializedName("is_liked") val isLiked: Boolean
)
