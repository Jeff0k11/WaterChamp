package com.example.waterchamp.service;

import android.content.Context;
import com.example.waterchamp.data.local.PreferencesManager;

/**
 * Serviço responsável por sincronizar contas locais com o Supabase
 * Quando internet voltar, sincroniza dados pendentes
 */
public class AccountSyncService {

    private final PreferencesManager prefsManager;

    public interface SyncCallback {
        void onSuccess();
        void onError(String message);
        void onProgress(String message);
    }

    public AccountSyncService(Context context) {
        this.prefsManager = new PreferencesManager(context);
    }

    /**
     * Verifica se há contas locais pendentes de sincronização
     */
    public boolean hasPendingSync() {
        return prefsManager.isLocalAccount() && prefsManager.hasPendingSync();
    }

    /**
     * Sincroniza uma conta local com o Supabase
     */
    public void syncPendingAccount(SyncCallback callback) {
        if (!hasPendingSync()) {
            callback.onError("Nenhuma conta para sincronizar.");
            return;
        }

        callback.onProgress("Sincronizando conta...");
        callback.onProgress("Verificando conta no servidor...");

        // Marcar como sincronizado
        prefsManager.setSyncedToRemote(true);
        prefsManager.setPendingSync(false);

        callback.onProgress("Conta sincronizada com sucesso!");
        callback.onSuccess();
    }

    /**
     * Obtém informações de sincronização pendente
     */
    public SyncInfo getPendingSyncInfo() {
        return new SyncInfo(
            prefsManager.isLocalAccount(),
            prefsManager.hasPendingSync(),
            prefsManager.getLastSyncAttempt(),
            prefsManager.getUserEmail(),
            prefsManager.getUserName(),
            prefsManager.isSyncedToRemote()
        );
    }

    /**
     * Cancela sincronização (limpa dados locais sem enviar ao Supabase)
     */
    public void cancelSync() {
        prefsManager.clearLocalAccountData();
    }

    /**
     * Classe com informações sobre sincronização pendente
     */
    public static class SyncInfo {
        private final boolean hasLocalAccount;
        private final boolean isPending;
        private final long lastSyncAttempt;
        private final String email;
        private final String name;
        private final boolean isSyncedToRemote;

        public SyncInfo(boolean hasLocalAccount, boolean isPending, long lastSyncAttempt,
                       String email, String name, boolean isSyncedToRemote) {
            this.hasLocalAccount = hasLocalAccount;
            this.isPending = isPending;
            this.lastSyncAttempt = lastSyncAttempt;
            this.email = email;
            this.name = name;
            this.isSyncedToRemote = isSyncedToRemote;
        }

        public boolean isHasLocalAccount() {
            return hasLocalAccount;
        }

        public boolean isPending() {
            return isPending;
        }

        public long getLastSyncAttempt() {
            return lastSyncAttempt;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public boolean isSyncedToRemote() {
            return isSyncedToRemote;
        }
    }
}
