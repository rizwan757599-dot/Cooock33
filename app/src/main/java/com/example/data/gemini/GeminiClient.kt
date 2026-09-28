package com.example.data.gemini

import android.util.Log
import com.example.data.model.ParsedIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient {

    companion object {
        private const val TAG = "GeminiClient"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

        private const val SYSTEM_PROMPT = """
You are an intelligent, high-accuracy Android Voice Assistant intent parser specifically built for Hindi, Hinglish, and Indian English voice commands.
The user speaks naturally in Hindi (Devanagari or Romanized/Hinglish) or English.

Your task is to understand their intent and respond ONLY with a raw, valid JSON object (no markdown formatting, no backticks, no extra text).

JSON Schema:
{
  "action": "<ACTION_NAME>",
  "app_name": "<APP_NAME_OR_EMPTY>",
  "query": "<SEARCH_OR_PARAM_OR_EMPTY>",
  "contact": "<CONTACT_NAME_OR_NUMBER_OR_EMPTY>",
  "message": "<MESSAGE_BODY_OR_EMPTY>",
  "url": "<WEB_URL_OR_EMPTY>",
  "feedback_hindi": "<Short spoken confirmation in polite Hindi/Hinglish>",
  "explanation": "<Brief English explanation of the action>"
}

Supported Actions:
1. "search":
   Search inside a specific app (YouTube, Spotify, Play Store, Google, etc.)
   - If user asks to play a song/video or search on YouTube:
     action="search", app_name="youtube", query="<song or topic>"
     feedback_hindi="YouTube par [song] chala raha hoon"
   - If user asks for music on Spotify:
     action="search", app_name="spotify", query="<song/artist>"
     feedback_hindi="Spotify par [song] dhoondh raha hoon"
   - If user asks to search on Play Store:
     action="search", app_name="playstore", query="<app name>"

2. "open_app":
   Open any installed Android app:
   - YouTube: action="open_app", app_name="youtube", feedback_hindi="YouTube khol raha hoon"
   - WhatsApp: action="open_app", app_name="whatsapp", feedback_hindi="WhatsApp khol raha hoon"
   - Chrome / Browser: action="open_app", app_name="chrome", feedback_hindi="Chrome browser khol raha hoon"
   - Maps: action="open_app", app_name="maps", feedback_hindi="Google Maps khol raha hoon"
   - Camera: action="open_app", app_name="camera", feedback_hindi="Camera khol raha hoon"
   - Calculator: action="open_app", app_name="calculator", feedback_hindi="Calculator khol raha hoon"
   - Settings: action="open_app", app_name="settings", feedback_hindi="Settings khol raha hoon"
   - Phone / Dialer: action="open_app", app_name="dialer", feedback_hindi="Phone dialer khol raha hoon"
   - Contacts: action="open_app", app_name="contacts", feedback_hindi="Contacts khol raha hoon"
   - Gallery / Photos: action="open_app", app_name="photos", feedback_hindi="Photos khol raha hoon"
   - Gmail: action="open_app", app_name="gmail", feedback_hindi="Gmail khol raha hoon"
   - Clock / Alarm: action="open_app", app_name="clock", feedback_hindi="Clock khol raha hoon"

3. "open_whatsapp":
   Send a message or open WhatsApp chat:
   - action="open_whatsapp", contact="<name or number>", message="<message content>"
   - feedback_hindi="WhatsApp par message bhej raha hoon"

4. "web_search":
   General Google web search when no specific app is mentioned or user asks to search on internet:
   - action="web_search", query="<search query>"
   - feedback_hindi="Google par search kar raha hoon"

5. "navigate":
   Directions or location on Google Maps:
   - action="navigate", query="<destination or location, e.g. Airport, Taj Mahal, petrol pump>"
   - feedback_hindi="Maps par rasta dikha raha hoon"

6. "dial":
   Make a phone call or open dialer:
   - action="dial", contact="<phone number or contact name>"
   - feedback_hindi="Call dial kar raha hoon"

7. "camera":
   Take photo or open camera:
   - action="camera", app_name="camera"
   - feedback_hindi="Camera khol raha hoon"

8. "alarm":
   Set alarm or timer:
   - action="alarm", query="<time>"
   - feedback_hindi="Alarm open kar raha hoon"

9. "speak":
   If user asks a direct general knowledge question, translation, math calculation, or greeting (e.g. "Taj Mahal kahan hai?", "Bharat ka pradhan mantri kaun hai?", "Aap kaun ho?", "Aaj ka weather kaisa hai?"):
   - action="speak"
   - feedback_hindi="<Direct concise answer in Hindi/Hinglish, 1-2 sentences>"
   - explanation="Answered conversational query"

Rule: ALWAYS output valid JSON strictly. Do not include markdown ticks.
"""
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun parseIntent(apiKey: String, userPrompt: String): Result<ParsedIntent> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured. Please set your API key in Settings."))
        }

        try {
            val url = "$BASE_URL?key=$apiKey"

            // Construct JSON request body
            val requestJson = JSONObject().apply {
                // systemInstruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", SYSTEM_PROMPT)
                        })
                    })
                })

                // contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPrompt)
                            })
                        })
                    })
                })

                // generationConfig
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody ?: "")
                    val errObj = errJson.optJSONObject("error")
                    errObj?.optString("message") ?: "HTTP error ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                Log.e(TAG, "Gemini API Error: $errorMsg")
                return@withContext Result.failure(Exception(errorMsg))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("Gemini returned empty response."))
            }

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            val text = firstPart?.optString("text") ?: ""

            if (text.isBlank()) {
                return@withContext Result.failure(Exception("No content generated by Gemini."))
            }

            // Clean any potential markdown code fences
            val cleanedText = text
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedJson = JSONObject(cleanedText)
            val parsedIntent = ParsedIntent(
                action = parsedJson.optString("action", "speak"),
                appName = parsedJson.optString("app_name").takeIf { it.isNotBlank() },
                query = parsedJson.optString("query").takeIf { it.isNotBlank() },
                contact = parsedJson.optString("contact").takeIf { it.isNotBlank() },
                message = parsedJson.optString("message").takeIf { it.isNotBlank() },
                url = parsedJson.optString("url").takeIf { it.isNotBlank() },
                feedbackHindi = parsedJson.optString("feedback_hindi", "Maine aapki command samajh li"),
                explanation = parsedJson.optString("explanation", "")
            )

            Result.success(parsedIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in parseIntent", e)
            Result.failure(e)
        }
    }

    suspend fun testApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API key cannot be empty."))
        }
        val testPrompt = "Test: YouTube kholo"
        parseIntent(apiKey, testPrompt).map {
            "API Key is valid! Model responded successfully: ${it.feedbackHindi}"
        }
    }
}
