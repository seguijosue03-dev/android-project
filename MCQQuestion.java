package com.student.learncraft;

import java.util.List;

public class MCQQuestion {
    private String question;
    private List<String> options;
    private int correctAnswerIndex;
    private String explanation;
    private QuestionType type;
    private String difficulty;

    public enum QuestionType {
        DEFINITION,
        FACT,
        FILL_IN_BLANK,
        NOT_QUESTION,
        COMPARISON
    }

    public MCQQuestion() {
    }

    public MCQQuestion(String question, List<String> options, int correctAnswerIndex,
                       String explanation, QuestionType type) {
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctAnswerIndex;
    }
}