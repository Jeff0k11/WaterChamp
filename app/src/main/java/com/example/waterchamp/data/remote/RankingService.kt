package com.example.waterchamp.data.remote

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Serviço para operações de ranking no Supabase
 */
class RankingService {

    @Serializable
    data class RankingEntry(
        val id: Int,
        val nome: String,
        val consumo_hoje: Int? = null,      // Para ranking diário
        val total_30_dias: Long? = null,    // Para ranking global
        val posicao: Long
    )

    /**
     * Buscar ranking diário (consumo de hoje)
     * Usa a view ranking_diario
     */
    suspend fun getDailyRanking(limit: Int = 100): List<RankingEntry> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("ranking_diario")
                .select {
                    limit(limit.toLong())
                }
                .decodeList<RankingEntry>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Buscar ranking global (últimos 30 dias)
     * Usa a view ranking_global
     */
    suspend fun getGlobalRanking(limit: Int = 100): List<RankingEntry> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("ranking_global")
                .select {
                    limit(limit.toLong())
                }
                .decodeList<RankingEntry>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Buscar posição de um usuário específico no ranking diário
     */
    suspend fun getUserDailyPosition(usuarioId: Int): Int? = withContext(Dispatchers.IO) {
        try {
            val ranking = getDailyRanking(1000)  // Buscar ranking completo
            val entry = ranking.find { it.id == usuarioId }
            entry?.posicao?.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Buscar posição de um usuário específico no ranking global
     */
    suspend fun getUserGlobalPosition(usuarioId: Int): Int? = withContext(Dispatchers.IO) {
        try {
            val ranking = getGlobalRanking(1000)  // Buscar ranking completo
            val entry = ranking.find { it.id == usuarioId }
            entry?.posicao?.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Buscar ranking de um grupo específico (consumo diário)
     */
    suspend fun getGroupDailyRanking(grupoId: Int): List<RankingEntry> = withContext(Dispatchers.IO) {
        try {
            // Buscar membros do grupo
            val membros = SupabaseClient.client
                .from("membros_grupo")
                .select {
                    filter {
                        eq("grupo_id", grupoId)
                    }
                }
                .decodeList<MembrosGrupo>()

            val userIds = membros.map { it.usuario_id }

            // Buscar ranking diário apenas dos membros
            val fullRanking = getDailyRanking(1000)
            fullRanking.filter { it.id in userIds }
                .sortedBy { it.posicao }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @Serializable
    private data class MembrosGrupo(
        val usuario_id: Int,
        val grupo_id: Int
    )

    // Blocking versions for Java interop
    fun getDailyRankingBlocking(limit: Int = 100): List<RankingEntry> = runBlocking { getDailyRanking(limit) }
    fun getGlobalRankingBlocking(limit: Int = 100): List<RankingEntry> = runBlocking { getGlobalRanking(limit) }
    fun getUserDailyPositionBlocking(usuarioId: Int): Int? = runBlocking { getUserDailyPosition(usuarioId) }
    fun getUserGlobalPositionBlocking(usuarioId: Int): Int? = runBlocking { getUserGlobalPosition(usuarioId) }
    fun getGroupDailyRankingBlocking(grupoId: Int): List<RankingEntry> = runBlocking { getGroupDailyRanking(grupoId) }
}
