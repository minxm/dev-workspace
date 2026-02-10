package com.redbookclone.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redbookclone.app.data.model.Note
import com.redbookclone.app.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    private val _publishState = MutableStateFlow<PublishState>(PublishState.Initial)
    val publishState: StateFlow<PublishState> = _publishState.asStateFlow()

    init {
        loadNotes()
        initializeMockData()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes().collect { notesList ->
                _notes.value = notesList
            }
        }
    }

    private fun initializeMockData() {
        viewModelScope.launch {
            if (_notes.value.isEmpty()) {
                noteRepository.initializeMockData()
            }
        }
    }

    fun loadNoteById(noteId: String) {
        viewModelScope.launch {
            noteRepository.getNoteById(noteId).collect { note ->
                _selectedNote.value = note
            }
        }
    }

    fun createNote(
        title: String,
        content: String,
        images: List<String>,
        location: String = "",
        topics: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _publishState.value = PublishState.Loading
            val result = noteRepository.createNote(title, content, images, location, topics)
            _publishState.value = if (result.isSuccess) {
                PublishState.Success
            } else {
                PublishState.Error(result.exceptionOrNull()?.message ?: "发布失败")
            }
        }
    }

    fun likeNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.likeNote(noteId)
        }
    }

    fun collectNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.collectNote(noteId)
        }
    }

    fun resetPublishState() {
        _publishState.value = PublishState.Initial
    }
}

sealed class PublishState {
    object Initial : PublishState()
    object Loading : PublishState()
    object Success : PublishState()
    data class Error(val message: String) : PublishState()
}
