package com.example.waterchamp.controller;

import com.example.waterchamp.model.HistoryRecord;
import com.example.waterchamp.model.UserDatabase;
import java.util.Stack;

public class HomeController {
    private HomeView view;
    private Stack<Integer> historyStack = new Stack<>();

    public HomeController(HomeView view) {
        this.view = view;
    }

    public void addWater(int amount) {
        if (UserDatabase.currentUser != null) {
            int currentWaterIntake = UserDatabase.currentUser.getWaterIntake();
            historyStack.push(amount);
            UserDatabase.currentUser.setWaterIntake(currentWaterIntake + amount);
            UserDatabase.currentUser.addHistoryRecord(new HistoryRecord(System.currentTimeMillis(), amount, "Adicionado"));
            view.animateProgress(currentWaterIntake, currentWaterIntake + amount);
            view.updateUI();
        }
    }

    public void undoLastAction() {
        if (UserDatabase.currentUser != null && !historyStack.isEmpty()) {
            int lastAmount = historyStack.pop();
            int currentWaterIntake = UserDatabase.currentUser.getWaterIntake();
            UserDatabase.currentUser.setWaterIntake(currentWaterIntake - lastAmount);
            UserDatabase.currentUser.addHistoryRecord(new HistoryRecord(System.currentTimeMillis(), lastAmount, "Removido"));
            view.animateProgress(currentWaterIntake, currentWaterIntake - lastAmount);
            view.updateUI();
        } else {
            view.showToast("Nada para desfazer!");
        }
    }

    public void updateUI() {
        view.updateUI();
    }

    public interface HomeView {
        void updateUI();
        void animateProgress(int from, int to);
        void showToast(String message);
        void showCustomAmountDialog();
    }
}
