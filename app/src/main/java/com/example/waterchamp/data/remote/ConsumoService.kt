package com.example.waterchamp.data.remote

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.*

/**
 * Serviço para operações de consumo diário no Supabase
 */
class ConsumoService {

    @Serializable
    data class ConsumoDiario(
        val id: Int? = null,
        val usuario_id: Int,
        val data: String,  // formato: yyyy-MM-dd
        val total_ml: Int
    )

    @Serializable
    data class SetConsumoParams(
        val p_usuario_id: Int,
        val p_data: String,
        val p_total_ml: Int
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Sincronizar consumo diário usando a função RPC set_consumo_diario
     * Faz upsert (insert ou update) do consumo
     */
    suspend fun syncDailyConsumption(
        usuarioId: Int,
        data: Date,
        totalMl: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val dateStr = dateFormat.format(data)

            val params = buildJsonObject {
                put("p_usuario_id", usuarioId)
                put("p_data", dateStr)
                put("p_total_ml", totalMl)
            }

            SupabaseClient.client.postgrest.rpc(
                function = "set_consumo_diario",
                parameters = params
            )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Buscar consumo de um usuário em uma data específica
     */
    suspend fun getConsumptionByDate(
        usuarioId: Int,
        data: Date
    ): ConsumoDiario? = withContext(Dispatchers.IO) {
        try {
            val dateStr = dateFormat.format(data)

            SupabaseClient.client
                .from("consumo_diario")
                .select {
                    filter {
                        eq("usuario_id", usuarioId)
                        eq("data", dateStr)
                    }
                }
                .decodeSingleOrNull<ConsumoDiario>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Buscar histórico de consumo de um usuário (últimos N dias)
     */
    suspend fun getConsumptionHistory(
        usuarioId: Int,
        days: Int = 30
    ): List<ConsumoDiario> = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            val startDate = dateFormat.format(calendar.time)

            SupabaseClient.client
                .from("consumo_diario")
                .select {
                    filter {
                        eq("usuario_id", usuarioId)
                        gte("data", startDate)
                    }
                    order("data", Order.DESCENDING)
                }
                .decodeList<ConsumoDiario>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Buscar total consumido nos últimos N dias
     */
    suspend fun getTotalConsumption(
        usuarioId: Int,
        days: Int = 30
    ): Int = withContext(Dispatchers.IO) {
        try {
            val history = getConsumptionHistory(usuarioId, days)
            history.sumOf { it.total_ml }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Calcular sequência de dias (streak) - dias consecutivos atingindo meta
     * @param metaDiaria meta em ml
     */
    suspend fun calculateStreak(
        usuarioId: Int,
        metaDiaria: Int
    ): Int = withContext(Dispatchers.IO) {
        try {
            val history = getConsumptionHistory(usuarioId, 365)
                .sortedByDescending { it.data }

            var streak = 0
            val today = dateFormat.format(Date())
            val calendar = Calendar.getInstance()

            // Verificar dias consecutivos a partir de hoje
            for (i in 0 until history.size) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val expectedDate = dateFormat.format(calendar.time)

                val consumo = history.find { it.data == expectedDate }
                if (consumo != null && consumo.total_ml >= metaDiaria) {
                    streak++
                } else if (i > 0) {
                    // Se não é hoje e não atingiu meta, quebra a sequência
                    break
                }
            }

            streak
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    // Blocking versions for Java interop
    fun syncDailyConsumptionBlocking(usuarioId: Int, data: Date, totalMl: Int): Boolean = runBlocking { syncDailyConsumption(usuarioId, data, totalMl) }
    fun getConsumptionByDateBlocking(usuarioId: Int, data: Date): ConsumoDiario? = runBlocking { getConsumptionByDate(usuarioId, data) }
    fun getConsumptionHistoryBlocking(usuarioId: Int, days: Int): List<ConsumoDiario> = runBlocking { getConsumptionHistory(usuarioId, days) }
    fun calculateStreakBlocking(usuarioId: Int, metaDiaria: Int): Int = runBlocking { calculateStreak(usuarioId, metaDiaria) }
}
