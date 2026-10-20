package com.softworld.mecadocs.data.remote.adapter

import com.softworld.mecadocs.data.model.Componente
import com.softworld.mecadocs.data.remote.pool.PayloadType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Adaptador de Payloads (Adapter Pattern).
 * Converte requisições genéricas de prompt para os formatos JSON específicos de cada provedor (OpenAI/Groq/OpenRouter x Gemini)
 * e faz o parse da resposta em formato JSON para o modelo de domínio [Componente].
 */
object AiPayloadAdapter {

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val componenteAdapter = moshi.adapter(Componente::class.java)
    private val mapAdapter = moshi.adapter(Map::class.java)

    /**
     * Constrói o prompt padronizado exigindo que a IA responda estritamente em JSON compatível com [Componente].
     */
    fun buildSystemPrompt(componentName: String, language: String = "pt-BR"): String {
        return """
Você é um engenheiro especialista em eletrônica e microcontroladores.
Gere a documentação técnica (datasheet) completa para o componente '$componentName' no idioma $language.

A sua resposta DEVE ser estritamente um objeto JSON válido sem Markdown ou blocos de código extra, seguindo exatamente este esquema de chaves:
{
  "nome": "Nome do Componente",
  "descricao": "Descrição detalhada",
  "categoria": "Categoria (ex: Microcontrolador / Driver)",
  "specs": {
    "tensaoOperacao": "ex: 3.3V",
    "tensaoEntrada": "ex: 5V-12V",
    "correnteMaxima": "ex: 500mA",
    "nivelLogico": "ex: 3.3V",
    "clock": "ex: 240 MHz",
    "memoria": "ex: 520 KB SRAM",
    "interfaces": ["36 GPIOs", "ADC 12-bit", "SPI", "I2C"]
  },
  "pinout": [
    { "pino": "3V3", "funcao": "Alimentação 3.3V", "notas": "Notas técnicas" }
  ],
  "alertas": [
    { "titulo": "Alerta", "descricao": "Descrição", "nivel": "PERIGO" }
  ],
  "codigo": {
    "linguagem": "C++ (Arduino IDE)",
    "codigo": "// Código de teste",
    "descricao": "Explicação"
  }
}
""".trimIndent()
    }

    /**
     * Cria o [RequestBody] no formato do provedor especificado (`OPEN_AI` ou `GEMINI`), ajustando o modelo conforme o provedor.
     */
    fun createRequestBody(componentName: String, payloadType: PayloadType, language: String = "pt-BR", providerId: String = "groq_llama"): RequestBody {
        val prompt = buildSystemPrompt(componentName, language)

        val jsonString = when (payloadType) {
            PayloadType.OPEN_AI -> {
                val modelName = when {
                    providerId.contains("openrouter") -> "meta-llama/llama-3.3-70b-instruct"
                    else -> "llama-3.3-70b-versatile"
                }
                val openAiMap = mapOf(
                    "model" to modelName,
                    "messages" to listOf(
                        mapOf("role" to "system", "content" to "Você é um gerador JSON de datasheets técnicos de eletrônica."),
                        mapOf("role" to "user", "content" to prompt),
                    ),
                    "response_format" to mapOf("type" to "json_object"),
                    "temperature" to 0.2,
                )
                mapAdapter.toJson(openAiMap)
            }
            PayloadType.GEMINI -> {
                val geminiMap = mapOf(
                    "contents" to listOf(
                        mapOf(
                            "parts" to listOf(
                                mapOf("text" to prompt),
                            ),
                        ),
                    ),
                )
                mapAdapter.toJson(geminiMap)
            }
        }

        return jsonString.toRequestBody(JSON_MEDIA_TYPE)
    }

    /**
     * Extrai e faz o parse da resposta bruta de IA (OpenAI / Groq / OpenRouter vs Gemini) para [Componente].
     */
    fun parseAiResponseToComponente(rawJson: String, payloadType: PayloadType): Componente? {
        if (rawJson.isBlank()) return null
        return try {
            val rootMap = mapAdapter.fromJson(rawJson) ?: return parseComponenteFromJson(rawJson)

            val textContent = when (payloadType) {
                PayloadType.OPEN_AI -> {
                    // Padrão OpenAI: choices[0].message.content
                    @Suppress("UNCHECKED_CAST")
                    val choices = rootMap["choices"] as? List<Map<String, Any>>
                    val firstChoice = choices?.firstOrNull()
                    @Suppress("UNCHECKED_CAST")
                    val message = firstChoice?.get("message") as? Map<String, Any>
                    message?.get("content") as? String
                }
                PayloadType.GEMINI -> {
                    // Padrão Gemini: candidates[0].content.parts[0].text
                    @Suppress("UNCHECKED_CAST")
                    val candidates = rootMap["candidates"] as? List<Map<String, Any>>
                    val firstCandidate = candidates?.firstOrNull()
                    @Suppress("UNCHECKED_CAST")
                    val content = firstCandidate?.get("content") as? Map<String, Any>
                    @Suppress("UNCHECKED_CAST")
                    val parts = content?.get("parts") as? List<Map<String, Any>>
                    val firstPart = parts?.firstOrNull()
                    firstPart?.get("text") as? String
                }
            }

            if (!textContent.isNullOrBlank()) {
                parseComponenteFromJson(textContent)
            } else {
                parseComponenteFromJson(rawJson)
            }
        } catch (_: Exception) {
            parseComponenteFromJson(rawJson)
        }
    }

    /**
     * Tenta fazer o parse direto do texto ou da resposta bruta de IA para um objeto [Componente],
     * removendo blocos de formatação Markdown e delimitadores JSON.
     */
    fun parseComponenteFromJson(jsonText: String): Componente? {
        if (jsonText.isBlank()) return null

        val cleaned = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            componenteAdapter.fromJson(cleaned)
        } catch (_: Exception) {
            val firstBrace = cleaned.indexOf('{')
            val lastBrace = cleaned.lastIndexOf('}')
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                val jsonSub = cleaned.substring(firstBrace, lastBrace + 1)
                try {
                    componenteAdapter.fromJson(jsonSub)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }
}
