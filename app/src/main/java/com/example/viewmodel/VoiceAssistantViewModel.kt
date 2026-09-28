package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.gemini.GeminiClient
import com.example.data.model.ParsedIntent
import com.example.data.model.VoiceCommand
import com.example.data.preferences.SettingsManager
import com.example.intent.IntentExecutionResult
import com.example.intent.IntentExecutor
import com.example.speech.SpeechRecognizerHelper
import com.example.speech.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VoiceAssistantUiState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val isSpeaking: Boolean = false,
    val currentSpeechText: String = "",
    val lastParsedIntent: ParsedIntent? = null,
    val lastExecutionResult: IntentExecutionResult? = null,
    val audioLevel: Float = 0f,
    val isSettingsOpen: Boolean = false,
    val isManualInputOpen: Boolean = false,
    val apiKeyInput: String = "",
    val effectiveApiKey: String = "",
    val isUsingBuildConfigKey: Boolean = false,
    val apiKeyStatusMessage: String? = null,
    val isTestingApiKey: Boolean = false,
    val snackbarMessage: String? = null,
    val isVoiceFeedbackEnabled: Boolean = true,
    val speechLanguage: String = SettingsManager.LANG_HINDI
)

class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.voiceCommandDao()
    private val settingsManager = SettingsManager(application)
    private val geminiClient = GeminiClient()
    private val intentExecutor = IntentExecutor(application)

    val historyList: StateFlow<List<VoiceCommand>> = dao.getAllCommands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(
        VoiceAssistantUiState(
            apiKeyInput = settingsManager.customApiKey,
            effectiveApiKey = settingsManager.effectiveApiKey,
            isUsingBuildConfigKey = settingsManager.isUsingBuildConfigKey,
            isVoiceFeedbackEnabled = settingsManager.isVoiceFeedbackEnabled,
            speechLanguage = settingsManager.speechLanguage
        )
    )
    val uiState: StateFlow<VoiceAssistantUiState> = _uiState.asStateFlow()

    private val speechRecognizer = SpeechRecognizerHelper(
        context = application,
        onListeningStateChanged = { listening ->
            _uiState.value = _uiState.value.copy(
                isListening = listening,
                audioLevel = if (listening) _uiState.value.audioLevel else 0f
            )
        },
        onRmsChanged = { rms ->
            _uiState.value = _uiState.value.copy(audioLevel = rms)
        },
        onPartialResult = { partial ->
            _uiState.value = _uiState.value.copy(currentSpeechText = partial)
        },
        onFinalResult = { finalResult ->
            _uiState.value = _uiState.value.copy(
                currentSpeechText = finalResult,
                isListening = false,
                audioLevel = 0f
            )
            processPrompt(finalResult)
        },
        onError = { errorMsg ->
            _uiState.value = _uiState.value.copy(
                isListening = false,
                audioLevel = 0f,
                snackbarMessage = errorMsg
            )
        }
    )

    private val textToSpeech = TextToSpeechHelper(application)

    init {
        speechRecognizer.initialize()
        textToSpeech.initialize()
    }

    fun startListening() {
        if (!settingsManager.hasApiKey) {
            _uiState.value = _uiState.value.copy(
                isSettingsOpen = true,
                snackbarMessage = "Pehle Gemini API key daalein"
            )
            return
        }

        if (_uiState.value.isSpeaking) {
            textToSpeech.stop()
            _uiState.value = _uiState.value.copy(isSpeaking = false)
        }

        _uiState.value = _uiState.value.copy(
            currentSpeechText = "",
            isListening = true
        )
        speechRecognizer.startListening(settingsManager.speechLanguage)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        _uiState.value = _uiState.value.copy(isListening = false, audioLevel = 0f)
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun processPrompt(userSpokenText: String) {
        val trimmed = userSpokenText.trim()
        if (trimmed.isEmpty()) return

        val apiKey = settingsManager.effectiveApiKey
        if (apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isSettingsOpen = true,
                snackbarMessage = "Gemini API key darj karein"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                currentSpeechText = trimmed
            )

            val parseResult = geminiClient.parseIntent(apiKey, trimmed)

            parseResult.onSuccess { parsedIntent ->
                // Execute intent on device
                val execution = intentExecutor.execute(parsedIntent)

                // Save command in history database
                val command = VoiceCommand(
                    userSpokenText = trimmed,
                    action = parsedIntent.action,
                    appName = parsedIntent.appName,
                    query = parsedIntent.query,
                    contact = parsedIntent.contact,
                    message = parsedIntent.message,
                    url = parsedIntent.url,
                    feedbackHindi = parsedIntent.feedbackHindi,
                    explanation = parsedIntent.explanation,
                    rawJson = parsedIntent.toString(),
                    executionStatus = execution.status
                )
                dao.insertCommand(command)

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    lastParsedIntent = parsedIntent,
                    lastExecutionResult = execution
                )

                // Voice feedback
                if (settingsManager.isVoiceFeedbackEnabled && parsedIntent.feedbackHindi.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(isSpeaking = true)
                    textToSpeech.speak(parsedIntent.feedbackHindi) {
                        _uiState.value = _uiState.value.copy(isSpeaking = false)
                    }
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    snackbarMessage = "Error: ${error.localizedMessage ?: "Command samajh nahi aayi"}"
                )
            }
        }
    }

    fun reExecuteCommand(command: VoiceCommand) {
        val parsedIntent = ParsedIntent(
            action = command.action,
            appName = command.appName,
            query = command.query,
            contact = command.contact,
            message = command.message,
            url = command.url,
            feedbackHindi = command.feedbackHindi,
            explanation = command.explanation
        )
        val result = intentExecutor.execute(parsedIntent)
        _uiState.value = _uiState.value.copy(
            lastParsedIntent = parsedIntent,
            lastExecutionResult = result,
            snackbarMessage = "Command executed: ${result.summary}"
        )
        if (settingsManager.isVoiceFeedbackEnabled && parsedIntent.feedbackHindi.isNotBlank()) {
            _uiState.value = _uiState.value.copy(isSpeaking = true)
            textToSpeech.speak(parsedIntent.feedbackHindi) {
                _uiState.value = _uiState.value.copy(isSpeaking = false)
            }
        }
    }

    fun deleteCommand(command: VoiceCommand) {
        viewModelScope.launch {
            dao.deleteCommand(command)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearAll()
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "History saaf kar di gayi"
            )
        }
    }

    fun onApiKeyInputChange(input: String) {
        _uiState.value = _uiState.value.copy(apiKeyInput = input)
    }

    fun saveApiKey() {
        val key = _uiState.value.apiKeyInput.trim()
        settingsManager.customApiKey = key
        _uiState.value = _uiState.value.copy(
            effectiveApiKey = settingsManager.effectiveApiKey,
            isUsingBuildConfigKey = settingsManager.isUsingBuildConfigKey,
            apiKeyStatusMessage = if (key.isNotEmpty()) "API Key successfully saved!" else "API Key cleared.",
            snackbarMessage = if (key.isNotEmpty()) "API Key saved successfully" else "API Key removed"
        )
    }

    fun testApiKey() {
        val key = _uiState.value.apiKeyInput.ifBlank { settingsManager.effectiveApiKey }
        if (key.isBlank()) {
            _uiState.value = _uiState.value.copy(apiKeyStatusMessage = "Please enter an API Key first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingApiKey = true, apiKeyStatusMessage = "Connecting to Gemini...")
            val result = geminiClient.testApiKey(key)
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(
                    isTestingApiKey = false,
                    apiKeyStatusMessage = "✓ Success! $msg"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isTestingApiKey = false,
                    apiKeyStatusMessage = "✗ Failed: ${err.message}"
                )
            }
        }
    }

    fun setVoiceFeedback(enabled: Boolean) {
        settingsManager.isVoiceFeedbackEnabled = enabled
        _uiState.value = _uiState.value.copy(isVoiceFeedbackEnabled = enabled)
    }

    fun setSpeechLanguage(lang: String) {
        settingsManager.speechLanguage = lang
        _uiState.value = _uiState.value.copy(speechLanguage = lang)
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(
            isSettingsOpen = true,
            apiKeyInput = settingsManager.customApiKey,
            apiKeyStatusMessage = null
        )
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(isSettingsOpen = false)
    }

    fun openManualInput() {
        _uiState.value = _uiState.value.copy(isManualInputOpen = true)
    }

    fun closeManualInput() {
        _uiState.value = _uiState.value.copy(isManualInputOpen = false)
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        textToSpeech.destroy()
    }
}
