package com.example.waterchamp.controller;

import com.example.waterchamp.model.HistoryRecord;
import com.example.waterchamp.model.UserDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryController {
    private HistoryView view;

    public HistoryController(HistoryView view) {
        this.view = view;
    }

    public void updateHistory() {
        if (UserDatabase.currentUser != null) {
            List<HistoryRecord> historyList = new ArrayList<>(UserDatabase.currentUser.getHistoryList());
            Collections.reverse(historyList); // Show newest first
            view.displayHistory(historyList);
        }
    }

    public interface HistoryView {
        void displayHistory(List<HistoryRecord> historyList);
    }
}
