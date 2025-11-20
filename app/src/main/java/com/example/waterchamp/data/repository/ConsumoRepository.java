package com.example.waterchamp.data.repository;

import android.content.Context;
import com.example.waterchamp.data.local.HistoryCache;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.data.remote.ConsumoService;
import com.example.waterchamp.model.HistoryRecord;
import com.example.waterchamp.utils.CoroutineHelper;

import java.util.Date;
import java.util.List;

/**
 * Repository para gerenciar operações de consumo de água
 * Coordena entre serviço remoto (Supabase) e cache local
 * Implementa estratégia de sincronização híbrida
 */
public class ConsumoRepository {
    private final ConsumoService consumoService;
    private final HistoryCache historyCache;
    private final PreferencesManager prefsManager;

    public ConsumoRepository(Context context) {
        this.consumoService = new ConsumoService();
        this.historyCache = new HistoryCache(context);
        this.prefsManager = new PreferencesManager(context);
    }

    /**
     * Interface para callbacks de operações assíncronas
     */
    public interface SyncCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Adicionar água consumida
     * 1. Salva localmente (histórico detalhado)
     * 2. Sincroniza total com servidor (background)
     */
    public void addWater(int amountMl) {
        // 1. Salvar localmente
        HistoryRecord record = new HistoryRecord(
            System.currentTimeMillis(),
            amountMl,
            "Adicionado"
        );
        historyCache.addRecord(record);

        // Atualizar estatística local
        prefsManager.addToTotalConsumed(amountMl);

        // 2. Sincronizar com servidor (background, sem bloquear UI)
        syncTodayConsumption(null);
    }

    /**
     * Desfazer última adição
     */
    public HistoryRecord undoLastWater() {
        HistoryRecord removedRecord = historyCache.removeLastRecord();

        if (removedRecord != null && "Adicionado".equals(removedRecord.getAction())) {
            // Adicionar registro de remoção
            HistoryRecord undoRecord = new HistoryRecord(
                System.currentTimeMillis(),
                removedRecord.getAmount(),
                "Removido"
            );
            historyCache.addRecord(undoRecord);

            // Atualizar estatística local
            prefsManager.addToTotalConsumed(-removedRecord.getAmount());

            // Sincronizar com servidor
            syncTodayConsumption(null);
        }

        return removedRecord;
    }

    /**
     * Obter total consumido hoje (do cache local)
     */
    public int getTodayTotal() {
        return historyCache.getTodayTotal();
    }

    /**
     * Obter histórico de hoje
     */
    public List<HistoryRecord> getTodayHistory() {
        return historyCache.getTodayRecords();
    }

    /**
     * Sincronizar consumo de hoje com servidor
     */
    public void syncTodayConsumption(SyncCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            if (callback != null) {
                callback.onError("Usuário não autenticado");
            }
            return;
        }

        int todayTotal = historyCache.getTodayTotal();
        Date today = new Date();

        CoroutineHelper.<Boolean>runAsync(
            () -> consumoService.syncDailyConsumptionBlocking(userId, today, todayTotal),
            (Boolean success, String error) -> {
                if (error != null) {
                    if (callback != null) {
                        callback.onError("Erro: " + error);
                    }
                } else if (Boolean.TRUE.equals(success)) {
                    // Atualizar timestamp de última sincronização
                    prefsManager.setLastSyncTimestamp(System.currentTimeMillis());
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onError("Falha ao sincronizar com servidor");
                    }
                }
            }
        );
    }

    /**
     * Buscar consumo do servidor para uma data específica
     */
    public void getConsumptionByDate(Date date, ConsumptionCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        CoroutineHelper.<ConsumoService.ConsumoDiario>runAsync(
            () -> consumoService.getConsumptionByDateBlocking(userId, date),
            (ConsumoService.ConsumoDiario consumo, String error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else if (consumo != null) {
                    callback.onSuccess(consumo.getTotal_ml());
                } else {
                    callback.onSuccess(0); // Nenhum consumo nessa data
                }
            }
        );
    }

    /**
     * Buscar histórico dos últimos N dias do servidor
     */
    public void getConsumptionHistory(int days, HistoryCallback callback) {
        int userId = prefsManager.getUserId();
        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        CoroutineHelper.<List<ConsumoService.ConsumoDiario>>runAsync(
            () -> consumoService.getConsumptionHistoryBlocking(userId, days),
            (List<ConsumoService.ConsumoDiario> history, String error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else {
                    callback.onSuccess(history);
                }
            }
        );
    }

    /**
     * Calcular sequência (streak) de dias
     */
    public void calculateStreak(StreakCallback callback) {
        int userId = prefsManager.getUserId();
        int metaDiaria = prefsManager.getDailyGoal();

        if (userId == -1) {
            callback.onError("Usuário não autenticado");
            return;
        }

        CoroutineHelper.<Integer>runAsync(
            () -> consumoService.calculateStreakBlocking(userId, metaDiaria),
            (Integer streak, String error) -> {
                if (error != null) {
                    callback.onError("Erro: " + error);
                } else {
                    callback.onSuccess(streak);
                }
            }
        );
    }

    /**
     * Verificar se há registros para desfazer
     */
    public boolean hasRecordsToUndo() {
        return historyCache.hasRecordsToUndo();
    }

    /**
     * Limpar histórico de hoje (útil para testes)
     */
    public void clearTodayHistory() {
        historyCache.clearTodayRecords();
    }

    // ============ Callbacks ============

    public interface ConsumptionCallback {
        void onSuccess(int totalMl);
        void onError(String message);
    }

    public interface HistoryCallback {
        void onSuccess(List<ConsumoService.ConsumoDiario> history);
        void onError(String message);
    }

    public interface StreakCallback {
        void onSuccess(int streak);
        void onError(String message);
    }
}
