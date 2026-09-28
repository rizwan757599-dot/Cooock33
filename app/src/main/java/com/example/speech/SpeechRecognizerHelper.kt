package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognizerHelper(
    private val context: Context,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    companion object {
        private const val TAG = "SpeechRecognizerHelper"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    var isListening: Boolean = false
        private set

    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "Speech recognition is not directly available on this device")
        }
    }

    fun startListening(languageCode: String = "hi-IN") {
        mainHandler.post {
            try {
                stopListening()

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US"))
                }

                speechRecognizer?.startListening(intent)
                isListening = true
                onListeningStateChanged(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening", e)
                isListening = false
                onListeningStateChanged(false)
                onError("Microphone start nahi ho saka: ${e.message}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                if (isListening) {
                    speechRecognizer?.stopListening()
                }
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recognizer", e)
            } finally {
                isListening = false
                onListeningStateChanged(false)
                onRmsChanged(0f)
            }
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB typically ranges from -2 to 10
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                onRmsChanged(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech")
                onListeningStateChanged(false)
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                onListeningStateChanged(false)
                onRmsChanged(0f)

                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record Audio permission chahiye"
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue. Internet check karein"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout. Dobara try karein"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Kuch sunai nahi diya. Kripya dobara bolein."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy hai. Ek second baad try karein."
                    SpeechRecognizer.ERROR_SERVER -> "Server error. Kripya dobara koshish karein."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Aapne kuch nahi bola. Mic dabakar dobara bolein."
                    else -> "Speech recognition error ($error)"
                }
                Log.w(TAG, "SpeechRecognizer error: $message ($error)")
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                onListeningStateChanged(false)
                onRmsChanged(0f)

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim().orEmpty()
                if (recognizedText.isNotEmpty()) {
                    onFinalResult(recognizedText)
                } else {
                    onError("Kuch sunai nahi diya. Dobara bolein.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull()?.trim().orEmpty()
                if (partialText.isNotEmpty()) {
                    onPartialResult(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun destroy() {
        stopListening()
    }
}
