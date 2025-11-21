package com.example.waterchamp.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Serviço para escutar atualizações em tempo real do ranking
 * Usa Supabase Realtime para monitorar mudanças na tabela consumo_diario
 *
 * NOTA: Este é um placeholder que será implementado conforme a API do Supabase Realtime
 * fica mais clara. Por enquanto, usaremos Pull-to-Refresh como método principal de atualização.
 */
class RankingRealtimeService {

    private var scope: CoroutineScope? = null
    private var isListening = false

    /**
     * Interface para callback de atualização do ranking
     */
    interface RankingUpdateListener {
        fun onRankingChanged()
        fun onRankingError(message: String)
    }

    /**
     * Inicia escuta de mudanças em tempo real
     * Monitora mudanças na tabela consumo_diario
     */
    suspend fun startListening(listener: RankingUpdateListener) {
        try {
            // Criar scope para gerenciar corrotinas
            scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            isListening = true

            // TODO: Implementar conexão com Supabase Realtime quando API ficar mais clara
            // Por enquanto, o Pull-to-Refresh funciona como alternativa

        } catch (e: Exception) {
            e.printStackTrace()
            listener.onRankingError("Erro ao iniciar realtime: ${e.message}")
        }
    }

    /**
     * Para escuta de mudanças em tempo real
     * Deve ser chamado no onPause() do Fragment
     */
    suspend fun stopListening() {
        try {
            scope?.cancel()
            isListening = false
            scope = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Verifica se está conectado ao realtime
     */
    fun isConnected(): Boolean = isListening
}
