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
     * @return Pair contendo o ID do usuário ou uma mensagem de erro
     */
    suspend fun registerUser(nome: String, email: String, senha: String): Pair<Int?, String?> = withContext(Dispatchers.IO) {
        try {
            // 1. Criar usuário na autenticação Supabase
            // IMPORTANTE: Desative a confirmação de e-mail no painel do Supabase se não for usá-la
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

            Pair(result.id, null)
        } catch (e: Exception) {
            e.printStackTrace()
            // Retorna a mensagem de erro específica da exceção
            Pair(null, e.message ?: "Falha no cadastro. Verifique os dados e tente novamente.")
        }
    }

    /**
     * Fazer login
     * @return Pair contendo ID do usuário ou mensagem de erro
     */
    suspend fun login(email: String, senha: String): Pair<Int?, String?> = withContext(Dispatchers.IO) {
        try {
            // 1. Login via Supabase Auth
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = senha
            }

            // 2. Buscar dados do usuário na tabela
            val usuario = SupabaseClient.client
                .from("usuarios")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeSingleOrNull<Usuario>()

            if (usuario == null) {
                Pair(null, "Usuário não encontrado no banco de dados.")
            } else {
                Pair(usuario.id, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = when {
                e.message?.contains("Invalid login credentials") == true ->
                    "Email ou senha inválidos."
                e.message?.contains("Email not confirmed") == true ->
                    "Email não confirmado. Verifique sua caixa de entrada."
                else ->
                    "Erro ao fazer login: ${e.message}"
            }
            Pair(null, errorMsg)
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

    fun registerUserBlocking(nome: String, email: String, senha: String): Pair<Int?, String?> = runBlocking { registerUser(nome, email, senha) }

    fun loginBlocking(email: String, senha: String): Pair<Int?, String?> = runBlocking { login(email, senha) }

    fun logoutBlocking() = runBlocking { logout() }

    fun getUserByIdBlocking(id: Int): Usuario? = runBlocking { getUserById(id) }
}
