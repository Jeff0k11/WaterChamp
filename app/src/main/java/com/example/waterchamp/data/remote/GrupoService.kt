package com.example.waterchamp.data.remote

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Serviço para operações de grupos no Supabase
 */
class GrupoService {

    @Serializable
    data class GrupoData(
        val id: Int? = null,
        val nome: String,
        val descricao: String,
        val criador_id: Int,
        val data_criacao: String? = null,
        val total_membros: Int? = 0
    )

    /**
     * Buscar todos os grupos do usuário
     */
    suspend fun getUserGroups(usuarioId: Int): List<GrupoData> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("membros_grupo")
                .select {
                    filter {
                        eq("usuario_id", usuarioId)
                    }
                }
                .decodeList<MembrosGrupoData>()
                .mapNotNull { membro ->
                    getGroupById(membro.grupo_id)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Buscar um grupo específico por ID
     */
    suspend fun getGroupById(grupoId: Int): GrupoData? = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("grupos")
                .select {
                    filter {
                        eq("id", grupoId)
                    }
                }
                .decodeSingleOrNull<GrupoData>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Criar um novo grupo
     */
    suspend fun createGroup(nome: String, descricao: String, criadorId: Int): GrupoData? = withContext(Dispatchers.IO) {
        try {
            val grupo = GrupoData(
                nome = nome,
                descricao = descricao,
                criador_id = criadorId
            )

            val result = SupabaseClient.client
                .from("grupos")
                .insert(grupo) {
                    select()
                }
                .decodeSingle<GrupoData>()

            // Adicionar criador como membro do grupo
            addMemberToGroup(result.id!!, criadorId)

            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletar um grupo
     */
    suspend fun deleteGroup(grupoId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            // Deletar membros primeiro
            SupabaseClient.client
                .from("membros_grupo")
                .delete {
                    filter {
                        eq("grupo_id", grupoId)
                    }
                }

            // Deletar grupo
            SupabaseClient.client
                .from("grupos")
                .delete {
                    filter {
                        eq("id", grupoId)
                    }
                }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Adicionar membro a um grupo
     */
    suspend fun addMemberToGroup(grupoId: Int, usuarioId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val membro = MembrosGrupoData(
                grupo_id = grupoId,
                usuario_id = usuarioId
            )

            SupabaseClient.client
                .from("membros_grupo")
                .insert(membro)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Remover membro de um grupo
     */
    suspend fun removeMemberFromGroup(grupoId: Int, usuarioId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("membros_grupo")
                .delete {
                    filter {
                        eq("grupo_id", grupoId)
                        eq("usuario_id", usuarioId)
                    }
                }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Buscar membros de um grupo
     */
    suspend fun getGroupMembers(grupoId: Int): List<Int> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("membros_grupo")
                .select {
                    filter {
                        eq("grupo_id", grupoId)
                    }
                }
                .decodeList<MembrosGrupoData>()
                .map { it.usuario_id }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Contar membros reais de um grupo
     */
    suspend fun countGroupMembers(grupoId: Int): Int = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("membros_grupo")
                .select {
                    filter {
                        eq("grupo_id", grupoId)
                    }
                }
                .decodeList<MembrosGrupoData>()
                .size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    // ============ Data Classes ============

    @Serializable
    private data class MembrosGrupoData(
        val grupo_id: Int,
        val usuario_id: Int
    )

    // ============ Blocking versions for Java interop ============

    fun getUserGroupsBlocking(usuarioId: Int): List<GrupoData> = runBlocking { getUserGroups(usuarioId) }

    fun getGroupByIdBlocking(grupoId: Int): GrupoData? = runBlocking { getGroupById(grupoId) }

    fun createGroupBlocking(nome: String, descricao: String, criadorId: Int): GrupoData? = runBlocking { createGroup(nome, descricao, criadorId) }

    fun deleteGroupBlocking(grupoId: Int): Boolean = runBlocking { deleteGroup(grupoId) }

    fun addMemberToGroupBlocking(grupoId: Int, usuarioId: Int): Boolean = runBlocking { addMemberToGroup(grupoId, usuarioId) }

    fun removeMemberFromGroupBlocking(grupoId: Int, usuarioId: Int): Boolean = runBlocking { removeMemberFromGroup(grupoId, usuarioId) }

    fun getGroupMembersBlocking(grupoId: Int): List<Int> = runBlocking { getGroupMembers(grupoId) }

    fun countGroupMembersBlocking(grupoId: Int): Int = runBlocking { countGroupMembers(grupoId) }
}
