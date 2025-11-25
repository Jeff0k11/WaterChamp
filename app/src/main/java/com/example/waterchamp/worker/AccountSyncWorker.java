package com.example.waterchamp.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.waterchamp.service.AccountSyncService;
import com.example.waterchamp.utils.NetworkUtils;

/**
 * Worker do WorkManager que sincroniza contas locais em background
 * Executa quando internet volta a estar disponível
 */
public class AccountSyncWorker extends Worker {

    private final AccountSyncService syncService;

    public AccountSyncWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.syncService = new AccountSyncService(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Verificar se tem internet
            boolean hasNetwork = NetworkUtils.isNetworkAvailable(getApplicationContext());

            if (!hasNetwork) {
                // Sem internet, tentar novamente mais tarde
                return Result.retry();
            }

            // Verificar se há contas para sincronizar
            if (!syncService.hasPendingSync()) {
                // Nada para sincronizar
                return Result.success();
            }

            // Contas pendentes, marcar como tentativa realizada
            // Em um app real, você faria a sincronização aqui
            return Result.success();

        } catch (Exception e) {
            // Erro durante sincronização, tentar novamente
            return Result.retry();
        }
    }

    public static final String WORK_NAME = "account_sync_work";
}
