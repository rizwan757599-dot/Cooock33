package com.example.data.model

data class ParsedIntent(
    val action: String, // "open_app", "search", "open_whatsapp", "web_search", "navigate", "dial", "camera", "alarm", "speak"
    val appName: String? = null,
    val query: String? = null,
    val contact: String? = null,
    val message: String? = null,
    val url: String? = null,
    val feedbackHindi: String = "",
    val explanation: String = ""
)
