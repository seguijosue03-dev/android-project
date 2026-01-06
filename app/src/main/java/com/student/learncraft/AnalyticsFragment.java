package com.student.learncraft;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private TextView tvTotalQuizzes, tvAverageScore, tvBestScore, tvRecentPerformance;
    private LineChart lineChart;
    private StorageManager storageManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        storageManager = new StorageManager(requireContext());

        initViews(view);
        loadAnalytics();

        return view;
    }

    private void initViews(View view) {
        tvTotalQuizzes = view.findViewById(R.id.tvTotalQuizzes);
        tvAverageScore = view.findViewById(R.id.tvAverageScore);
        tvBestScore = view.findViewById(R.id.tvBestScore);
        tvRecentPerformance = view.findViewById(R.id.tvRecentPerformance);
        lineChart = view.findViewById(R.id.lineChart);
    }

    private void loadAnalytics() {
        List<QuizResult> allResults = storageManager.getAllQuizResults();

        if (allResults.isEmpty()) {
            showEmptyState();
            return;
        }

        // Calculate statistics
        int totalQuizzes = allResults.size();
        float averageScore = storageManager.getOverallAveragePercentage();

        // Find best score
        float bestScore = 0f;
        for (QuizResult result : allResults) {
            if (result.getPercentage() > bestScore) {
                bestScore = result.getPercentage();
            }
        }

        // Get recent performance (last 5 quizzes)
        List<QuizResult> recentResults = storageManager.getLatestResults(5);
        float recentAverage = 0f;
        for (QuizResult result : recentResults) {
            recentAverage += result.getPercentage();
        }
        if (!recentResults.isEmpty()) {
            recentAverage /= recentResults.size();
        }

        // Display statistics
        tvTotalQuizzes.setText(String.valueOf(totalQuizzes));
        tvAverageScore.setText(String.format("%.1f%%", averageScore));
        tvBestScore.setText(String.format("%.1f%%", bestScore));
        tvRecentPerformance.setText(String.format("%.1f%%", recentAverage));

        // Setup chart
        setupChart(allResults);
    }

    private void setupChart(List<QuizResult> results) {
        // Sort results by timestamp
        results.sort((r1, r2) -> Long.compare(r1.getTimestamp(), r2.getTimestamp()));

        // Prepare data entries
        List<Entry> entries = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        final List<String> dateLabels = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            QuizResult result = results.get(i);
            entries.add(new Entry(i, result.getPercentage()));
            dateLabels.add(dateFormat.format(result.getDate()));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Performance Over Time");

        // Style the line
        dataSet.setColor(Color.parseColor("#1A237E")); // Primary blue
        dataSet.setCircleColor(Color.parseColor("#00BCD4")); // Accent teal
        dataSet.setCircleRadius(5f);
        dataSet.setLineWidth(3f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#212121"));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E3F2FD"));
        dataSet.setFillAlpha(100);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Create LineData
        LineData lineData = new LineData(dataSet);

        // Configure chart
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.animateX(1000);

        // Configure X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dateLabels.size()) {
                    return dateLabels.get(index);
                }
                return "";
            }
        });
        xAxis.setTextColor(Color.parseColor("#757575"));

        // Configure left Y axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextColor(Color.parseColor("#757575"));

        // Disable right Y axis
        lineChart.getAxisRight().setEnabled(false);

        // Refresh chart
        lineChart.invalidate();
    }

    private void showEmptyState() {
        tvTotalQuizzes.setText("0");
        tvAverageScore.setText("0%");
        tvBestScore.setText("0%");
        tvRecentPerformance.setText("0%");

        // Show empty message on chart
        lineChart.setNoDataText("📊 No quiz data yet. Take your first quiz!");
        lineChart.setNoDataTextColor(Color.parseColor("#757575"));
        lineChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalytics();
    }
}