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

        CoroutineHelper.runAsync(
            () -> grupoService.getUserGroupsBlocking(userId),
            new CoroutineHelper.CoroutineCallback<List<GrupoService.GrupoData>>() {
                @Override
                public void onComplete(List<GrupoService.GrupoData> grupos, String error) {
                    if (error != null) {
                        callback.onError("Erro: " + error);
                    } else if (grupos != null && !grupos.isEmpty()) {
                        // Converter de GrupoData para Group e buscar contagem real de membros
                        List<Group> groupList = new ArrayList<>();
                        for (GrupoService.GrupoData grupo : grupos) {
                            // Buscar contagem real de membros
                            int realMemberCount = grupoService.countGroupMembersBlocking(grupo.getId());

                            Group group = new Group(
                                grupo.getId(),
                                grupo.getNome(),
                                grupo.getDescricao(),
                                grupo.getCriador_id(),
                                grupo.getData_criacao(),
                                realMemberCount
                            );
                            groupList.add(group);
                        }
                        callback.onSuccess(groupList);
                    } else {
                        callback.onSuccess(new ArrayList<>()); // Lista vazia
                    }
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

        CoroutineHelper.runAsync(
            () -> grupoService.createGroupBlocking(nome, descricao, userId),
            new CoroutineHelper.CoroutineCallback<GrupoService.GrupoData>() {
                @Override
                public void onComplete(GrupoService.GrupoData grupo, String error) {
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

        CoroutineHelper.runAsync(
            () -> grupoService.deleteGroupBlocking(groupId),
            new CoroutineHelper.CoroutineCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success, String error) {
                    if (error != null) {
                        callback.onError("Erro: " + error);
                    } else if (Boolean.TRUE.equals(success)) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Falha ao deletar grupo");
                    }
                }
            }
        );
    }

    /**
     * Adicionar membro a um grupo
     */
    public void addMemberToGroup(int groupId, int userId, OperationCallback callback) {
        CoroutineHelper.runAsync(
            () -> grupoService.addMemberToGroupBlocking(groupId, userId),
            new CoroutineHelper.CoroutineCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success, String error) {
                    if (error != null) {
                        callback.onError("Erro: " + error);
                    } else if (Boolean.TRUE.equals(success)) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Falha ao adicionar membro");
                    }
                }
            }
        );
    }

    /**
     * Remover membro de um grupo
     */
    public void removeMemberFromGroup(int groupId, int userId, OperationCallback callback) {
        CoroutineHelper.runAsync(
            () -> grupoService.removeMemberFromGroupBlocking(groupId, userId),
            new CoroutineHelper.CoroutineCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success, String error) {
                    if (error != null) {
                        callback.onError("Erro: " + error);
                    } else if (Boolean.TRUE.equals(success)) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Falha ao remover membro");
                    }
                }
            }
        );
    }

    /**
     * Entrar em um grupo (máximo 1 grupo por usuário)
     */
    public void joinGroup(int groupId, OperationCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        // Verificar se usuário já está em um grupo
        getUserGroups(new GruposCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (groups != null && !groups.isEmpty()) {
                    // Usuário já está em um grupo
                    callback.onError("Você só pode estar em um grupo por vez. Deixe o grupo atual primeiro.");
                    return;
                }

                // Adicionar usuário ao grupo
                CoroutineHelper.runAsync(
                    () -> grupoService.addMemberToGroupBlocking(groupId, userId),
                    new CoroutineHelper.CoroutineCallback<Boolean>() {
                        @Override
                        public void onComplete(Boolean success, String error) {
                            if (error != null) {
                                callback.onError("Erro: " + error);
                            } else if (Boolean.TRUE.equals(success)) {
                                callback.onSuccess();
                            } else {
                                callback.onError("Falha ao entrar no grupo");
                            }
                        }
                    }
                );
            }

            @Override
            public void onError(String message) {
                callback.onError("Erro ao verificar grupos: " + message);
            }
        });
    }

    /**
     * Sair de um grupo
     * Se for o único membro, deleta o grupo automaticamente
     */
    public void leaveGroup(Group group, OperationCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        // Remover usuário do grupo
        CoroutineHelper.runAsync(
            () -> grupoService.removeMemberFromGroupBlocking(group.getId(), userId),
            new CoroutineHelper.CoroutineCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success, String error) {
                    if (error != null) {
                        callback.onError("Erro: " + error);
                    } else if (Boolean.TRUE.equals(success)) {
                        // Verificar se o grupo ficou vazio
                        CoroutineHelper.runAsync(
                            () -> grupoService.getGroupMembersBlocking(group.getId()),
                            new CoroutineHelper.CoroutineCallback<List<Integer>>() {
                                @Override
                                public void onComplete(List<Integer> membros, String error) {
                                    if (error != null) {
                                        // Erro ao verificar membros, mas usuário foi removido
                                        callback.onSuccess();
                                    } else if (membros == null || membros.isEmpty()) {
                                        // Grupo ficou vazio, deletar
                                        CoroutineHelper.runAsync(
                                            () -> grupoService.deleteGroupBlocking(group.getId()),
                                            new CoroutineHelper.CoroutineCallback<Boolean>() {
                                                @Override
                                                public void onComplete(Boolean deleted, String deleteError) {
                                                    if (deleteError != null) {
                                                        // Falha ao deletar, mas usuário foi removido
                                                        callback.onSuccess();
                                                    } else {
                                                        callback.onSuccess();
                                                    }
                                                }
                                            }
                                        );
                                    } else {
                                        // Grupo ainda tem membros
                                        callback.onSuccess();
                                    }
                                }
                            }
                        );
                    } else {
                        callback.onError("Falha ao sair do grupo");
                    }
                }
            }
        );
    }
}
