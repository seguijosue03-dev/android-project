package com.student.learncraft;

import android.os.CountDownTimer;

public class QuizTimer {

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private long totalTimeInMillis;
    private boolean isRunning;
    private TimerListener listener;

    public interface TimerListener {
        void onTick(long millisUntilFinished, String formattedTime);
        void onFinish();
    }

    public QuizTimer(int numQuestions, TimerListener listener) {
        // 1 minute per question
        this.totalTimeInMillis = numQuestions * 60 * 1000L;
        this.timeLeftInMillis = totalTimeInMillis;
        this.listener = listener;
        this.isRunning = false;
    }

    /**
     * Start the timer
     */
    public void start() {
        if (isRunning) return;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;

                // Format time as MM:SS
                String formattedTime = formatTime(millisUntilFinished);

                if (listener != null) {
                    listener.onTick(millisUntilFinished, formattedTime);
                }
            }

            @Override
            public void onFinish() {
                isRunning = false;
                timeLeftInMillis = 0;

                if (listener != null) {
                    listener.onFinish();
                }
            }
        }.start();

        isRunning = true;
    }

    /**
     * Pause the timer
     */
    public void pause() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isRunning = false;
        }
    }

    /**
     * Resume the timer
     */
    public void resume() {
        if (!isRunning && timeLeftInMillis > 0) {
            start();
        }
    }

    /**
     * Stop and reset the timer
     */
    public void stop() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isRunning = false;
        }
        timeLeftInMillis = totalTimeInMillis;
    }

    /**
     * Format milliseconds to MM:SS format
     */
    private String formatTime(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Get formatted time left
     */
    public String getFormattedTimeLeft() {
        return formatTime(timeLeftInMillis);
    }

    /**
     * Get time left in milliseconds
     */
    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }

    /**
     * Get total time in milliseconds
     */
    public long getTotalTimeInMillis() {
        return totalTimeInMillis;
    }

    /**
     * Check if timer is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get percentage of time remaining
     */
    public int getPercentageRemaining() {
        return (int) ((timeLeftInMillis * 100) / totalTimeInMillis);
    }

    /**
     * Check if time is running low (less than 5 minutes)
     */
    public boolean isTimeLow() {
        return timeLeftInMillis < 5 * 60 * 1000;
    }

    /**
     * Check if time is critical (less than 2 minutes)
     */
    public boolean isTimeCritical() {
        return timeLeftInMillis < 2 * 60 * 1000;
    }
}