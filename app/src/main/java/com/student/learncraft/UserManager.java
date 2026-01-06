package com.student.learncraft;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserManager {

    private static final String PREF_NAME = "LearnCraftUsers";
    private static final String KEY_USERS = "users_list";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_FIRST_RUN = "first_run";

    private SharedPreferences preferences;
    private Gson gson;

    public UserManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();

        // Create default admin account on first run
        if (isFirstRun()) {
            createDefaultAdmin();
            setFirstRunComplete();
        }
    }

    /**
     * Register a new user
     */
    public boolean registerUser(String fullName, String email, String password) {
        // Validate inputs
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return false;
        }

        // Check if email already exists
        if (isEmailExists(email)) {
            return false;
        }

        // Create new user
        String userId = UUID.randomUUID().toString();
        String hashedPassword = hashPassword(password);
        User user = new User(userId, fullName, email, hashedPassword, "STUDENT");

        // Save user
        List<User> users = getAllUsers();
        users.add(user);
        saveUsers(users);

        return true;
    }

    /**
     * Login user
     */
    public User login(String email, String password) {
        List<User> users = getAllUsers();
        String hashedPassword = hashPassword(password);

        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(hashedPassword)) {
                // Save current user and login status
                setCurrentUser(user);
                setLoggedIn(true);
                return user;
            }
        }

        return null; // Login failed
    }

    /**
     * Logout current user
     */
    public void logout() {
        preferences.edit()
                .remove(KEY_CURRENT_USER)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        String json = preferences.getString(KEY_CURRENT_USER, null);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, User.class);
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get all users (Admin only)
     */
    public List<User> getAllUsers() {
        String json = preferences.getString(KEY_USERS, null);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<User>>(){}.getType();
        List<User> users = gson.fromJson(json, type);

        return users != null ? users : new ArrayList<>();
    }

    /**
     * Delete user (Admin only)
     */
    public boolean deleteUser(String userId) {
        List<User> users = getAllUsers();

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(userId)) {
                // Don't allow deleting admin
                if (users.get(i).isAdmin()) {
                    return false;
                }
                users.remove(i);
                saveUsers(users);
                return true;
            }
        }

        return false;
    }

    /**
     * Update user profile
     */
    public boolean updateUser(User updatedUser) {
        List<User> users = getAllUsers();

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(updatedUser.getUserId())) {
                users.set(i, updatedUser);
                saveUsers(users);

                // If updating current user, update session
                User currentUser = getCurrentUser();
                if (currentUser != null && currentUser.getUserId().equals(updatedUser.getUserId())) {
                    setCurrentUser(updatedUser);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Get total student count
     */
    public int getTotalStudents() {
        List<User> users = getAllUsers();
        int count = 0;
        for (User user : users) {
            if (user.isStudent()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if email exists
     */
    private boolean isEmailExists(String email) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Save all users
     */
    private void saveUsers(List<User> users) {
        String json = gson.toJson(users);
        preferences.edit().putString(KEY_USERS, json).apply();
    }

    /**
     * Set current user
     */
    private void setCurrentUser(User user) {
        String json = gson.toJson(user);
        preferences.edit().putString(KEY_CURRENT_USER, json).apply();
    }

    /**
     * Set logged in status
     */
    private void setLoggedIn(boolean isLoggedIn) {
        preferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    /**
     * Check if first run
     */
    private boolean isFirstRun() {
        return !preferences.getBoolean(KEY_FIRST_RUN, false);
    }

    /**
     * Set first run complete
     */
    private void setFirstRunComplete() {
        preferences.edit().putBoolean(KEY_FIRST_RUN, true).apply();
    }

    /**
     * Create default admin account
     * Email: admin@learncraft.com
     * Password: Admin@2025
     */
    private void createDefaultAdmin() {
        String adminId = UUID.randomUUID().toString();
        String hashedPassword = hashPassword("Admin@2025");
        User admin = new User(adminId, "Administrator", "admin@learncraft.com", hashedPassword, "ADMIN");

        List<User> users = new ArrayList<>();
        users.add(admin);
        saveUsers(users);
    }

    /**
     * Force reset admin account (for debugging)
     */
    public void resetAdminAccount() {
        List<User> users = getAllUsers();

        // Remove old admin
        users.removeIf(user -> "ADMIN".equals(user.getRole()));

        // Create new admin
        String adminId = UUID.randomUUID().toString();
        String hashedPassword = hashPassword("Admin@2025");
        User admin = new User(adminId, "Administrator", "admin@learncraft.com", hashedPassword, "ADMIN");

        users.add(admin);
        saveUsers(users);
    }

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            return password; // Fallback to plain text (not recommended for production)
        }
    }
}