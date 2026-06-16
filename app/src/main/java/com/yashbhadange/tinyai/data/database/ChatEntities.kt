package com.yashbhadange.tinyai.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "model_name") val modelName: String,
    @ColumnInfo(name = "system_prompt") val systemPrompt: String = ""
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "is_user") val isUser: Boolean,
    @ColumnInfo(name = "message_type") val messageType: String = "text",
    @ColumnInfo(name = "image_path") val imagePath: String? = null,
    @ColumnInfo(name = "image_name") val imageName: String? = null,
    @ColumnInfo(name = "thinking_text") val thinkingText: String = "",
    @ColumnInfo(name = "timestamp") val timestamp: Long
)
