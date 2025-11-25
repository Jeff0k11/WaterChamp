package com.example.waterchamp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gerenciador de preferências locais do usuário
 * Armazena configurações pessoais que não precisam ser compartilhadas
 */
public class PreferencesManager {
    private static final String PREF_NAME = "WaterChampPreferences";

    // Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_DAILY_GOAL = "daily_goal";
    private static final String KEY_DEFAULT_CUP_SIZE = "default_cup_size";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";
    private static final String KEY_TOTAL_CONSUMED_ALL_TIME = "total_consumed_all_time";
    private static final String KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp";
    private static final String KEY_REMEMBER_LOGIN = "remember_login";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    // Keys Cache Ranking
    private static final String KEY_CACHED_RANKING_GLOBAL = "cached_ranking_global";
    private static final String KEY_CACHED_RANKING_GROUP = "cached_ranking_group";

    // Keys para Contas Locais (Offline-First)
    private static final String KEY_IS_LOCAL_ACCOUNT = "is_local_account";
    private static final String KEY_LOCAL_ACCOUNT_ID = "local_account_id";
    private static final String KEY_PENDING_SYNC = "pending_sync";
    private static final String KEY_LOCAL_PASSWORD_HASH = "local_password_hash";
    private static final String KEY_LAST_SYNC_ATTEMPT = "last_sync_attempt";
    private static final String KEY_SYNCED_TO_REMOTE = "synced_to_remote";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ============ User Info ============

    public void setUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void setUserEmail(String email) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    // ============ User Settings ============

    public void setDailyGoal(int goalInMl) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goalInMl).apply();
    }

    public void setDailyGoalSync(int goalInMl) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goalInMl).commit();
    }

    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, 2000); // Default: 2000ml
    }

    public void setDefaultCupSize(int sizeInMl) {
        prefs.edit().putInt(KEY_DEFAULT_CUP_SIZE, sizeInMl).apply();
    }

    public void setDefaultCupSizeSync(int sizeInMl) {
        prefs.edit().putInt(KEY_DEFAULT_CUP_SIZE, sizeInMl).commit();
    }

    public int getDefaultCupSize() {
        return prefs.getInt(KEY_DEFAULT_CUP_SIZE, 250); // Default: 250ml
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public void setNotificationsEnabledSync(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).commit();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true); // Default: true
    }

    public void setProfilePictureUri(String uri) {
        prefs.edit().putString(KEY_PROFILE_PICTURE_URI, uri).apply();
    }

    public void setProfilePictureUriSync(String uri) {
        prefs.edit().putString(KEY_PROFILE_PICTURE_URI, uri).commit();
    }

    public String getProfilePictureUri() {
        return prefs.getString(KEY_PROFILE_PICTURE_URI, null);
    }

    // ============ Statistics ============

    public void setTotalConsumedAllTime(long totalInMl) {
        prefs.edit().putLong(KEY_TOTAL_CONSUMED_ALL_TIME, totalInMl).apply();
    }

    public long getTotalConsumedAllTime() {
        return prefs.getLong(KEY_TOTAL_CONSUMED_ALL_TIME, 0);
    }

    public void addToTotalConsumed(int amountInMl) {
        long current = getTotalConsumedAllTime();
        setTotalConsumedAllTime(current + amountInMl);
    }

    // ============ Sync ============

    public void setLastSyncTimestamp(long timestamp) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, timestamp).apply();
    }

    public long getLastSyncTimestamp() {
        return prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0);
    }
    
    // ============ Ranking Cache ============
    
    public void setCachedRankingGlobal(String json) {
        prefs.edit().putString(KEY_CACHED_RANKING_GLOBAL, json).apply();
    }
    
    public String getCachedRankingGlobal() {
        return prefs.getString(KEY_CACHED_RANKING_GLOBAL, "[]");
    }
    
    public void setCachedRankingGroup(String json) {
        prefs.edit().putString(KEY_CACHED_RANKING_GROUP, json).apply();
    }
    
    public String getCachedRankingGroup() {
        return prefs.getString(KEY_CACHED_RANKING_GROUP, "[]");
    }

    // ============ Remember Login ============

    public void setRememberLogin(boolean remember) {
        prefs.edit().putBoolean(KEY_REMEMBER_LOGIN, remember).apply();
    }

    public boolean isRememberLogin() {
        return prefs.getBoolean(KEY_REMEMBER_LOGIN, false);
    }

    public void setSavedEmail(String email) {
        prefs.edit().putString(KEY_SAVED_EMAIL, email).apply();
    }

    public String getSavedEmail() {
        return prefs.getString(KEY_SAVED_EMAIL, "");
    }

    public void setSavedPassword(String password) {
        prefs.edit().putString(KEY_SAVED_PASSWORD, password).apply();
    }

    public String getSavedPassword() {
        return prefs.getString(KEY_SAVED_PASSWORD, "");
    }

    public void clearSavedCredentials() {
        prefs.edit()
            .remove(KEY_REMEMBER_LOGIN)
            .remove(KEY_SAVED_EMAIL)
            .remove(KEY_SAVED_PASSWORD)
            .apply();
    }

    // ============ Bulk Operations ============

    /**
     * Salva múltiplas configurações de usuário de forma síncrona (offline-first)
     * Garante que todos os dados sejam persistidos imediatamente no disco
     */
    public void saveUserSettingsSync(String name, int dailyGoal, int cupSize, boolean notificationsEnabled, String profilePictureUri) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putInt(KEY_DAILY_GOAL, dailyGoal);
        editor.putInt(KEY_DEFAULT_CUP_SIZE, cupSize);
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, notificationsEnabled);
        if (profilePictureUri != null) {
            editor.putString(KEY_PROFILE_PICTURE_URI, profilePictureUri);
        }
        // Usar commit() para garantir salvamento síncrono (bloqueia até salvar)
        editor.commit();
    }

    // ============ Session Management ============

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    public void clearUserData() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_PROFILE_PICTURE_URI)
            .remove(KEY_TOTAL_CONSUMED_ALL_TIME)
            .remove(KEY_LAST_SYNC_TIMESTAMP)
            .remove(KEY_CACHED_RANKING_GLOBAL) // Limpa ranking ao deslogar
            .remove(KEY_CACHED_RANKING_GROUP)
            .apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    // ============ Local Accounts (Offline-First) ============

    /**
     * Marca se a conta é local (criada offline)
     */
    public void setIsLocalAccount(boolean isLocal) {
        prefs.edit().putBoolean(KEY_IS_LOCAL_ACCOUNT, isLocal).apply();
    }

    public boolean isLocalAccount() {
        return prefs.getBoolean(KEY_IS_LOCAL_ACCOUNT, false);
    }

    /**
     * ID único local gerado para a conta offline (UUID)
     */
    public void setLocalAccountId(String localId) {
        prefs.edit().putString(KEY_LOCAL_ACCOUNT_ID, localId).apply();
    }

    public String getLocalAccountId() {
        return prefs.getString(KEY_LOCAL_ACCOUNT_ID, "");
    }

    /**
     * Hash da senha para validação offline
     * Nunca armazenar a senha original por segurança
     */
    public void setLocalPasswordHash(String hash) {
        prefs.edit().putString(KEY_LOCAL_PASSWORD_HASH, hash).apply();
    }

    public String getLocalPasswordHash() {
        return prefs.getString(KEY_LOCAL_PASSWORD_HASH, "");
    }

    /**
     * Flag indicando se há dados pendentes de sincronização
     */
    public void setPendingSync(boolean pending) {
        prefs.edit().putBoolean(KEY_PENDING_SYNC, pending).apply();
    }

    public boolean hasPendingSync() {
        return prefs.getBoolean(KEY_PENDING_SYNC, false);
    }

    /**
     * Timestamp da última tentativa de sincronização
     */
    public void setLastSyncAttempt(long timestamp) {
        prefs.edit().putLong(KEY_LAST_SYNC_ATTEMPT, timestamp).apply();
    }

    public long getLastSyncAttempt() {
        return prefs.getLong(KEY_LAST_SYNC_ATTEMPT, 0);
    }

    /**
     * Flag indicando se a conta foi sincronizada com sucesso com o Supabase
     */
    public void setSyncedToRemote(boolean synced) {
        prefs.edit().putBoolean(KEY_SYNCED_TO_REMOTE, synced).apply();
    }

    public boolean isSyncedToRemote() {
        return prefs.getBoolean(KEY_SYNCED_TO_REMOTE, false);
    }

    /**
     * Limpar dados de conta local após sincronização bem-sucedida
     */
    public void clearLocalAccountData() {
        prefs.edit()
            .remove(KEY_IS_LOCAL_ACCOUNT)
            .remove(KEY_LOCAL_ACCOUNT_ID)
            .remove(KEY_LOCAL_PASSWORD_HASH)
            .remove(KEY_PENDING_SYNC)
            .remove(KEY_SYNCED_TO_REMOTE)
            .apply();
    }
}
