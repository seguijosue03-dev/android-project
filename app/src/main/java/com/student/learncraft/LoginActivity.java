package com.student.learncraft;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvAdminHint;

    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userManager = new UserManager(this);

        // Check if already logged in
        if (userManager.isLoggedIn()) {
            navigateBasedOnRole();
            return;
        }

        // Initialize views
        initViews();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvAdminHint = findViewById(R.id.tvAdminHint);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> goToRegister());

        // MULTI TAP on app name to force create admin
        TextView tvAppName = findViewById(R.id.tvAppName);
        if (tvAppName != null) {
            tvAppName.setOnClickListener(new View.OnClickListener() {
                private int clickCount = 0;
                private long lastClickTime = 0;

                @Override
                public void onClick(View v) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime < 500) {
                        clickCount++;
                    } else {
                        clickCount = 1;
                    }
                    lastClickTime = currentTime;

                    if (clickCount == 5) {
                        forceCreateAdmin();
                        clickCount = 0;
                    }
                }
            });
        }
    }

    private void forceCreateAdmin() {
        userManager.resetAdminAccount();
        Toast.makeText(
                this,
                "✅ Admin Created!\nEmail: admin@learncraft.com\nPassword: admin2025",
                Toast.LENGTH_LONG
        ).show();
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Attempt login
        User user = userManager.login(email, password);

        if (user != null) {
            Toast.makeText(
                    this,
                    "✅ Welcome, " + user.getFullName() + "!",
                    Toast.LENGTH_SHORT
            ).show();
            navigateBasedOnRole();
        } else {
            Toast.makeText(
                    this,
                    "❌ Invalid email or password",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void navigateBasedOnRole() {
        User currentUser = userManager.getCurrentUser();
        if (currentUser == null) return;

        Intent intent;
        if (currentUser.isAdmin()) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void goToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
