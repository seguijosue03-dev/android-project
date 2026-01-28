package com.student.learncraft;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AllQuizzesActivity extends AppCompatActivity {

    private RecyclerView rvAllQuizzes;
    private TextView tvTitle;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_quizzes);

        // 🔥 MATCHING YOUR XML IDs NOW
        tvTitle = findViewById(R.id.tvTitle);
        rvAllQuizzes = findViewById(R.id.rvAllQuizzes);

        rvAllQuizzes.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Database
        databaseHelper = new DatabaseHelper(this);

        loadAllResults();
    }

    private void loadAllResults() {
        // Fetch from Database
        List<QuizResult> results = databaseHelper.getAllResults();

        if (results.isEmpty()) {
            tvTitle.setText("No Quiz Results Found");
        } else {
            tvTitle.setText("All Quiz Results (" + results.size() + ")");

            // Set the adapter
            AdminQuizAdapter adapter = new AdminQuizAdapter(results);
            rvAllQuizzes.setAdapter(adapter);
        }
    }

    // =========================================
    //           INTERNAL ADAPTER CLASS
    // =========================================
    private class AdminQuizAdapter extends RecyclerView.Adapter<AdminQuizAdapter.ViewHolder> {

        private List<QuizResult> quizList;

        public AdminQuizAdapter(List<QuizResult> quizList) {
            this.quizList = quizList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Uses the layout you created earlier: item_admin_quiz_result.xml
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_quiz_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuizResult result = quizList.get(position);

            // 1. Set Student Email
            String email = result.getStudentEmail();
            if (email == null || email.isEmpty()) email = "Unknown Student";
            holder.tvStudentEmail.setText(email);

            // 2. Set Topic
            holder.tvQuizTopic.setText(result.getPptName());

            // 3. Set Score
            holder.tvQuizScore.setText(result.getCorrectAnswers() + "/" + result.getTotalQuestions());

            // 4. Set Date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.tvQuizDate.setText(sdf.format(result.getDate()));
        }

        @Override
        public int getItemCount() {
            return quizList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            // These IDs come from 'item_admin_quiz_result.xml'
            TextView tvStudentEmail, tvQuizTopic, tvQuizScore, tvQuizDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
                tvQuizTopic = itemView.findViewById(R.id.tvQuizTopic);
                tvQuizScore = itemView.findViewById(R.id.tvQuizScore);
                tvQuizDate = itemView.findViewById(R.id.tvQuizDate);
            }
        }
    }
}