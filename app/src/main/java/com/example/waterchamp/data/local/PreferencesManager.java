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

    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, 2000); // Default: 2000ml
    }

    public void setDefaultCupSize(int sizeInMl) {
        prefs.edit().putInt(KEY_DEFAULT_CUP_SIZE, sizeInMl).apply();
    }

    public int getDefaultCupSize() {
        return prefs.getInt(KEY_DEFAULT_CUP_SIZE, 250); // Default: 250ml
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true); // Default: true
    }

    public void setProfilePictureUri(String uri) {
        prefs.edit().putString(KEY_PROFILE_PICTURE_URI, uri).apply();
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
            .apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
