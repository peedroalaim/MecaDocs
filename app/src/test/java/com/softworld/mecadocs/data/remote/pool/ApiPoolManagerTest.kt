package com.softworld.mecadocs.data.remote.pool

import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para o [ApiPoolManager] cobrindo controle de rotação,
 * seleção da próxima API ativa e marcação de tempo de cooldown.
 */
class ApiPoolManagerTest {

    @Before
    fun setUp() {
        ApiPoolManager.resetAllCooldowns()
    }

    @After
    fun tearDown() {
        ApiPoolManager.resetAllCooldowns()
    }

    @Test
    fun getNextAvailableApi_returnsFirstAvailableProvider() {
        val provider = ApiPoolManager.getNextAvailableApi()
        assertNotNull(provider)
        assertTrue(provider!!.isAvailable())
    }

    @Test
    fun markCooldown_rotatesToNextAvailableProvider() {
        val firstProvider = ApiPoolManager.getNextAvailableApi()
        assertNotNull(firstProvider)

        val nowMs = System.currentTimeMillis()
        ApiPoolManager.markCooldown(firstProvider!!.id, durationMs = 60_000L, nowMs = nowMs)

        val secondProvider = ApiPoolManager.getNextAvailableApi(nowMs = nowMs)
        assertNotNull(secondProvider)
        assertTrue(firstProvider.id != secondProvider!!.id)
    }

    @Test
    fun resetAllCooldowns_clearsCooldownForAllProviders() {
        val nowMs = System.currentTimeMillis()
        val providers = ApiPoolManager.getProviders()

        providers.forEach {
            ApiPoolManager.markCooldown(it.id, durationMs = 60_000L, nowMs = nowMs)
        }

        ApiPoolManager.resetAllCooldowns()
        val available = ApiPoolManager.getNextAvailableApi(nowMs = nowMs)
        assertNotNull(available)
        assertTrue(available!!.isAvailable(nowMs))
    }
}
