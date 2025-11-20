package com.example.waterchamp.model;

import java.util.ArrayList;
import java.util.List;

public class User implements Comparable<User> {
    private String name;
    private int waterIntake; // Current daily intake
    private String email;
    private int rank;
    private List<HistoryRecord> historyList;

    // New fields for Profile
    private int dailyGoal = 2000; // Default 2000ml
    private int defaultCupSize = 250; // Default 250ml
    private long totalConsumedAllTime = 0;
    private int streak = 0;
    private long creationDate;
    private String profilePictureUri; // URI string for profile picture
    private boolean notificationsEnabled = true; // Default to true

    public User(String name, String email, int waterIntake) {
        this.name = name;
        this.email = email;
        this.waterIntake = waterIntake;
        this.historyList = new ArrayList<>();
        this.creationDate = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWaterIntake() {
        return waterIntake;
    }

    public void setWaterIntake(int waterIntake) {
        // Calculate difference to update total
        int diff = waterIntake - this.waterIntake;
        if (diff > 0) {
            this.totalConsumedAllTime += diff;
        }
        if (diff < 0) {
            this.totalConsumedAllTime += diff;
        }

        this.waterIntake = waterIntake;
    }

    public String getEmail() {
        return email;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public List<HistoryRecord> getHistoryList() {
        return historyList;
    }

    public void addHistoryRecord(HistoryRecord record) {
        this.historyList.add(record);
    }

    public int getDailyGoal() {
        return dailyGoal;
    }

    public void setDailyGoal(int dailyGoal) {
        this.dailyGoal = dailyGoal;
    }

    public int getDefaultCupSize() {
        return defaultCupSize;
    }

    public void setDefaultCupSize(int defaultCupSize) {
        this.defaultCupSize = defaultCupSize;
    }

    public long getTotalConsumedAllTime() {
        return totalConsumedAllTime;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    @Override
    public int compareTo(User other) {
        // Descending order (higher intake first)
        return Integer.compare(other.waterIntake, this.waterIntake);
    }
}
