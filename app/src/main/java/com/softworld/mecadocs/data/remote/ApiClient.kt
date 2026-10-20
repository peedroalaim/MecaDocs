package com.softworld.mecadocs.data.remote

import com.softworld.mecadocs.data.remote.interceptor.ApiRotationInterceptor
import com.softworld.mecadocs.data.remote.pool.ApiPoolManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Singleton para os serviços de API e IA da MecaDocs.
 * Gerencia a comunicação transparente com retentativas, resiliência e rotação automática
 * de provedores (Groq, OpenRouter, Gemini, MecaDocs Primary Gateway) via [ApiRotationInterceptor].
 */
object ApiClient {

    /**
     * Flag configurável para habilitar/desabilitar chamadas de rede em testes unitários ou modo estritamente offline.
     */
    @Volatile
    var isOnlineMode: Boolean = true

    @Volatile
    private var apiServiceInstance: ApiService? = null

    /**
     * Retorna se a aplicação possui algum provedor ativo disponível no pool e se o modo online está ativo.
     */
    fun isApiKeyConfigured(): Boolean {
        if (!isOnlineMode) return false
        val activeProvider = ApiPoolManager.getNextAvailableApi()
        return (activeProvider != null) && activeProvider.apiKey.isNotBlank()
    }

    /**
     * Retorna a instância do serviço Retrofit configurada com o [ApiRotationInterceptor].
     */
    fun getService(): ApiService {
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: buildRetrofit().create(ApiService::class.java).also {
                apiServiceInstance = it
            }
        }
    }

    private fun buildRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val rotationInterceptor = ApiRotationInterceptor(maxAttempts = ApiPoolManager.getProviders().size)

        // Timeouts estendidos (30s) para garantir tempo suficiente para geração de JSONs grandes por LLMs
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(rotationInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.mecadocs.softworld.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
