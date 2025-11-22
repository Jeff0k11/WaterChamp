package com.example.waterchamp.controller;

import android.content.Context;
import com.example.waterchamp.data.repository.RankingRepository;
import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankingController {
    private RankingView view;
    private RankingRepository rankingRepository;

    public RankingController(RankingView view, Context context) {
        this.view = view;
        this.rankingRepository = new RankingRepository(context);
    }

    public void updateRanking() {
        // Buscar ranking diário do servidor
        rankingRepository.getDailyRanking(100, new RankingRepository.RankingCallback() {
            @Override
            public void onSuccess(List<User> users) {
                // Atribuir ranks
                for (int i = 0; i < users.size(); i++) {
                    users.get(i).setRank(i + 1);
                }

                // Atualizar UserDatabase para compatibilidade (cache local)
                UserDatabase.usersList.clear();
                UserDatabase.usersList.addAll(users);

                // Ensure current user is in the list
                if (UserDatabase.currentUser != null) {
                    boolean found = false;
                    for (int i = 0; i < users.size(); i++) {
                        if (users.get(i).getEmail().equals(UserDatabase.currentUser.getEmail())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        users.add(UserDatabase.currentUser);
                    }
                }

                view.displayRanking(users);
            }

            @Override
            public void onError(String message) {
                // Em caso de erro, usar dados locais (fallback)
                List<User> rankingList = new ArrayList<>(UserDatabase.usersList);

                if (UserDatabase.currentUser != null) {
                    boolean found = false;
                    for (int i = 0; i < rankingList.size(); i++) {
                        if (rankingList.get(i).getEmail().equals(UserDatabase.currentUser.getEmail())) {
                            rankingList.set(i, UserDatabase.currentUser);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        rankingList.add(UserDatabase.currentUser);
                    }
                }

                Collections.sort(rankingList);
                for (int i = 0; i < rankingList.size(); i++) {
                    rankingList.get(i).setRank(i + 1);
                }

                view.displayRanking(rankingList);
                view.showError("Erro ao carregar ranking: " + message);
            }
        });
    }

    /**
     * Carregar ranking do grupo do usuário logado
     */
    public void updateGroupRanking() {
        rankingRepository.getUserGroupRanking(new RankingRepository.RankingCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (users == null || users.isEmpty()) {
                    view.displayRanking(new ArrayList<>());
                    return;
                }

                view.displayRanking(users);
            }

            @Override
            public void onError(String message) {
                view.displayRanking(new ArrayList<>());
                view.showError("Erro ao carregar ranking do grupo: " + message);
            }
        });
    }

    /**
     * Carregar ranking global (últimos 30 dias)
     */
    public void updateGlobalRanking() {
        rankingRepository.getGlobalRanking(100, new RankingRepository.RankingCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (users == null || users.isEmpty()) {
                    view.displayRanking(new ArrayList<>());
                    return;
                }

                // Atribuir ranks
                for (int i = 0; i < users.size(); i++) {
                    users.get(i).setRank(i + 1);
                }

                view.displayRanking(users);
            }

            @Override
            public void onError(String message) {
                view.displayRanking(new ArrayList<>());
                view.showError("Erro ao carregar ranking global: " + message);
            }
        });
    }

    public interface RankingView {
        void displayRanking(List<User> rankingList);
        void showError(String message);
    }
}
