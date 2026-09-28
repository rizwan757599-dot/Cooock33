package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ParsedIntent
import com.example.intent.IntentExecutor
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Awaz AI", appName)
    }

    @Test
    fun `intent executor parses and handles actions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val executor = IntentExecutor(context)
        val intent = ParsedIntent(
            action = "speak",
            feedbackHindi = "Namaste, main aapki madad kar sakta hoon",
            explanation = "Greeted user"
        )
        val result = executor.execute(intent)
        assertEquals(true, result.success)
        assertEquals("INFO", result.status)
        assertEquals("Namaste, main aapki madad kar sakta hoon", result.feedbackMessage)
    }

    @Test
    fun `parse json intent structure`() {
        val jsonStr = """
            {
                "action": "search",
                "app_name": "youtube",
                "query": "Arijit Singh songs",
                "feedback_hindi": "YouTube par Arijit Singh ke gaane chala raha hoon",
                "explanation": "Searching YouTube"
            }
        """.trimIndent()
        val json = JSONObject(jsonStr)
        val parsed = ParsedIntent(
            action = json.getString("action"),
            appName = json.getString("app_name"),
            query = json.getString("query"),
            feedbackHindi = json.getString("feedback_hindi"),
            explanation = json.getString("explanation")
        )
        assertEquals("search", parsed.action)
        assertEquals("youtube", parsed.appName)
        assertEquals("Arijit Singh songs", parsed.query)
    }
}
