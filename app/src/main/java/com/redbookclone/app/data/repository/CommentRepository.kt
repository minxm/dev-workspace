package com.redbookclone.app.data.repository

import com.redbookclone.app.data.local.CommentDao
import com.redbookclone.app.data.model.Comment
import com.redbookclone.app.data.remote.ApiService
import com.redbookclone.app.data.remote.dto.CommentRequest
import com.redbookclone.app.data.remote.toComment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val apiService: ApiService,
    private val commentDao: CommentDao
) {
    
    fun getCommentsByNoteId(noteId: String): Flow<List<Comment>> = flow {
        try {
            val response = apiService.getComments(noteId)
            if (response.isSuccessful && response.body()?.success == true) {
                val comments = response.body()!!.data!!.map { it.toComment() }
                commentDao.deleteAllComments()
                commentDao.insertComments(comments)
                emit(comments)
            } else {
                emit(commentDao.getCommentsByNoteId(noteId).kotlinx.coroutines.flow.first())
            }
        } catch (e: Exception) {
            emit(commentDao.getCommentsByNoteId(noteId).kotlinx.coroutines.flow.first())
        }
    }

    suspend fun addComment(noteId: String, content: String): Result<Comment> {
        return try {
            val response = apiService.createComment(noteId, CommentRequest(content))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val comment = response.body()!!.data!!.toComment()
                commentDao.insertComment(comment)
                Result.success(comment)
            } else {
                Result.failure(Exception(response.body()?.message ?: "评论失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeComment(commentId: String, noteId: String) {
        try {
            val response = apiService.toggleCommentLike(commentId)
            if (response.isSuccessful) {
                val comments = commentDao.getCommentsByNoteId(noteId).kotlinx.coroutines.flow.first()
                val comment = comments.find { it.id == commentId } ?: return
                val isLiked = response.body()?.data?.isLiked ?: !comment.isLiked
                val updatedComment = comment.copy(
                    isLiked = isLiked,
                    likesCount = if (isLiked) comment.likesCount + 1 else comment.likesCount - 1
                )
                commentDao.updateComment(updatedComment)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteComment(commentId: String) {
        try {
            val response = apiService.deleteComment(commentId)
            if (response.isSuccessful) {
                commentDao.deleteComment(commentId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
