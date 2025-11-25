package com.example.waterchamp.event;

/**
 * Evento disparado quando o perfil do usuário é atualizado
 * Usado para sincronizar automaticamente outras fragments (Home, Ranking, etc)
 * quando mudanças como cup size, daily goal, etc são alteradas
 */
public class ProfileUpdateEvent {
    public int dailyGoal;
    public int cupSize;
    public String name;

    public ProfileUpdateEvent(int dailyGoal, int cupSize, String name) {
        this.dailyGoal = dailyGoal;
        this.cupSize = cupSize;
        this.name = name;
    }

    public ProfileUpdateEvent() {
        this.dailyGoal = 0;
        this.cupSize = 0;
        this.name = "";
    }
}
