package com.example.waterchamp;

import android.app.Application;
import com.example.waterchamp.data.remote.SupabaseClient;

/**
 * Classe Application do WaterChamp
 * Inicializa componentes globais como o cliente Supabase
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
    }
}
