package com.redbookclone.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.redbookclone.app.data.model.Comment
import com.redbookclone.app.data.model.Note
import com.redbookclone.app.data.model.User

@Database(
    entities = [User::class, Note::class, Comment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun commentDao(): CommentDao
}
