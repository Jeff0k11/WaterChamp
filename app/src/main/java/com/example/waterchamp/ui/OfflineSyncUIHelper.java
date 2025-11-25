package com.example.waterchamp.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.example.waterchamp.data.local.PreferencesManager;
import com.example.waterchamp.service.AccountSyncService;
import com.example.waterchamp.utils.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;

/**
 * Utilitário para mostrar indicadores visuais de status offline/sync na UI
 * Oferece métodos simples para mostrar notificações ao usuário
 */
public class OfflineSyncUIHelper {

    private final Activity activity;
    private final PreferencesManager prefsManager;
    private final AccountSyncService syncService;

    public OfflineSyncUIHelper(Activity activity) {
        this.activity = activity;
        this.prefsManager = new PreferencesManager(activity);
        this.syncService = new AccountSyncService(activity);
    }

    /**
     * Mostra um Snackbar informando que está no modo offline
     */
    public void showOfflineModeSnackbar() {
        if (!NetworkUtils.isNetworkAvailable(activity)) {
            showSnackbar("Modo Offline - Seus dados serão sincronizados quando conectar", Snackbar.LENGTH_LONG);
        }
    }

    /**
     * Mostra um Snackbar informando que há sincronização pendente
     */
    public void showPendingSyncSnackbar() {
        if (syncService.hasPendingSync()) {
            AccountSyncService.SyncInfo syncInfo = syncService.getPendingSyncInfo();
            String message = "Conta pendente de sincronização: " + syncInfo.getEmail();
            showSnackbar(message, Snackbar.LENGTH_INDEFINITE);
        }
    }

    /**
     * Mostra um Snackbar informando sucesso de sincronização
     */
    public void showSyncSuccessSnackbar() {
        showSnackbar("Conta sincronizada com sucesso!", Snackbar.LENGTH_SHORT);
    }

    /**
     * Mostra um Snackbar informando erro de sincronização
     */
    public void showSyncErrorSnackbar(String error) {
        showSnackbar("Erro na sincronização: " + error, Snackbar.LENGTH_LONG);
    }

    /**
     * Adiciona um badge no topo indicando modo offline
     * Útil para manter sempre visível o status offline
     */
    public void addOfflineBadge() {
        if (!NetworkUtils.isNetworkAvailable(activity)) {
            addStatusBadge("📡 Offline", android.R.color.holo_red_dark);
        }
    }

    /**
     * Adiciona um badge indicando sincronização pendente
     */
    public void addSyncPendingBadge() {
        if (syncService.hasPendingSync()) {
            addStatusBadge("⏳ Sincronizando...", android.R.color.holo_orange_dark);
        }
    }

    /**
     * Monitora status de rede e sincronização
     * Chama callback quando status mudar
     */
    public void monitorSyncStatus(SyncStatusListener listener) {
        // Verificar status atual
        boolean hasNetwork = NetworkUtils.isNetworkAvailable(activity);
        boolean hasPending = syncService.hasPendingSync();

        if (!hasNetwork) {
            listener.onOfflineMode();
        } else if (hasPending) {
            listener.onSyncPending();
        } else {
            listener.onOnlineMode();
        }

        // Nota: Em um app real, você usaria um service ou LocalBroadcastReceiver
        // para monitorar mudanças de conectividade em tempo real
        // Este é apenas um exemplo simplificado
    }

    /**
     * Mostra um dialog confirmando sincronização de conta local
     */
    public void showAccountSyncConfirmationDialog(SyncConfirmationListener listener) {
        if (!syncService.hasPendingSync()) {
            return;
        }

        AccountSyncService.SyncInfo syncInfo = syncService.getPendingSyncInfo();

        // Criar um AlertDialog (requer androidx.appcompat)
        new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Sincronizar Conta")
                .setMessage("Sua conta '" + syncInfo.getName() + "' (" + syncInfo.getEmail() + ") está pendente de sincronização.\n\nDeseja sincronizar agora?")
                .setPositiveButton("Sincronizar", (dialog, which) -> {
                    listener.onConfirmSync();
                    syncService.syncPendingAccount(new AccountSyncService.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            showSyncSuccessSnackbar();
                            listener.onSyncComplete();
                        }

                        @Override
                        public void onError(String message) {
                            showSyncErrorSnackbar(message);
                            listener.onSyncError(message);
                        }

                        @Override
                        public void onProgress(String message) {
                            listener.onSyncProgress(message);
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> listener.onCancelSync())
                .setCancelable(false)
                .show();
    }

    /**
     * Helper para mostrar Snackbar
     */
    private void showSnackbar(String message, int duration) {
        try {
            View rootView = activity.findViewById(android.R.id.content);
            if (rootView != null) {
                Snackbar.make(rootView, message, duration).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper para adicionar badge de status no topo da activity
     */
    private void addStatusBadge(String text, int colorResId) {
        try {
            FrameLayout rootView = (FrameLayout) activity.getWindow().getDecorView().getRootView();

            // Criar TextView para o badge
            TextView badge = new TextView(activity);
            badge.setText(text);
            badge.setTextColor(android.graphics.Color.WHITE);
            badge.setBackgroundColor(activity.getResources().getColor(colorResId));
            badge.setTextSize(12);
            badge.setPadding(16, 8, 16, 8);
            badge.setGravity(Gravity.CENTER);

            // Parâmetros de layout
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.TOP;

            // Adicionar ao root view
            rootView.addView(badge, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Interface para monitorar status de sincronização
     */
    public interface SyncStatusListener {
        void onOfflineMode();

        void onSyncPending();

        void onOnlineMode();
    }

    /**
     * Interface para confirmar sincronização
     */
    public interface SyncConfirmationListener {
        void onConfirmSync();

        void onSyncProgress(String message);

        void onSyncComplete();

        void onSyncError(String error);

        void onCancelSync();
    }
}
