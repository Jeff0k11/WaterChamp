package com.example.waterchamp.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.waterchamp.model.HistoryRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Cache local para histórico detalhado de consumo de água
 * Mantém registros individuais (timestamp, quantidade, ação) para funcionalidade de undo
 */
public class HistoryCache {
    private static final String PREF_NAME = "WaterChampHistoryCache";
    private static final String KEY_HISTORY_RECORDS = "history_records";
    private static final String KEY_TODAY_TOTAL = "today_total";
    private static final String KEY_TODAY_DATE = "today_date";

    private final SharedPreferences prefs;
    private final Gson gson;
    private final SimpleDateFormat dateFormat;

    public HistoryCache(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    // ============ History Records ============

    /**
     * Adicionar registro ao histórico
     */
    public void addRecord(HistoryRecord record) {
        List<HistoryRecord> records = getTodayRecords();
        records.add(record);
        saveRecords(records);

        // Atualizar total do dia
        updateTodayTotal();
    }

    /**
     * Remover último registro (para undo)
     */
    public HistoryRecord removeLastRecord() {
        List<HistoryRecord> records = getTodayRecords();
        if (records.isEmpty()) {
            return null;
        }

        HistoryRecord last = records.remove(records.size() - 1);
        saveRecords(records);

        // Atualizar total do dia
        updateTodayTotal();

        return last;
    }

    /**
     * Obter todos os registros de hoje
     */
    public List<HistoryRecord> getTodayRecords() {
        String today = dateFormat.format(new Date());
        String savedDate = prefs.getString(KEY_TODAY_DATE, "");

        // Se mudou de dia, limpar registros antigos
        if (!today.equals(savedDate)) {
            clearTodayRecords();
            prefs.edit().putString(KEY_TODAY_DATE, today).apply();
            return new ArrayList<>();
        }

        String json = prefs.getString(KEY_HISTORY_RECORDS, "[]");
        Type listType = new TypeToken<ArrayList<HistoryRecord>>(){}.getType();
        List<HistoryRecord> records = gson.fromJson(json, listType);

        return records != null ? records : new ArrayList<>();
    }

    /**
     * Salvar lista de registros
     */
    private void saveRecords(List<HistoryRecord> records) {
        String json = gson.toJson(records);
        prefs.edit().putString(KEY_HISTORY_RECORDS, json).apply();
    }

    /**
     * Limpar registros de hoje
     */
    public void clearTodayRecords() {
        prefs.edit()
            .putString(KEY_HISTORY_RECORDS, "[]")
            .putInt(KEY_TODAY_TOTAL, 0)
            .apply();
    }

    // ============ Today's Total ============

    /**
     * Atualizar total do dia baseado nos registros
     */
    private void updateTodayTotal() {
        List<HistoryRecord> records = getTodayRecords();
        int total = 0;

        for (HistoryRecord record : records) {
            if ("Adicionado".equals(record.getAction())) {
                total += record.getAmount();
            } else if ("Removido".equals(record.getAction())) {
                total -= record.getAmount();
            }
        }

        prefs.edit().putInt(KEY_TODAY_TOTAL, total).apply();
    }

    /**
     * Obter total consumido hoje
     */
    public int getTodayTotal() {
        String today = dateFormat.format(new Date());
        String savedDate = prefs.getString(KEY_TODAY_DATE, "");

        // Se mudou de dia, retornar 0
        if (!today.equals(savedDate)) {
            return 0;
        }

        return prefs.getInt(KEY_TODAY_TOTAL, 0);
    }

    /**
     * Definir total do dia (usado ao sincronizar com servidor)
     */
    public void setTodayTotal(int total) {
        String today = dateFormat.format(new Date());
        prefs.edit()
            .putInt(KEY_TODAY_TOTAL, total)
            .putString(KEY_TODAY_DATE, today)
            .apply();
    }

    // ============ Utility ============

    /**
     * Verificar se há registros para desfazer
     */
    public boolean hasRecordsToUndo() {
        return !getTodayRecords().isEmpty();
    }

    /**
     * Obter número de registros hoje
     */
    public int getRecordCount() {
        return getTodayRecords().size();
    }

    /**
     * Verificar se já sincronizou hoje
     */
    public boolean isTodaySynced() {
        String today = dateFormat.format(new Date());
        String savedDate = prefs.getString(KEY_TODAY_DATE, "");
        return today.equals(savedDate);
    }
}
