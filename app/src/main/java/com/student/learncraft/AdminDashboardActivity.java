package com.student.learncraft;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalStudents, tvTotalQuizzes, tvAvgScore;
    private RecyclerView rvUsers;
    private Button btnLogout, btnViewAllQuizzes;
    private CardView cardStats;

    private UserManager userManager;
    private StorageManager storageManager;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        userManager = new UserManager(this);
        storageManager = new StorageManager(this);

        // Initialize views
        initViews();

        // Load data
        loadAdminData();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        tvAvgScore = findViewById(R.id.tvAvgScore);
        rvUsers = findViewById(R.id.rvUsers);
        btnLogout = findViewById(R.id.btnLogout);
        btnViewAllQuizzes = findViewById(R.id.btnViewAllQuizzes);

        User admin = userManager.getCurrentUser();
        tvWelcome.setText("Welcome, " + admin.getFullName() + "!");

        btnLogout.setOnClickListener(v -> logout());
        btnViewAllQuizzes.setOnClickListener(v -> viewAllQuizzes());

        // Setup RecyclerView
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAdminData() {
        // Get statistics
        int totalStudents = userManager.getTotalStudents();
        int totalQuizzes = storageManager.getTotalQuizzesTaken();
        float avgScore = storageManager.getOverallAveragePercentage();

        tvTotalStudents.setText(String.valueOf(totalStudents));
        tvTotalQuizzes.setText(String.valueOf(totalQuizzes));
        tvAvgScore.setText(String.format("%.1f%%", avgScore));

        // Load all users
        List<User> allUsers = userManager.getAllUsers();
        userAdapter = new UserAdapter(allUsers);
        rvUsers.setAdapter(userAdapter);
    }

    private void viewAllQuizzes() {
        Intent intent = new Intent(this, AllQuizzesActivity.class);
        startActivity(intent);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Stay", null)
                .show();
    }

    // User Adapter for RecyclerView
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

        private List<User> users;

        public UserAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);

            holder.tvName.setText(user.getFullName());
            holder.tvEmail.setText(user.getEmail());
            holder.tvRole.setText(user.getRole());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.tvDate.setText("Registered: " + sdf.format(user.getRegistrationDate()));

            // Color code role
            if (user.isAdmin()) {
                holder.tvRole.setTextColor(getColor(R.color.error_red));
                holder.btnDelete.setVisibility(View.GONE);
            } else {
                holder.tvRole.setTextColor(getColor(R.color.accent_teal));
                holder.btnDelete.setVisibility(View.VISIBLE);
            }

            // Delete button
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(AdminDashboardActivity.this)
                        .setTitle("Delete User")
                        .setMessage("Are you sure you want to delete " + user.getFullName() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            boolean success = userManager.deleteUser(user.getUserId());
                            if (success) {
                                users.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, users.size());
                                Toast.makeText(AdminDashboardActivity.this,
                                        "✅ User deleted", Toast.LENGTH_SHORT).show();
                                loadAdminData();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this,
                                        "❌ Cannot delete admin", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            // View user's quizzes
            holder.itemView.setOnClickListener(v -> {
                if (user.isStudent()) {
                    Intent intent = new Intent(AdminDashboardActivity.this, UserQuizzesActivity.class);
                    intent.putExtra("user_email", user.getEmail());
                    intent.putExtra("user_name", user.getFullName());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvRole, tvDate;
            Button btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvUserName);
                tvEmail = itemView.findViewById(R.id.tvUserEmail);
                tvRole = itemView.findViewById(R.id.tvUserRole);
                tvDate = itemView.findViewById(R.id.tvUserDate);
                btnDelete = itemView.findViewById(R.id.btnDeleteUser);
            }
        }
    }
}