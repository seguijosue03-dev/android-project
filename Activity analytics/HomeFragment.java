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
    private DatabaseHelper databaseHelper;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Uri selectedPPTUri;
    private String selectedPPTName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Helpers
        userManager = new UserManager(requireContext());
        storageManager = new StorageManager(requireContext());
        databaseHelper = new DatabaseHelper(requireContext());

        initViews(view);
        setupFilePicker();

        // Load stats using the DB
        loadStats();

        checkPermissions();

        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvStats = view.findViewById(R.id.tvStats);

        cardUploadPPT = view.findViewById(R.id.cardUploadPPT);
        cardStartQuiz = view.findViewById(R.id.cardStartQuiz);
        cardViewProgress = view.findViewById(R.id.cardViewProgress);

        User currentUser = userManager.getCurrentUser();

        if (currentUser != null) {

            String nameToDisplay = currentUser.getFullName();

            // Fallback: If name is somehow empty, show "Student", but never the email.
            if (nameToDisplay == null || nameToDisplay.isEmpty()) {
                nameToDisplay = "Student";
            }

            tvWelcome.setText("Welcome back, " + nameToDisplay + "! 👋");
        }

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
        filePickerLauncher.launch(intent);
    }

    private void handlePPTSelection(Uri uri) {
        String fileName = getFileName(uri);
        if (fileName == null) fileName = "presentation_" + System.currentTimeMillis() + ".pptx";

        selectedPPTName = fileName;
        selectedPPTUri = uri;

        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) { e.printStackTrace(); }

        PPTReader pptReader = new PPTReader(requireContext());

        if (pptReader.isValidPPT(uri)) {
            PPTInfo pptInfo = new PPTInfo(fileName, uri.toString());
            storageManager.savePPTInfo(pptInfo);

            Toast.makeText(requireContext(), "✅ PPT uploaded: " + fileName, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireActivity(), QuizSetupActivity.class);
            intent.putExtra("ppt_uri", uri.toString());
            intent.putExtra("ppt_name", selectedPPTName);
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "❌ Invalid PPT file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String path = uri.getPath();
        return path == null ? "unknown.pptx" : path.substring(path.lastIndexOf('/') + 1);
    }

    private void startQuiz() {
        Toast.makeText(requireContext(), "Upload a PPT first!", Toast.LENGTH_SHORT).show();
    }

    private void viewProgress() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AnalyticsFragment())
                .commit();
        ((com.google.android.material.bottomnavigation.BottomNavigationView)
                requireActivity().findViewById(R.id.bottom_navigation))
                .setSelectedItemId(R.id.nav_analytics);
    }

    private void loadStats() {
        User currentUser = userManager.getCurrentUser();

        if (currentUser != null) {
            // Using getEmail() for stats lookup (which is correct for DB lookup)
            int totalQuizzes = databaseHelper.getQuizCountForUser(currentUser.getEmail());
            tvStats.setText(String.format("📊 Quizzes\n%d", totalQuizzes));
        } else {
            tvStats.setText("📊 Quizzes\n0");
        }
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
        // Force update name when returning to fragment
        initViews(getView());
    }
}