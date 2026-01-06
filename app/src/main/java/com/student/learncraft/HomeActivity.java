package com.student.learncraft;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private CardView cardUploadPPT, cardStartQuiz, cardViewProgress;
    private TextView tvWelcome, tvStats;
    private Button btnLogout;
    private StorageManager storageManager;
    private UserManager userManager;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Uri selectedPPTUri;
    private String selectedPPTName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize storage manager
        storageManager = new StorageManager(this);
        userManager = new UserManager(this);

        // Check if user is logged in
        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Setup file picker
        setupFilePicker();

        // Load statistics
        loadStats();

        // Check permissions
        checkPermissions();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStats = findViewById(R.id.tvStats);
        btnLogout = findViewById(R.id.btnLogout);

        // Display user name
        User currentUser = userManager.getCurrentUser();
        if (currentUser != null) {
            tvWelcome.setText("Welcome, " + currentUser.getFullName() + "!");
        }

        cardUploadPPT = findViewById(R.id.cardUploadPPT);
        cardStartQuiz = findViewById(R.id.cardStartQuiz);
        cardViewProgress = findViewById(R.id.cardViewProgress);

        // Set click listeners
        cardUploadPPT.setOnClickListener(v -> openFilePicker());
        cardStartQuiz.setOnClickListener(v -> startQuiz());
        cardViewProgress.setOnClickListener(v -> viewProgress());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedPPTUri = result.getData().getData();

                        if (selectedPPTUri != null) {
                            handlePPTSelection(selectedPPTUri);
                        }
                    }
                }
        );
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.presentationml.presentation");

        // Also allow .ppt files
        String[] mimeTypes = {
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-powerpoint"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        filePickerLauncher.launch(intent);
    }

    private void handlePPTSelection(Uri uri) {
        // Get file name
        String fileName = getFileName(uri);

        if (fileName == null) {
            fileName = "presentation_" + System.currentTimeMillis();
        }

        selectedPPTName = fileName;

        // Validate PPT
        PPTReader pptReader = new PPTReader(this);

        if (pptReader.isValidPPT(uri)) {
            // Save PPT name
            storageManager.savePPTName(fileName);

            Toast.makeText(this, "✅ PPT uploaded: " + fileName, Toast.LENGTH_SHORT).show();

            // Enable start quiz button
            cardStartQuiz.setEnabled(true);
            cardStartQuiz.setAlpha(1.0f);

        } else {
            Toast.makeText(this, "❌ Invalid PPT file", Toast.LENGTH_SHORT).show();
            selectedPPTUri = null;
            selectedPPTName = null;
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        }

        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }

        return fileName;
    }

    private void startQuiz() {
        if (selectedPPTUri == null) {
            Toast.makeText(this, "⚠️ Please upload a PPT first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Go to quiz setup
        Intent intent = new Intent(this, QuizSetupActivity.class);
        intent.putExtra("ppt_uri", selectedPPTUri.toString());
        intent.putExtra("ppt_name", selectedPPTName);
        startActivity(intent);
    }

    private void viewProgress() {
        Intent intent = new Intent(this, AnalyticsActivity.class);
        startActivity(intent);
    }

    private void loadStats() {
        int totalQuizzes = storageManager.getTotalQuizzesTaken();
        float avgPercentage = storageManager.getOverallAveragePercentage();

        String stats = String.format("Total Quizzes: %d | Average: %.1f%%",
                totalQuizzes, avgPercentage);
        tvStats.setText(stats);
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Permission needed to read PPT files", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats(); // Refresh stats when returning to home
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you want to logout or exit?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Stay", null)
                .show();
    }
}