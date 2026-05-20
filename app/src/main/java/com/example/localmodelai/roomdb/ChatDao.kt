package com.example.localmodelai.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatDao {
    @Insert
    suspend fun insertSession(session: ChatSession): Long

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM ChatSession ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestSession(): ChatSession?

    @Query("SELECT * FROM ChatMessageEntity WHERE session_id = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity>

    @Query("UPDATE ChatSession SET title = :title WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: Long, title: String)
}
