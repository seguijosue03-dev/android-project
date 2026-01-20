package com.student.learncraft;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private Switch switchNotifications, switchSoundEffects, switchTimer, switchAutoSave;
    private Button btnResetSettings, btnClearData, btnAbout;
    private TextView tvVersion;

    private SettingsManager settingsManager;
    private StorageManager storageManager;

    // 🔥 NEW: Add DatabaseHelper
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsManager = new SettingsManager(requireContext());
        storageManager = new StorageManager(requireContext());

        // 🔥 Initialize Database Helper
        dbHelper = new DatabaseHelper(requireContext());

        initViews(view);
        loadSettings();

        return view;
    }

    private void initViews(View view) {

        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchSoundEffects = view.findViewById(R.id.switchSoundEffects);
        switchTimer = view.findViewById(R.id.switchTimer);
        switchAutoSave = view.findViewById(R.id.switchAutoSave);

        btnResetSettings = view.findViewById(R.id.btnResetSettings);
        btnClearData = view.findViewById(R.id.btnClearData);
        btnAbout = view.findViewById(R.id.btnAbout);
        tvVersion = view.findViewById(R.id.tvVersion);

        // Switch listeners
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setNotificationsEnabled(isChecked);
            Toast.makeText(
                    requireContext(),
                    isChecked ? "✅ Notifications enabled" : "🔕 Notifications disabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        switchSoundEffects.setOnCheckedChangeListener((buttonView, isChecked) ->
                settingsManager.setSoundEffectsEnabled(isChecked)
        );

        switchTimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setTimerEnabled(isChecked);
            Toast.makeText(
                    requireContext(),
                    isChecked ? "⏱️ Timer enabled" : "Timer disabled",
                    Toast.LENGTH_SHORT
            ).show();
        });

        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) ->
                settingsManager.setAutoSaveEnabled(isChecked)
        );

        // Button listeners
        btnResetSettings.setOnClickListener(v -> resetSettings());
        btnClearData.setOnClickListener(v -> clearData());
        btnAbout.setOnClickListener(v -> showAbout());

        tvVersion.setText("Version 1.0.0");
    }

    private void loadSettings() {
        switchNotifications.setChecked(settingsManager.isNotificationsEnabled());
        switchSoundEffects.setChecked(settingsManager.isSoundEffectsEnabled());
        switchTimer.setChecked(settingsManager.isTimerEnabled());
        switchAutoSave.setChecked(settingsManager.isAutoSaveEnabled());
    }

    private void resetSettings() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Settings")
                .setMessage("Reset all settings to default?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    settingsManager.resetToDefaults();
                    loadSettings();
                    Toast.makeText(
                            requireContext(),
                            "✅ Settings reset to default",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearData() {
        new AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Clear All Data")
                .setMessage(
                        "This will delete all quiz results and history. " +
                                "This action cannot be undone!"
                )
                .setPositiveButton("Clear", (dialog, which) -> {

                    // 🔥 FIX: Clear the database instead of just storage
                    dbHelper.clearAllData();

                    // Optional: Clear old storage too if you want
                    storageManager.clearAllResults();

                    Toast.makeText(
                            requireContext(),
                            "✅ All history cleared!",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAbout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About LearnCraft")
                .setMessage(
                        "LearnCraft v1.0.0\n\n" +
                                "Transform your PowerPoint presentations into smart MCQ quizzes!\n\n" +
                                "Features:\n" +
                                "• Offline quiz generation\n" +
                                "• Marwadi-style MCQs\n" +
                                "• Performance analytics\n" +
                                "• Progress tracking\n\n" +
                                "Developed with ❤️ for students"
                )
                .setPositiveButton("OK", null)
                .show();
    }
}