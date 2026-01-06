package com.student.learncraft;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePicture;
    private TextView tvProfileName, tvProfileEmail, tvProfileRole, tvRegistrationDate;
    private TextView tvTotalQuizzes, tvAvgScore, tvBestScore;
    private Button btnEditProfile, btnLogout, btnChangePhoto, btnSettings;

    private UserManager userManager;
    private StorageManager storageManager;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userManager = new UserManager(requireContext());
        storageManager = new StorageManager(requireContext());

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

        // Also allow clicking on image to change
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
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImageSelection(imageUri);
                        }
                    }
                }
        );
    }

    private void changeProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            // Load and compress image
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Compress to reasonable size
            Bitmap resizedBitmap = resizeBitmap(bitmap, 400, 400);

            // Convert to Base64 string for storage
            String base64Image = bitmapToBase64(resizedBitmap);

            // Save to user profile
            User currentUser = userManager.getCurrentUser();
            if (currentUser != null) {
                currentUser.setProfilePicturePath(base64Image);
                userManager.updateUser(currentUser);

                // Display image
                ivProfilePicture.setImageBitmap(resizedBitmap);

                Toast.makeText(requireContext(), "✅ Profile picture updated!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "❌ Error updating picture", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadUserData() {
        User currentUser = userManager.getCurrentUser();

        if (currentUser != null) {
            tvProfileName.setText(currentUser.getFullName());
            tvProfileEmail.setText(currentUser.getEmail());
            tvProfileRole.setText(currentUser.getRole());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvRegistrationDate.setText("Member since " + sdf.format(currentUser.getRegistrationDate()));

            // Load profile picture
            if (currentUser.getProfilePicturePath() != null && !currentUser.getProfilePicturePath().isEmpty()) {
                Bitmap bitmap = base64ToBitmap(currentUser.getProfilePicturePath());
                if (bitmap != null) {
                    ivProfilePicture.setImageBitmap(bitmap);
                }
            } else {
                // Default icon
                ivProfilePicture.setImageResource(android.R.drawable.ic_menu_myplaces);
            }

            // Load quiz statistics
            int totalQuizzes = storageManager.getTotalQuizzesTaken();
            float avgScore = storageManager.getOverallAveragePercentage();

            // Calculate best score
            float bestScore = 0f;
            for (QuizResult result : storageManager.getAllQuizResults()) {
                if (result.getPercentage() > bestScore) {
                    bestScore = result.getPercentage();
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

        // Create edit dialog
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