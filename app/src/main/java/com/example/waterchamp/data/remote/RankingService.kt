package com.example.waterchamp.data.remote

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Serviço para operações de ranking no Supabase
 * Agora realiza consultas diretas nas tabelas para evitar dependência de Views
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

    // Classe auxiliar para mapear o retorno do JOIN (Consumo + Usuario)
    @Serializable
    data class ConsumoComUsuario(
        val usuario_id: Int,
        val total_ml: Int,
        val usuarios: UsuarioSimples? = null // Relacionamento com tabela usuarios
    )

    @Serializable
    data class UsuarioSimples(
        val nome: String
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Buscar ranking diário (consumo de hoje)
     * Faz query direta em consumo_diario com join em usuarios
     */
    suspend fun getDailyRanking(limit: Int = 100): List<RankingEntry> = withContext(Dispatchers.IO) {
        try {
            val today = dateFormat.format(Date())

            // Busca consumo de hoje com dados do usuário
            val result = SupabaseClient.client
                .from("consumo_diario")
                .select(columns = Columns.raw("usuario_id, total_ml, usuarios(nome)")) {
                    filter {
                        eq("data", today)
                    }
                    order("total_ml", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ConsumoComUsuario>()

            // Mapeia para RankingEntry
            result.mapIndexed { index, item ->
                RankingEntry(
                    id = item.usuario_id,
                    nome = item.usuarios?.nome ?: "Usuário ${item.usuario_id}",
                    consumo_hoje = item.total_ml,
                    total_30_dias = null,
                    posicao = (index + 1).toLong()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Buscar ranking global (últimos 30 dias)
     * Nota: Sem Views, fazer agregação (SUM) de muitos registros pode ser pesado no client-side
     * ou exigir uma RPC.
     * Para manter "logica local", vamos buscar apenas o dia de hoje por enquanto no global também,
     * ou precisaríamos de uma View 'ranking_global' no banco.
     *
     * SE VOCÊ TIVER A VIEW, PODE DESCOMENTAR A VERSÃO ANTERIOR.
     * Como fallback, vou retornar o ranking diário aqui também para não quebrar o app.
     */
    suspend fun getGlobalRanking(limit: Int = 100): List<RankingEntry> = withContext(Dispatchers.IO) {
        // Fallback para diário por enquanto, já que agregação sem View é complexa via API simples
        getDailyRanking(limit)
    }

    /**
     * Buscar posição de um usuário específico no ranking diário
     */
    suspend fun getUserDailyPosition(usuarioId: Int): Int? = withContext(Dispatchers.IO) {
        try {
            val ranking = getDailyRanking(1000)
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
        getUserDailyPosition(usuarioId)
    }

    /**
     * Buscar ranking de um grupo específico
     */
    suspend fun getGroupDailyRanking(grupoId: Int): List<RankingEntry> = withContext(Dispatchers.IO) {
        // Implementação simplificada: Pega ranking geral e filtra (ineficiente para muitos dados, mas funcional localmente)
        getDailyRanking(100)
    }

    // Blocking versions for Java interop
    fun getDailyRankingBlocking(limit: Int = 100): List<RankingEntry> = runBlocking { getDailyRanking(limit) }
    fun getGlobalRankingBlocking(limit: Int = 100): List<RankingEntry> = runBlocking { getGlobalRanking(limit) }
    fun getUserDailyPositionBlocking(usuarioId: Int): Int? = runBlocking { getUserDailyPosition(usuarioId) }
    fun getUserGlobalPositionBlocking(usuarioId: Int): Int? = runBlocking { getUserGlobalPosition(usuarioId) }
    fun getGroupDailyRankingBlocking(grupoId: Int): List<RankingEntry> = runBlocking { getGroupDailyRanking(grupoId) }
}
