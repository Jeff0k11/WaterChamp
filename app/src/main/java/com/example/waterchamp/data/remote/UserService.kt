package com.example.waterchamp.data.remote

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Serviço para operações de usuários no Supabase
 */
class UserService {

    @Serializable
    data class Usuario(
        val id: Int? = null,
        val nome: String,
        val email: String,
        val senha_hash: String? = null,
        val data_criacao: String? = null
    )

    /**
     * Registrar novo usuário
     * @return ID do usuário criado ou null em caso de erro
     */
    suspend fun registerUser(nome: String, email: String, senha: String): Int? = withContext(Dispatchers.IO) {
        try {
            // 1. Criar usuário na autenticação Supabase
            SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = senha
            }

            // 2. Inserir dados públicos na tabela usuarios
            val usuario = Usuario(
                nome = nome,
                email = email
            )

            val result = SupabaseClient.client
                .from("usuarios")
                .insert(usuario) {
                    select()
                }
                .decodeSingle<Usuario>()

            result.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Fazer login
     * @return ID do usuário ou null em caso de erro
     */
    suspend fun login(email: String, senha: String): Int? = withContext(Dispatchers.IO) {
        try {
            // Login via Supabase Auth
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = senha
            }

            // Buscar dados do usuário
            val usuario = SupabaseClient.client
                .from("usuarios")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeSingle<Usuario>()

            usuario.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Fazer logout
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Buscar usuário por email
     */
    suspend fun getUserByEmail(email: String): Usuario? = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("usuarios")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeSingleOrNull<Usuario>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Buscar usuário por ID
     */
    suspend fun getUserById(id: Int): Usuario? = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("usuarios")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<Usuario>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Verificar se usuário está autenticado
     */
    suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
        try {
            val session = SupabaseClient.client.auth.currentSessionOrNull()
            session != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obter email do usuário autenticado
     */
    suspend fun getCurrentUserEmail(): String? = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.auth.currentUserOrNull()?.email
        } catch (e: Exception) {
            null
        }
    }

    // Blocking versions for Java interop

    fun registerUserBlocking(nome: String, email: String, senha: String): Int? = runBlocking { registerUser(nome, email, senha) }

    fun loginBlocking(email: String, senha: String): Int? = runBlocking { login(email, senha) }

    fun logoutBlocking() = runBlocking { logout() }

    fun getUserByIdBlocking(id: Int): Usuario? = runBlocking { getUserById(id) }
}
