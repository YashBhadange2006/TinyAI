package com.example.localmodelai.data.database

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

    @Query("SELECT * FROM ChatSession ORDER BY created_at DESC")
    suspend fun getAllSessions(): List<ChatSession>

    @Query("SELECT * FROM ChatSession WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): ChatSession?

    @Query("SELECT * FROM ChatMessageEntity WHERE session_id = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity>

    @Query("UPDATE ChatSession SET title = :title WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: Long, title: String)

    @Query("DELETE FROM ChatSession WHERE id = :sessionId")
    suspend fun  deleteSession(sessionId: Long)
}
