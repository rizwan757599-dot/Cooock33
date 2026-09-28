package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_commands")
data class VoiceCommand(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userSpokenText: String,
    val action: String, // "open_app", "search", "open_whatsapp", "web_search", "navigate", "dial", "camera", "speak"
    val appName: String? = null,
    val query: String? = null,
    val contact: String? = null,
    val message: String? = null,
    val url: String? = null,
    val feedbackHindi: String = "",
    val explanation: String = "",
    val rawJson: String = "",
    val executionStatus: String = "SUCCESS" // "SUCCESS", "FAILED", "NOT_INSTALLED", "INFO"
)
