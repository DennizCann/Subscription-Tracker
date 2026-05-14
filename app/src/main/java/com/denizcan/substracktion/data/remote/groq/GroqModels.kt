package com.denizcan.substracktion.data.remote.groq

import com.google.gson.annotations.SerializedName

data class GroqChatRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<GroqChatMessage>,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("max_tokens") val maxTokens: Int,
    @SerializedName("response_format") val responseFormat: GroqResponseFormat? = null
)

data class GroqResponseFormat(
    @SerializedName("type") val type: String = "json_object"
)

data class GroqChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqChatResponse(
    @SerializedName("choices") val choices: List<GroqChoice>?
)

data class GroqChoice(
    @SerializedName("message") val message: GroqChatMessage?
)
