package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.CommandHistoryItem
import com.example.ui.components.ManualInputDialog
import com.example.ui.components.MicPulseButton
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SuggestionChips
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VioletNeon
import com.example.viewmodel.VoiceAssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(
    viewModel: VoiceAssistantViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.historyList.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            // Permission denied
        }
    }

    // Function to check permission and trigger listening
    val onMicClicked = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.toggleListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Show snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(CyanNeon, VioletNeon)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Logo",
                                tint = Color(0xFF031427),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Awaz AI",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyanNeon.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Hindi & Hinglish",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyanNeon
                                    )
                                }
                            }
                            Text(
                                text = "Voice Assistant • Gemini 3.5 Flash",
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openManualInput() },
                        modifier = Modifier.testTag("open_manual_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Type Command",
                            tint = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.openSettings() },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (uiState.effectiveApiKey.isNotEmpty()) CyanNeon else VioletNeon
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content area: Suggestions or History List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (history.isEmpty()) {
                    // Empty state with guide & suggestions
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = DarkSurfaceElevated
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(CyanNeon.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = "AI Assistant",
                                            tint = CyanNeon,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Namaste! Main hoon Awaz AI",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Aap Hindi ya Hinglish mein aam bhasha mein bolein. Gemini AI aapki command samajh kar YouTube, WhatsApp, Maps ya Phone jaisi apps kholega.",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        item {
                            SuggestionChips(
                                onSelectPrompt = { selectedPrompt ->
                                    viewModel.processPrompt(selectedPrompt)
                                }
                            )
                        }
                    }
                } else {
                    // Command History List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Conversation & Actions History (${history.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Tap ▶ to re-run",
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                            }
                        }

                        items(
                            items = history,
                            key = { it.id }
                        ) { command ->
                            CommandHistoryItem(
                                command = command,
                                onReExecute = { viewModel.reExecuteCommand(command) },
                                onDelete = { viewModel.deleteCommand(command) }
                            )
                        }
                    }
                }
            }

            // Bottom Listening & Voice Action Dock
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Live Transcription & Assistant feedback area
                    AnimatedVisibility(
                        visible = uiState.currentSpeechText.isNotBlank() || uiState.isListening || uiState.isProcessing,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("speech_transcription_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DarkSurfaceElevated
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (uiState.isListening) CyanNeon.copy(alpha = 0.5f) else DarkSurfaceBorder
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isListening) EmeraldSuccess else CyanNeon)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when {
                                            uiState.isListening -> "Sun raha hoon (Listening)..."
                                            uiState.isProcessing -> "Gemini samajh raha hai..."
                                            uiState.isSpeaking -> "Bol raha hai..."
                                            else -> "Command"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (uiState.isListening) EmeraldSuccess else CyanNeon
                                    )
                                    Text(
                                        text = if (uiState.currentSpeechText.isNotBlank()) uiState.currentSpeechText else "Kuch bolein...",
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Main Big Pulsing Microphone Button
                    MicPulseButton(
                        isListening = uiState.isListening,
                        isProcessing = uiState.isProcessing,
                        isSpeaking = uiState.isSpeaking,
                        audioLevel = uiState.audioLevel,
                        onClick = onMicClicked
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status and Helper Subtitle
                    Text(
                        text = when {
                            uiState.isListening -> "Boliye, sun raha hoon... (Tap mic to stop)"
                            uiState.isProcessing -> "Gemini intent parse kar raha hai..."
                            uiState.isSpeaking -> "Assistant bol kar bata raha hai..."
                            uiState.effectiveApiKey.isBlank() -> "Gemini API key enter karein (Top right Settings)"
                            else -> "Mic dabayein aur bolein: 'YouTube pe gaana chalao'"
                        },
                        fontSize = 12.sp,
                        color = if (uiState.isListening) CyanNeon else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Settings Dialog
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            apiKeyInput = uiState.apiKeyInput,
            effectiveApiKey = uiState.effectiveApiKey,
            isUsingBuildConfigKey = uiState.isUsingBuildConfigKey,
            isTestingApiKey = uiState.isTestingApiKey,
            apiKeyStatusMessage = uiState.apiKeyStatusMessage,
            isVoiceFeedbackEnabled = uiState.isVoiceFeedbackEnabled,
            speechLanguage = uiState.speechLanguage,
            onApiKeyChange = { viewModel.onApiKeyInputChange(it) },
            onSaveApiKey = { viewModel.saveApiKey() },
            onTestApiKey = { viewModel.testApiKey() },
            onVoiceFeedbackToggle = { viewModel.setVoiceFeedback(it) },
            onLanguageChange = { viewModel.setSpeechLanguage(it) },
            onClearHistory = { viewModel.clearHistory() },
            onDismiss = { viewModel.closeSettings() }
        )
    }

    // Manual Input Dialog
    if (uiState.isManualInputOpen) {
        ManualInputDialog(
            onSubmit = { text ->
                viewModel.processPrompt(text)
            },
            onDismiss = { viewModel.closeManualInput() }
        )
    }
}
