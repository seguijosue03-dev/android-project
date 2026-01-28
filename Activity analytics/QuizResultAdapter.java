package com.student.learncraft;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuizResultAdapter
        extends RecyclerView.Adapter<QuizResultViewHolder> {

    private final List<QuizResult> quizResults;

    public QuizResultAdapter(List<QuizResult> quizResults) {
        this.quizResults = quizResults;
    }

    @NonNull
    @Override
    public QuizResultViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_result, parent, false);

        return new QuizResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull QuizResultViewHolder holder, int position) {

        holder.bind(quizResults.get(position)); // ✅ NOW EXISTS
    }

    @Override
    public int getItemCount() {
        return quizResults.size();
    }
}
