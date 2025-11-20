package com.example.waterchamp.model;

public class HistoryRecord {
    private long timestamp;
    private int amount;
    private String action; // "Adicionado" or "Removido"

    public HistoryRecord(long timestamp, int amount, String action) {
        this.timestamp = timestamp;
        this.amount = amount;
        this.action = action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getAmount() {
        return amount;
    }

    public String getAction() {
        return action;
    }
}
