package com.redbookclone.app.data.repository

import com.redbookclone.app.data.local.NoteDao
import com.redbookclone.app.data.local.UserPreferences
import com.redbookclone.app.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val userPreferences: UserPreferences
) {
    
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getNoteById(noteId: String): Flow<Note?> = noteDao.getNoteById(noteId)

    fun getNotesByUserId(userId: String): Flow<List<Note>> = noteDao.getNotesByUserId(userId)

    suspend fun createNote(
        title: String,
        content: String,
        images: List<String>,
        location: String = "",
        topics: List<String> = emptyList()
    ): Result<Note> {
        return try {
            val userId = userPreferences.userId.first() ?: throw IllegalStateException("用户未登录")
            val username = userPreferences.username.first() ?: "未知用户"
            
            val note = Note(
                id = UUID.randomUUID().toString(),
                userId = userId,
                username = username,
                userAvatar = "https://picsum.photos/200?random=$userId",
                title = title,
                content = content,
                images = images,
                coverImage = images.firstOrNull() ?: "",
                location = location,
                topics = topics
            )
            
            noteDao.insertNote(note)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeNote(noteId: String) {
        val note = noteDao.getNoteById(noteId).first() ?: return
        val updatedNote = note.copy(
            isLiked = !note.isLiked,
            likesCount = if (note.isLiked) note.likesCount - 1 else note.likesCount + 1
        )
        noteDao.updateNote(updatedNote)
    }

    suspend fun collectNote(noteId: String) {
        val note = noteDao.getNoteById(noteId).first() ?: return
        val updatedNote = note.copy(
            isCollected = !note.isCollected,
            collectsCount = if (note.isCollected) note.collectsCount - 1 else note.collectsCount + 1
        )
        noteDao.updateNote(updatedNote)
    }

    suspend fun deleteNote(noteId: String) {
        noteDao.deleteNote(noteId)
    }

    suspend fun initializeMockData() {
        // 创建一些模拟数据
        val mockNotes = listOf(
            Note(
                id = UUID.randomUUID().toString(),
                userId = "mock_user_1",
                username = "小红薯1",
                userAvatar = "https://picsum.photos/200?random=1",
                title = "分享一个超美的旅行地",
                content = "这个地方真的太美了！强烈推荐大家来打卡📸",
                images = listOf(
                    "https://picsum.photos/400/600?random=1",
                    "https://picsum.photos/400/600?random=2"
                ),
                coverImage = "https://picsum.photos/400/600?random=1",
                likesCount = 1234,
                commentsCount = 56,
                collectsCount = 789,
                topics = listOf("旅行", "打卡"),
                location = "丽江古城"
            ),
            Note(
                id = UUID.randomUUID().toString(),
                userId = "mock_user_2",
                username = "美妆达人",
                userAvatar = "https://picsum.photos/200?random=2",
                title = "新手化妆教程",
                content = "超详细的新手化妆步骤，手把手教你画出精致妆容✨",
                images = listOf(
                    "https://picsum.photos/400/600?random=3"
                ),
                coverImage = "https://picsum.photos/400/600?random=3",
                likesCount = 2345,
                commentsCount = 123,
                collectsCount = 1234,
                topics = listOf("美妆", "教程")
            ),
            Note(
                id = UUID.randomUUID().toString(),
                userId = "mock_user_3",
                username = "美食探店",
                userAvatar = "https://picsum.photos/200?random=3",
                title = "探店｜这家餐厅太好吃了",
                content = "今天去了一家超好吃的餐厅，环境也特别棒！强烈推荐🍜",
                images = listOf(
                    "https://picsum.photos/400/600?random=4",
                    "https://picsum.photos/400/600?random=5",
                    "https://picsum.photos/400/600?random=6"
                ),
                coverImage = "https://picsum.photos/400/600?random=4",
                likesCount = 3456,
                commentsCount = 234,
                collectsCount = 2345,
                topics = listOf("美食", "探店"),
                location = "上海"
            ),
            Note(
                id = UUID.randomUUID().toString(),
                userId = "mock_user_4",
                username = "穿搭博主",
                userAvatar = "https://picsum.photos/200?random=4",
                title = "秋冬穿搭分享",
                content = "分享几套秋冬穿搭，简单又时尚👗",
                images = listOf(
                    "https://picsum.photos/400/600?random=7"
                ),
                coverImage = "https://picsum.photos/400/600?random=7",
                likesCount = 4567,
                commentsCount = 345,
                collectsCount = 3456,
                topics = listOf("穿搭", "时尚")
            ),
            Note(
                id = UUID.randomUUID().toString(),
                userId = "mock_user_5",
                username = "健身教练",
                userAvatar = "https://picsum.photos/200?random=5",
                title = "居家健身计划",
                content = "不用去健身房也能练出好身材！分享我的居家健身计划💪",
                images = listOf(
                    "https://picsum.photos/400/600?random=8",
                    "https://picsum.photos/400/600?random=9"
                ),
                coverImage = "https://picsum.photos/400/600?random=8",
                likesCount = 5678,
                commentsCount = 456,
                collectsCount = 4567,
                topics = listOf("健身", "运动")
            )
        )
        
        noteDao.insertNotes(mockNotes)
    }
}
