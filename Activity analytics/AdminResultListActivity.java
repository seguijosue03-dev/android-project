package com.student.learncraft;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminResultListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvNoData;
    private DatabaseHelper databaseHelper;
    private ResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_result_list);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Quiz Results");
        }

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerView);
        tvNoData = findViewById(R.id.tvNoData);

        // Initialize DB
        databaseHelper = new DatabaseHelper(this);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load Data
        loadAllResults();
    }

    private void loadAllResults() {
        List<QuizResult> allResults = databaseHelper.getAllResults();

        if (allResults.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);

            // Reusing the same Adapter you already have
            adapter = new ResultAdapter(allResults);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back when back arrow is pressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}