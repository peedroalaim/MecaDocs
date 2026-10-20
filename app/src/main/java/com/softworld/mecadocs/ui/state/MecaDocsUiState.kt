package com.softworld.mecadocs.ui.state

import com.softworld.mecadocs.data.model.DatasheetResponse

/**
 * Interface selada que representa os estados da interface de usuário (UI) na aplicação MecaDocs.
 */
sealed interface MecaDocsUiState {
    /**
     * Estado inicial antes de qualquer busca ser realizada ou após limpar a pesquisa.
     */
    data object Idle : MecaDocsUiState

    /**
     * Estado de carregamento exibido enquanto a busca ou geração do datasheet está em andamento.
     */
    data object Loading : MecaDocsUiState

    /**
     * Estado de sucesso indicando que o datasheet do componente foi carregado com êxito.
     *
     * @property datasheet O componente/datasheet retornado contendo especificações, pinout, alertas e exemplos de código.
     */
    data class Success(val datasheet: DatasheetResponse) : MecaDocsUiState

    /**
     * Estado de erro exibido quando ocorre uma falha na requisição ou o componente não foi encontrado.
     *
     * @property message Mensagem descritiva do erro para orientação do usuário e suporte a tentativa de repetição (retry).
     */
    data class Error(val message: String) : MecaDocsUiState
}
