package com.student.learncraft;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvWelcome, tvStats;
    private CardView cardUploadPPT, cardStartQuiz, cardViewProgress;

    private UserManager userManager;
    private StorageManager storageManager;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Uri selectedPPTUri;
    private String selectedPPTName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        userManager = new UserManager(requireContext());
        storageManager = new StorageManager(requireContext());

        initViews(view);
        setupFilePicker();
        loadStats();
        checkPermissions();

        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvStats = view.findViewById(R.id.tvStats);

        // Display user name
        User currentUser = userManager.getCurrentUser();
        if (currentUser != null) {
            tvWelcome.setText("Welcome back, " + currentUser.getFullName().split(" ")[0] + "! 👋");
        }

        cardUploadPPT = view.findViewById(R.id.cardUploadPPT);
        cardStartQuiz = view.findViewById(R.id.cardStartQuiz);
        cardViewProgress = view.findViewById(R.id.cardViewProgress);

        cardUploadPPT.setOnClickListener(v -> openFilePicker());
        cardStartQuiz.setOnClickListener(v -> startQuiz());
        cardViewProgress.setOnClickListener(v -> viewProgress());
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
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

        String[] mimeTypes = {
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-powerpoint"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        filePickerLauncher.launch(intent);
    }

    private void handlePPTSelection(Uri uri) {
        String fileName = getFileName(uri);

        if (fileName == null) {
            fileName = "presentation_" + System.currentTimeMillis() + ".pptx";
        }

        selectedPPTName = fileName;
        selectedPPTUri = uri;

        // IMPORTANT: Take persistent permission
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        PPTReader pptReader = new PPTReader(requireContext());

        if (pptReader.isValidPPT(uri)) {
            // IMPORTANT: Save PPT info with URI for later use
            PPTInfo pptInfo = new PPTInfo(fileName, uri.toString());
            storageManager.savePPTInfo(pptInfo);

            Toast.makeText(requireContext(), "✅ PPT uploaded: " + fileName, Toast.LENGTH_SHORT).show();

            // Auto navigate to quiz setup
            Intent intent = new Intent(requireActivity(), QuizSetupActivity.class);
            intent.putExtra("ppt_uri", uri.toString());
            intent.putExtra("ppt_name", selectedPPTName);
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "❌ Invalid PPT file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
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
        // Navigate to quiz tab
        ((MainActivity) requireActivity()).findViewById(R.id.bottom_navigation);
        Toast.makeText(requireContext(), "Upload a PPT first!", Toast.LENGTH_SHORT).show();
    }

    private void viewProgress() {
        // Navigate to analytics fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AnalyticsFragment())
                .commit();
        ((com.google.android.material.bottomnavigation.BottomNavigationView)
                requireActivity().findViewById(R.id.bottom_navigation))
                .setSelectedItemId(R.id.nav_analytics);
    }

    private void loadStats() {
        int totalQuizzes = storageManager.getTotalQuizzesTaken();
        float avgPercentage = storageManager.getOverallAveragePercentage();

        tvStats.setText(String.format("📊 Quizzes\n%d", totalQuizzes));
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }
}