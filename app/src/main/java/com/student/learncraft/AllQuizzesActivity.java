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
    private StorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_quizzes);

        storageManager = new StorageManager(this);

        tvTitle = findViewById(R.id.tvTitle);
        rvAllQuizzes = findViewById(R.id.rvAllQuizzes);

        rvAllQuizzes.setLayoutManager(new LinearLayoutManager(this));

        // Load all quiz results
        List<QuizResult> allResults = storageManager.getAllQuizResults();

        if (allResults.isEmpty()) {
            tvTitle.setText("No quiz results yet");
        } else {
            tvTitle.setText("All Quiz Results (" + allResults.size() + ")");
            QuizResultAdapter adapter = new QuizResultAdapter(allResults);
            rvAllQuizzes.setAdapter(adapter);
        }
    }

    // Adapter for quiz results
    private class QuizResultAdapter extends RecyclerView.Adapter<QuizResultAdapter.ViewHolder> {

        private List<QuizResult> results;

        public QuizResultAdapter(List<QuizResult> results) {
            this.results = results;
            // Sort by timestamp (newest first)
            this.results.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_quiz_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuizResult result = results.get(position);

            holder.tvPPTName.setText(result.getPptName());
            holder.tvScore.setText(String.format("%d/%d",
                    result.getCorrectAnswers(), result.getTotalQuestions()));
            holder.tvPercentage.setText(String.format("%.1f%%", result.getPercentage()));

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(result.getDate()));

            // Color code percentage
            if (result.getPercentage() >= 75) {
                holder.tvPercentage.setTextColor(getColor(R.color.success_green));
            } else if (result.getPercentage() >= 50) {
                holder.tvPercentage.setTextColor(getColor(R.color.accent_teal));
            } else {
                holder.tvPercentage.setTextColor(getColor(R.color.error_red));
            }
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPPTName, tvScore, tvPercentage, tvDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPPTName = itemView.findViewById(R.id.tvQuizPPTName);
                tvScore = itemView.findViewById(R.id.tvQuizScore);
                tvPercentage = itemView.findViewById(R.id.tvQuizPercentage);
                tvDate = itemView.findViewById(R.id.tvQuizDate);
            }
        }
    }
}