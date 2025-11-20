package com.example.waterchamp.data.remote

import android.content.Context
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import com.example.waterchamp.BuildConfig

/**
 * Singleton client para gerenciar conexão com Supabase
 *
 * Uso do Java:
 * SupabaseClient client = SupabaseClient.INSTANCE;
 * client.initialize(context);
 */
object SupabaseClient {
    private var supabaseUrl: String = ""
    private var supabaseKey: String = ""

    private var _client: io.github.jan.supabase.SupabaseClient? = null

    val client: io.github.jan.supabase.SupabaseClient
        get() = _client ?: throw IllegalStateException(
            "SupabaseClient não inicializado. Chame initialize(context) primeiro."
        )

    /**
     * Inicializa o cliente Supabase com credenciais do local.properties
     * Deve ser chamado no Application.onCreate() ou antes de usar o cliente
     */
    fun initialize(context: Context) {
        if (_client != null) return  // Já inicializado

        // Ler credenciais do local.properties
        loadCredentials(context)

        // Criar cliente Supabase
        _client = createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Postgrest)
            install(Auth)
        }
    }

    private fun loadCredentials(context: Context) {
        // Ler credenciais do BuildConfig (gerado a partir do local.properties)
        supabaseUrl = BuildConfig.SUPABASE_URL
        supabaseKey = BuildConfig.SUPABASE_KEY

        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty()) {
            throw IllegalStateException(
                "Credenciais Supabase não encontradas. " +
                "Configure supabase.url e supabase.key em local.properties e faça rebuild do projeto"
            )
        }
    }

    fun isInitialized(): Boolean = _client != null
}
