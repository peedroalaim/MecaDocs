package com.softworld.mecadocs.ui.viewmodel

import com.softworld.mecadocs.data.model.AlertaItem
import com.softworld.mecadocs.data.model.CodigoExemplo
import com.softworld.mecadocs.data.model.Componente
import com.softworld.mecadocs.data.model.SpecsData
import com.softworld.mecadocs.data.remote.ApiClient
import com.softworld.mecadocs.data.repository.MecaDocsRepository
import com.softworld.mecadocs.ui.state.MecaDocsUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para o [MainViewModel] cobrindo os estados da UI e ferramentas do Modo Expert.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: MainViewModel
    private lateinit var repository: MecaDocsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ApiClient.isOnlineMode = false
        repository = MecaDocsRepository(ioDispatcher = testDispatcher)
        viewModel = MainViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        ApiClient.isOnlineMode = true
    }

    @Test
    fun initialState_isIdle_withEmptySearchQueryAndPopularComponents() {
        assertEquals(MecaDocsUiState.Idle, viewModel.uiState.value)
        assertEquals("", viewModel.searchQuery.value)
        assertTrue(viewModel.popularComponents.value.contains("ESP32"))
        assertTrue(viewModel.popularComponents.value.contains("Arduino Uno"))
        assertTrue(viewModel.popularComponents.value.contains("L298N"))
        assertEquals(viewModel.popularComponents.value, viewModel.componentesSugeridos)
        assertTrue(viewModel.recentSearches.value.isEmpty())
        assertFalse(viewModel.showVoltageDividerDialog.value)
    }

    @Test
    fun updateSearchQuery_updatesSearchQueryFlow() {
        viewModel.updateSearchQuery("ESP32")
        assertEquals("ESP32", viewModel.searchQuery.value)

        viewModel.updateSearchQuery("Arduino")
        assertEquals("Arduino", viewModel.searchQuery.value)
    }

    @Test
    fun buscarDatasheet_withBlankQuery_setsStateToIdle() = runTest {
        viewModel.updateSearchQuery("   ")
        viewModel.buscarDatasheet()

        assertEquals(MecaDocsUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun buscarDatasheet_emitsLoadingStateBeforeSuccess() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        val slowRepo = object : MecaDocsRepository(ioDispatcher = standardDispatcher) {
            override suspend fun getComponentDetails(componentName: String): Result<Componente> {
                delay(100)
                return super.getComponentDetails(componentName)
            }
        }
        val vm = MainViewModel(slowRepo)

        vm.buscarDatasheet("ESP32")

        // Inicialmente o estado transita para Loading
        assertEquals(MecaDocsUiState.Loading, vm.uiState.value)

        // Avança o tempo do agendador até completar a requisição
        advanceUntilIdle()

        val currentState = vm.uiState.value
        assertTrue("Estado esperado era Success, mas foi $currentState", currentState is MecaDocsUiState.Success)
        val successState = currentState as MecaDocsUiState.Success
        assertEquals("ESP32", successState.datasheet.nome)
    }

    @Test
    fun buscarDatasheet_withValidComponent_emitsSuccessState() = runTest {
        viewModel.buscarDatasheet("ESP32")

        val currentState = viewModel.uiState.value
        assertTrue("Estado esperado era Success, mas foi $currentState", currentState is MecaDocsUiState.Success)

        val successState = currentState as MecaDocsUiState.Success
        assertEquals("ESP32", successState.datasheet.nome)
        assertEquals("3.3V", successState.datasheet.specs.tensaoOperacao)
        assertFalse(successState.datasheet.pinout.isEmpty())
        assertFalse(successState.datasheet.alertas.isEmpty())

        assertEquals("ESP32", viewModel.searchQuery.value)
        assertTrue(viewModel.recentSearches.value.contains("ESP32"))
    }

    @Test
    fun dialogControls_toggleVisibilityCorrectly() {
        viewModel.setVoltageDividerDialogVisible(true)
        assertTrue(viewModel.showVoltageDividerDialog.value)

        viewModel.setVoltageDividerDialogVisible(false)
        assertFalse(viewModel.showVoltageDividerDialog.value)
    }

    @Test
    fun buscarDatasheet_withUnknownComponent_emitsErrorState() = runTest {
        val unknownQuery = "ComponenteInexistenteX123"
        viewModel.buscarDatasheet(unknownQuery)

        val currentState = viewModel.uiState.value
        assertTrue("Estado esperado era Error, mas foi $currentState", currentState is MecaDocsUiState.Error)

        val errorState = currentState as MecaDocsUiState.Error
        assertTrue(errorState.message.contains(unknownQuery) || errorState.message.contains("não foi encontrado"))
    }

    @Test
    fun selecionarComponenteSugerido_updatesQueryAndEmitsSuccessState() = runTest {
        viewModel.selecionarComponenteSugerido("Arduino Uno")

        assertEquals("Arduino Uno", viewModel.searchQuery.value)

        val currentState = viewModel.uiState.value
        assertTrue(currentState is MecaDocsUiState.Success)

        val successState = currentState as MecaDocsUiState.Success
        assertEquals("Arduino Uno", successState.datasheet.nome)
    }

    @Test
    fun limparBusca_resetsQueryAndSetsIdleState() = runTest {
        viewModel.buscarDatasheet("L298N")
        assertTrue(viewModel.uiState.value is MecaDocsUiState.Success)

        viewModel.limparBusca()

        assertEquals("", viewModel.searchQuery.value)
        assertEquals(MecaDocsUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun retry_reExecutesLastSearchQuery() = runTest {
        val fakeRepo = FakeRepository(shouldFail = true, testDispatcher = testDispatcher)
        val customViewModel = MainViewModel(fakeRepo)

        // Busca com falha simulada
        customViewModel.buscarDatasheet("Sensor X")
        assertTrue(customViewModel.uiState.value is MecaDocsUiState.Error)

        // Altera o repositório para ter sucesso na próxima tentativa
        fakeRepo.shouldFail = false

        // Aciona o retry
        customViewModel.retry()

        val retryState = customViewModel.uiState.value
        assertTrue("Estado após retry deveria ser Success, mas foi $retryState", retryState is MecaDocsUiState.Success)
        val successState = retryState as MecaDocsUiState.Success
        assertEquals("Sensor X", successState.datasheet.nome)
    }

    @Test
    fun popularComponents_and_recentSearches_management() = runTest {
        viewModel.buscarDatasheet("ESP32")
        viewModel.buscarDatasheet("L298N")

        val recent = viewModel.recentSearches.value
        assertEquals("L298N", recent[0])
        assertEquals("ESP32", recent[1])
    }

    /**
     * Fake Repository auxiliar para simular falhas e sucessos controlados nos testes unitários.
     */
    private class FakeRepository(
        var shouldFail: Boolean = false,
        testDispatcher: CoroutineDispatcher
    ) : MecaDocsRepository(mecaDocsDao = null, ioDispatcher = testDispatcher) {

        override suspend fun getComponentDetails(componentName: String): Result<Componente> {
            return if (shouldFail) {
                Result.failure(RuntimeException("Falha de conexão simulada para '$componentName'."))
            } else {
                Result.success(
                    Componente(
                        nome = componentName,
                        descricao = "Componente de Teste Fake",
                        categoria = "Testes",
                        specs = SpecsData(
                            tensaoOperacao = "5V",
                            tensaoEntrada = "5V",
                            correnteMaxima = "100mA",
                            nivelLogico = "5V"
                        ),
                        pinout = emptyList(),
                        alertas = listOf(AlertaItem("Aviso Teste", "Descrição aviso")),
                        codigo = CodigoExemplo("C++", "// Teste", "Descrição")
                    )
                )
            }
        }
    }
}
