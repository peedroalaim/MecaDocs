package com.softworld.mecadocs.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelo genérico/neutro de requisição de prompt de IA (Adapter Pattern).
 * Isola a ViewModel e a UI das diferenças técnicas de cada provedor de IA (OpenAI, Gemini, Groq, etc.).
 */
@JsonClass(generateAdapter = true)
data class AiPromptRequest(
    @Json(name = "componentName")
    val componentName: String,

    @Json(name = "language")
    val language: String = "pt-BR"
)
