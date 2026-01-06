package com.student.learncraft;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QuizResultViewHolder extends RecyclerView.ViewHolder {

    TextView tvQuizPPTName, tvQuizScore, tvQuizDate, tvQuizPercentage;

    public QuizResultViewHolder(@NonNull View itemView) {
        super(itemView);

        tvQuizPPTName = itemView.findViewById(R.id.tvQuizPPTName);
        tvQuizScore = itemView.findViewById(R.id.tvQuizScore);
        tvQuizDate = itemView.findViewById(R.id.tvQuizDate);
        tvQuizPercentage = itemView.findViewById(R.id.tvQuizPercentage);
    }

    // 🔥 THIS IS WHAT YOU WERE MISSING
    public void bind(QuizResult result) {

        tvQuizPPTName.setText(result.getPptName());

        tvQuizScore.setText(
                result.getCorrectAnswers() + "/" + result.getTotalQuestions()
        );

        tvQuizPercentage.setText(
                String.format(Locale.getDefault(), "%.0f%%", result.getPercentage())
        );

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy, HH:mm", Locale.getDefault()
        );
        tvQuizDate.setText(sdf.format(new Date(result.getTimestamp())));

        // Color percentage dynamically
        if (result.getPercentage() >= 75) {
            tvQuizPercentage.setTextColor(itemView.getContext()
                    .getColor(R.color.success_green));
        } else if (result.getPercentage() >= 50) {
            tvQuizPercentage.setTextColor(itemView.getContext()
                    .getColor(R.color.warning_yellow));
        } else {
            tvQuizPercentage.setTextColor(itemView.getContext()
                    .getColor(R.color.error_red));
        }
    }
}
