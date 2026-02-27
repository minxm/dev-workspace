package com.redbookclone.app.data.repository

import android.content.Context
import android.net.Uri
import com.redbookclone.app.data.local.NoteDao
import com.redbookclone.app.data.model.Note
import com.redbookclone.app.data.remote.ApiService
import com.redbookclone.app.data.remote.toNote
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val apiService: ApiService,
    private val noteDao: NoteDao,
    @ApplicationContext private val context: Context
) {
    
    fun getAllNotes(): Flow<List<Note>> = flow {
        try {
            val response = apiService.getAllNotes()
            if (response.isSuccessful && response.body()?.success == true) {
                val notes = response.body()!!.data!!.notes.map { it.toNote() }
                noteDao.deleteAllNotes()
                noteDao.insertNotes(notes)
                emit(notes)
            } else {
                emit(noteDao.getAllNotes().kotlinx.coroutines.flow.first())
            }
        } catch (e: Exception) {
            emit(noteDao.getAllNotes().kotlinx.coroutines.flow.first())
        }
    }

    fun getNoteById(noteId: String): Flow<Note?> = flow {
        try {
            val response = apiService.getNoteById(noteId)
            if (response.isSuccessful && response.body()?.success == true) {
                val note = response.body()!!.data!!.toNote()
                noteDao.insertNote(note)
                emit(note)
            } else {
                emit(noteDao.getNoteById(noteId).kotlinx.coroutines.flow.first())
            }
        } catch (e: Exception) {
            emit(noteDao.getNoteById(noteId).kotlinx.coroutines.flow.first())
        }
    }

    fun getNotesByUserId(userId: String): Flow<List<Note>> = flow {
        try {
            val response = apiService.getUserNotes(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val notes = response.body()!!.data!!.map { it.toNote() }
                emit(notes)
            } else {
                emit(noteDao.getNotesByUserId(userId).kotlinx.coroutines.flow.first())
            }
        } catch (e: Exception) {
            emit(noteDao.getNotesByUserId(userId).kotlinx.coroutines.flow.first())
        }
    }

    suspend fun createNote(
        title: String,
        content: String,
        imageUris: List<Uri>,
        location: String = "",
        topics: List<String> = emptyList()
    ): Result<Note> {
        return try {
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val locationBody = if (location.isNotEmpty()) {
                location.toRequestBody("text/plain".toMediaTypeOrNull())
            } else null
            val topicsBody = if (topics.isNotEmpty()) {
                com.google.gson.Gson().toJson(topics).toRequestBody("application/json".toMediaTypeOrNull())
            } else null

            val imageParts = imageUris.mapIndexed { index, uri ->
                val file = createTempFileFromUri(uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, requestFile)
            }

            val response = apiService.createNote(
                title = titleBody,
                content = contentBody,
                location = locationBody,
                topics = topicsBody,
                images = imageParts
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val note = response.body()!!.data!!.toNote()
                noteDao.insertNote(note)
                Result.success(note)
            } else {
                Result.failure(Exception(response.body()?.message ?: "发布失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeNote(noteId: String) {
        try {
            val response = apiService.toggleLike(noteId)
            if (response.isSuccessful) {
                val note = noteDao.getNoteById(noteId).kotlinx.coroutines.flow.first() ?: return
                val isLiked = response.body()?.data?.isLiked ?: !note.isLiked
                val updatedNote = note.copy(
                    isLiked = isLiked,
                    likesCount = if (isLiked) note.likesCount + 1 else note.likesCount - 1
                )
                noteDao.updateNote(updatedNote)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun collectNote(noteId: String) {
        try {
            val response = apiService.toggleCollect(noteId)
            if (response.isSuccessful) {
                val note = noteDao.getNoteById(noteId).kotlinx.coroutines.flow.first() ?: return
                val isCollected = response.body()?.data?.isCollected ?: !note.isCollected
                val updatedNote = note.copy(
                    isCollected = isCollected,
                    collectsCount = if (isCollected) note.collectsCount + 1 else note.collectsCount - 1
                )
                noteDao.updateNote(updatedNote)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteNote(noteId: String) {
        try {
            val response = apiService.deleteNote(noteId)
            if (response.isSuccessful) {
                noteDao.deleteNote(noteId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun searchNotes(query: String): Flow<List<Note>> = flow {
        try {
            val response = apiService.searchNotes(query)
            if (response.isSuccessful && response.body()?.success == true) {
                val notes = response.body()!!.data!!.map { it.toNote() }
                emit(notes)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private fun createTempFileFromUri(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }
}
