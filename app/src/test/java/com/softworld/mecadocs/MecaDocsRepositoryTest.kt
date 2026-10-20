package com.softworld.mecadocs

import com.softworld.mecadocs.data.remote.ApiClient
import com.softworld.mecadocs.data.repository.MecaDocsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MecaDocsRepositoryTest {

    private lateinit var repository: MecaDocsRepository

    @Before
    fun setUp() {
        ApiClient.isOnlineMode = false
        repository = MecaDocsRepository()
    }

    @Test
    fun getComponentDetails_returnsESP32MockSuccessfully() = runTest {
        val result = repository.getComponentDetails("ESP32")
        assertTrue(result.isSuccess)

        val componente = result.getOrNull()
        assertNotNull(componente)
        assertEquals("ESP32", componente?.nome)
        assertEquals("3.3V", componente?.specs?.tensaoOperacao)
        assertTrue(componente?.pinout?.isNotEmpty() == true)
        assertTrue(componente?.alertas?.isNotEmpty() == true)
        assertNotNull(componente?.codigo)
    }

    @Test
    fun getComponentDetails_returnsArduinoUnoMockSuccessfully() = runTest {
        val result = repository.getComponentDetails("Arduino Uno")
        assertTrue(result.isSuccess)

        val componente = result.getOrNull()
        assertNotNull(componente)
        assertEquals("Arduino Uno", componente?.nome)
        assertEquals("5V", componente?.specs?.tensaoOperacao)
    }

    @Test
    fun getComponentDetails_returnsL298NMockSuccessfully() = runTest {
        val result = repository.getComponentDetails("L298N")
        assertTrue(result.isSuccess)

        val componente = result.getOrNull()
        assertNotNull(componente)
        assertEquals("L298N", componente?.nome)
        assertTrue(componente?.specs?.correnteMaxima?.contains("2A") == true)
    }

    @Test
    fun searchComponents_returnsMatchingResults() = runTest {
        val searchResult = repository.searchComponents("motor")
        assertTrue(searchResult.isSuccess)

        val list = searchResult.getOrDefault(emptyList())
        assertTrue(list.any { it.nome == "L298N" })
    }

    @Test
    fun getMockComponentsList_containsEssentialComponents() {
        val mocks = repository.getMockComponentsList()
        assertTrue(mocks.size >= 3)
        assertTrue(mocks.any { it.nome == "ESP32" })
        assertTrue(mocks.any { it.nome == "Arduino Uno" })
        assertTrue(mocks.any { it.nome == "L298N" })
    }
}
