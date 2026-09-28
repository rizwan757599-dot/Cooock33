package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletNeon

data class PromptSuggestion(
    val emoji: String,
    val hindiText: String,
    val category: String
)

val DEFAULT_SUGGESTIONS = listOf(
    PromptSuggestion("▶️", "YouTube pe romantic songs chalao", "YouTube"),
    PromptSuggestion("💬", "WhatsApp kholo", "WhatsApp"),
    PromptSuggestion("📱", "WhatsApp pe Papa ko message bhejo", "Message"),
    PromptSuggestion("🏛️", "Taj Mahal kahan hai?", "AI Q&A"),
    PromptSuggestion("🗺️", "Delhi se Agra ka rasta dikhao", "Maps"),
    PromptSuggestion("📸", "Camera open karo selfie lena hai", "Camera"),
    PromptSuggestion("🔍", "Google pe search karo cricket score", "Search"),
    PromptSuggestion("⚙️", "Settings kholo", "System")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestionChips(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Suggestions",
                tint = CyanNeon,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Try bol kar dekhein (Hindi / Hinglish):",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DEFAULT_SUGGESTIONS.forEachIndexed { index, suggestion ->
                Card(
                    modifier = Modifier
                        .clickable { onSelectPrompt(suggestion.hindiText) }
                        .testTag("suggestion_chip_$index"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurfaceElevated
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = suggestion.emoji, fontSize = 14.sp)
                        Text(
                            text = suggestion.hindiText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
