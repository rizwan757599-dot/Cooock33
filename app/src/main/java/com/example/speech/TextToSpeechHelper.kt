package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(private val context: Context) {

    companion object {
        private const val TAG = "TextToSpeechHelper"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun initialize(onReady: () -> Unit = {}) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                val hindiLocale = Locale("hi", "IN")
                val result = tts?.setLanguage(hindiLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val englishIn = Locale("en", "IN")
                    tts?.setLanguage(englishIn)
                }
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.05f)
                onReady()
            } else {
                Log.e(TAG, "TTS Initialization failed")
            }
        }
    }

    fun speak(text: String, onDone: () -> Unit = {}) {
        if (!isInitialized || tts == null || text.isBlank()) {
            onDone()
            return
        }

        val utteranceId = "utterance_${System.currentTimeMillis()}"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                onDone()
            }

            override fun onError(utteranceId: String?) {
                onDone()
            }
        })

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        } finally {
            tts = null
            isInitialized = false
        }
    }
}
