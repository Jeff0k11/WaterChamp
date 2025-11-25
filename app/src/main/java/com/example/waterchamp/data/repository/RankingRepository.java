package com.example.waterchamp.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.data.remote.RankingService;
import com.example.waterchamp.model.Group;
import com.example.waterchamp.model.User;
import com.example.waterchamp.utils.CoroutineHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository para gerenciar operações de ranking
 * Implementa estratégia Offline-First: Carrega do cache -> Atualiza da API -> Salva no cache
 */
public class RankingRepository {
    private final RankingService rankingService;
    private final PreferencesManager prefsManager;
    private final GrupoRepository grupoRepository;

    public RankingRepository(Context context) {
        this.rankingService = new RankingService();
        this.prefsManager = new PreferencesManager(context);
        this.grupoRepository = new GrupoRepository(context);
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

    // ================== RANKING DIÁRIO ==================

    /**
     * Buscar ranking diário (de hoje)
     * 1. Retorna dados do cache imediatamente (se houver)
     * 2. Busca dados atualizados da API em background
     * 3. Salva novos dados no cache e atualiza a UI novamente
     */
    public void getDailyRanking(RankingCallback callback) {
        // 1. Carregar do Cache
        List<User> cachedRanking = loadRankingFromCache(prefsManager.getCachedRankingGlobal());
        if (!cachedRanking.isEmpty()) {
            Log.d("RankingRepository", "Ranking Diário: Carregado do cache (" + cachedRanking.size() + " itens)");
            callback.onSuccess(cachedRanking);
        }

        // 2. Buscar da API (Network)
        CoroutineHelper.runAsync(
            () -> rankingService.getDailyRankingBlocking(100),
            (entries, error) -> {
                if (error != null) {
                    Log.e("RankingRepository", "Ranking Diário: Erro na API: " + error);
                    if (cachedRanking.isEmpty()) {
                        callback.onError("Falha ao carregar ranking. Verifique sua conexão.");
                    }
                } else if (entries != null) {
                    // Converter e processar
                    List<User> liveRanking = convertEntriesToUsers(entries);

                    // 3. Salvar no Cache
                    saveRankingToCache(liveRanking, true); // true = global

                    Log.d("RankingRepository", "Ranking Diário: Atualizado da API (" + liveRanking.size() + " itens)");
                    callback.onSuccess(liveRanking);
                }
            }
        );
    }

    // ================== RANKING GLOBAL ==================

    /**
     * Buscar ranking global
     * 1. Retorna dados do cache imediatamente (se houver)
     * 2. Busca dados atualizados da API em background
     * 3. Salva novos dados no cache e atualiza a UI novamente
     */
    public void getGlobalRanking(int limit, RankingCallback callback) {
        // 1. Carregar do Cache
        List<User> cachedRanking = loadRankingFromCache(prefsManager.getCachedRankingGlobal());
        if (!cachedRanking.isEmpty()) {
            Log.d("RankingRepository", "Ranking Global: Carregado do cache (" + cachedRanking.size() + " itens)");
            callback.onSuccess(cachedRanking);
        }

        // 2. Buscar da API (Network)
        CoroutineHelper.runAsync(
            () -> rankingService.getGlobalRankingBlocking(limit),
            (entries, error) -> {
                if (error != null) {
                    Log.e("RankingRepository", "Ranking Global: Erro na API: " + error);
                    if (cachedRanking.isEmpty()) {
                        callback.onError("Falha ao carregar ranking. Verifique sua conexão.");
                    }
                } else if (entries != null) {
                    // Converter e processar
                    List<User> liveRanking = convertEntriesToUsers(entries);
                    
                    // 3. Salvar no Cache
                    saveRankingToCache(liveRanking, true); // true = global

                    Log.d("RankingRepository", "Ranking Global: Atualizado da API (" + liveRanking.size() + " itens)");
                    callback.onSuccess(liveRanking);
                }
            }
        );
    }

    // ================== RANKING DO GRUPO ==================

    /**
     * Buscar ranking do grupo do usuário
     * Segue a mesma estratégia Offline-First
     */
    public void getUserGroupRanking(RankingCallback callback) {
        // 1. Carregar do Cache
        List<User> cachedRanking = loadRankingFromCache(prefsManager.getCachedRankingGroup());
        if (!cachedRanking.isEmpty()) {
            Log.d("RankingRepository", "Ranking Grupo: Carregado do cache (" + cachedRanking.size() + " itens)");
            callback.onSuccess(cachedRanking);
        }

        // Buscar ID do grupo primeiro (necessário para a API)
        // Nota: Isso pode falhar offline se o usuário nunca logou/baixou grupos.
        // Assumindo que o GrupoRepository tem seu próprio cache ou que vamos tentar mesmo assim.
        grupoRepository.getUserGroups(new GrupoRepository.GruposCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (groups == null || groups.isEmpty()) {
                    // Sem grupo, limpa cache de grupo
                    prefsManager.setCachedRankingGroup("[]");
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                int groupId = groups.get(0).getId();

                // 2. Buscar da API (Network)
                Log.d("RankingRepository", "Ranking Grupo: Buscando ranking para grupo " + groupId);
                CoroutineHelper.runAsync(
                    () -> {
                        Log.d("RankingRepository", "Ranking Grupo: Chamando getGroupDailyRankingBlocking para grupo " + groupId);
                        List<RankingService.RankingEntry> result = rankingService.getGroupDailyRankingBlocking(groupId);
                        Log.d("RankingRepository", "Ranking Grupo: Resultado=" + (result != null ? result.size() : "null") + " entries");
                        return result;
                    },
                    (entries, error) -> {
                        Log.d("RankingRepository", "Ranking Grupo: onComplete - error=" + error + ", entries=" + (entries != null ? entries.size() : "null"));

                        if (error != null) {
                            Log.e("RankingRepository", "Ranking Grupo: Erro na API: " + error);
                            // Erro silencioso se já mostramos o cache
                        } else if (entries != null && !entries.isEmpty()) {
                            List<User> liveRanking = convertEntriesToUsers(entries);

                            // 3. Salvar no Cache
                            saveRankingToCache(liveRanking, false); // false = grupo

                            Log.d("RankingRepository", "Ranking Grupo: Atualizado da API (" + liveRanking.size() + " itens)");
                            callback.onSuccess(liveRanking);
                        } else {
                            Log.d("RankingRepository", "Ranking Grupo: Nenhum dado retornado, mostrando lista vazia");
                            callback.onSuccess(new ArrayList<>());
                        }
                    }
                );
            }

            @Override
            public void onError(String message) {
                // Se falhar ao buscar grupos e não tiver cache, erro.
                if (cachedRanking.isEmpty()) {
                    callback.onError("Não foi possível carregar seu grupo.");
                }
            }
        });
    }

    // ================== MÉTODOS AUXILIARES ==================

    /**
     * Converte entradas do serviço para objetos User
     */
    private List<User> convertEntriesToUsers(List<RankingService.RankingEntry> entries) {
        List<User> users = new ArrayList<>();
        for (RankingService.RankingEntry entry : entries) {
            // Usa consumo_hoje para ambos por enquanto (simplificação pedida)
            int consumo = entry.getConsumo_hoje() != null ? entry.getConsumo_hoje() : 0;
            
            User user = new User(entry.getNome(), "", consumo);
            // Se o serviço já retornou posição, usa. Se não, será reordenado depois se necessário.
            user.setRank((int) entry.getPosicao());
            users.add(user);
        }
        // Garante ordenação
        Collections.sort(users, (u1, u2) -> Integer.compare(u2.getWaterIntake(), u1.getWaterIntake()));
        
        // Recalcula ranks baseados na lista ordenada (1..N)
        for (int i = 0; i < users.size(); i++) {
            users.get(i).setRank(i + 1);
        }
        
        return users;
    }

    /**
     * Salva lista de usuários como JSON no SharedPreferences
     */
    private void saveRankingToCache(List<User> users, boolean isGlobal) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (User user : users) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", user.getName());
                jsonObject.put("waterIntake", user.getWaterIntake());
                jsonObject.put("rank", user.getRank());
                jsonArray.put(jsonObject);
            }
            
            String jsonString = jsonArray.toString();
            if (isGlobal) {
                prefsManager.setCachedRankingGlobal(jsonString);
            } else {
                prefsManager.setCachedRankingGroup(jsonString);
            }
        } catch (Exception e) {
            Log.e("RankingRepository", "Erro ao salvar cache: " + e.getMessage());
        }
    }

    /**
     * Carrega lista de usuários do JSON do SharedPreferences
     */
    private List<User> loadRankingFromCache(String jsonString) {
        List<User> users = new ArrayList<>();
        try {
            if (jsonString == null || jsonString.isEmpty()) return users;

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                User user = new User(
                    obj.getString("name"),
                    "",
                    obj.getInt("waterIntake")
                );
                user.setRank(obj.getInt("rank"));
                users.add(user);
            }
        } catch (Exception e) {
            Log.e("RankingRepository", "Erro ao ler cache: " + e.getMessage());
        }
        return users;
    }
}
