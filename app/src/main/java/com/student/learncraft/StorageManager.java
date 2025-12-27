package com.student.learncraft;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {

    private static final String PREF_NAME = "LearnCraftPrefs";
    private static final String KEY_QUIZ_RESULTS = "quiz_results";
    private static final String KEY_PPT_LIST = "ppt_list";

    private SharedPreferences preferences;
    private Gson gson;

    public StorageManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Save a quiz result
     */
    public void saveQuizResult(QuizResult result) {
        List<QuizResult> results = getAllQuizResults();
        results.add(result);

        String json = gson.toJson(results);
        preferences.edit().putString(KEY_QUIZ_RESULTS, json).apply();
    }

    /**
     * Get all quiz results
     */
    public List<QuizResult> getAllQuizResults() {
        String json = preferences.getString(KEY_QUIZ_RESULTS, null);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<QuizResult>>(){}.getType();
        List<QuizResult> results = gson.fromJson(json, type);

        return results != null ? results : new ArrayList<>();
    }

    /**
     * Get quiz results for a specific PPT
     */
    public List<QuizResult> getResultsForPPT(String pptName) {
        List<QuizResult> allResults = getAllQuizResults();
        List<QuizResult> filtered = new ArrayList<>();

        for (QuizResult result : allResults) {
            if (result.getPptName().equals(pptName)) {
                filtered.add(result);
            }
        }

        return filtered;
    }

    /**
     * Get latest N results
     */
    public List<QuizResult> getLatestResults(int count) {
        List<QuizResult> allResults = getAllQuizResults();

        // Sort by timestamp (newest first)
        allResults.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));

        int endIndex = Math.min(count, allResults.size());
        return allResults.subList(0, endIndex);
    }

    /**
     * Calculate average percentage for a PPT
     */
    public float getAveragePercentage(String pptName) {
        List<QuizResult> results = getResultsForPPT(pptName);

        if (results.isEmpty()) {
            return 0f;
        }

        float total = 0f;
        for (QuizResult result : results) {
            total += result.getPercentage();
        }

        return total / results.size();
    }

    /**
     * Get improvement trend (last 5 results)
     */
    public List<Float> getImprovementTrend(String pptName) {
        List<QuizResult> results = getResultsForPPT(pptName);
        List<Float> percentages = new ArrayList<>();

        // Sort by timestamp
        results.sort((r1, r2) -> Long.compare(r1.getTimestamp(), r2.getTimestamp()));

        for (QuizResult result : results) {
            percentages.add(result.getPercentage());
        }

        return percentages;
    }

    /**
     * Clear all quiz results
     */
    public void clearAllResults() {
        preferences.edit().remove(KEY_QUIZ_RESULTS).apply();
    }

    /**
     * Save PPT name to list of uploaded PPTs
     */
    public void savePPTName(String pptName) {
        List<String> pptList = getAllPPTNames();

        if (!pptList.contains(pptName)) {
            pptList.add(pptName);
            String json = gson.toJson(pptList);
            preferences.edit().putString(KEY_PPT_LIST, json).apply();
        }
    }

    /**
     * Get all uploaded PPT names
     */
    public List<String> getAllPPTNames() {
        String json = preferences.getString(KEY_PPT_LIST, null);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> pptList = gson.fromJson(json, type);

        return pptList != null ? pptList : new ArrayList<>();
    }

    /**
     * Get total number of quizzes taken
     */
    public int getTotalQuizzesTaken() {
        return getAllQuizResults().size();
    }

    /**
     * Get overall average percentage
     */
    public float getOverallAveragePercentage() {
        List<QuizResult> results = getAllQuizResults();

        if (results.isEmpty()) {
            return 0f;
        }

        float total = 0f;
        for (QuizResult result : results) {
            total += result.getPercentage();
        }

        return total / results.size();
    }
}