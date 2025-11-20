package com.example.waterchamp.data.repository;

import android.content.Context;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.data.remote.RankingService;
import com.example.waterchamp.model.User;
import com.example.waterchamp.utils.CoroutineHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository para gerenciar operações de ranking
 */
public class RankingRepository {
    private final RankingService rankingService;
    private final PreferencesManager prefsManager;

    public RankingRepository(Context context) {
        this.rankingService = new RankingService();
        this.prefsManager = new PreferencesManager(context);
    }

    /**
     * Interface para callbacks
     */
    public interface RankingCallback {
        void onSuccess(List<User> users);
        void onError(String message);
    }

    public interface PositionCallback {
        void onSuccess(int position);
        void onError(String message);
    }

    /**
     * Buscar ranking diário
     */
    public void getDailyRanking(int limit, RankingCallback callback) {
        CoroutineHelper.runAsync(
            () -> rankingService.getDailyRankingBlocking(limit),
            (entries, error) -> {
                if (error != null) {
                    callback.onError("Erro ao buscar ranking: " + error);
                } else if (entries != null) {
                    // Converter para lista de Users
                    List<User> users = new ArrayList<>();
                    for (RankingService.RankingEntry entry : entries) {
                        User user = new User(
                            entry.getNome(),
                            "",  // Email não é necessário para ranking
                            entry.getConsumo_hoje() != null ? entry.getConsumo_hoje() : 0
                        );
                        user.setRank((int) entry.getPosicao());
                        users.add(user);
                    }

                    // Ordenar por posição
                    Collections.sort(users, (u1, u2) -> Integer.compare(u1.getRank(), u2.getRank()));

                    callback.onSuccess(users);
                } else {
                    callback.onError("Nenhum resultado encontrado");
                }
            }
        );
    }

    /**
     * Buscar ranking global (últimos 30 dias)
     */
    public void getGlobalRanking(int limit, RankingCallback callback) {
        CoroutineHelper.runAsync(
            () -> rankingService.getGlobalRankingBlocking(limit),
            (entries, error) -> {
                if (error != null) {
                    callback.onError("Erro ao buscar ranking global: " + error);
                } else if (entries != null) {
                    // Converter para lista de Users
                    List<User> users = new ArrayList<>();
                    for (RankingService.RankingEntry entry : entries) {
                        User user = new User(
                            entry.getNome(),
                            "",
                            entry.getTotal_30_dias() != null ? entry.getTotal_30_dias().intValue() : 0
                        );
                        user.setRank((int) entry.getPosicao());
                        users.add(user);
                    }

                    // Ordenar por posição
                    Collections.sort(users, (u1, u2) -> Integer.compare(u1.getRank(), u2.getRank()));

                    callback.onSuccess(users);
                } else {
                    callback.onError("Nenhum resultado encontrado");
                }
            }
        );
    }

    /**
     * Buscar posição do usuário atual no ranking diário
     */
    public void getCurrentUserDailyPosition(PositionCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        CoroutineHelper.runAsync(
            () -> rankingService.getUserDailyPositionBlocking(userId),
            (position, error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else if (position != null) {
                    callback.onSuccess(position);
                } else {
                    callback.onError("Posição não encontrada");
                }
            }
        );
    }

    /**
     * Buscar posição do usuário atual no ranking global
     */
    public void getCurrentUserGlobalPosition(PositionCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        CoroutineHelper.runAsync(
            () -> rankingService.getUserGlobalPositionBlocking(userId),
            (position, error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else if (position != null) {
                    callback.onSuccess(position);
                } else {
                    callback.onError("Posição não encontrada");
                }
            }
        );
    }

    /**
     * Buscar ranking de um grupo específico
     */
    public void getGroupDailyRanking(int groupId, RankingCallback callback) {
        CoroutineHelper.runAsync(
            () -> rankingService.getGroupDailyRankingBlocking(groupId),
            (entries, error) -> {
                if (error != null) {
                    callback.onError("Erro ao buscar ranking do grupo: " + error);
                } else if (entries != null) {
                    // Converter para lista de Users
                    List<User> users = new ArrayList<>();
                    for (RankingService.RankingEntry entry : entries) {
                        User user = new User(
                            entry.getNome(),
                            "",
                            entry.getConsumo_hoje() != null ? entry.getConsumo_hoje() : 0
                        );
                        user.setRank((int) entry.getPosicao());
                        users.add(user);
                    }

                    callback.onSuccess(users);
                } else {
                    callback.onError("Nenhum resultado encontrado");
                }
            }
        );
    }
}
