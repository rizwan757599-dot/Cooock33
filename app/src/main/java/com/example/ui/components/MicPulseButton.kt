package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.MicGradientEnd
import com.example.ui.theme.MicGradientStart
import com.example.ui.theme.VioletNeon

@Composable
fun MicPulseButton(
    isListening: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    audioLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    // Idle breathing scale
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    // Animated ripple rings when listening
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_ripple"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_alpha"
    )

    // Dynamic scale responding to speech volume
    val dynamicAudioScale = remember { Animatable(1f) }
    LaunchedEffect(audioLevel) {
        val target = 1.0f + (audioLevel * 0.25f)
        dynamicAudioScale.animateTo(target, tween(80))
    }

    val buttonScale = when {
        isProcessing -> 0.96f
        isListening -> dynamicAudioScale.value
        else -> idleScale
    }

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring 2 (Listening)
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(rippleScale)
                    .clip(CircleShape)
                    .background(CyanGlow.copy(alpha = rippleAlpha * 0.4f))
            )
            // Inner pulsing ring 1
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .scale(1f + (rippleScale - 1f) * 0.6f)
                    .clip(CircleShape)
                    .background(VioletNeon.copy(alpha = rippleAlpha * 0.6f))
            )
        }

        // Speaking pulse
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(idleScale * 1.15f)
                    .clip(CircleShape)
                    .background(VioletNeon.copy(alpha = 0.35f))
            )
        }

        // Processing ring
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(86.dp),
                color = CyanNeon,
                strokeWidth = 3.dp
            )
        }

        // Main core button
        val gradientBrush = when {
            isListening -> Brush.linearGradient(
                colors = listOf(CyanNeon, Color(0xFF0284C7))
            )
            isProcessing -> Brush.linearGradient(
                colors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
            )
            isSpeaking -> Brush.linearGradient(
                colors = listOf(VioletNeon, Color(0xFF4F46E5))
            )
            else -> Brush.linearGradient(
                colors = listOf(MicGradientStart, MicGradientEnd)
            )
        }

        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(buttonScale)
                .shadow(
                    elevation = if (isListening) 16.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = CyanNeon,
                    spotColor = VioletNeon
                )
                .clip(CircleShape)
                .background(gradientBrush)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onClick
                )
                .testTag("mic_button"),
            contentAlignment = Alignment.Center
        ) {
            when {
                isProcessing -> {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Gemini Processing",
                        tint = CyanNeon,
                        modifier = Modifier.size(36.dp)
                    )
                }
                isSpeaking -> {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Speaking",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                isListening -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Listening",
                        tint = Color(0xFF031427),
                        modifier = Modifier.size(38.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start Speaking",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
