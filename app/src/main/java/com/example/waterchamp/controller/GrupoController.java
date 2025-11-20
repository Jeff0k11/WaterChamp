package com.example.waterchamp.controller;

import android.content.Context;

import com.example.waterchamp.data.repository.GrupoRepository;
import com.example.waterchamp.model.Group;

import java.util.List;

/**
 * Controller para gerenciar operações de grupos
 * Implementa padrão MVP (Model-View-Presenter)
 */
public class GrupoController {

    private GrupoView view;
    private GrupoRepository grupoRepository;

    public interface GrupoView {
        void showGroups(List<Group> groups);
        void showError(String message);
        void showSuccess(String message);
        void showLoading();
        void hideLoading();
    }

    public GrupoController(GrupoView view, Context context) {
        this.view = view;
        this.grupoRepository = new GrupoRepository(context);
    }

    /**
     * Carrega todos os grupos do usuário logado
     */
    public void loadUserGroups() {
        view.showLoading();
        grupoRepository.getUserGroups(new GrupoRepository.GruposCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                view.hideLoading();
                view.showGroups(groups);
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                view.showError(message);
                // Mostrar lista vazia em caso de erro
                view.showGroups(null);
            }
        });
    }

    /**
     * Cria um novo grupo
     */
    public void createGroup(String nome, String descricao) {
        if (nome == null || nome.isEmpty()) {
            view.showError("Nome do grupo não pode estar vazio");
            return;
        }

        view.showLoading();
        grupoRepository.createGroup(nome, descricao, new GrupoRepository.CreateGroupCallback() {
            @Override
            public void onSuccess(Group group) {
                view.hideLoading();
                view.showSuccess("Grupo '" + group.getNome() + "' criado com sucesso!");
                // Recarregar lista de grupos
                loadUserGroups();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    /**
     * Deleta um grupo
     */
    public void deleteGroup(int groupId) {
        view.showLoading();
        grupoRepository.deleteGroup(groupId, new GrupoRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                // Recarregar lista de grupos
                loadUserGroups();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    /**
     * Adiciona um membro a um grupo
     */
    public void addMemberToGroup(int groupId, int userId) {
        grupoRepository.addMemberToGroup(groupId, userId, new GrupoRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                // Recarregar lista de grupos
                loadUserGroups();
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    /**
     * Remove um membro de um grupo
     */
    public void removeMemberFromGroup(int groupId, int userId) {
        grupoRepository.removeMemberFromGroup(groupId, userId, new GrupoRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                // Recarregar lista de grupos
                loadUserGroups();
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    /**
     * Entrar em um grupo específico (máximo 1 grupo por usuário)
     */
    public void joinGroup(int groupId) {
        view.showLoading();
        grupoRepository.joinGroup(groupId, new GrupoRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                // Recarregar lista de grupos
                loadUserGroups();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }
}
