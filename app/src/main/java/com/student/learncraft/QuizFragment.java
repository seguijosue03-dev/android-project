package com.student.learncraft;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuizFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private RecyclerView rvPPTList;
    private Button btnUploadPPT;
    private LinearLayout tvEmptyState;
    private CardView cardUploadSection;

    private StorageManager storageManager;
    private PPTAdapter pptAdapter;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Uri selectedPPTUri;
    private String selectedPPTName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        try {
            storageManager = new StorageManager(requireContext());

            initViews(view);
            setupFilePicker();
            loadPPTList();
            checkPermissions();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error loading quiz screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void initViews(View view) {
        try {
            rvPPTList = view.findViewById(R.id.rvPPTList);
            btnUploadPPT = view.findViewById(R.id.btnUploadPPT);
            tvEmptyState = view.findViewById(R.id.tvEmptyState);
            cardUploadSection = view.findViewById(R.id.cardUploadSection);

            if (rvPPTList != null) {
                rvPPTList.setLayoutManager(new LinearLayoutManager(requireContext()));
            }

            if (btnUploadPPT != null) {
                btnUploadPPT.setOnClickListener(v -> openFilePicker());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupFilePicker() {
        try {
            filePickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        try {
                            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                                selectedPPTUri = result.getData().getData();

                                if (selectedPPTUri != null) {
                                    handlePPTSelection(selectedPPTUri);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Error handling file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/vnd.openxmlformats-officedocument.presentationml.presentation");

            String[] mimeTypes = {
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-powerpoint"
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            filePickerLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error opening file picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePPTSelection(Uri uri) {
        try {
            String fileName = getFileName(uri);

            if (fileName == null) {
                fileName = "presentation_" + System.currentTimeMillis() + ".pptx";
            }

            selectedPPTName = fileName;

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
                // Save PPT info with URI
                PPTInfo pptInfo = new PPTInfo(fileName, uri.toString());
                storageManager.savePPTInfo(pptInfo);

                Toast.makeText(requireContext(), "✅ PPT uploaded: " + fileName, Toast.LENGTH_SHORT).show();

                // Reload list
                loadPPTList();

                // Auto navigate to quiz setup
                Intent intent = new Intent(requireActivity(), QuizSetupActivity.class);
                intent.putExtra("ppt_uri", uri.toString());
                intent.putExtra("ppt_name", selectedPPTName);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "❌ Invalid PPT file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error processing PPT: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown_file.pptx";
        }
    }

    private void loadPPTList() {
        try {
            List<PPTInfo> pptInfoList = storageManager.getAllPPTInfo();

            if (pptInfoList == null || pptInfoList.isEmpty()) {
                if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                if (rvPPTList != null) rvPPTList.setVisibility(View.GONE);
            } else {
                if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                if (rvPPTList != null) rvPPTList.setVisibility(View.VISIBLE);

                pptAdapter = new PPTAdapter(pptInfoList);
                rvPPTList.setAdapter(pptAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error loading PPT list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            loadPPTList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Adapter for PPT List
    private class PPTAdapter extends RecyclerView.Adapter<PPTAdapter.ViewHolder> {

        private List<PPTInfo> pptInfoList;

        public PPTAdapter(List<PPTInfo> pptInfoList) {
            this.pptInfoList = pptInfoList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ppt, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                PPTInfo pptInfo = pptInfoList.get(position);
                String pptName = pptInfo.getFileName();

                holder.tvPPTName.setText(pptName);

                // Get quiz count for this PPT
                List<QuizResult> results = storageManager.getResultsForPPT(pptName);
                holder.tvQuizCount.setText(results.size() + " quizzes taken");

                // Calculate average score
                if (!results.isEmpty()) {
                    float avgScore = storageManager.getAveragePercentage(pptName);
                    holder.tvAvgScore.setText(String.format("Avg: %.1f%%", avgScore));
                    holder.tvAvgScore.setVisibility(View.VISIBLE);
                } else {
                    holder.tvAvgScore.setVisibility(View.GONE);
                }

                // Start quiz button
                holder.btnStartQuiz.setOnClickListener(v -> {
                    try {
                        String uriString = pptInfo.getUriString();

                        if (uriString == null || uriString.isEmpty()) {
                            Toast.makeText(requireContext(),
                                    "⚠️ Please re-upload this PPT",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Intent intent = new Intent(requireActivity(), QuizSetupActivity.class);
                        intent.putExtra("ppt_uri", uriString);
                        intent.putExtra("ppt_name", pptInfo.getFileName());
                        startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(),
                                "⚠️ Error. Please re-upload this PPT.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                // Delete button
                holder.btnDelete.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Delete PPT")
                            .setMessage("Delete " + pptName + "?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                pptInfoList.remove(position);
                                storageManager.savePPTInfo(pptInfo); // This will remove it

                                // Actually delete from storage
                                List<PPTInfo> allPPTs = storageManager.getAllPPTInfo();
                                allPPTs.removeIf(p -> p.getFileName().equals(pptName));
                                // Save updated list
                                android.content.SharedPreferences prefs = requireContext().getSharedPreferences("LearnCraftPrefs", android.content.Context.MODE_PRIVATE);
                                prefs.edit().putString("ppt_info_list", new com.google.gson.Gson().toJson(allPPTs)).apply();

                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, pptInfoList.size());

                                Toast.makeText(requireContext(), "✅ PPT deleted", Toast.LENGTH_SHORT).show();

                                if (pptInfoList.isEmpty()) {
                                    loadPPTList();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return pptInfoList != null ? pptInfoList.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPPTName, tvQuizCount, tvAvgScore;
            Button btnStartQuiz, btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPPTName = itemView.findViewById(R.id.tvPPTName);
                tvQuizCount = itemView.findViewById(R.id.tvPPTQuizCount);
                tvAvgScore = itemView.findViewById(R.id.tvPPTAvgScore);
                btnStartQuiz = itemView.findViewById(R.id.btnStartPPTQuiz);
                btnDelete = itemView.findViewById(R.id.btnDeletePPT);
            }
        }
    }
}