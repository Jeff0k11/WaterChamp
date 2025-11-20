package com.example.waterchamp.data.repository;

import android.content.Context;

import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.data.remote.GrupoService;
import com.example.waterchamp.model.Group;
import com.example.waterchamp.utils.CoroutineHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository para gerenciar operações de grupos
 * Coordena entre serviço remoto (Supabase) e cache local
 */
public class GrupoRepository {

    private final GrupoService grupoService;
    private final PreferencesManager prefsManager;

    public GrupoRepository(Context context) {
        this.grupoService = new GrupoService();
        this.prefsManager = new PreferencesManager(context);
    }

    // ============ Callbacks ============

    public interface GruposCallback {
        void onSuccess(List<Group> groups);
        void onError(String message);
    }

    public interface CreateGroupCallback {
        void onSuccess(Group group);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    // ============ Métodos públicos ============

    /**
     * Buscar todos os grupos do usuário logado
     */
    public void getUserGroups(GruposCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        CoroutineHelper.<List<GrupoService.GrupoData>>runAsync(
            () -> grupoService.getUserGroupsBlocking(userId),
            (List<GrupoService.GrupoData> grupos, String error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else if (grupos != null && !grupos.isEmpty()) {
                    // Converter de GrupoData para Group
                    List<Group> groupList = new ArrayList<>();
                    for (GrupoService.GrupoData grupo : grupos) {
                        Group group = new Group(
                            grupo.getId(),
                            grupo.getNome(),
                            grupo.getDescricao(),
                            grupo.getCriador_id(),
                            grupo.getData_criacao(),
                            grupo.getTotal_membros()
                        );
                        groupList.add(group);
                    }
                    callback.onSuccess(groupList);
                } else {
                    callback.onSuccess(new ArrayList<>()); // Lista vazia
                }
            }
        );
    }

    /**
     * Criar um novo grupo
     */
    public void createGroup(String nome, String descricao, CreateGroupCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        CoroutineHelper.<GrupoService.GrupoData>runAsync(
            () -> grupoService.createGroupBlocking(nome, descricao, userId),
            (GrupoService.GrupoData grupo, String error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else if (grupo != null) {
                    Group group = new Group(
                        grupo.getId(),
                        grupo.getNome(),
                        grupo.getDescricao(),
                        grupo.getCriador_id(),
                        grupo.getData_criacao(),
                        grupo.getTotal_membros()
                    );
                    callback.onSuccess(group);
                } else {
                    callback.onError("Falha ao criar grupo");
                }
            }
        );
    }

    /**
     * Deletar um grupo
     */
    public void deleteGroup(int groupId, OperationCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        CoroutineHelper.<Boolean>runAsync(
            () -> grupoService.deleteGroupBlocking(groupId),
            (Boolean success, String error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else if (Boolean.TRUE.equals(success)) {
                    callback.onSuccess();
                } else {
                    callback.onError("Falha ao deletar grupo");
                }
            }
        );
    }

    /**
     * Adicionar membro a um grupo
     */
    public void addMemberToGroup(int groupId, int userId, OperationCallback callback) {
        CoroutineHelper.<Boolean>runAsync(
            () -> grupoService.addMemberToGroupBlocking(groupId, userId),
            (Boolean success, String error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else if (Boolean.TRUE.equals(success)) {
                    callback.onSuccess();
                } else {
                    callback.onError("Falha ao adicionar membro");
                }
            }
        );
    }

    /**
     * Remover membro de um grupo
     */
    public void removeMemberFromGroup(int groupId, int userId, OperationCallback callback) {
        CoroutineHelper.<Boolean>runAsync(
            () -> grupoService.removeMemberFromGroupBlocking(groupId, userId),
            (Boolean success, String error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else if (Boolean.TRUE.equals(success)) {
                    callback.onSuccess();
                } else {
                    callback.onError("Falha ao remover membro");
                }
            }
        );
    }
}
