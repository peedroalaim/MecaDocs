package com.softworld.mecadocs.data.remote.pool

import java.io.IOException

/**
 * Exceção lançada quando todos os provedores do pool de IAs falharem ou estiverem temporariamente em cooldown.
 */
class AllProvidersExhaustedException(
    message: String = "Todos os provedores de IA estão temporariamente indisponíveis ou em cooldown.",
    cause: Throwable? = null,
) : IOException(message, cause)
