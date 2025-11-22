package com.example.waterchamp.controller;

import android.content.Context;
import android.util.Log;
import com.example.waterchamp.data.repository.ConsumoRepository;
import com.example.waterchamp.data.repository.UserRepository;
import com.example.waterchamp.event.HistoryUpdateEvent;
import com.example.waterchamp.model.HistoryRecord;
import com.example.waterchamp.model.UserDatabase;
import org.greenrobot.eventbus.EventBus;

public class HomeController {
    private HomeView view;
    private ConsumoRepository consumoRepository;
    private UserRepository userRepository;

    public HomeController(HomeView view, Context context) {
        this.view = view;
        this.consumoRepository = new ConsumoRepository(context);
        this.userRepository = new UserRepository(context);
    }

    public void addWater(int amount) {
        if (UserDatabase.currentUser != null) {
            int currentWaterIntake = UserDatabase.currentUser.getWaterIntake();
            int newIntake = currentWaterIntake + amount;

            Log.d("HomeController", "addWater() - Antes: " + currentWaterIntake + "ml, Adicionando: " + amount + "ml, Depois: " + newIntake + "ml");

            // Adicionar água usando o repository (salva local + sincroniza com servidor)
            consumoRepository.addWater(amount);

            // Atualizar UserDatabase para compatibilidade
            UserDatabase.currentUser.setWaterIntake(newIntake);
            UserDatabase.currentUser.addHistoryRecord(new HistoryRecord(System.currentTimeMillis(), amount, "Adicionado"));

            view.animateProgress(currentWaterIntake, newIntake);
            view.updateUI();

            Log.d("HomeController", "addWater() - Valor final no UserDatabase: " + UserDatabase.currentUser.getWaterIntake() + "ml");

            // Disparar evento para atualizar histórico em tempo real
            EventBus.getDefault().post(new HistoryUpdateEvent(amount));
        }
    }

    public void undoLastAction() {
        if (UserDatabase.currentUser != null && consumoRepository.hasRecordsToUndo()) {
            HistoryRecord removed = consumoRepository.undoLastWater();

            if (removed != null) {
                int currentWaterIntake = UserDatabase.currentUser.getWaterIntake();
                int newIntake = currentWaterIntake - removed.getAmount();

                // Prevenir undo se resultaria em valor negativo
                if (newIntake < 0) {
                    view.showToast("Não é possível desfazer! Valor não pode ser negativo.");
                    return;
                }

                // Atualizar UserDatabase para compatibilidade
                UserDatabase.currentUser.setWaterIntake(newIntake);
                UserDatabase.currentUser.addHistoryRecord(new HistoryRecord(System.currentTimeMillis(), removed.getAmount(), "Removido"));

                view.animateProgress(currentWaterIntake, newIntake);
                view.updateUI();

                // Disparar evento para atualizar histórico em tempo real
                EventBus.getDefault().post(new HistoryUpdateEvent(-removed.getAmount()));
            }
        } else {
            view.showToast("Nada para desfazer!");
        }
    }

    public void updateUI() {
        // Atualizar consumo de hoje do cache
        if (UserDatabase.currentUser != null) {
            int todayTotal = consumoRepository.getTodayTotal();
            UserDatabase.currentUser.setWaterIntake(todayTotal);
        }
        view.updateUI();
    }

    public interface HomeView {
        void updateUI();
        void animateProgress(int from, int to);
        void showToast(String message);
        void showCustomAmountDialog();
    }
}
