package com.example.intent

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import com.example.data.model.ParsedIntent

data class IntentExecutionResult(
    val success: Boolean,
    val status: String, // "SUCCESS", "FAILED", "NOT_INSTALLED", "INFO"
    val summary: String,
    val feedbackMessage: String
)

class IntentExecutor(private val context: Context) {

    companion object {
        private const val TAG = "IntentExecutor"

        val KNOWN_PACKAGES = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "spotify" to "com.spotify.music",
            "playstore" to "com.android.vending",
            "play store" to "com.android.vending",
            "gmail" to "com.google.android.gm",
            "calculator" to "com.google.android.calculator",
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android",
            "telegram" to "org.telegram.messenger"
        )
    }

    fun execute(parsed: ParsedIntent): IntentExecutionResult {
        return try {
            when (parsed.action.lowercase()) {
                "search" -> handleSearch(parsed)
                "open_app" -> handleOpenApp(parsed)
                "open_whatsapp" -> handleWhatsApp(parsed)
                "web_search" -> handleWebSearch(parsed)
                "navigate" -> handleNavigate(parsed)
                "dial" -> handleDial(parsed)
                "camera" -> handleCamera()
                "alarm" -> handleAlarm(parsed)
                "speak" -> IntentExecutionResult(
                    success = true,
                    status = "INFO",
                    summary = "Conversational response provided",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "Aapka sawal samajh liya gaya hai" }
                )
                else -> handleFallback(parsed)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Execution failed", e)
            IntentExecutionResult(
                success = false,
                status = "FAILED",
                summary = "Failed to launch action: ${e.message}",
                feedbackMessage = "Action poora nahi ho saka: ${e.localizedMessage}"
            )
        }
    }

    private fun handleSearch(parsed: ParsedIntent): IntentExecutionResult {
        val app = parsed.appName?.lowercase() ?: "youtube"
        val query = parsed.query.orEmpty()

        if (app.contains("youtube")) {
            val pkg = KNOWN_PACKAGES["youtube"]!!
            val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
                `package` = pkg
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (isIntentResolvable(searchIntent)) {
                context.startActivity(searchIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Searched YouTube for '$query'",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "YouTube par $query chala raha hoon" }
                )
            } else {
                // Browser fallback for YouTube
                val webUri = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Opened YouTube in browser for '$query'",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "YouTube par $query dhoondh raha hoon" }
                )
            }
        } else if (app.contains("spotify")) {
            val spotifyUri = Uri.parse("spotify:search:" + Uri.encode(query))
            val spotifyIntent = Intent(Intent.ACTION_VIEW, spotifyUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (isIntentResolvable(spotifyIntent)) {
                context.startActivity(spotifyIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Searched Spotify for '$query'",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "Spotify par $query play kar raha hoon" }
                )
            } else {
                val webUri = Uri.parse("https://open.spotify.com/search/" + Uri.encode(query))
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Opened Spotify web for '$query'",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "Spotify web par $query search kiya" }
                )
            }
        } else if (app.contains("playstore") || app.contains("play store")) {
            val marketUri = Uri.parse("market://search?q=" + Uri.encode(query))
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (isIntentResolvable(marketIntent)) {
                context.startActivity(marketIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Searched Play Store for '$query'",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "Play Store par search kar raha hoon" }
                )
            }
        }

        // Generic search fallback
        return handleWebSearch(parsed)
    }

    private fun handleOpenApp(parsed: ParsedIntent): IntentExecutionResult {
        val appName = parsed.appName?.lowercase()?.trim().orEmpty()

        when {
            appName.contains("camera") -> return handleCamera()
            appName.contains("dialer") || appName.contains("phone") || appName.contains("call") -> {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Opened Phone Dialer",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "Phone dialer khol raha hoon" }
                )
            }
            appName.contains("contact") -> {
                val contactsIntent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(contactsIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Opened Contacts",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "Contacts khol raha hoon" }
                )
            }
            appName.contains("setting") -> {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Opened Android Settings",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "Settings khol raha hoon" }
                )
            }
            appName.contains("calculator") -> {
                val calcIntent = Intent().apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (isIntentResolvable(calcIntent)) {
                    context.startActivity(calcIntent)
                    return IntentExecutionResult(
                        success = true,
                        status = "SUCCESS",
                        summary = "Opened Calculator",
                        feedbackMessage = parsed.feedbackHindi.ifEmpty { "Calculator khol raha hoon" }
                    )
                }
            }
            appName.contains("clock") || appName.contains("alarm") -> {
                val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (isIntentResolvable(clockIntent)) {
                    context.startActivity(clockIntent)
                    return IntentExecutionResult(
                        success = true,
                        status = "SUCCESS",
                        summary = "Opened Clock / Alarms",
                        feedbackMessage = parsed.feedbackHindi.ifEmpty { "Clock app khol raha hoon" }
                    )
                }
            }
        }

        // Check known packages
        val packageName = KNOWN_PACKAGES[appName] ?: findPackageByName(appName)

        if (packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return IntentExecutionResult(
                    success = true,
                    status = "SUCCESS",
                    summary = "Opened $appName",
                    feedbackMessage = parsed.feedbackHindi.ifEmpty { "$appName open kar raha hoon" }
                )
            }
        }

        // Web fallbacks for common apps if not installed
        if (appName.contains("youtube")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.youtube.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return IntentExecutionResult(
                success = true,
                status = "SUCCESS",
                summary = "Opened YouTube in browser",
                feedbackMessage = "YouTube browser mein khol raha hoon"
            )
        }

        if (appName.contains("whatsapp")) {
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.whatsapp")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (isIntentResolvable(playStoreIntent)) {
                context.startActivity(playStoreIntent)
            }
            return IntentExecutionResult(
                success = false,
                status = "NOT_INSTALLED",
                summary = "WhatsApp is not installed on this device",
                feedbackMessage = "WhatsApp is device par installed nahi hai"
            )
        }

        // Fallback: search in Google or Play Store
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + Uri.encode(appName))).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (isIntentResolvable(marketIntent)) {
            context.startActivity(marketIntent)
            return IntentExecutionResult(
                success = true,
                status = "INFO",
                summary = "Searching Play Store for $appName",
                feedbackMessage = "$appName Play Store par search kar raha hoon"
            )
        }

        return handleWebSearch(ParsedIntent(action = "web_search", query = appName, feedbackHindi = "$appName ke baare mein search kar raha hoon"))
    }

    private fun handleWhatsApp(parsed: ParsedIntent): IntentExecutionResult {
        val message = parsed.message ?: parsed.query ?: ""
        val contact = parsed.contact ?: ""

        val url = if (contact.isNotBlank() && contact.all { it.isDigit() || it == '+' }) {
            val cleanNumber = contact.replace("+", "").replace(" ", "").replace("-", "")
            "https://api.whatsapp.com/send?phone=$cleanNumber&text=" + Uri.encode(message)
        } else {
            "https://api.whatsapp.com/send?text=" + Uri.encode(message)
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            `package` = "com.whatsapp"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return if (isIntentResolvable(intent)) {
            context.startActivity(intent)
            IntentExecutionResult(
                success = true,
                status = "SUCCESS",
                summary = "Opened WhatsApp message draft",
                feedbackMessage = parsed.feedbackHindi.ifEmpty { "WhatsApp par message bhej raha hoon" }
            )
        } else {
            // General share intent fallback or browser link
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(sendIntent, "Share with").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
            IntentExecutionResult(
                success = true,
                status = "INFO",
                summary = "WhatsApp not directly available, opened share sheet",
                feedbackMessage = "Message share karne ke liye options open kar diye hain"
            )
        }
    }

    private fun handleWebSearch(parsed: ParsedIntent): IntentExecutionResult {
        val query = parsed.query ?: parsed.explanation.ifEmpty { "Google Search" }
        val webSearchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (isIntentResolvable(webSearchIntent)) {
            context.startActivity(webSearchIntent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }

        return IntentExecutionResult(
            success = true,
            status = "SUCCESS",
            summary = "Web search for '$query'",
            feedbackMessage = parsed.feedbackHindi.ifEmpty { "Google par search kar raha hoon" }
        )
    }

    private fun handleNavigate(parsed: ParsedIntent): IntentExecutionResult {
        val query = parsed.query.orEmpty()
        val mapsUri = Uri.parse("google.navigation:q=" + Uri.encode(query))
        val mapsIntent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
            `package` = "com.google.android.apps.maps"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (isIntentResolvable(mapsIntent)) {
            context.startActivity(mapsIntent)
            return IntentExecutionResult(
                success = true,
                status = "SUCCESS",
                summary = "Navigating to '$query' via Google Maps",
                feedbackMessage = parsed.feedbackHindi.ifEmpty { "$query ke liye Google Maps par rasta khol raha hoon" }
            )
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            return IntentExecutionResult(
                success = true,
                status = "SUCCESS",
                summary = "Opened Maps location '$query' in browser",
                feedbackMessage = parsed.feedbackHindi.ifEmpty { "Maps location open kar raha hoon" }
            )
        }
    }

    private fun handleDial(parsed: ParsedIntent): IntentExecutionResult {
        val numberOrQuery = parsed.contact ?: parsed.query.orEmpty()
        val dialUri = Uri.parse("tel:" + Uri.encode(numberOrQuery))
        val dialIntent = Intent(Intent.ACTION_DIAL, dialUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dialIntent)
        return IntentExecutionResult(
            success = true,
            status = "SUCCESS",
            summary = "Opened dialer with '$numberOrQuery'",
            feedbackMessage = parsed.feedbackHindi.ifEmpty { "Call dial karne ke liye number open kiya" }
        )
    }

    private fun handleCamera(): IntentExecutionResult {
        val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (isIntentResolvable(cameraIntent)) {
            context.startActivity(cameraIntent)
            return IntentExecutionResult(
                success = true,
                status = "SUCCESS",
                summary = "Opened Camera",
                feedbackMessage = "Camera open kar raha hoon"
            )
        } else {
            return IntentExecutionResult(
                success = false,
                status = "FAILED",
                summary = "Camera app not found",
                feedbackMessage = "Camera app nahi mila"
            )
        }
    }

    private fun handleAlarm(parsed: ParsedIntent): IntentExecutionResult {
        val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (isIntentResolvable(alarmIntent)) {
            context.startActivity(alarmIntent)
            return IntentExecutionResult(
                success = true,
                status = "SUCCESS",
                summary = "Opened Alarms",
                feedbackMessage = parsed.feedbackHindi.ifEmpty { "Alarm clock khol raha hoon" }
            )
        }
        return IntentExecutionResult(
            success = false,
            status = "FAILED",
            summary = "Clock app not accessible",
            feedbackMessage = "Clock app open nahi ho saka"
        )
    }

    private fun handleFallback(parsed: ParsedIntent): IntentExecutionResult {
        if (!parsed.query.isNullOrBlank()) {
            return handleWebSearch(parsed)
        }
        return IntentExecutionResult(
            success = true,
            status = "INFO",
            summary = parsed.explanation.ifEmpty { "Command processed" },
            feedbackMessage = parsed.feedbackHindi.ifEmpty { "Aapka command samajh liya gaya hai" }
        )
    }

    private fun isIntentResolvable(intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }

    private fun findPackageByName(appName: String): String? {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(0)
        val cleanName = appName.lowercase()
        return packages.firstOrNull { appInfo ->
            val label = pm.getApplicationLabel(appInfo).toString().lowercase()
            label.contains(cleanName) || cleanName.contains(label)
        }?.packageName
    }
}
