package com.softworld.mecadocs.data.remote.pool

/**
 * Enum que define a estrutura de payload exigida por cada família de provedor de IA.
 */
enum class PayloadType {
    /**
     * Padrão OpenAI Chat Completions (ex: OpenAI, Groq, Cerebras, OpenRouter).
     * Endpoint: /v1/chat/completions
     */
    OPEN_AI,

    /**
     * Padrão Google Gemini REST (ex: Gemini 1.5 Flash / 2.0 Flash).
     * Endpoint: /v1beta/models/...:generateContent
     */
    GEMINI
}
