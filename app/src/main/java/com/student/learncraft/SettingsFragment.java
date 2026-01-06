package com.student.learncraft;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private RadioGroup rgTheme;
    private RadioButton rbLight, rbDark;
    private Switch switchNotifications, switchSoundEffects, switchTimer, switchAutoSave;
    private Button btnResetSettings, btnClearData, btnAbout;
    private TextView tvVersion;

    private SettingsManager settingsManager;
    private StorageManager storageManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsManager = new SettingsManager(requireContext());
        storageManager = new StorageManager(requireContext());

        initViews(view);
        loadSettings();

        return view;
    }

    private void initViews(View view) {
        rgTheme = view.findViewById(R.id.rgTheme);
        rbLight = view.findViewById(R.id.rbLight);
        rbDark = view.findViewById(R.id.rbDark);

        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchSoundEffects = view.findViewById(R.id.switchSoundEffects);
        switchTimer = view.findViewById(R.id.switchTimer);
        switchAutoSave = view.findViewById(R.id.switchAutoSave);

        btnResetSettings = view.findViewById(R.id.btnResetSettings);
        btnClearData = view.findViewById(R.id.btnClearData);
        btnAbout = view.findViewById(R.id.btnAbout);
        tvVersion = view.findViewById(R.id.tvVersion);

        // Theme change listener
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLight) {
                settingsManager.setThemeMode(SettingsManager.THEME_LIGHT);
            } else if (checkedId == R.id.rbDark) {
                settingsManager.setThemeMode(SettingsManager.THEME_DARK);
            }

            applyTheme(checkedId);
        });



        // Switch listeners
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setNotificationsEnabled(isChecked);
            Toast.makeText(requireContext(), isChecked ? "✅ Notifications enabled" : "🔕 Notifications disabled", Toast.LENGTH_SHORT).show();
        });

        switchSoundEffects.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setSoundEffectsEnabled(isChecked);
        });

        switchTimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setTimerEnabled(isChecked);
            Toast.makeText(requireContext(), isChecked ? "⏱️ Timer enabled" : "Timer disabled", Toast.LENGTH_SHORT).show();
        });

        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setAutoSaveEnabled(isChecked);
        });

        // Button listeners
        btnResetSettings.setOnClickListener(v -> resetSettings());
        btnClearData.setOnClickListener(v -> clearData());
        btnAbout.setOnClickListener(v -> showAbout());

        tvVersion.setText("Version 1.0.0");
    }

    private void loadSettings() {
        // Load theme
        String theme = settingsManager.getThemeMode();
        if (SettingsManager.THEME_LIGHT.equals(theme)) {
            rbLight.setChecked(true);
        } else {
            rbDark.setChecked(true);
        }

        // Load switches
        switchNotifications.setChecked(settingsManager.isNotificationsEnabled());
        switchSoundEffects.setChecked(settingsManager.isSoundEffectsEnabled());
        switchTimer.setChecked(settingsManager.isTimerEnabled());
        switchAutoSave.setChecked(settingsManager.isAutoSaveEnabled());
    }

    private void applyTheme(int checkedId) {

        int newMode;

        if (checkedId == R.id.rbDark) {
            newMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            newMode = AppCompatDelegate.MODE_NIGHT_NO;
        }

        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == newMode) return;

        AppCompatDelegate.setDefaultNightMode(newMode);
    }



    private void resetSettings() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Settings")
                .setMessage("Reset all settings to default?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    settingsManager.resetToDefaults();
                    loadSettings();
                    Toast.makeText(requireContext(), "✅ Settings reset to default", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Clear All Data")
                .setMessage("This will delete all quiz results and uploaded PPTs. This action cannot be undone!")
                .setPositiveButton("Clear", (dialog, which) -> {
                    storageManager.clearAllResults();
                    Toast.makeText(requireContext(), "✅ All data cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAbout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About LearnCraft")
                .setMessage("LearnCraft v1.0.0\n\n" +
                        "Transform your PowerPoint presentations into smart MCQ quizzes!\n\n" +
                        "Features:\n" +
                        "• Offline quiz generation\n" +
                        "• Marwadi-style MCQs\n" +
                        "• Performance analytics\n" +
                        "• Progress tracking\n\n" +
                        "Developed with ❤️ for students")
                .setPositiveButton("OK", null)
                .show();
    }
}