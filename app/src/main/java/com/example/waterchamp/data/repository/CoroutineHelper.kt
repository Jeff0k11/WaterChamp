package com.example.waterchamp.data.repository

import kotlinx.coroutines.*

/**
 * Helper para executar corrotinas de código Java
 * Simplificado para melhor interoperabilidade Java-Kotlin
 */
object CoroutineHelper {

    @JvmStatic
    fun <T> runAsync(task: suspend () -> T, onResult: (T?, String?) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    task()
                }
                onResult(result, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null, e.message ?: "Erro desconhecido")
            }
        }
    }
}
