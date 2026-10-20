package com.softworld.mecadocs.data.remote

import com.softworld.mecadocs.data.model.Componente
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit para consulta de documentação e geração de datasheets via API.
 * Suporta tanto APIs REST diretas quanto endpoints baseados em LLM / IA generativa (Gemini/OpenAI).
 */
interface ApiService {

    /**
     * Consulta direta de componente por nome em endpoint REST compatível.
     */
    @GET("api/v1/components/{name}")
    suspend fun getComponentByName(
        @Path("name") name: String,
        @Header("Authorization") authHeader: String? = null,
    ): Response<Componente>

    /**
     * Busca ou gera documentação de componente enviando prompt/query para endpoint de IA/Search.
     * Interceptado pelo [ApiRotationInterceptor] para rotação automática entre Groq, OpenRouter e Gemini.
     */
    @POST("api/v1/datasheet/generate")
    suspend fun generateDatasheet(
        @Body request: GenerateDatasheetRequest,
        @Header("Authorization") authHeader: String? = null,
    ): Response<ResponseBody>

    /**
     * Endpoint alternativo para consulta via query param.
     */
    @GET("api/v1/components/search")
    suspend fun searchComponent(
        @Query("query") query: String,
        @Header("Authorization") authHeader: String? = null,
    ): Response<List<Componente>>
}

/**
 * Payload de requisição para geração de datasheet via IA/Backend.
 */
@JsonClass(generateAdapter = true)
data class GenerateDatasheetRequest(
    @param:Json(name = "componentName")
    val componentName: String,

    @param:Json(name = "language")
    val language: String = "pt-BR",
)
