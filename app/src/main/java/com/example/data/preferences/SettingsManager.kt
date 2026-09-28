package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "awaz_ai_settings"
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val KEY_VOICE_FEEDBACK = "voice_feedback_enabled"
        private const val KEY_SPEECH_LANGUAGE = "speech_language"

        const val LANG_HINDI = "hi-IN"
        const val LANG_ENGLISH_IN = "en-IN"
        const val LANG_ENGLISH_US = "en-US"
    }

    var customApiKey: String
        get() = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_API_KEY, value.trim()).apply()

    val effectiveApiKey: String
        get() {
            val userKey = customApiKey.trim()
            if (userKey.isNotEmpty()) {
                return userKey
            }
            val buildConfigKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("")
            return if (buildConfigKey.isNotEmpty() && buildConfigKey != "MY_GEMINI_API_KEY") {
                buildConfigKey
            } else {
                ""
            }
        }

    val isUsingBuildConfigKey: Boolean
        get() = customApiKey.isBlank() && effectiveApiKey.isNotEmpty()

    val hasApiKey: Boolean
        get() = effectiveApiKey.isNotEmpty()

    var isVoiceFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_FEEDBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_FEEDBACK, value).apply()

    var speechLanguage: String
        get() = prefs.getString(KEY_SPEECH_LANGUAGE, LANG_HINDI) ?: LANG_HINDI
        set(value) = prefs.edit().putString(KEY_SPEECH_LANGUAGE, value).apply()
}
