package com.nlinterface.utility

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * LLMAppConnector handles communication with the LLM API
 * for processing voice commands and getting appropriate action labels.
 */
class LLMAppConnector {
    private val TAG = "LLMAppConnector"
    private val API_URL = "http://192.168.50.21:8001"
    private val USERNAME = "johndoe"
    private val PASSWORD = "secret"

    // Create a client with custom timeout settings
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Authenticates with the LLM API and returns an access token.
     *
     * @return Authentication token string
     * @throws Exception if authentication fails
     */
    suspend fun authenticate(): String {
        return withContext(Dispatchers.IO) {
            try {
                val formBody = FormBody.Builder()
                    .add("username", USERNAME)
                    .add("password", PASSWORD)
                    .build()

                val request = Request.Builder()
                    .url("$API_URL/token")
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    throw IOException("Authentication failed: ${response.code}")
                }

                // Parse JSON response to get token
                val jsonObject = JSONObject(responseBody)
                jsonObject.getString("access_token")
            } catch (e: Exception) {
                Log.e(TAG, "Authentication failed: ${e.message}", e)
                throw Exception("Authentication failed: ${e.message}")
            }
        }
    }

    /**
     * Sends a command to the LLM API and returns the raw response.
     *
     * @param command The user's voice command
     * @param token The authentication token
     * @return Raw JSON response from the API
     * @throws Exception if the API request fails
     */
    suspend fun sendCommandToLLM(command: String, token: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val jsonObject = JSONObject()
                val inputObject = JSONObject()
                inputObject.put("messages", command)
                jsonObject.put("input", inputObject)

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = RequestBody.create(mediaType, jsonObject.toString())

                val request = Request.Builder()
                    .url("$API_URL/llm-app-interface/invoke")
                    .addHeader("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    throw IOException("API request failed: ${response.code}")
                }

                responseBody
            } catch (e: Exception) {
                Log.e(TAG, "API request failed: ${e.message}", e)
                throw Exception("API request failed: ${e.message}")
            }
        }
    }

    /**
     * Parses the LLM response to extract labels and additional data requirements.
     *
     * @param responseJson Raw JSON response from the API
     * @return Pair of the label and whether additional data is required
     */
    fun parseResponse(responseJson: String): Pair<String?, Boolean> {
        try {
            Log.d(TAG, "Parsing response: $responseJson")

            // Parse the API response - first get the output object
            val jsonObject = JSONObject(responseJson)
            val outputObject = jsonObject.getJSONObject("output")
            val messages = outputObject.getJSONArray("messages")

            // Get the last message (which should be the AI response)
            if (messages.length() > 0) {
                val lastIndex = messages.length() - 1
                val message = messages.getJSONObject(lastIndex)

                // Check if it's an AI message
                val type = message.optString("type")
                if (type == "ai") {
                    val content = message.getString("content")
                    Log.d(TAG, "AI content: $content")

                    // Extract label and additional-data-required flag
                    val labelPattern = """label=([^;]+)""".toRegex()
                    val additionalDataPattern = """additional-data-required=(True|False)""".toRegex()

                    val labelMatch = labelPattern.find(content)
                    val additionalDataMatch = additionalDataPattern.find(content)

                    if (labelMatch != null) {
                        // Extract only the value part (without "label=")
                        val label = labelMatch.groupValues[1].trim()
                        val needsAdditionalData = additionalDataMatch?.groupValues?.get(1) == "True"
                        return Pair(label, needsAdditionalData)
                    }

                    // If no label pattern was found, return the content as is
                    return Pair(content, false)
                }
            }

            return Pair(null, false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response: ${e.message}", e)
            Log.e(TAG, "Response JSON: $responseJson")
            e.printStackTrace()
            return Pair(null, false)
        }
    }

    companion object {
        private var instance: LLMAppConnector? = null

        @get:Synchronized
        val getInstance: LLMAppConnector
            get() {
                if (instance == null) {
                    instance = LLMAppConnector()
                }
                return instance!!
            }
    }
}