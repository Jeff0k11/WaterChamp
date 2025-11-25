package com.example.waterchamp;

import android.app.Application;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.waterchamp.data.remote.SupabaseClient;
import com.example.waterchamp.worker.AccountSyncWorker;
import java.util.concurrent.TimeUnit;

/**
 * Classe Application do WaterChamp
 * Inicializa componentes globais como o cliente Supabase e WorkManager para sincronização
 */
public class WaterChampApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar Supabase Client
        try {
            SupabaseClient.INSTANCE.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicializar sincronização automática de contas locais
        scheduleSyncWorker();
    }

    /**
     * Agenda o worker de sincronização para rodar periodicamente
     * Sincroniza contas locais quando internet voltar a estar disponível
     */
    private void scheduleSyncWorker() {
        try {
            // Configurar constraints: só sincronizar quando tiver internet
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Configurar work: executar a cada 15 minutos
            PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                    AccountSyncWorker.class,
                    15,  // interval
                    TimeUnit.MINUTES
            )
                    .setConstraints(constraints)
                    .build();

            // Enfileirar o trabalho
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    AccountSyncWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,  // Manter o trabalho existente
                    syncWorkRequest
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
