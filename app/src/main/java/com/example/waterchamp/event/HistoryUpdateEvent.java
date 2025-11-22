package com.example.waterchamp.event;

/**
 * Evento disparado quando o histórico de consumo é atualizado
 * Usado para sincronizar automaticamente a HistoryFragment com novos registros
 */
public class HistoryUpdateEvent {
    public int amountAdded;

    public HistoryUpdateEvent(int amountAdded) {
        this.amountAdded = amountAdded;
    }

    public HistoryUpdateEvent() {
        this.amountAdded = 0;
    }
}
