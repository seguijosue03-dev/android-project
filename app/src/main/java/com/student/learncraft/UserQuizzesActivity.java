package com.student.learncraft;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserQuizzesActivity extends AppCompatActivity {

    private TextView tvUserName, tvStats;
    private RecyclerView rvQuizzes;
    private StorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_quizzes);

        tvUserName = findViewById(R.id.tvUserName);
        tvStats = findViewById(R.id.tvUserStats);
        rvQuizzes = findViewById(R.id.rvUserQuizzes);

        storageManager = new StorageManager(this);

        String userName = getIntent().getStringExtra("user_name");
        if (userName == null) userName = "User";

        tvUserName.setText(userName + "'s Quizzes");

        List<QuizResult> quizResults = storageManager.getAllQuizResults();

        if (quizResults.isEmpty()) {
            tvStats.setText("No quiz results yet");
        } else {
            tvStats.setText("Total Quizzes: " + quizResults.size());

            rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
            rvQuizzes.setAdapter(new UserQuizAdapter(quizResults));
        }
    }
}
