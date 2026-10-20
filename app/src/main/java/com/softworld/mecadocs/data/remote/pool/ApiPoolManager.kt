package com.softworld.mecadocs.data.remote.pool

/**
 * Gerenciador Singleton do pool de provedores de IA.
 * Organiza os provedores por ordem de prioridade (OpenRouter e Gemini no topo),
 * controlando quem está livre e quem está bloqueado temporariamente (cooldown).
 */
object ApiPoolManager {

    private val providers = mutableListOf(
        AiProvider(
            id = "openrouter_ai",
            name = "OpenRouter AI",
            baseUrl = "https://openrouter.ai/api/",
            apiKey = "YOUR_OPENROUTER_API_KEY",
            payloadType = PayloadType.OPEN_AI
        ),
        AiProvider(
            id = "gemini_flash",
            name = "Google Gemini Flash",
            baseUrl = "https://generativelanguage.googleapis.com/",
            apiKey = "YOUR_GEMINI_API_KEY",
            payloadType = PayloadType.GEMINI
        ),
        AiProvider(
            id = "groq_llama",
            name = "Groq Llama 3.3",
            baseUrl = "https://api.groq.com/openai/",
            apiKey = "YOUR_GROQ_API_KEY",
            payloadType = PayloadType.OPEN_AI
        ),
        AiProvider(
            id = "mecadocs_primary",
            name = "MecaDocs Primary Gateway",
            baseUrl = "https://api.mecadocs.softworld.com/",
            apiKey = "mecadocs_prod_v1",
            payloadType = PayloadType.OPEN_AI
        )
    )

    @Volatile
    private var currentIndex = 0

    /**
     * Retorna a lista atual de provedores cadastrados.
     */
    fun getProviders(): List<AiProvider> = synchronized(this) { providers.toList() }

    /**
     * Registra ou atualiza um provedor no pool em tempo de execução.
     */
    fun registerProvider(provider: AiProvider) {
        synchronized(this) {
            val existingIndex = providers.indexOfFirst { it.id == provider.id }
            if (existingIndex >= 0) {
                providers[existingIndex] = provider
            } else {
                providers.add(provider)
            }
        }
    }

    /**
     * Retorna o próximo provedor disponível cujo tempo de cooldown expirou.
     * Caso todos estejam em cooldown, retorna null.
     */
    fun getNextAvailableApi(nowMs: Long = System.currentTimeMillis()): AiProvider? {
        synchronized(this) {
            val total = providers.size
            if (total == 0) return null

            for (i in 0 until total) {
                val candidateIndex = (currentIndex + i) % total
                val candidate = providers[candidateIndex]
                if (candidate.isAvailable(nowMs)) {
                    currentIndex = candidateIndex
                    return candidate
                }
            }
            return null
        }
    }

    /**
     * Marca o provedor informado em cooldown até (tempoAtual + cooldownDurationMs)
     * e avança o índice atual para o próximo provedor.
     */
    fun markCooldown(providerId: String, durationMs: Long = 60_000L, nowMs: Long = System.currentTimeMillis()) {
        synchronized(this) {
            val index = providers.indexOfFirst { it.id == providerId }
            if (index >= 0) {
                providers[index].cooldownUntil = nowMs + durationMs
                currentIndex = (index + 1) % providers.size
            }
        }
    }

    /**
     * Reseta todos os tempos de cooldown de todos os provedores.
     */
    fun resetAllCooldowns() {
        synchronized(this) {
            providers.forEach { it.cooldownUntil = 0L }
            currentIndex = 0
        }
    }
}
