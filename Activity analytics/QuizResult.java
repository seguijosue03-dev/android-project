package com.student.learncraft;

import java.util.Date;

public class QuizResult {
    private int id;
    // 🔥 NEW: Added to track who took the quiz
    private String studentEmail;

    private String pptName;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private float percentage;
    private long timestamp;
    private String difficulty;

    public QuizResult() {
        this.timestamp = System.currentTimeMillis();
    }

    public QuizResult(String pptName, int totalQuestions, int correctAnswers) {
        this.pptName = pptName;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = totalQuestions - correctAnswers;
        this.percentage = (float) (correctAnswers * 100.0 / totalQuestions);
        this.timestamp = System.currentTimeMillis();
    }

    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // 🔥 NEW: Getter and Setter for Student Email
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    // ------------------------------------------------

    public String getPptName() { return pptName; }
    public void setPptName(String pptName) { this.pptName = pptName; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = totalQuestions - correctAnswers;
        if (totalQuestions > 0) {
            this.percentage = (float) (correctAnswers * 100.0 / totalQuestions);
        }
    }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public float getPercentage() { return percentage; }
    public void setPercentage(float percentage) { this.percentage = percentage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Date getDate() { return new Date(timestamp); }
}