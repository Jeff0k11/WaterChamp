package com.example.waterchamp.controller;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.event.ProfileUpdateEvent;
import com.example.waterchamp.model.User;
import com.example.waterchamp.model.UserDatabase;
import org.greenrobot.eventbus.EventBus;

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

            // IMPORTANTE: Persistir OFFLINE FIRST - salvar localmente de forma síncrona
            if (preferencesManager != null) {
                String profilePictureUri = selectedImageUri != null ? selectedImageUri.toString() : null;
                // Usar saveUserSettingsSync para garantir salvamento imediato no disco (sem delay)
                preferencesManager.saveUserSettingsSync(newName, newGoal, newCup, notificationsEnabled, profilePictureUri);
            }

            view.showSaveSuccess();

            // Já está salvo offline, notificar que está pronto
            view.onSaveComplete();

            // Disparar evento para atualizar outras fragments em tempo real
            EventBus.getDefault().post(new ProfileUpdateEvent(newGoal, newCup, newName));

            // Sincronizar com servidor de forma assíncrona (fire-and-forget)
            syncUserDataToServer(newName, newGoal, newCup, notificationsEnabled);
        }
    }

    public void logout() {
        // Limpar dados de sessão ativa
        UserDatabase.currentUser = null;

        // IMPORTANTE: Limpar PreferencesManager (sessão de login)
        if (preferencesManager != null) {
            preferencesManager.clearUserData();
            // Também limpar dados de conta local se existir
            preferencesManager.clearLocalAccountData();
            // Limpar credenciais salvas
            preferencesManager.clearSavedCredentials();
        }

        // Navegar para login com clear back stack
        view.navigateToLogin();
    }

    // New helper: delegate to the view to open file chooser (called from ProfileFragment)
    public void openFileChooser() {
        view.openFileChooser();
    }

    /**
     * Sincronizar dados do perfil com o servidor de forma assíncrona
     * Executa em background sem bloquear a UI
     * Se falhar, os dados locais já estão salvos (offline-first)
     */
    private void syncUserDataToServer(String name, int dailyGoal, int cupSize, boolean notificationsEnabled) {
        // TODO: Implementar sincronização com Supabase quando API estiver disponível
        // Por enquanto, os dados já estão salvos offline localmente
        // A sincronização acontecerá quando houver conectividade
        android.util.Log.d("ProfileController", "Dados salvos offline. Sincronizando com servidor...");
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
        void onSaveComplete();
        void navigateToLogin();
        void openFileChooser();
    }
}
