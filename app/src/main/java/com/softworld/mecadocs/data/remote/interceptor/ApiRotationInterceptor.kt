package com.softworld.mecadocs.data.remote.interceptor

import com.softworld.mecadocs.data.remote.adapter.AiPayloadAdapter
import com.softworld.mecadocs.data.remote.pool.AllProvidersExhaustedException
import com.softworld.mecadocs.data.remote.pool.ApiPoolManager
import com.softworld.mecadocs.data.remote.pool.PayloadType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * Interceptor OkHttp de Rotação e Contingência.
 * Intercepta as requisições de rede, extrai o nome do componente pesquisado pelo usuário no corpo da requisição,
 * obtém a API ativa livre do [ApiPoolManager], resolve a URL e o payload JSON conforme o [PayloadType] (OpenAI vs Gemini),
 * e realiza rotação automática em caso de erros de requisição (`400`), autenticação (`401`, `403`), limite (`429`), `5xx` ou `Timeout`.
 */
class ApiRotationInterceptor(
    private val maxAttempts: Int = 4,
) : Interceptor {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val mapAdapter = moshi.adapter(Map::class.java)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Se a requisição não for de geração de datasheet, prossegue normalmente
        if (!originalRequest.url.encodedPath.contains("generate") && !originalRequest.url.encodedPath.contains("datasheet")) {
            return chain.proceed(originalRequest)
        }

        // Extrai o nome do componente enviado no corpo da requisição original (ex: "módulo relé")
        var componentName = "ESP32"
        if (originalRequest.method == "POST" && originalRequest.body != null) {
            try {
                val buffer = Buffer()
                originalRequest.body?.writeTo(buffer)
                val jsonBody = buffer.readUtf8()
                val map = mapAdapter.fromJson(jsonBody)
                val extracted = map?.get("componentName") as? String
                if (!extracted.isNullOrBlank()) {
                    componentName = extracted.trim()
                }
            } catch (_: Exception) {
                // Mantém o padrão caso falhe a leitura do buffer
            }
        }

        var attempts = 0
        var lastException: Exception? = null
        val totalProviders = ApiPoolManager.getProviders().size
        val effectiveMaxAttempts = if (maxAttempts < totalProviders) totalProviders else maxAttempts

        while (attempts < effectiveMaxAttempts) {
            attempts++
            val provider = ApiPoolManager.getNextAvailableApi()
                ?: throw AllProvidersExhaustedException(
                    "Nenhum provedor de IA livre encontrado no pool. Todos estão em cooldown.",
                    lastException
                )

            val newRequest = buildAdaptedRequest(originalRequest, provider.baseUrl, provider.apiKey, provider.payloadType, provider.id, componentName)

            try {
                val response = chain.proceed(newRequest)

                // Se retornar erro de requisição (400), autenticação (401, 403), limite (429) ou indisponibilidade de servidor (500..504)
                if (response.code == 400 || response.code == 401 || response.code == 403 || response.code == 429 || (response.code in 500..504)) {
                    response.close()
                    ApiPoolManager.markCooldown(provider.id, durationMs = 60_000L)
                    continue
                }

                return response
            } catch (e: IOException) {
                lastException = e
                ApiPoolManager.markCooldown(provider.id, durationMs = 60_000L)
            }
        }

        throw AllProvidersExhaustedException(
            "Esgotadas as $effectiveMaxAttempts tentativas de rotação no pool de IAs. Última falha: ${lastException?.message}",
            lastException
        )
    }

    private fun buildAdaptedRequest(
        originalRequest: Request,
        baseUrl: String,
        apiKey: String,
        payloadType: PayloadType,
        providerId: String,
        componentName: String,
    ): Request {
        val parsedBaseUrl = baseUrl.toHttpUrlOrNull() ?: originalRequest.url

        val targetPath = when (payloadType) {
            PayloadType.OPEN_AI -> "v1/chat/completions"
            PayloadType.GEMINI -> "v1beta/models/gemini-1.5-pro:generateContent"
        }

        val resolvedUrl = parsedBaseUrl.resolve(targetPath) ?: originalRequest.url
        val newUrlBuilder = resolvedUrl.newBuilder()

        if (payloadType == PayloadType.GEMINI) {
            newUrlBuilder.setQueryParameter("key", apiKey)
        }

        val requestBuilder = originalRequest.newBuilder()
            .url(newUrlBuilder.build())
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("x-provider-id", providerId)

        if (payloadType == PayloadType.OPEN_AI) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }

        // Reescreve o corpo do envio para o padrão nativo do provedor utilizando o nome do componente pesquisado
        if (originalRequest.method == "POST") {
            val adaptedBody = AiPayloadAdapter.createRequestBody(componentName, payloadType, providerId = providerId)
            requestBuilder.post(adaptedBody)
        }

        return requestBuilder.build()
    }
}
