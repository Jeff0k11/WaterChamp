package com.example.waterchamp.controller;

import android.content.Context;
import com.example.waterchamp.data.repository.ConsumoRepository;
import com.example.waterchamp.model.HistoryRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryController {
    private HistoryView view;
    private ConsumoRepository consumoRepository;

    public HistoryController(HistoryView view, Context context) {
        this.view = view;
        this.consumoRepository = new ConsumoRepository(context);
    }

    public void updateHistory() {
        // Carregar histórico de hoje do cache local
        List<HistoryRecord> historyList = new ArrayList<>(consumoRepository.getTodayHistory());
        Collections.reverse(historyList); // Show newest first
        view.displayHistory(historyList);
    }

    public interface HistoryView {
        void displayHistory(List<HistoryRecord> historyList);
    }
}
