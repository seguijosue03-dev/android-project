package com.student.learncraft;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREF_NAME = "LearnCraftSettings";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_TIMER_ENABLED = "timer_enabled";
    private static final String KEY_AUTO_SAVE = "auto_save_progress";
    private static final String KEY_FONT_SIZE = "font_size";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_AUTO = "auto";

    private SharedPreferences preferences;

    public SettingsManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Theme Settings
    public void setThemeMode(String themeMode) {
        preferences.edit().putString(KEY_THEME_MODE, themeMode).apply();
    }

    public String getThemeMode() {
        return preferences.getString(KEY_THEME_MODE, THEME_LIGHT);
    }

    public boolean isDarkMode() {
        return THEME_DARK.equals(getThemeMode());
    }

    // Notifications
    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public boolean isNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS, true);
    }

    // Sound Effects
    public void setSoundEffectsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SOUND_EFFECTS, enabled).apply();
    }

    public boolean isSoundEffectsEnabled() {
        return preferences.getBoolean(KEY_SOUND_EFFECTS, true);
    }

    // Timer
    public void setTimerEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_TIMER_ENABLED, enabled).apply();
    }

    public boolean isTimerEnabled() {
        return preferences.getBoolean(KEY_TIMER_ENABLED, true);
    }

    // Auto Save
    public void setAutoSaveEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AUTO_SAVE, enabled).apply();
    }

    public boolean isAutoSaveEnabled() {
        return preferences.getBoolean(KEY_AUTO_SAVE, true);
    }

    // Font Size
    public void setFontSize(String size) {
        preferences.edit().putString(KEY_FONT_SIZE, size).apply();
    }

    public String getFontSize() {
        return preferences.getString(KEY_FONT_SIZE, "medium");
    }

    // Reset All Settings
    public void resetToDefaults() {
        preferences.edit().clear().apply();
    }
}