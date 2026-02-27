package com.redbookclone.app.data.remote

import com.redbookclone.app.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>
    
    @Multipart
    @PUT("auth/profile")
    suspend fun updateProfile(
        @Part("username") username: RequestBody? = null,
        @Part("bio") bio: RequestBody? = null,
        @Part avatar: MultipartBody.Part? = null
    ): Response<UserResponse>
    
    // Notes
    @GET("notes")
    suspend fun getAllNotes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<NotesResponse>
    
    @GET("notes/{id}")
    suspend fun getNoteById(@Path("id") id: String): Response<NoteDetailResponse>
    
    @GET("notes/user/{userId}")
    suspend fun getUserNotes(@Path("userId") userId: String): Response<NotesListResponse>
    
    @GET("notes/search")
    suspend fun searchNotes(@Query("q") query: String): Response<NotesListResponse>
    
    @Multipart
    @POST("notes")
    suspend fun createNote(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("location") location: RequestBody? = null,
        @Part("topics") topics: RequestBody? = null,
        @Part images: List<MultipartBody.Part>
    ): Response<NoteDetailResponse>
    
    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<BaseResponse>
    
    @POST("notes/{id}/like")
    suspend fun toggleLike(@Path("id") id: String): Response<LikeResponse>
    
    @POST("notes/{id}/collect")
    suspend fun toggleCollect(@Path("id") id: String): Response<CollectResponse>
    
    // Comments
    @GET("comments/note/{noteId}")
    suspend fun getComments(@Path("noteId") noteId: String): Response<CommentsResponse>
    
    @POST("comments/note/{noteId}")
    suspend fun createComment(
        @Path("noteId") noteId: String,
        @Body request: CommentRequest
    ): Response<CommentDetailResponse>
    
    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") id: String): Response<BaseResponse>
    
    @POST("comments/{id}/like")
    suspend fun toggleCommentLike(@Path("id") id: String): Response<CommentLikeResponse>
}
