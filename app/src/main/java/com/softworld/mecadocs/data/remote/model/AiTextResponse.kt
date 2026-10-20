package com.softworld.mecadocs.data.remote.model

/**
 * Modelo genérico de resposta contendo o conteúdo textual extraído da IA
 * e o nome do provedor que respondeu a requisição.
 */
data class AiTextResponse(
    val rawContent: String,
    val providerName: String
)
