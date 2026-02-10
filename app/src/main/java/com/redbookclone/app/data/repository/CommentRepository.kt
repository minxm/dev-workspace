package com.redbookclone.app.data.repository

import com.redbookclone.app.data.local.CommentDao
import com.redbookclone.app.data.local.UserPreferences
import com.redbookclone.app.data.model.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val commentDao: CommentDao,
    private val userPreferences: UserPreferences
) {
    
    fun getCommentsByNoteId(noteId: String): Flow<List<Comment>> = 
        commentDao.getCommentsByNoteId(noteId)

    suspend fun addComment(noteId: String, content: String): Result<Comment> {
        return try {
            val userId = userPreferences.userId.first() ?: throw IllegalStateException("用户未登录")
            val username = userPreferences.username.first() ?: "未知用户"
            
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                noteId = noteId,
                userId = userId,
                username = username,
                userAvatar = "https://picsum.photos/200?random=$userId",
                content = content
            )
            
            commentDao.insertComment(comment)
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeComment(commentId: String, noteId: String) {
        val comments = commentDao.getCommentsByNoteId(noteId).first()
        val comment = comments.find { it.id == commentId } ?: return
        
        val updatedComment = comment.copy(
            isLiked = !comment.isLiked,
            likesCount = if (comment.isLiked) comment.likesCount - 1 else comment.likesCount + 1
        )
        commentDao.updateComment(updatedComment)
    }

    suspend fun deleteComment(commentId: String) {
        commentDao.deleteComment(commentId)
    }
}
