package com.mojealterego.newgpt.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val isPending: Boolean
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observe(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("UPDATE messages SET content = :content, isPending = :pending WHERE id = :id")
    suspend fun update(id: String, content: String, pending: Boolean)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clear(conversationId: String)
}
