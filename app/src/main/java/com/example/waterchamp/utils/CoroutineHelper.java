package com.example.waterchamp.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoroutineHelper {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface CoroutineCallback<T> {
        void onComplete(T result, String error);
    }

    public static <T> void runAsync(java.util.concurrent.Callable<T> task, CoroutineCallback<T> callback) {
        executor.execute(() -> {
            try {
                T result = task.call();
                handler.post(() -> callback.onComplete(result, null));
            } catch (Exception e) {
                handler.post(() -> callback.onComplete(null, e.getMessage()));
            }
        });
    }
}