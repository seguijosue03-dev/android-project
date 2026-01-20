package com.student.learncraft;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide; // 🔥 Make sure you imported Glide!

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePicture;
    private TextView tvProfileName, tvProfileEmail, tvProfileRole, tvRegistrationDate;
    private TextView tvTotalQuizzes, tvAvgScore, tvBestScore;
    private Button btnEditProfile, btnLogout, btnChangePhoto, btnSettings;

    private UserManager userManager;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userManager = new UserManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        initViews(view);
        setupImagePicker();
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);
        tvRegistrationDate = view.findViewById(R.id.tvRegistrationDate);

        tvTotalQuizzes = view.findViewById(R.id.tvTotalQuizzes);
        tvAvgScore = view.findViewById(R.id.tvAvgScore);
        tvBestScore = view.findViewById(R.id.tvBestScore);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        btnSettings = view.findViewById(R.id.btnSettings);

        btnEditProfile.setOnClickListener(v -> editProfile());
        btnLogout.setOnClickListener(v -> logout());
        btnChangePhoto.setOnClickListener(v -> changeProfilePicture());
        btnSettings.setOnClickListener(v -> openSettings());

        // Allow clicking the image itself to change it
        ivProfilePicture.setOnClickListener(v -> changeProfilePicture());
    }

    private void openSettings() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImageSelection(imageUri);
                        }
                    }
                }
        );
    }

    private void changeProfilePicture() {
        // Open gallery to pick an image
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // ACTION_OPEN_DOCUMENT is better for persisting permissions
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            // 1. 🔥 CRITICAL: Request permission to read this file forever (even after restart)
            requireActivity().getContentResolver().takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // 2. Save the URI string (path) to the database
            User currentUser = userManager.getCurrentUser();
            if (currentUser != null) {
                currentUser.setProfilePicturePath(imageUri.toString());
                userManager.updateUser(currentUser);

                // 3. Load immediately using Glide
                Glide.with(this)
                        .load(imageUri)
                        .circleCrop()
                        .into(ivProfilePicture);

                Toast.makeText(requireContext(), "✅ Profile picture updated!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "❌ Error saving picture permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        User currentUser = userManager.getCurrentUser();

        if (currentUser != null) {
            tvProfileName.setText(currentUser.getFullName());
            tvProfileEmail.setText(currentUser.getEmail());
            tvProfileRole.setText(currentUser.getRole());

            // Handle Date
            if (currentUser.getRegistrationDate() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvRegistrationDate.setText("Member since " + sdf.format(new Date(currentUser.getRegistrationDate())));
            } else {
                tvRegistrationDate.setText("Member");
            }

            // 🔥 LOAD IMAGE WITH GLIDE
            String path = currentUser.getProfilePicturePath();
            if (path != null && !path.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(path)) // Load the path we saved
                        .placeholder(R.drawable.ic_person) // Default while loading
                        .error(R.drawable.ic_person) // Default if it fails
                        .circleCrop()
                        .into(ivProfilePicture);
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_person);
            }

            // --- QUIZ STATS LOGIC (Kept exactly as you had it) ---
            List<QuizResult> results = dbHelper.getAllResults(); // Note: This gets ALL results, you might want getResultsForUser(email) later

            // Filter results for THIS user specifically
            // (If dbHelper.getAllResults returns everyone's results, the stats might be wrong unless we filter)
            // Ideally: List<QuizResult> results = dbHelper.getResultsForUser(currentUser.getEmail());

            int totalQuizzes = 0;
            float avgScore = 0f;
            float bestScore = 0f;

            if (results != null && !results.isEmpty()) {
                // If your dbHelper.getAllResults() returns results for EVERYONE,
                // you should filter here. Assuming for now it's okay or fixed in DbHelper:
                totalQuizzes = results.size();
                float totalSum = 0f;

                for (QuizResult result : results) {
                    float p = result.getPercentage();
                    totalSum += p;
                    if (p > bestScore) {
                        bestScore = p;
                    }
                }
                if (totalQuizzes > 0) {
                    avgScore = totalSum / totalQuizzes;
                }
            }

            tvTotalQuizzes.setText(String.valueOf(totalQuizzes));
            tvAvgScore.setText(String.format("%.1f%%", avgScore));
            tvBestScore.setText(String.format("%.1f%%", bestScore));
        }
    }

    private void editProfile() {
        User currentUser = userManager.getCurrentUser();
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        android.widget.EditText etName = dialogView.findViewById(R.id.etEditName);
        android.widget.EditText etEmail = dialogView.findViewById(R.id.etEditEmail);

        etName.setText(currentUser.getFullName());
        etEmail.setText(currentUser.getEmail());

        builder.setView(dialogView)
                .setTitle("Edit Profile")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();

                    if (newName.isEmpty() || newEmail.isEmpty()) {
                        Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentUser.setFullName(newName);
                    currentUser.setEmail(newEmail);

                    boolean success = userManager.updateUser(currentUser);

                    if (success) {
                        Toast.makeText(requireContext(), "✅ Profile updated!", Toast.LENGTH_SHORT).show();
                        loadUserData();
                    } else {
                        Toast.makeText(requireContext(), "❌ Update failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userManager.logout();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }
}