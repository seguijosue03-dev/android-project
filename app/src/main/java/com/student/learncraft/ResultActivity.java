package com.student.learncraft;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvScore, tvPercentage, tvCorrect, tvWrong, tvMessage;
    private Button btnReviewAnswers, btnHome, btnRetake;

    private int correctAnswers;
    private int totalQuestions;
    private float percentage;
    private String pptName;
    private boolean isSaved = false;

    // Managers
    private UserManager userManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        userManager = new UserManager(this);
        dbHelper = new DatabaseHelper(this);

        // Get data from intent
        correctAnswers = getIntent().getIntExtra("correct_answers", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 1); // Default to 1 to avoid divide by zero
        pptName = getIntent().getStringExtra("ppt_name");

        if (pptName == null) pptName = "Mixed Quiz";

        // 🔥 FIX: Recalculate Percentage Here (Forces Decimal Math)
        // casting to (float) ensures we don't get 0
        percentage = ((float) correctAnswers / totalQuestions) * 100;

        initViews();
        displayResults();

        // Save automatically
        saveResultToDatabase();
    }

    private void saveResultToDatabase() {
        if (isSaved) return;

        try {
            // 1. Get Current User Email
            String userEmail = "guest@example.com";
            User currentUser = userManager.getCurrentUser();

            if (currentUser != null) {
                userEmail = currentUser.getEmail();
            } else {
                // Debugging: Warn if no user is found
                Toast.makeText(this, "⚠️ Warning: Saving as Guest (Not Logged In)", Toast.LENGTH_SHORT).show();
            }

            // 2. Create Result Object
            QuizResult result = new QuizResult();
            result.setPptName(pptName);
            result.setTotalQuestions(totalQuestions);
            result.setCorrectAnswers(correctAnswers);
            result.setWrongAnswers(totalQuestions - correctAnswers);
            result.setPercentage(percentage); // Uses the fixed math
            result.setDifficulty("Normal");
            result.setTimestamp(System.currentTimeMillis());

            // 3. Save to Database
            dbHelper.addResult(result, userEmail);

            isSaved = true;

            // 🔥 DEBUG TOAST: This tells you EXACTLY who it saved for
            Toast.makeText(this, "✅ Saved for: " + userEmail, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
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
        if (percentage >= 60) {
            message = "🎉 Good Job! Passed!";
            tvMessage.setTextColor(getColor(R.color.success_green));
        } else {
            message = "💪 Keep Trying! You can do it!";
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