package com.student.learncraft;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvScore, tvPercentage, tvCorrect, tvWrong, tvMessage;

    // ✅ FIX 1: 'btnHome' removed from here (it's not a Button)
    private Button btnReviewAnswers, btnRetake;

    // ✅ FIX 2: 'btnHome' added here (it matches your XML TextView)
    private TextView btnHome;

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
        totalQuestions = getIntent().getIntExtra("total_questions", 1);
        pptName = getIntent().getStringExtra("ppt_name");

        if (pptName == null) pptName = "Mixed Quiz";

        percentage = ((float) correctAnswers / totalQuestions) * 100;

        initViews();
        displayResults();

        // Save automatically
        saveResultToDatabase();
    }

    private void saveResultToDatabase() {
        if (isSaved) return;

        try {
            String userEmail = "guest@example.com";
            User currentUser = userManager.getCurrentUser();

            if (currentUser != null) {
                userEmail = currentUser.getEmail();
            }

            QuizResult result = new QuizResult();
            result.setPptName(pptName);
            result.setTotalQuestions(totalQuestions);
            result.setCorrectAnswers(correctAnswers);
            result.setWrongAnswers(totalQuestions - correctAnswers);
            result.setPercentage(percentage);
            result.setDifficulty("Normal");
            result.setTimestamp(System.currentTimeMillis());

            dbHelper.addResult(result, userEmail);
            isSaved = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvCorrect = findViewById(R.id.tvCorrect);
        tvWrong = findViewById(R.id.tvWrong);
        tvMessage = findViewById(R.id.tvMessage);

        btnReviewAnswers = findViewById(R.id.btnReviewAnswers);
        btnRetake = findViewById(R.id.btnRetake);

        // ✅ FIX 3: Cast correctly to TextView
        btnHome = findViewById(R.id.btnHome);

        btnReviewAnswers.setOnClickListener(v -> reviewAnswers());

        // This will now work perfectly!
        btnHome.setOnClickListener(v -> goHome());

        btnRetake.setOnClickListener(v -> retakeQuiz());
    }

    private void displayResults() {
        tvScore.setText(correctAnswers + "/" + totalQuestions);
        tvPercentage.setText(String.format("%.1f%%", percentage));
        tvCorrect.setText("✅ Correct: " + correctAnswers);
        tvWrong.setText("❌ Wrong: " + (totalQuestions - correctAnswers));

        String message;
        // Ensure these colors exist in colors.xml, otherwise use Color.GREEN / Color.RED
        if (percentage >= 60) {
            message = "🎉 Good Job! Passed!";
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            message = "💪 Keep Trying! You can do it!";
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
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