package com.example.waterchamp.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.waterchamp.data.local.HistoryCache;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.data.remote.RankingService;
import com.example.waterchamp.model.Group;
import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;
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
    private final GrupoRepository grupoRepository;
    private final HistoryCache historyCache;

    public RankingRepository(Context context) {
        this.rankingService = new RankingService();
        this.prefsManager = new PreferencesManager(context);
        this.grupoRepository = new GrupoRepository(context);
        this.historyCache = new HistoryCache(context);
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
     * Usa cache local para o consumo do dia do usuário atual
     */
    public void getGlobalRanking(int limit, RankingCallback callback) {
        Log.d("RankingRepository", "getGlobalRanking() - Buscando ranking global com limite: " + limit);

        CoroutineHelper.runAsync(
            () -> rankingService.getGlobalRankingBlocking(limit),
            (entries, error) -> {
                if (error != null) {
                    Log.e("RankingRepository", "getGlobalRanking() - Erro: " + error);
                    callback.onError("Erro ao buscar ranking global: " + error);
                } else if (entries != null) {
                    Log.d("RankingRepository", "getGlobalRanking() - Recebido " + entries.size() + " entries do servidor");

                    // Obter consumo local do usuário atual do cache
                    int localConsumption = historyCache.getTodayTotal();

                    // Converter para lista de Users
                    List<User> users = new ArrayList<>();
                    for (RankingService.RankingEntry entry : entries) {
                        long total30dias;

                        // Se for o usuário atual, usar cache local como base para hoje
                        if (UserDatabase.currentUser != null &&
                            entry.getNome().equalsIgnoreCase(UserDatabase.currentUser.getName())) {
                            // Usar o total_30_dias do servidor, mas atualizar hoje com cache local
                            long serverTotal = entry.getTotal_30_dias() != null ? entry.getTotal_30_dias() : 0;
                            // Atualizar o valor de hoje no total 30 dias
                            total30dias = serverTotal - historyCache.getTodayTotal() + localConsumption;
                            Log.d("RankingRepository", "  Usuário atual (" + entry.getNome() + ") - usando cache local para hoje: " + localConsumption + "ml, total 30 dias: " + total30dias + "ml");
                        } else {
                            total30dias = entry.getTotal_30_dias() != null ? entry.getTotal_30_dias() : 0;
                            Log.d("RankingRepository", "  Outro usuário (" + entry.getNome() + ") - usando servidor: " + total30dias + "ml");
                        }

                        User user = new User(
                            entry.getNome(),
                            "",
                            (int) total30dias  // Usar o total 30 dias como waterIntake para exibição
                        );
                        user.setRank((int) entry.getPosicao());
                        users.add(user);
                    }

                    // Ordenar por posição
                    Collections.sort(users, (u1, u2) -> Integer.compare(u1.getRank(), u2.getRank()));

                    callback.onSuccess(users);
                } else {
                    Log.e("RankingRepository", "getGlobalRanking() - Nenhum resultado encontrado");
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
        Log.d("RankingRepository", "getGroupDailyRanking() - Buscando ranking do grupo: " + groupId);

        CoroutineHelper.runAsync(
            () -> rankingService.getGroupDailyRankingBlocking(groupId),
            (entries, error) -> {
                if (error != null) {
                    Log.e("RankingRepository", "getGroupDailyRanking() - Erro: " + error);
                    callback.onError("Erro ao buscar ranking do grupo: " + error);
                } else if (entries != null) {
                    Log.d("RankingRepository", "getGroupDailyRanking() - Recebido " + entries.size() + " entries do servidor");
                    // Converter para lista de Users
                    List<User> users = new ArrayList<>();
                    for (RankingService.RankingEntry entry : entries) {
                        int consumo = entry.getConsumo_hoje() != null ? entry.getConsumo_hoje() : 0;
                        User user = new User(
                            entry.getNome(),
                            "",
                            consumo
                        );
                        user.setRank((int) entry.getPosicao());
                        users.add(user);
                        Log.d("RankingRepository", "  [" + entry.getPosicao() + "] " + entry.getNome() + " - " + consumo + "ml (consumo_hoje=" + entry.getConsumo_hoje() + ")");
                    }

                    callback.onSuccess(users);
                } else {
                    Log.e("RankingRepository", "getGroupDailyRanking() - Nenhum resultado encontrado");
                    callback.onError("Nenhum resultado encontrado");
                }
            }
        );
    }

    /**
     * Buscar ranking do grupo do usuário logado
     * Obtém o grupo do usuário e busca o ranking desse grupo
     * Usa cache local do consumo do dia para o usuário atual
     */
    public void getUserGroupRanking(RankingCallback callback) {
        // Buscar grupos do usuário
        grupoRepository.getUserGroups(new GrupoRepository.GruposCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (groups == null || groups.isEmpty()) {
                    // Usuário não está em nenhum grupo
                    callback.onSuccess(new ArrayList<>());
                } else {
                    // Pegar o primeiro grupo (máximo 1 grupo por usuário)
                    int groupId = groups.get(0).getId();
                    getGroupDailyRankingWithLocalCache(groupId, callback);
                }
            }

            @Override
            public void onError(String message) {
                // Se houver erro ao buscar grupos, retornar lista vazia
                callback.onSuccess(new ArrayList<>());
            }
        });
    }

    /**
     * Buscar ranking do grupo, mas usar cache local para o usuário atual
     * Substitui o consumo_hoje do servidor pelo valor do cache local
     */
    private void getGroupDailyRankingWithLocalCache(int groupId, RankingCallback callback) {
        Log.d("RankingRepository", "getGroupDailyRankingWithLocalCache() - Buscando ranking do grupo: " + groupId);

        CoroutineHelper.runAsync(
            () -> rankingService.getGroupDailyRankingBlocking(groupId),
            (entries, error) -> {
                if (error != null) {
                    Log.e("RankingRepository", "getGroupDailyRankingWithLocalCache() - Erro: " + error);
                    callback.onError("Erro ao buscar ranking do grupo: " + error);
                } else if (entries != null) {
                    Log.d("RankingRepository", "getGroupDailyRankingWithLocalCache() - Recebido " + entries.size() + " entries do servidor");

                    // Obter consumo local do usuário atual do cache
                    int localConsumption = historyCache.getTodayTotal();

                    // Converter para lista de Users
                    List<User> users = new ArrayList<>();
                    for (RankingService.RankingEntry entry : entries) {
                        int consumo;

                        // Se for o usuário atual, usar o cache local
                        if (UserDatabase.currentUser != null &&
                            entry.getNome().equalsIgnoreCase(UserDatabase.currentUser.getName())) {
                            consumo = localConsumption;
                            Log.d("RankingRepository", "  Usuário atual (" + entry.getNome() + ") - usando cache local: " + consumo + "ml");
                        } else {
                            consumo = entry.getConsumo_hoje() != null ? entry.getConsumo_hoje() : 0;
                            Log.d("RankingRepository", "  Outro usuário (" + entry.getNome() + ") - usando servidor: " + consumo + "ml");
                        }

                        User user = new User(entry.getNome(), "", consumo);
                        user.setRank((int) entry.getPosicao());
                        users.add(user);
                    }

                    // Reordenar por consumo (maior primeiro)
                    Collections.sort(users, (u1, u2) -> Integer.compare(u2.getWaterIntake(), u1.getWaterIntake()));

                    // Atribuir novas posições baseado na ordem
                    for (int i = 0; i < users.size(); i++) {
                        users.get(i).setRank(i + 1);
                    }

                    callback.onSuccess(users);
                } else {
                    Log.e("RankingRepository", "getGroupDailyRankingWithLocalCache() - Nenhum resultado encontrado");
                    callback.onError("Nenhum resultado encontrado");
                }
            }
        );
    }
}
