package com.example.waterchamp.controller;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;

public class ProfileController {
    private ProfileView view;
    private PreferencesManager preferencesManager;

    public ProfileController(ProfileView view, Context context) {
        this.view = view;
        this.preferencesManager = new PreferencesManager(context);
    }

    // Constructor antigo para compatibilidade
    public ProfileController(ProfileView view) {
        this.view = view;
        this.preferencesManager = null;
    }

    public void loadUserData() {
        if (UserDatabase.currentUser != null) {
            User user = UserDatabase.currentUser;

            view.setName(user.getName());
            view.setEmail(user.getEmail());
            view.setDailyGoal(String.valueOf(user.getDailyGoal()));
            view.setCupSize(String.valueOf(user.getDefaultCupSize()));
            view.setNotificationsEnabled(user.isNotificationsEnabled());

            if (user.getProfilePictureUri() != null) {
                try {
                    Uri uri = Uri.parse(user.getProfilePictureUri());
                    view.setProfileImage(uri);
                } catch (Exception e) {
                    view.setDefaultProfileImage();
                }
            } else {
                view.setDefaultProfileImage();
            }

            // Statistics
            long total = user.getTotalConsumedAllTime();
            if (total >= 1000) {
                view.setTotalIntake(String.format("%.1f L", total / 1000.0));
            } else {
                view.setTotalIntake(total + " ml");
            }

            // Average
            long daysSinceCreation = (System.currentTimeMillis() - user.getCreationDate()) / (1000 * 60 * 60 * 24);
            if (daysSinceCreation < 1) daysSinceCreation = 1;
            long average = total / daysSinceCreation;
            view.setAverage(average + " ml");

            view.setStreak(user.getStreak() + " 🔥");
        }
    }

    public void saveUserData(String newName, String newPass, String newGoalStr, String newCupStr, boolean notificationsEnabled, Uri selectedImageUri) {
        if (UserDatabase.currentUser != null) {
            if (TextUtils.isEmpty(newName)) {
                view.showNameError("Nome não pode ser vazio");
                return;
            }

            if (TextUtils.isEmpty(newGoalStr)) {
                view.showGoalError("Meta inválida");
                return;
            }

            if (TextUtils.isEmpty(newCupStr)) {
                view.showCupError("Tamanho inválido");
                return;
            }

            int newGoal;
            try {
                newGoal = Integer.parseInt(newGoalStr);
            } catch (NumberFormatException e) {
                view.showGoalError("Meta deve ser um número válido");
                return;
            }

            int newCup;
            try {
                newCup = Integer.parseInt(newCupStr);
            } catch (NumberFormatException e) {
                view.showCupError("Tamanho deve ser um número válido");
                return;
            }

            // Atualizar em memória
            UserDatabase.currentUser.setName(newName);
            UserDatabase.currentUser.setDailyGoal(newGoal);
            UserDatabase.currentUser.setDefaultCupSize(newCup);
            UserDatabase.currentUser.setNotificationsEnabled(notificationsEnabled);

            if (selectedImageUri != null) {
                UserDatabase.currentUser.setProfilePictureUri(selectedImageUri.toString());
            }

            if (!TextUtils.isEmpty(newPass)) {
                // Update password in the main credentials map
                UserDatabase.usuariosCadastrados.put(UserDatabase.currentUser.getEmail(), newPass);
            }

            // IMPORTANTE: Persistir no PreferencesManager
            if (preferencesManager != null) {
                preferencesManager.setUserName(newName);
                preferencesManager.setDailyGoal(newGoal);
                preferencesManager.setDefaultCupSize(newCup);
                preferencesManager.setNotificationsEnabled(notificationsEnabled);
                if (selectedImageUri != null) {
                    preferencesManager.setProfilePictureUri(selectedImageUri.toString());
                }
            }

            view.showSaveSuccess();
        }
    }

    public void logout() {
        UserDatabase.currentUser = null;
        view.navigateToLogin();
    }

    // New helper: delegate to the view to open file chooser (called from ProfileFragment)
    public void openFileChooser() {
        view.openFileChooser();
    }

    public interface ProfileView {
        void setName(String name);
        void setEmail(String email);
        void setDailyGoal(String goal);
        void setCupSize(String cupSize);
        void setNotificationsEnabled(boolean enabled);
        void setProfileImage(Uri uri);
        void setDefaultProfileImage();
        void setTotalIntake(String intake);
        void setAverage(String average);
        void setStreak(String streak);
        void showNameError(String message);
        void showGoalError(String message);
        void showCupError(String message);
        void showSaveSuccess();
        void navigateToLogin();
        void openFileChooser();
    }
}
