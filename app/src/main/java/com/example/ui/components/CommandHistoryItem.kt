package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VoiceCommand
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VioletNeon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommandHistoryItem(
    command: VoiceCommand,
    onReExecute: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = rememberTimeFormat(command.timestamp)
    val (actionIcon, actionLabel, actionColor) = getActionMeta(command.action, command.appName)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_${command.id}"),
        shape = RoundedCornerShape(16.dp),
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
                .padding(14.dp)
        ) {
            // Header: Action badge & Timestamp & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(actionColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = actionLabel,
                            tint = actionColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = actionColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_item_${command.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete item",
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User's Hindi/Hinglish spoken text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "“${command.userSpokenText}”",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Assistant Hindi response / execution detail
            if (command.feedbackHindi.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Assistant Response",
                        tint = CyanNeon,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = command.feedbackHindi,
                        fontSize = 13.sp,
                        color = CyanNeon,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Details & Run Again button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val detailText = when {
                    !command.query.isNullOrBlank() -> "Query: ${command.query}"
                    !command.appName.isNullOrBlank() -> "App: ${command.appName}"
                    command.explanation.isNotBlank() -> command.explanation
                    else -> "Executed"
                }

                Text(
                    text = detailText,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Re-run button
                if (command.action != "speak") {
                    IconButton(
                        onClick = onReExecute,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = 0.15f))
                            .testTag("rerun_item_${command.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run action again",
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberTimeFormat(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun getActionMeta(action: String, appName: String?): Triple<ImageVector, String, Color> {
    val app = appName?.lowercase().orEmpty()
    return when (action.lowercase()) {
        "search" -> {
            if (app.contains("youtube")) Triple(Icons.Default.PlayArrow, "YouTube", Color(0xFFFF4444))
            else if (app.contains("spotify")) Triple(Icons.Default.MusicNote, "Spotify", Color(0xFF1DB954))
            else Triple(Icons.Default.Search, "Search", CyanNeon)
        }
        "open_app" -> {
            if (app.contains("youtube")) Triple(Icons.Default.PlayArrow, "Open YouTube", Color(0xFFFF4444))
            else if (app.contains("whatsapp")) Triple(Icons.AutoMirrored.Filled.Chat, "Open WhatsApp", Color(0xFF25D366))
            else if (app.contains("camera")) Triple(Icons.Default.CameraAlt, "Camera", VioletNeon)
            else Triple(Icons.Default.Launch, "Open ${appName ?: "App"}", CyanNeon)
        }
        "open_whatsapp" -> Triple(Icons.AutoMirrored.Filled.Chat, "WhatsApp Message", Color(0xFF25D366))
        "web_search" -> Triple(Icons.Default.Search, "Google Search", Color(0xFF4285F4))
        "navigate" -> Triple(Icons.Default.Explore, "Navigation", Color(0xFF34A853))
        "dial" -> Triple(Icons.Default.Call, "Phone Call", EmeraldSuccess)
        "camera" -> Triple(Icons.Default.CameraAlt, "Camera", VioletNeon)
        "speak" -> Triple(Icons.Default.SmartToy, "Awaz AI", VioletNeon)
        else -> Triple(Icons.Default.Launch, "Action", CyanNeon)
    }
}
