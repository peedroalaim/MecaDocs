package com.softworld.mecadocs.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softworld.mecadocs.data.model.Componente
import com.softworld.mecadocs.data.repository.MecaDocsRepository
import com.softworld.mecadocs.ui.state.MecaDocsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel principal da aplicação MecaDocs (Modo Expert).
 * Gerencia o estado da interface (UI state), buscas de datasheets, favoritos no Room Database
 * e diálogos de ferramentas de bancada (Calculadora Divisor de Tensão).
 *
 * @param repository Instância de [MecaDocsRepository] utilizada para buscar os dados técnicos.
 * @param savedStateHandle Instância de [SavedStateHandle] para persistência de estado através de process death.
 */
class MainViewModel(
    private val repository: MecaDocsRepository = MecaDocsRepository(),
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
    }

    private val _uiState = MutableStateFlow<MecaDocsUiState>(MecaDocsUiState.Idle)
    /**
     * Fluxo de estado da UI observável pela View/Compose (Idle, Loading, Success, Error).
     */
    val uiState: StateFlow<MecaDocsUiState> = _uiState.asStateFlow()

    /**
     * Termo de busca atual presente no campo de pesquisa (preservado via SavedStateHandle).
     */
    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(SEARCH_QUERY_KEY, "")

    private val _popularComponents = MutableStateFlow(
        listOf("ESP32", "Arduino Uno", "L298N", "STM32 Blue Pill", "HC-SR04"),
    )
    /**
     * Lista de componentes populares/sugeridos para atalho rápido de busca.
     */
    val popularComponents: StateFlow<List<String>> = _popularComponents.asStateFlow()

    /**
     * Atalho para leitura direta dos componentes sugeridos em formato de Lista.
     */
    val componentesSugeridos: List<String>
        get() = _popularComponents.value

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    /**
     * Lista com as últimas pesquisas realizadas na sessão.
     */
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    /**
     * Fluxo de componentes favoritados e salvos localmente.
     */
    val favorites: StateFlow<List<Componente>> = repository.getFavoritesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _showVoltageDividerDialog = MutableStateFlow(false)
    val showVoltageDividerDialog: StateFlow<Boolean> = _showVoltageDividerDialog.asStateFlow()

    private var lastSearchedQuery: String = ""

    /**
     * Atualiza o termo de busca na barra de pesquisa sem disparar a busca imediatamente.
     *
     * @param query Texto digitado pelo usuário.
     */
    fun updateSearchQuery(query: String) {
        savedStateHandle[SEARCH_QUERY_KEY] = query
    }

    /**
     * Inicia a busca do datasheet referente ao [query] informado ou ao valor atual de [searchQuery].
     */
    fun buscarDatasheet(query: String = searchQuery.value) {
        val targetQuery = query.trim()

        if (targetQuery.isBlank()) {
            _uiState.value = MecaDocsUiState.Idle
            return
        }

        updateSearchQuery(targetQuery)
        lastSearchedQuery = targetQuery
        adicionarBuscaRecente(targetQuery)

        viewModelScope.launch {
            flow {
                emit(MecaDocsUiState.Loading)

                val result = repository.getComponentDetails(targetQuery)
                if (result.isSuccess) {
                    val datasheet = result.getOrThrow()
                    emit(MecaDocsUiState.Success(datasheet))
                } else {
                    val errorMessage = result.exceptionOrNull()?.message
                        ?: "Não foi possível encontrar o datasheet para '$targetQuery'."
                    emit(MecaDocsUiState.Error(errorMessage))
                }
            }.catch { throwable ->
                val errorMsg = throwable.message ?: "Ocorreu um erro inesperado ao buscar o datasheet."
                emit(MecaDocsUiState.Error(errorMsg))
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Alterna se o componente atual é favorito ou não.
     */
    fun toggleFavorite(componente: Componente) {
        viewModelScope.launch {
            repository.toggleFavorite(componente)
        }
    }

    /**
     * Define a visibilidade da Calculadora de Divisor de Tensão de Bancada.
     */
    fun setVoltageDividerDialogVisible(visible: Boolean) {
        _showVoltageDividerDialog.value = visible
    }

    /**
     * Executa novamente a busca utilizando a última consulta pesquisada ou o termo atual no campo de busca.
     */
    fun retry() {
        val queryToRetry = searchQuery.value.ifBlank { lastSearchedQuery }
        if (queryToRetry.isNotBlank()) {
            buscarDatasheet(queryToRetry)
        }
    }

    /**
     * Reseta o texto de busca e retorna o estado da interface para [MecaDocsUiState.Idle].
     */
    fun limparBusca() {
        updateSearchQuery("")
        _uiState.value = MecaDocsUiState.Idle
    }

    /**
     * Define o termo de busca com o nome do componente sugerido informado e dispara a consulta.
     *
     * @param nome Nome do componente sugerido (ex: "ESP32", "Arduino Uno", "L298N").
     */
    fun selecionarComponenteSugerido(nome: String) {
        updateSearchQuery(nome)
        buscarDatasheet(nome)
    }

    /**
     * Registra a consulta na lista de buscas recentes, garantindo no máximo 10 itens sem duplicação no topo.
     */
    private fun adicionarBuscaRecente(query: String) {
        val currentList = _recentSearches.value.toMutableList()
        currentList.remove(query)
        currentList.add(0, query)
        if (currentList.size > 10) {
            _recentSearches.value = currentList.subList(0, 10)
        } else {
            _recentSearches.value = currentList
        }

        viewModelScope.launch {
            repository.addSearchHistory(query)
        }
    }
}
