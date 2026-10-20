package com.softworld.mecadocs.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa os dados técnicos detalhados e documentação de um componente eletrônico.
 * Compatível com deserialização JSON via Moshi e kotlinx.serialization.
 * Contém valores padrão em todos os campos para garantir resiliência total contra respostas incompletas de IAs.
 */
@JsonClass(generateAdapter = true)
data class Componente(
    @param:Json(name = "nome")
    val nome: String = "Componente Eletrônico",

    @param:Json(name = "descricao")
    val descricao: String = "",

    @param:Json(name = "categoria")
    val categoria: String = "Geral",

    @param:Json(name = "specs")
    val specs: SpecsData = SpecsData(),

    @param:Json(name = "pinout")
    val pinout: List<PinoutItem> = emptyList(),

    @param:Json(name = "alertas")
    val alertas: List<AlertaItem> = emptyList(),

    @param:Json(name = "codigo")
    val codigo: CodigoExemplo? = null,
)

/**
 * Alias para DatasheetResponse conforme requisitos do domínio.
 */
typealias DatasheetResponse = Componente

/**
 * Especificações elétricas e técnicas do componente.
 */
@JsonClass(generateAdapter = true)
data class SpecsData(
    @param:Json(name = "tensaoOperacao")
    val tensaoOperacao: String = "N/A",

    @param:Json(name = "tensaoEntrada")
    val tensaoEntrada: String = "N/A",

    @param:Json(name = "correnteMaxima")
    val correnteMaxima: String = "N/A",

    @param:Json(name = "nivelLogico")
    val nivelLogico: String = "N/A",

    @param:Json(name = "clock")
    val clock: String = "N/A",

    @param:Json(name = "memoria")
    val memoria: String = "N/A",

    @param:Json(name = "interfaces")
    val interfaces: List<String> = emptyList(),
)

/**
 * Mapeamento e função de cada pino (pinout).
 */
@JsonClass(generateAdapter = true)
data class PinoutItem(
    @param:Json(name = "pino")
    val pino: String = "Pino",

    @param:Json(name = "funcao")
    val funcao: String = "",

    @param:Json(name = "notas")
    val notas: String = "",
)

/**
 * Avisos e cuidados elétricos cruciais para evitar danos aos componentes.
 * Nível pode ser: "INFO", "ATENCAO", "PERIGO".
 */
@JsonClass(generateAdapter = true)
data class AlertaItem(
    @param:Json(name = "titulo")
    val titulo: String = "Aviso",

    @param:Json(name = "descricao")
    val descricao: String = "",

    @param:Json(name = "nivel")
    val nivel: String = "ATENCAO",
)

/**
 * Exemplo de código funcional e pronto para uso (Arduino C++, MicroPython, etc.).
 */
@JsonClass(generateAdapter = true)
data class CodigoExemplo(
    @param:Json(name = "linguagem")
    val linguagem: String = "C++",

    @param:Json(name = "codigo")
    val codigo: String = "",

    @param:Json(name = "descricao")
    val descricao: String = "",
)
