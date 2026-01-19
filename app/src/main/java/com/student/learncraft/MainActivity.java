package com.student.learncraft;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userManager = new UserManager(this);

        // Check if user is logged in
        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        bottomNav = findViewById(R.id.bottom_navigation);

        // Bottom navigation listener
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_quiz) {
                selectedFragment = new QuizFragment();
            } else if (itemId == R.id.nav_analytics) {
                selectedFragment = new AnalyticsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // 🔥 HANDLE REDIRECT FROM RESULT ACTIVITY
        if (savedInstanceState == null) {
            String openFragment = getIntent().getStringExtra("open_fragment");

            if ("home".equals(openFragment)) {
                // FORCE HOME
                bottomNav.setSelectedItemId(R.id.nav_home);

                // CLEAR EXTRA SO IT DOESN'T REPEAT
                getIntent().removeExtra("open_fragment");
            } else {
                // Default behavior
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // If not on home fragment, go back to home
        if (bottomNav.getSelectedItemId() != R.id.nav_home) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}
