package com.student.learncraft;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private List<QuizResult> resultList;

    public ResultAdapter(List<QuizResult> resultList) {
        this.resultList = resultList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizResult result = resultList.get(position);

        holder.tvTopic.setText(result.getPptName());
        holder.tvScore.setText(result.getCorrectAnswers() + "/" + result.getTotalQuestions());
        holder.tvPercentage.setText(String.format("%.0f%%", result.getPercentage()));

        // Format Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(result.getTimestamp())));

        // 🔥 LOGIC: PASSING MARK 5/10 (50%)
        if (result.getCorrectAnswers() >= 5) {
            holder.tvStatus.setText("Passed");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvStatus.setText("Failed");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopic, tvScore, tvPercentage, tvDate, tvStatus; // Added tvStatus

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            tvDate = itemView.findViewById(R.id.tvDate);
            // 🔥 Make sure your XML has this ID!
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}