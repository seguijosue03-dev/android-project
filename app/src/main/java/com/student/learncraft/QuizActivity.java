package com.student.learncraft;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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
    private TextView tvOption1, tvOption2, tvOption3, tvOption4;
    private CheckBox checkOption1, checkOption2, checkOption3, checkOption4;
    private LinearLayout layoutOption1, layoutOption2, layoutOption3, layoutOption4;
    private Button btnNext, btnPrevious;
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

        pptName = getIntent().getStringExtra("ppt_name");
        int timeMinutes = getIntent().getIntExtra("time_minutes", 20);

        questions = QuizSetupActivity.QuizDataHolder.getQuestions();

        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "❌ No questions available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userAnswers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            userAnswers.add(-1);
        }

        initViews();

        quizTimer = new QuizTimer(timeMinutes, this);
        quizTimer.start();

        loadQuestion();
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvTimer = findViewById(R.id.tvTimer);
        progressBar = findViewById(R.id.progressBar);

        layoutOption1 = findViewById(R.id.layoutOption1);
        layoutOption2 = findViewById(R.id.layoutOption2);
        layoutOption3 = findViewById(R.id.layoutOption3);
        layoutOption4 = findViewById(R.id.layoutOption4);

        checkOption1 = findViewById(R.id.checkOption1);
        checkOption2 = findViewById(R.id.checkOption2);
        checkOption3 = findViewById(R.id.checkOption3);
        checkOption4 = findViewById(R.id.checkOption4);

        tvOption1 = findViewById(R.id.tvOption1);
        tvOption2 = findViewById(R.id.tvOption2);
        tvOption3 = findViewById(R.id.tvOption3);
        tvOption4 = findViewById(R.id.tvOption4);

        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);

        layoutOption1.setOnClickListener(v -> selectOption(0));
        layoutOption2.setOnClickListener(v -> selectOption(1));
        layoutOption3.setOnClickListener(v -> selectOption(2));
        layoutOption4.setOnClickListener(v -> selectOption(3));

        btnNext.setOnClickListener(v -> nextQuestion());
        btnPrevious.setOnClickListener(v -> previousQuestion());

        progressBar.setMax(questions.size());
    }

    private void loadQuestion() {
        MCQQuestion q = questions.get(currentQuestionIndex);

        tvQuestionNumber.setText(
                "Question " + (currentQuestionIndex + 1) + " of " + questions.size()
        );

        progressBar.setProgress(currentQuestionIndex + 1);
        tvQuestion.setText(q.getQuestion());

        List<String> opts = q.getOptions();
        tvOption1.setText("A. " + opts.get(0));
        tvOption2.setText("B. " + opts.get(1));
        tvOption3.setText("C. " + opts.get(2));
        tvOption4.setText("D. " + opts.get(3));

        resetOptionStyles();
        selectedOptionIndex = userAnswers.get(currentQuestionIndex);

        if (selectedOptionIndex != -1) {
            highlightSelectedOption(selectedOptionIndex);
        }

        btnPrevious.setEnabled(currentQuestionIndex > 0);

        btnNext.setText(
                currentQuestionIndex == questions.size() - 1
                        ? "Finish Quiz"
                        : "Next Question"
        );
    }

    private void selectOption(int index) {
        resetOptionStyles();
        selectedOptionIndex = index;
        userAnswers.set(currentQuestionIndex, index);
        highlightSelectedOption(index);
    }

    private void highlightSelectedOption(int index) {
        LinearLayout[] layouts = {layoutOption1, layoutOption2, layoutOption3, layoutOption4};
        CheckBox[] checks = {checkOption1, checkOption2, checkOption3, checkOption4};

        for (int i = 0; i < checks.length; i++) {
            checks[i].setChecked(i == index);
            layouts[i].setSelected(i == index);
        }
    }

    private void resetOptionStyles() {
        CheckBox[] checks = {checkOption1, checkOption2, checkOption3, checkOption4};
        LinearLayout[] layouts = {layoutOption1, layoutOption2, layoutOption3, layoutOption4};

        for (int i = 0; i < checks.length; i++) {
            checks[i].setChecked(false);
            layouts[i].setSelected(false);
        }
    }

    private void nextQuestion() {
        if (selectedOptionIndex == -1) {
            Toast.makeText(this, "Select an option", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentQuestionIndex == questions.size() - 1) {
            finishQuiz();
        } else {
            currentQuestionIndex++;
            loadQuestion();
        }
    }

    private void previousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            loadQuestion();
        }
    }

    private void finishQuiz() {
        if (quizFinished) return;
        quizFinished = true;

        if (quizTimer != null) quizTimer.stop();

        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (userAnswers.get(i) == questions.get(i).getCorrectAnswerIndex()) {
                correct++;
            }
        }

        ResultDataHolder.setUserAnswers(userAnswers);

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("correct_answers", correct);
        intent.putExtra("total_questions", questions.size());
        intent.putExtra("ppt_name", pptName);
        startActivity(intent);
        finish();
    }

    @Override
    public void onTick(long ms, String time) {
        tvTimer.setText("⏱️ " + time);

        if (quizTimer.isTimeLow()) {
            tvTimer.setTextColor(ContextCompat.getColor(this, R.color.warning_yellow));
        }
        if (quizTimer.isTimeCritical()) {
            tvTimer.setTextColor(ContextCompat.getColor(this, R.color.error_red));
        }
    }

    @Override
    public void onFinish() {
        finishQuiz();
    }

    public static class ResultDataHolder {
        private static List<Integer> userAnswers;

        public static void setUserAnswers(List<Integer> answers) {
            userAnswers = answers;
        }

        public static List<Integer> getUserAnswers() {
            return userAnswers;
        }
    }
}
