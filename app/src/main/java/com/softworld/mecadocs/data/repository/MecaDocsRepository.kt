package com.softworld.mecadocs.data.repository

import com.softworld.mecadocs.data.local.dao.MecaDocsDao
import com.softworld.mecadocs.data.local.entity.CachedComponentEntity
import com.softworld.mecadocs.data.local.entity.FavoriteComponentEntity
import com.softworld.mecadocs.data.local.entity.SearchHistoryEntity
import com.softworld.mecadocs.data.model.AlertaItem
import com.softworld.mecadocs.data.model.CodigoExemplo
import com.softworld.mecadocs.data.model.Componente
import com.softworld.mecadocs.data.model.PinoutItem
import com.softworld.mecadocs.data.model.SpecsData
import com.softworld.mecadocs.data.remote.ApiClient
import com.softworld.mecadocs.data.remote.GenerateDatasheetRequest
import com.softworld.mecadocs.data.remote.adapter.AiPayloadAdapter
import com.softworld.mecadocs.data.remote.pool.AllProvidersExhaustedException
import com.softworld.mecadocs.data.remote.pool.ApiPoolManager
import com.softworld.mecadocs.data.remote.pool.PayloadType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repositório central de dados da aplicação MecaDocs (Modo Expert).
 * Executa chamadas de rede em segundo plano com rotação e fallback automático transparente
 * entre provedores do pool de IA (Groq, OpenRouter, Gemini) e banco local offline mockado.
 *
 * @param mecaDocsDao Instância opcional do DAO do Room para persistência de favoritos/histórico.
 * @param ioDispatcher Dispatcher de Coroutines utilizado para operações assíncronas I/O.
 */
open class MecaDocsRepository(
    private val mecaDocsDao: MecaDocsDao? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /**
     * Retorna se a aplicação possui uma chave de API configurada.
     */
    open fun isApiKeyAvailable(): Boolean = ApiClient.isApiKeyConfigured()

    /**
     * Obtém os detalhes completos e datasheet de um componente pelo nome.
     * Tenta em ordem:
     * 1. Favoritos salvos localmente
     * 2. Pool de IAs em segundo plano com rotação dinâmica (Groq / OpenRouter / Gemini) gerenciado pelo Interceptor
     * 3. Captura de [AllProvidersExhaustedException] ou falha de rede
     * 4. Fallback automático para base local mockada
     */
    open suspend fun getComponentDetails(componentName: String): Result<Componente> = withContext(ioDispatcher) {
        val trimmedName = componentName.trim()

        // 1. Tenta buscar nos favoritos salvos localmente no Room DB
        val localFavorite = mecaDocsDao?.getFavoriteByName(trimmedName)
        if (localFavorite != null) {
            return@withContext Result.success(localFavorite.toDomainModel())
        }

        // 1.5. Tenta buscar no cache persistente de IAs do Room DB
        val cachedComponent = mecaDocsDao?.getCachedComponentByName(trimmedName)
        if (cachedComponent != null) {
            return@withContext Result.success(cachedComponent.toDomainModel())
        }

        // 2. Tentativa via Pool de IAs Online com Interceptor de Rotação automática
        if (isApiKeyAvailable()) {
            try {
                val response = ApiClient.getService().generateDatasheet(
                    GenerateDatasheetRequest(componentName = trimmedName)
                )

                if (response.isSuccessful && response.body() != null) {
                    val rawString = response.body()!!.string()
                    val providerId = response.raw().header("x-provider-id") ?: "groq_llama"
                    val payloadType = ApiPoolManager.getProviders().firstOrNull { it.id == providerId }?.payloadType ?: PayloadType.OPEN_AI

                    val parsedComponent = AiPayloadAdapter.parseAiResponseToComponente(rawString, payloadType)
                    if (parsedComponent != null) {
                        mecaDocsDao?.insertCachedComponent(CachedComponentEntity.fromDomainModel(parsedComponent))
                        return@withContext Result.success(parsedComponent)
                    }
                }
            } catch (_: AllProvidersExhaustedException) {
                // Captura específica quando todas as APIs do pool falharem ou estiverem em cooldown
            } catch (_: Exception) {
                // Captura de exceções de IO / Timeout genéricas
            }
        }

        // 3. Fallback offline automático via dados mockados locais
        val localMock = findMockComponent(trimmedName)
        if (localMock != null) {
            Result.success(localMock)
        } else {
            Result.failure(
                NoSuchElementException(
                    "Componente '$trimmedName' não foi encontrado na base de dados local offline nem via API. Tente buscar por 'ESP32', 'Arduino Uno' ou 'L298N'."
                )
            )
        }
    }

    /**
     * Pesquisa componentes por termo de busca (query) com fallback automático.
     */
    open suspend fun searchComponents(query: String): Result<List<Componente>> = withContext(ioDispatcher) {
        val cleanQuery = query.trim().lowercase()

        if (cleanQuery.isBlank()) {
            return@withContext Result.success(getMockComponentsList())
        }

        val filtered = getMockComponentsList().filter { comp ->
            comp.nome.lowercase().contains(cleanQuery) ||
                    comp.categoria.lowercase().contains(cleanQuery) ||
                    comp.descricao.lowercase().contains(cleanQuery)
        }

        Result.success(filtered)
    }

    // =========================================================================
    // PERSISTÊNCIA DE FAVORITOS (ROOM DB)
    // =========================================================================

    /**
     * Retorna o fluxo de componentes favoritados ordenados do mais recente para o mais antigo.
     */
    open fun getFavoritesFlow(): Flow<List<Componente>> {
        return mecaDocsDao?.getAllFavoritesFlow()?.map { list ->
            list.map { it.toDomainModel() }
        } ?: flowOf(emptyList())
    }

    /**
     * Retorna um fluxo indicando se determinado componente é favorito.
     */
    open fun isFavoriteFlow(componentName: String): Flow<Boolean> {
        return mecaDocsDao?.isFavoriteFlow(componentName.trim()) ?: flowOf(false)
    }

    /**
     * Alterna o estado de favorito de um componente (adiciona ou remove).
     * Retorna true se agora estiver favoritado, ou false se tiver sido removido.
     */
    open suspend fun toggleFavorite(componente: Componente): Boolean = withContext(ioDispatcher) {
        val dao = mecaDocsDao ?: return@withContext false
        val isFav = dao.isFavorite(componente.nome)
        if (isFav) {
            dao.deleteFavoriteByName(componente.nome)
            false
        } else {
            dao.insertFavorite(FavoriteComponentEntity.fromDomainModel(componente))
            true
        }
    }

    // =========================================================================
    // HISTÓRICO DE BUSCA PERSISTENTE (ROOM DB)
    // =========================================================================

    /**
     * Retorna o fluxo das pesquisas recentes salvas no banco.
     */
    open fun getRecentSearchesFlow(): Flow<List<String>> {
        return mecaDocsDao?.getRecentSearchesFlow() ?: flowOf(emptyList())
    }

    /**
     * Registra um novo termo de busca no histórico persistente.
     */
    open suspend fun addSearchHistory(searchTerm: String) = withContext(ioDispatcher) {
        val cleanTerm = searchTerm.trim()
        if (cleanTerm.isNotBlank()) {
            mecaDocsDao?.insertSearchHistory(SearchHistoryEntity(searchTerm = cleanTerm))
        }
    }

    /**
     * Procura um componente mock que responda pelo nome ou termo aproximado.
     */
    private fun findMockComponent(name: String): Componente? {
        val lower = name.lowercase()
        val allMocks = getMockComponentsList()

        // 1. Busca por correspondência exata
        allMocks.firstOrNull { it.nome.equals(name, ignoreCase = true) }?.let { return it }

        // 2. Busca por termos específicos
        return when {
            lower.contains("esp32") || lower.contains("esp-32") || lower.contains("wroom") -> esp32Mock
            lower.contains("uno") || lower.contains("arduino r3") || lower.contains("arduino uno") -> arduinoUnoMock
            lower.contains("l298n") || lower.contains("l298") || lower.contains("ponte h") || lower.contains("driver motor") -> l298nMock
            lower.contains("stm32") || lower.contains("blue pill") -> stm32Mock
            lower.contains("hc-sr04") || lower.contains("sr04") || lower.contains("ultrassonico") || lower.contains("ultrassônico") -> hcsr04Mock
            else -> allMocks.firstOrNull { lower.contains(it.nome.lowercase()) }
        }
    }

    /**
     * Retorna a lista completa de componentes mockados para exibição offline.
     */
    fun getMockComponentsList(): List<Componente> {
        return listOf(
            esp32Mock,
            arduinoUnoMock,
            l298nMock,
            stm32Mock,
            hcsr04Mock,
        )
    }

    // =========================================================================
    // OBJETOS MOCK LOCAIS (BASE DE DADOS OFFLINE)
    // =========================================================================

    private val esp32Mock = Componente(
        nome = "ESP32",
        descricao = "Microcontrolador de alta performance de 32 bits da Espressif com Wi-Fi 802.11 b/g/n e Bluetooth v4.2 BR/EDR e BLE integrados. Ideal para projetos IoT e automação.",
        categoria = "Microcontrolador / Módulo IoT",
        specs = SpecsData(
            tensaoOperacao = "3.3V",
            tensaoEntrada = "5V - 12V (via pino VIN com regulador onboard)",
            correnteMaxima = "500mA (pico de transmissão Wi-Fi), ~12mA por pino GPIO",
            nivelLogico = "3.3V (NÃO tolerante a 5V!)",
            clock = "240 MHz (Tensilica Xtensa Dual-Core 32-bit LX6)",
            memoria = "520 KB SRAM / 4 MB Flash SPI",
            interfaces = listOf(
                "36 Pinos GPIO",
                "12-bit ADC (18 canais)",
                "8-bit DAC (2 canais)",
                "3 x UART",
                "3 x SPI",
                "2 x I2C",
                "PWM / LED PWM",
                "Touch Capacitivo (10 pinos)",
                "CAN Bus 2.0",
            ),
        ),
        pinout = listOf(
            PinoutItem("3V3", "Saída de alimentação regulada de 3.3V", "Fornece até 250mA para sensores externos"),
            PinoutItem("GND", "Terra de referência comum (0V)", "Conectar ao GND comum de todo o circuito"),
            PinoutItem("VIN / 5V", "Entrada de alimentação bruta (5V a 12V)", "Alimenta o regulador interno LDO da placa"),
            PinoutItem("GPIO 2", "I/O Digital e LED Onboard", "Ativo alto. Usado para boot mode (deve estar solto ao ligar)"),
            PinoutItem("GPIO 4", "Entrada/Saída Digital e ADC2_CH0", "Pino de I/O de uso geral e sensor analógico"),
            PinoutItem("GPIO 21", "I2C SDA (Linha de Dados)", "Requer resistor de pull-up externo se ausente no sensor"),
            PinoutItem("GPIO 22", "I2C SCL (Linha de Clock)", "Requer resistor de pull-up externo"),
            PinoutItem("GPIO 16 (RX2)", "UART2 Receive", "Entrada serial RX2 com nível lógico 3.3V"),
            PinoutItem("GPIO 17 (TX2)", "UART2 Transmit", "Saída serial TX2 com nível lógico 3.3V"),
            PinoutItem("EN / RST", "Enable / Reset da CPU", "Puxe para GND momentaneamente para reiniciar o ESP32"),
        ),
        alertas = listOf(
            AlertaItem(
                titulo = "Tensão Lógica Estrita de 3.3V",
                descricao = "ATENÇÃO PERIGO: Os pinos GPIO do ESP32 operam estritamente em 3.3V. Conectar diretamente sinais de 5V (como do Arduino ou sensores 5V) queimará os pinos do microcontrolador. Utilize divisor de tensão ou conversor de nível lógico.",
                nivel = "PERIGO",
            ),
            AlertaItem(
                titulo = "Restrição do ADC2 com Wi-Fi Ativo",
                descricao = "O módulo ADC2 compartilha recursos com o rádio Wi-Fi. Quando o Wi-Fi estiver transmitindo, leituras analógicas nos pinos do ADC2 (GPIOs 0, 2, 4, 12-15, 25-27) falharão. Utilize os pinos do ADC1 (GPIOs 32 a 39) para leituras contínuas.",
                nivel = "ATENCAO",
            ),
            AlertaItem(
                titulo = "Picos de Corrente durante Boot e Wi-Fi",
                descricao = "Durante a inicialização e conexão com redes sem fio, o ESP32 exige picos de corrente de até 500mA. Alimentação instável via cabos USB finos causará resets contínuos (Brownout Detector reset).",
                nivel = "INFO",
            ),
        ),
        codigo = CodigoExemplo(
            linguagem = "C++ (Arduino IDE)",
            descricao = "Código completo para teste de Blink no LED interno e inicialização Serial no ESP32.",
            codigo = """
// MecaDocs: Exemplo de Inicialização e Pisca-Pisca para ESP32
#define LED_GPIO 2

void setup() {
  // Inicializa comunicação serial com alta velocidade
  Serial.begin(115200);
  delay(500);
  
  pinMode(LED_GPIO, OUTPUT);
  Serial.println("=========================================");
  Serial.println("MecaDocs Datasheet: ESP32 Inicializado!");
  Serial.println("Frequencia CPU: " + String(getCpuFrequencyMhz()) + " MHz");
  Serial.println("=========================================");
}

void loop() {
  digitalWrite(LED_GPIO, HIGH);
  Serial.println("[ESP32] Status: LED LIGADO (3.3V)");
  delay(1000);

  digitalWrite(LED_GPIO, LOW);
  Serial.println("[ESP32] Status: LED DESLIGADO (0V)");
  delay(1000);
}
""".trimIndent(),
        ),
    )

    private val arduinoUnoMock = Componente(
        nome = "Arduino Uno",
        descricao = "Placa de desenvolvimento padrão baseada no microcontrolador ATmega328P de 8 bits. É a plataforma mais popular para aprendizado de eletrônica, robótica e prototipagem.",
        categoria = "Placa de Microcontrolador",
        specs = SpecsData(
            tensaoOperacao = "5V",
            tensaoEntrada = "7V - 12V (Recomendada via Jack P4 / VIN) / Limites: 6V - 20V",
            correnteMaxima = "20mA por pino de I/O (Absoluto: 40mA), 50mA na saída de 3.3V",
            nivelLogico = "5V",
            clock = "16 MHz (Oscilador Cristal de Quartzo)",
            memoria = "32 KB Flash (0.5 KB do Bootloader), 2 KB SRAM, 1 KB EEPROM",
            interfaces = listOf(
                "14 Pinos Digitais de I/O (6 com PWM)",
                "6 Entradas Analógicas (ADC 10-bit)",
                "1 x UART TTL (D0/D1)",
                "1 x SPI (D10, D11, D12, D13)",
                "1 x I2C / TWI (A4/SDA, A5/SCL)",
            ),
        ),
        pinout = listOf(
            PinoutItem("5V", "Saída de tensão de 5V regulada", "Alimenta circuitos e sensores de 5V. Corrente máxima ~500mA"),
            PinoutItem("3V3", "Saída de tensão de 3.3V regulada", "Fornecida pelo CI USB/LDO onboard. Máximo 50mA"),
            PinoutItem("GND", "Aterramento / Terra comum", "Múltiplos pinos conectados em paralelo na placa"),
            PinoutItem("VIN", "Entrada de Tensão Externa", "Utilizado para alimentar a placa com baterias de 7V a 12V sem USB"),
            PinoutItem("A0 - A5", "Entradas Analógicas (0V a 5V)", "Conversores ADC de 10 bits (0-1023). Funcionam também como GPIOs digitais"),
            PinoutItem("D0 (RX) / D1 (TX)", "Interface Serial TTL 5V", "Conectados ao chip USB. Evitar ligar sensores nesses pinos ao gravar código"),
            PinoutItem("D3, D5, D6, D9, D10, D11", "Saídas PWM (8-bit)", "Permitem controle de velocidade de motores e brilho de LEDs via analogWrite()"),
            PinoutItem("D13", "GPIO Digital e LED Onboard L", "Possui resistor interno integrado para o LED de teste da placa"),
            PinoutItem("SDA / SCL", "Barramento I2C Wire", "Pinos dedicados no topo e espelhados nos pinos analógicos A4 e A5"),
        ),
        alertas = listOf(
            AlertaItem(
                titulo = "Tensão Máxima de I/O de 5V",
                descricao = "Não aplique tensões superiores a 5.5V nos pinos de I/O do Arduino Uno para evitar a queima permanente do microcontrolador ATmega328P.",
                nivel = "PERIGO",
            ),
            AlertaItem(
                titulo = "Limite de Corrente Total do Encapsulamento",
                descricao = "Embora cada pino suporte até 20mA, a corrente total somada de todas as saídas juntas não deve exceder 200mA.",
                nivel = "ATENCAO",
            ),
            AlertaItem(
                titulo = "Aquecimento do Regulador VIN",
                descricao = "Ao utilizar fontes externas acima de 12V no conector P4 ou pino VIN, o regulador de tensão linear (AMS1117-5.0) pode aquecer excessivamente.",
                nivel = "INFO",
            ),
        ),
        codigo = CodigoExemplo(
            linguagem = "C++ (Arduino IDE)",
            descricao = "Exemplo de leitura de potenciômetro na entrada analógica A0 e controle proporcional PWM no pino D3.",
            codigo = """
// MecaDocs: Exemplo Leitura Analógica e Sinal PWM
const int PINO_POT = A0;
const int PINO_LED_PWM = 3;

void setup() {
  Serial.begin(9600);
  pinMode(PINO_LED_PWM, OUTPUT);
  Serial.println("MecaDocs: Arduino Uno Pronto!");
}

void loop() {
  int valorADC = analogRead(PINO_POT); // Retorna de 0 a 1023
  int valorPWM = map(valorADC, 0, 1023, 0, 255); // Mapeia para 0-255
  
  analogWrite(PINO_LED_PWM, valorPWM);
  
  Serial.print("Leitura ADC (A0): ");
  Serial.print(valorADC);
  Serial.print(" -> Saida PWM (D3): ");
  Serial.println(valorPWM);
  
  delay(100);
}
""".trimIndent(),
        ),
    )

    private val l298nMock = Componente(
        nome = "L298N",
        descricao = "Módulo Driver de Motor baseado no CI Ponte H Dupla L298N. Capaz de controlar velocidade e sentido de rotação de até 2 motores CC ou 1 motor de passo bipolar.",
        categoria = "Driver de Motor / Eletrônica de Potência",
        specs = SpecsData(
            tensaoOperacao = "5V (Sinal Lógico)",
            tensaoEntrada = "5V a 35V (Alimentação da Carga/Motores no pino VMS)",
            correnteMaxima = "2A por ponte (Pico de até 3A por canal)",
            nivelLogico = "5V / 3.3V (Sinais de controle TTL)",
            clock = "Frequência PWM de até 40 kHz",
            memoria = "N/A",
            interfaces = listOf(
                "Borne de Potência (VMS, GND, 5V)",
                "Bornes de Saída (OUT1, OUT2, OUT3, OUT4)",
                "Pinos de Entrada (IN1, IN2, IN3, IN4)",
                "Pinos de Habilitação PWM (ENA, ENB)",
            ),
        ),
        pinout = listOf(
            PinoutItem("VMS / VCC", "Entrada de Tensão dos Motores (5V a 35V)", "Conectar ao polo positivo da fonte de alimentação dos motores"),
            PinoutItem("GND", "Terra Comum / Aterramento de Referência", "OBRIGATÓRIO conectar ao GND da fonte e ao GND do microcontrolador"),
            PinoutItem("5V (Borne)", "Entrada / Saída de 5V da Lógica", "Com Jumper 78M05 ativo: Saída 5V para alimentar o Arduino. Sem Jumper: Entrada 5V externa"),
            PinoutItem("IN1 / IN2", "Sinais de Direção do Motor A", "IN1=HIGH & IN2=LOW (Frente); IN1=LOW & IN2=HIGH (Ré); IN1=LOW & IN2=LOW (Freio)"),
            PinoutItem("IN3 / IN4", "Sinais de Direção do Motor B", "Controla o sentido de rotação do Motor B da mesma forma"),
            PinoutItem("ENA", "Enable A (PWM Velocidade Motor A)", "Remova o jumper de fábrica e aplique sinal PWM do microcontrolador para ajustar velocidade"),
            PinoutItem("ENB", "Enable B (PWM Velocidade Motor B)", "Remova o jumper para aplicar sinal PWM no Motor B"),
            PinoutItem("OUT1 / OUT2", "Terminais de Saída Motor A", "Conectar aos dois fios do Motor CC A"),
            PinoutItem("OUT3 / OUT4", "Terminais de Saída Motor B", "Conectar aos dois fios do Motor CC B"),
        ),
        alertas = listOf(
            AlertaItem(
                titulo = "Cuidado com o Jumper do Regulador de 5V",
                descricao = "ATENÇÃO PERIGO: Se a tensão dos motores no pino VMS for SUPERIOR a 12V, você DEVE REMOVER o jumper do regulador de 5V onboard. Deixá-lo conectado com tensões >12V queimará o CI regulador 78M05 por superaquecimento.",
                nivel = "PERIGO",
            ),
            AlertaItem(
                titulo = "GND Compartilhado Obrigatório",
                descricao = "Você DEVE interligar o GND do módulo L298N com o GND do Arduino/ESP32. Sem terra de referência comum, as entradas IN1-IN4 flutuarão e os motores não responderão adequadamente.",
                nivel = "ATENCAO",
            ),
            AlertaItem(
                titulo = "Queda de Tensão Interna (Drop-Out de ~2V)",
                descricao = "A tecnologia transistorizada bipolar do L298N causa uma queda de tensão interna de aproximadamente 1.8V a 2V. Se alimentar a VMS com 12V, a saída para o motor será de cerca de 10V.",
                nivel = "INFO",
            ),
        ),
        codigo = CodigoExemplo(
            linguagem = "C++ (Arduino IDE)",
            descricao = "Exemplo completo de acionamento do Motor A com sentido e controle de velocidade via PWM na ponte H L298N.",
            codigo = """
// MecaDocs: Controle de Motor CC com L298N e Arduino
const int PINO_ENA = 9;  // Pino PWM
const int PINO_IN1 = 8;  // Direção 1
const int PINO_IN2 = 7;  // Direção 2

void setup() {
  pinMode(PINO_ENA, OUTPUT);
  pinMode(PINO_IN1, OUTPUT);
  pinMode(PINO_IN2, OUTPUT);
}

void loop() {
  // 1. Mover para Frente a 80% da velocidade (PWM 200)
  digitalWrite(PINO_IN1, HIGH);
  digitalWrite(PINO_IN2, LOW);
  analogWrite(PINO_ENA, 200);
  delay(3000);

  // 2. Parada / Freio
  digitalWrite(PINO_IN1, LOW);
  digitalWrite(PINO_IN2, LOW);
  analogWrite(PINO_ENA, 0);
  delay(1000);

  // 3. Mover para Trás a 50% da velocidade (PWM 128)
  digitalWrite(PINO_IN1, LOW);
  digitalWrite(PINO_IN2, HIGH);
  analogWrite(PINO_ENA, 128);
  delay(3000);

  // 4. Parar
  digitalWrite(PINO_IN1, LOW);
  digitalWrite(PINO_IN2, LOW);
  analogWrite(PINO_ENA, 0);
  delay(2000);
}
""".trimIndent(),
        ),
    )

    private val stm32Mock = Componente(
        nome = "STM32 Blue Pill",
        descricao = "Placa de desenvolvimento ARM Cortex-M3 de 32 bits acessível e de alta velocidade. Muito utilizada em projetos que exigem processamento avançado, múltiplos canais PWM e portas seriais.",
        categoria = "Microcontrolador ARM",
        specs = SpecsData(
            tensaoOperacao = "3.3V",
            tensaoEntrada = "5V (via USB ou pino 5V) / 3.3V no pino 3.3V",
            correnteMaxima = "150mA total, 8mA a 25mA por pino GPIO",
            nivelLogico = "3.3V (Alguns pinos são tolerantes a 5V)",
            clock = "72 MHz (Cristal de 8 MHz com multiplicador PLL)",
            memoria = "64 KB ou 128 KB Flash / 20 KB SRAM",
            interfaces = listOf(
                "37 Pinos GPIO",
                "2 x ADC 12-bit (10 canais)",
                "3 x USART",
                "2 x SPI",
                "2 x I2C",
                "1 x CAN 2.0B",
                "USB 2.0 Full Speed",
            ),
        ),
        pinout = listOf(
            PinoutItem("3V3", "Saída/Alimentação de 3.3V", "Conectar apenas circuitos de 3.3V"),
            PinoutItem("GND", "Aterramento / 0V", "Terra comum"),
            PinoutItem("5V", "Entrada/Saída de 5V", "Ligado diretamente ao pino de VBUS da porta USB"),
            PinoutItem("PA0", "GPIO / ADC1_IN0 / Tim2_CH1", "Suporta interrupções e leitura analógica de 12 bits"),
            PinoutItem("PB6 (SCL) / PB7 (SDA)", "Barramento I2C1", "Pinos de comunicação I2C padrão do STM32"),
        ),
        alertas = listOf(
            AlertaItem(
                titulo = "Pinos Tolerantes a 5V",
                descricao = "Apenas os pinos marcados com FT (Five-volt Tolerant) no datasheet suportam sinais de 5V. Pinos de entrada analógica (ADC) NÃO são tolerantes a 5V.",
                nivel = "ATENCAO",
            ),
        ),
        codigo = CodigoExemplo(
            linguagem = "C++ (STM32Cube / Arduino IDE)",
            descricao = "Blink no LED integrado no pino PC13.",
            codigo = """
// Exemplo MecaDocs: STM32 Blue Pill Blink (PC13)
#define LED_PIN PC13

void setup() {
  pinMode(LED_PIN, OUTPUT);
}

void loop() {
  digitalWrite(LED_PIN, LOW);  // Liga o LED no PC13 (Lógica Invertida)
  delay(500);
  digitalWrite(LED_PIN, HIGH); // Desliga o LED
  delay(500);
}
""".trimIndent(),
        ),
    )

    private val hcsr04Mock = Componente(
        nome = "HC-SR04",
        descricao = "Sensor de distância ultrassônico capaz de medir distâncias de 2 cm a 400 cm com precisão de aproximadamente 3 mm. Utiliza pulsos de ultrassom de 40 kHz.",
        categoria = "Sensor de Distância",
        specs = SpecsData(
            tensaoOperacao = "5V DC",
            tensaoEntrada = "5V DC",
            correnteMaxima = "15mA em operação",
            nivelLogico = "5V TTL",
            clock = "40 kHz (Frequência do pulso ultrassônico)",
            memoria = "N/A",
            interfaces = listOf("Pino Trigger (Entrada)", "Pino Echo (Saída)"),
        ),
        pinout = listOf(
            PinoutItem("VCC", "Alimentação de +5V", "Corrente de 5V do microcontrolador"),
            PinoutItem("Trig", "Trigger (Gatilho do Pulso)", "Dispara pulso ultrassônico de 10µs"),
            PinoutItem("Echo", "Echo (Retorno)", "Sinal de retorno do eco de ultrassom"),
            PinoutItem("GND", "Terra", "Terra comum"),
        ),
        alertas = listOf(
            AlertaItem(
                titulo = "Nível Lógico de 5V no Echo",
                descricao = "O pino Echo emite 5V. Proteja pinos de 3.3V.",
                nivel = "ATENCAO",
            ),
        ),
        codigo = CodigoExemplo(
            linguagem = "C++ (Arduino IDE)",
            descricao = "Código HC-SR04",
            codigo = "// Código exemplo",
        ),
    )
}
