package com.softworld.mecadocs.data.remote.pool

/**
 * Representa um provedor do pool de contingência de IAs.
 *
 * @property id Identificador único do provedor.
 * @property name Nome exibível para logs e diagnósticos.
 * @property baseUrl URL base da API do provedor.
 * @property apiKey Chave de API ou token de autenticação.
 * @property payloadType Tipo de payload de envio/resposta (OPEN_AI ou GEMINI).
 * @property cooldownUntil Timestamp (em ms) indicando até quando este provedor está em bloqueio/cooldown.
 */
data class AiProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val payloadType: PayloadType,
    var cooldownUntil: Long = 0L
) {
    /**
     * Retorna se o provedor está livre para receber requisições (fora do período de cooldown).
     */
    fun isAvailable(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        return currentTimeMs >= cooldownUntil
    }
}
