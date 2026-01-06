package com.student.learncraft;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity implements QuizTimer.TimerListener {

    private TextView tvQuestion, tvQuestionNumber, tvTimer;
    private Button btnOption1, btnOption2, btnOption3, btnOption4, btnNext;
    private ProgressBar progressBar;

    private List<MCQQuestion> questions;
    private List<Integer> userAnswers;
    private int currentQuestionIndex = 0;
    private int selectedOptionIndex = -1;

    private String pptName;
    private QuizTimer quizTimer;
    private boolean quizFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        try {
            // Get data from intent
            pptName = getIntent().getStringExtra("ppt_name");
            int questionCount = getIntent().getIntExtra("question_count", 20);
            int timeMinutes = getIntent().getIntExtra("time_minutes", 20);

            // Get questions from holder
            questions = QuizSetupActivity.QuizDataHolder.getQuestions();

            if (questions == null || questions.isEmpty()) {
                Toast.makeText(this, "❌ Error: No questions available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Initialize user answers list
            userAnswers = new ArrayList<>();
            for (int i = 0; i < questions.size(); i++) {
                userAnswers.add(-1); // -1 means not answered
            }

            // Initialize views
            initViews();

            // Initialize and start timer
            quizTimer = new QuizTimer(timeMinutes, this);
            quizTimer.start();

            // Load first question
            loadQuestion();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Error starting quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvTimer = findViewById(R.id.tvTimer);
        progressBar = findViewById(R.id.progressBar);

        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnNext = findViewById(R.id.btnNext);

        // Set option click listeners
        btnOption1.setOnClickListener(v -> selectOption(0, btnOption1));
        btnOption2.setOnClickListener(v -> selectOption(1, btnOption2));
        btnOption3.setOnClickListener(v -> selectOption(2, btnOption3));
        btnOption4.setOnClickListener(v -> selectOption(3, btnOption4));

        btnNext.setOnClickListener(v -> nextQuestion());

        // Update progress bar
        progressBar.setMax(questions.size());
        progressBar.setProgress(0);
    }

    private void loadQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        try {
            MCQQuestion question = questions.get(currentQuestionIndex);

            // Validate question
            if (question == null || question.getOptions() == null || question.getOptions().size() < 4) {
                Toast.makeText(this, "Error loading question. Skipping...", Toast.LENGTH_SHORT).show();
                currentQuestionIndex++;
                loadQuestion();
                return;
            }

            // Update question number
            tvQuestionNumber.setText(String.format("Question %d of %d",
                    currentQuestionIndex + 1, questions.size()));

            // Update progress
            progressBar.setProgress(currentQuestionIndex + 1);

            // Set question text
            tvQuestion.setText(question.getQuestion());

            // Set options
            List<String> options = question.getOptions();
            btnOption1.setText("A. " + options.get(0));
            btnOption2.setText("B. " + options.get(1));
            btnOption3.setText("C. " + options.get(2));
            btnOption4.setText("D. " + options.get(3));

            // Reset option styles
            resetOptionStyles();

            // If user already answered this question, show their selection
            int previousAnswer = userAnswers.get(currentQuestionIndex);
            if (previousAnswer != -1) {
                selectedOptionIndex = previousAnswer;
                highlightSelectedOption(previousAnswer);
            } else {
                selectedOptionIndex = -1;
            }

            // Update next button text
            if (currentQuestionIndex == questions.size() - 1) {
                btnNext.setText("Finish Quiz");
            } else {
                btnNext.setText("Next Question");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void selectOption(int optionIndex, Button selectedButton) {
        // Reset all options first
        resetOptionStyles();

        // Highlight selected option
        selectedOptionIndex = optionIndex;
        highlightSelectedOption(optionIndex);

        // Save user's answer
        userAnswers.set(currentQuestionIndex, optionIndex);
    }

    private void highlightSelectedOption(int index) {
        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};

        // Set selected state for the chosen button
        if (index >= 0 && index < buttons.length) {
            buttons[index].setSelected(true);
        }
    }

    private void resetOptionStyles() {
        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};

        // Reset selected state for all buttons
        for (Button btn : buttons) {
            btn.setSelected(false);
        }
    }

    private void nextQuestion() {
        try {
            // Check if user selected an option
            if (selectedOptionIndex == -1) {
                Toast.makeText(this, "⚠️ Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            // Move to next question
            currentQuestionIndex++;

            if (currentQuestionIndex < questions.size()) {
                loadQuestion();
            } else {
                finishQuiz();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finishQuiz();
        }
    }

    private void finishQuiz() {
        if (quizFinished) return;

        try {
            quizFinished = true;

            if (quizTimer != null) {
                quizTimer.stop();
            }

            // Calculate score
            int correctAnswers = 0;

            for (int i = 0; i < questions.size(); i++) {
                int userAnswer = userAnswers.get(i);
                int correctAnswer = questions.get(i).getCorrectAnswerIndex();

                if (userAnswer == correctAnswer) {
                    correctAnswers++;
                }
            }

            // Save result
            QuizResult result = new QuizResult(pptName, questions.size(), correctAnswers);
            StorageManager storageManager = new StorageManager(this);
            storageManager.saveQuizResult(result);

            // Go to result activity
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("correct_answers", correctAnswers);
            intent.putExtra("total_questions", questions.size());
            intent.putExtra("ppt_name", pptName);
            intent.putExtra("percentage", result.getPercentage());

            // Pass questions and answers for review
            QuizSetupActivity.QuizDataHolder.setQuestions(questions);
            ResultDataHolder.setUserAnswers(userAnswers);

            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error finishing quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onTick(long millisUntilFinished, String formattedTime) {
        tvTimer.setText("⏱️ " + formattedTime);

        // Change color if time is low
        if (quizTimer.isTimeCritical()) {
            tvTimer.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        } else if (quizTimer.isTimeLow()) {
            tvTimer.setTextColor(ContextCompat.getColor(this, R.color.warning_yellow));
        }
    }

    @Override
    public void onFinish() {
        // Time's up!
        new AlertDialog.Builder(this)
                .setTitle("⏰ Time's Up!")
                .setMessage("The quiz time has ended. Your answers will be submitted.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> finishQuiz())
                .show();
    }

    @Override
    public void onBackPressed() {
        // Confirm before exiting
        new AlertDialog.Builder(this)
                .setTitle("Exit Quiz?")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    quizTimer.stop();
                    super.onBackPressed();
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (quizTimer != null && !quizFinished) {
            quizTimer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (quizTimer != null && !quizFinished) {
            quizTimer.resume();
        }
    }

    // Static holder for passing user answers
    public static class ResultDataHolder {
        private static List<Integer> userAnswers;

        public static void setUserAnswers(List<Integer> answers) {
            userAnswers = answers;
        }

        public static List<Integer> getUserAnswers() {
            return userAnswers;
        }

        public static void clear() {
            userAnswers = null;
        }
    }
}