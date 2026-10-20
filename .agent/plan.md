
-































































































# Project Plan

Aplicativo Android 'MecaDocs' (Datasheet de Bolso com IA). Permite ao usuário pesquisar componentes eletrônicos (ex: Arduino R3, ESP32, L298N) e receber resumos técnicos em cartões organizados: Quick Specs, Pinout, Alertas de Bancada e Código de Teste. Inclui SplashScreen 'made by SoftWorld', interface em Jetpack Compose Material 3, MVVM, Retrofit, Coroutines e StateFlow.

## Project Brief

# Project Brief: MecaDocs (Datasheet de Bolso com IA)

O **MecaDocs** é um aplicativo Android voltado a estudantes, técnicos, engenheiros e makers, funcionando como um guia de bancada ágil e resumido com IA para consulta de componentes eletrônicos.

---

## Features

1. **SplashScreen "Made by SoftWorld"**:
   - Tela de inicialização personalizada com branding da marca e transição reativa para a experiência principal do app.

2. **Pesquisa Rápida de Componentes**:
   - Campo de busca intuitivo para localização de componentes eletrônicos, microcontroladores e módulos (ex.: Arduino Uno R3, ESP32, L298N, sensores e CIs).

3. **Cartões Técnicos Estruturados**:
   - **Quick Specs**: Especificações vitais em formato resumido (tensão de operação, corrente máxima, clock, pinos I/O).
   - **Pinout**: Mapeamento limpo dos pinos e interfaces de comunicação (SPI, I2C, UART, PWM).
   - **Alertas de Bancada**: Avisos essenciais de montagem e proteção (ex.: limites de tolerância 3.3V/5V, necessidade de dissipador térmico, diodos de proteção).
   - **Código de Teste**: Snippet mínimo executável para teste rápido na bancada (Arduino C++ ou MicroPython).

4. **Tratamento de Resposta e Estados de Conexão**:
   - Gestão clara de estados de carregamento (Loading shimmer/progresso), exibição resiliente de dados e mensagens de erro amigáveis com opção de nova tentativa.

---

## High-Level Technical Stack

- **Linguagem**: Kotlin
- **UI Toolkit**: Jetpack Compose com Material Design 3 (M3)
- **Navegação & Layout Adaptativo**:
  - **Jetpack Navigation 3** (Navegação orientada a estado)
  - **Compose Material Adaptive Library** (Layouts adaptativos com suporte responsivo)
- **Arquitetura**: MVVM (Model-View-ViewModel) com Unidirectional Data Flow (UDF)
- **Assincronia & Fluxos de Dados**: Kotlin Coroutines & StateFlow
- **Camada de Rede (Networking)**: Retrofit com Kotlinx Serialization / Moshi para comunicação com a API de IA e dados dos componentes

## Implementation Steps
**Total Duration:** 1h 30m 45s

### Task_1_Setup_Data_And_Network_Layer: Implement domain models, Retrofit API client, and repository with mock/fallback support for electronic components (ESP32, Arduino Uno, L298N) when API_KEY is not provided.
- **Status:** COMPLETED
- **Updates:** Modelos de dados (ComponentModels.kt), ApiService, ApiClient e MecaDocsRepository com suporte a mock offline para ESP32, Arduino, L298N criados e validados com testes unitários e build de debug executados com sucesso.
- **Acceptance Criteria:**
  - Domain models for electronic components (Quick Specs, Pinout, Bench Alerts, Code Snippets) created
  - Retrofit ApiClient and repository implemented with API_KEY integration and offline mock fallback support
  - Data repository returns valid component data
- **Duration:** 57m 39s

### Task_2_ViewModel_And_State_Management: Implement ViewModel with StateFlow managing search query, UI states (Idle, Loading, Success, Error with retry), and component selection.
- **Status:** COMPLETED
- **Updates:** MainViewModel, MecaDocsUiState (Idle, Loading, Success, Error com retry) e testes unitários implementados e validados com 100% de sucesso nos testes (16/16).
- **Acceptance Criteria:**
  - ViewModel created exposing StateFlow for UI state (Idle, Loading, Success, Error)
  - Search and query handling connected to data repository
  - Retry mechanism implemented for network failure recovery
- **Duration:** 15m 46s

### Task_3_UI_Screens_And_Components: Build Jetpack Compose Material 3 UI including Splash Screen with 'Made by SoftWorld' branding, Search Bar, Quick Specs cards, Pinout list, Bench Warning alerts, and monospaced code viewer.
- **Status:** COMPLETED
- **Updates:** Interface em Jetpack Compose Material 3 criada com SplashScreen 'Made by SoftWorld', HomeScreen com busca e chips rápidos, Cards de Quick Specs, Pinout, Alertas de Bancada com ícone ⚠️ e bloco de código de teste em fonte monoespaçada com botão de cópia. Compilado e testado com sucesso.
- **Acceptance Criteria:**
  - Splash screen displaying 'MecaDocs - Made by SoftWorld' implemented
  - Search bar with loading indicators and error retry view implemented
  - Technical cards for Quick Specs, Pinout, Warning alerts, and monospaced code viewer created in Jetpack Compose Material 3
  - Full user flow from splash to search and component details connected
- **Duration:** 17m 20s

### Task_4_Run_And_Verify: Run and verify application stability, performance, and feature completeness. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - App builds successfully and does not crash on launch or search interaction
  - Search and mock fallback work seamlessly for key components (ESP32, Arduino Uno, L298N)
  - Make sure all existing tests pass
  - Build pass
  - App does not crash
- **StartTime:** 2026-10-03 23:23:30 WAT

