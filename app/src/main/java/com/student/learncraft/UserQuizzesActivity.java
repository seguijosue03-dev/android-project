package com.student.learncraft;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserQuizzesActivity extends AppCompatActivity {

    private TextView tvUserName, tvStats;
    private RecyclerView rvQuizzes;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_quizzes);

        // 1. Initialize Views
        tvUserName = findViewById(R.id.tvUserName);
        tvStats = findViewById(R.id.tvUserStats);
        rvQuizzes = findViewById(R.id.rvUserQuizzes);

        // 2. Initialize DB
        dbHelper = new DatabaseHelper(this);

        // 3. Check Intent Data (Flags)
        boolean showAll = getIntent().getBooleanExtra("show_all", false);
        String userEmail = getIntent().getStringExtra("user_email");
        String userName = getIntent().getStringExtra("user_name");

        List<QuizResult> results;

        // 4. Decide what to load
        if (showAll) {
            // Case A: Admin clicked "View All Quiz Results"
            tvUserName.setText("All Global Results");
            results = dbHelper.getAllResults(); // Uses the new method
        } else {
            // Case B: Admin clicked a specific Student
            if (userName == null) userName = "User";
            tvUserName.setText(userName + "'s History");

            if (userEmail != null) {
                results = dbHelper.getResultsByUser(userEmail);
            } else {
                results = null;
            }
        }

        // 5. Update UI
        if (results == null || results.isEmpty()) {
            tvStats.setText("No quizzes found.");
            rvQuizzes.setVisibility(View.GONE);
        } else {
            tvStats.setText("Total Records Found: " + results.size());
            rvQuizzes.setVisibility(View.VISIBLE);

            // Set up the Recycler View
            rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
            rvQuizzes.setAdapter(new ResultAdapter(results));
        }
    }
}