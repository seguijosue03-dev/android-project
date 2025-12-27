package com.student.learncraft;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class QuizSetupActivity extends AppCompatActivity {

    private TextView tvPPTName, tvInfo;
    private RadioGroup rgQuestionCount;
    private Button btnStartQuiz;

    private String pptName;
    private Uri pptUri;
    private PPTContent pptContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_setup);

        // Get intent data
        String uriString = getIntent().getStringExtra("ppt_uri");
        pptUri = Uri.parse(uriString);
        pptName = getIntent().getStringExtra("ppt_name");

        // Initialize views
        initViews();

        // Load PPT content
        loadPPTContent();
    }

    private void initViews() {
        tvPPTName = findViewById(R.id.tvPPTName);
        tvInfo = findViewById(R.id.tvInfo);
        rgQuestionCount = findViewById(R.id.rgQuestionCount);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);

        tvPPTName.setText(pptName);

        btnStartQuiz.setOnClickListener(v -> startQuiz());
    }

    private void loadPPTContent() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Reading PPT...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Load PPT in background
        new Thread(() -> {
            try {
                PPTReader pptReader = new PPTReader(this);
                pptContent = pptReader.readPPT(pptUri, pptName);

                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    int slideCount = pptContent.getSlides().size();
                    tvInfo.setText(String.format("📊 %d slides loaded\n⏱️ Timer: 1 minute per question", slideCount));

                    Toast.makeText(this, "✅ PPT loaded successfully!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void startQuiz() {
        if (pptContent == null) {
            Toast.makeText(this, "⚠️ Please wait for PPT to load", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected question count
        int selectedId = rgQuestionCount.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "⚠️ Please select number of questions", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedId);
        String text = selectedRadio.getText().toString();
        int questionCount = Integer.parseInt(text.split(" ")[0]);

        // Generate questions
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating questions...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                MCQGenerator generator = new MCQGenerator();
                List<MCQQuestion> questions = generator.generateQuestions(pptContent, questionCount);

                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    if (questions.isEmpty()) {
                        Toast.makeText(this, "❌ Not enough content to generate questions", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Start quiz activity
                    Intent intent = new Intent(this, QuizActivity.class);
                    intent.putExtra("ppt_name", pptName);
                    intent.putExtra("question_count", questions.size());
                    intent.putExtra("time_minutes", questionCount); // 1 minute per question

                    // Pass questions (we'll use a static holder for simplicity)
                    QuizDataHolder.setQuestions(questions);

                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "❌ Error generating questions", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // Static holder for passing data between activities
    public static class QuizDataHolder {
        private static List<MCQQuestion> questions;

        public static void setQuestions(List<MCQQuestion> q) {
            questions = q;
        }

        public static List<MCQQuestion> getQuestions() {
            return questions;
        }

        public static void clear() {
            questions = null;
        }
    }
}