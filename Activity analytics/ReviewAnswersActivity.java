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
import java.util.List;

public class ReviewAnswersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTitle;

    private List<MCQQuestion> questions;
    private List<Integer> userAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_answers);

        String pptName = getIntent().getStringExtra("ppt_name");

        // Get data
        questions = QuizSetupActivity.QuizDataHolder.getQuestions();
        userAnswers = ResultDataHolder.getUserAnswers();


        // Initialize views
        tvTitle = findViewById(R.id.tvTitle);
        recyclerView = findViewById(R.id.recyclerView);

        tvTitle.setText("Answer Review - " + pptName);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ReviewAdapter adapter = new ReviewAdapter(questions, userAnswers);
        recyclerView.setAdapter(adapter);
    }

    // Adapter for Review
    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

        private List<MCQQuestion> questions;
        private List<Integer> userAnswers;

        public ReviewAdapter(List<MCQQuestion> questions, List<Integer> userAnswers) {
            this.questions = questions;
            this.userAnswers = userAnswers;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review_answer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MCQQuestion question = questions.get(position);
            int userAnswer = userAnswers.get(position);
            int correctAnswer = question.getCorrectAnswerIndex();

            boolean isCorrect = userAnswer == correctAnswer;

            // Question number
            holder.tvQuestionNumber.setText("Question " + (position + 1));

            // Question text
            holder.tvQuestion.setText(question.getQuestion());

            // User's answer
            if (userAnswer == -1) {
                holder.tvUserAnswer.setText("Your Answer: Not answered");
                holder.tvUserAnswer.setTextColor(getColor(R.color.text_secondary));
            } else {
                holder.tvUserAnswer.setText("Your Answer: " + question.getOptions().get(userAnswer));
                holder.tvUserAnswer.setTextColor(isCorrect ?
                        getColor(R.color.success_green) : getColor(R.color.error_red));
            }

            // Correct answer
            holder.tvCorrectAnswer.setText("Correct Answer: " +
                    question.getOptions().get(correctAnswer));
            holder.tvCorrectAnswer.setTextColor(getColor(R.color.success_green));

            // Explanation
            if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
                holder.tvExplanation.setVisibility(View.VISIBLE);
                holder.tvExplanation.setText("💡 " + question.getExplanation());
            } else {
                holder.tvExplanation.setVisibility(View.GONE);
            }

            // Status icon
            holder.tvStatus.setText(isCorrect ? "✅" : "❌");
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvQuestionNumber, tvQuestion, tvUserAnswer, tvCorrectAnswer,
                    tvExplanation, tvStatus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
                tvQuestion = itemView.findViewById(R.id.tvQuestion);
                tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
                tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
                tvExplanation = itemView.findViewById(R.id.tvExplanation);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}