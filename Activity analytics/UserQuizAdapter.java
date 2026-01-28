package com.student.learncraft;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UserQuizAdapter extends RecyclerView.Adapter<UserQuizAdapter.QuizViewHolder> {

    private final List<QuizResult> quizResults;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public UserQuizAdapter(List<QuizResult> quizResults) {
        this.quizResults = quizResults;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_result, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizResult result = quizResults.get(position);

        // PPT name
        holder.tvQuizPPTName.setText(result.getPptName());

        // Score (e.g. 18/20)
        String scoreText = result.getCorrectAnswers() + "/" + result.getTotalQuestions();
        holder.tvQuizScore.setText(scoreText);

        // Date
        holder.tvQuizDate.setText(dateFormat.format(result.getDate()));

        // Percentage
        holder.tvQuizPercentage.setText(
                String.format(Locale.getDefault(), "%.0f%%", result.getPercentage())
        );
    }

    @Override
    public int getItemCount() {
        return quizResults.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {

        TextView tvQuizPPTName, tvQuizScore, tvQuizDate, tvQuizPercentage;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);

            tvQuizPPTName = itemView.findViewById(R.id.tvQuizPPTName);
            tvQuizScore = itemView.findViewById(R.id.tvQuizScore);
            tvQuizDate = itemView.findViewById(R.id.tvQuizDate);
            tvQuizPercentage = itemView.findViewById(R.id.tvQuizPercentage);
        }
    }
}
