package com.student.learncraft;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Added Toast
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvScore, tvPercentage, tvCorrect, tvWrong, tvMessage;
    private Button btnReviewAnswers, btnHome, btnRetake;

    private int correctAnswers;
    private int totalQuestions;
    private float percentage;
    private String pptName;
    private boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get data
        correctAnswers = getIntent().getIntExtra("correct_answers", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 0);
        percentage = getIntent().getFloatExtra("percentage", 0f);
        pptName = getIntent().getStringExtra("ppt_name");

        if (pptName == null) pptName = "Mixed Quiz";

        initViews();
        displayResults();

        // 🔥 Save and Notify User
        saveResultToDatabase();
    }

    private void saveResultToDatabase() {
        if (isSaved) return;

        try {
            QuizResult result = new QuizResult(pptName, totalQuestions, correctAnswers);
            result.setDifficulty("Normal");

            DatabaseHelper db = new DatabaseHelper(this);
            db.addResult(result);
            isSaved = true;

            // 🔥 SHOW SUCCESS MESSAGE
            // This lets you verify the save actually happened!
            Toast.makeText(this, "✅ Quiz Saved to History!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            // 🔥 SHOW ERROR MESSAGE
            Toast.makeText(this, "❌ Error: Could not save result", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvCorrect = findViewById(R.id.tvCorrect);
        tvWrong = findViewById(R.id.tvWrong);
        tvMessage = findViewById(R.id.tvMessage);

        btnReviewAnswers = findViewById(R.id.btnReviewAnswers);
        btnHome = findViewById(R.id.btnHome);
        btnRetake = findViewById(R.id.btnRetake);

        btnReviewAnswers.setOnClickListener(v -> reviewAnswers());
        btnHome.setOnClickListener(v -> goHome());
        btnRetake.setOnClickListener(v -> retakeQuiz());
    }

    private void displayResults() {
        tvScore.setText(correctAnswers + "/" + totalQuestions);
        tvPercentage.setText(String.format("%.1f%%", percentage));
        tvCorrect.setText("✅ Correct: " + correctAnswers);
        tvWrong.setText("❌ Wrong: " + (totalQuestions - correctAnswers));

        String message;
        if (percentage >= 90) {
            message = "🎉 Outstanding! You're a star!";
            tvMessage.setTextColor(getColor(R.color.success_green));
        } else if (percentage >= 75) {
            message = "🌟 Great Job! Keep it up!";
            tvMessage.setTextColor(getColor(R.color.success_green));
        } else if (percentage >= 60) {
            message = "👍 Good Work! Practice more!";
            tvMessage.setTextColor(getColor(R.color.accent_teal));
        } else if (percentage >= 40) {
            message = "📚 Keep Trying! You can do better!";
            tvMessage.setTextColor(getColor(R.color.warning_yellow));
        } else {
            message = "💪 Don't Give Up! Review and retry!";
            tvMessage.setTextColor(getColor(R.color.error_red));
        }

        tvMessage.setText(message);
    }

    private void reviewAnswers() {
        Intent intent = new Intent(this, ReviewAnswersActivity.class);
        intent.putExtra("ppt_name", pptName);
        startActivity(intent);
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("open_fragment", "home");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void retakeQuiz() {
        finish();
    }

    @Override
    public void onBackPressed() {
        goHome();
    }
}