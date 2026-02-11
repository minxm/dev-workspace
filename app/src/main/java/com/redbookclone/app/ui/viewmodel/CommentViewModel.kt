package com.redbookclone.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redbookclone.app.data.model.Comment
import com.redbookclone.app.data.repository.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _commentState = MutableStateFlow<CommentState>(CommentState.Initial)
    val commentState: StateFlow<CommentState> = _commentState.asStateFlow()

    fun loadComments(noteId: String) {
        viewModelScope.launch {
            commentRepository.getCommentsByNoteId(noteId).collect { commentsList ->
                _comments.value = commentsList
            }
        }
    }

    fun addComment(noteId: String, content: String) {
        viewModelScope.launch {
            _commentState.value = CommentState.Loading
            val result = commentRepository.addComment(noteId, content)
            _commentState.value = if (result.isSuccess) {
                CommentState.Success
            } else {
                CommentState.Error(result.exceptionOrNull()?.message ?: "评论失败")
            }
        }
    }

    fun likeComment(commentId: String, noteId: String) {
        viewModelScope.launch {
            commentRepository.likeComment(commentId, noteId)
        }
    }

    fun resetCommentState() {
        _commentState.value = CommentState.Initial
    }
}

sealed class CommentState {
    object Initial : CommentState()
    object Loading : CommentState()
    object Success : CommentState()
    data class Error(val message: String) : CommentState()
}
