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

    // 🔐 ADMIN CREDENTIALS (WORKING)
    public static final String ADMIN_EMAIL = "admin@learncraft.com";
    public static final String ADMIN_PASSWORD = "Admin@123";

    private final SharedPreferences preferences;
    private final Gson gson;

    public UserManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        ensureAdminExists();
    }

    /* ================= LOGIN ================= */

    public User login(String email, String password) {
        String hashed = hashPassword(password);

        for (User user : getAllUsers()) {
            if (user.getEmail().equalsIgnoreCase(email)
                    && user.getPassword().equals(hashed)) {

                setCurrentUser(user);
                setLoggedIn(true);
                return user;
            }
        }
        return null;
    }

    public void logout() {
        preferences.edit()
                .remove(KEY_CURRENT_USER)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getCurrentUser() {
        String json = preferences.getString(KEY_CURRENT_USER, null);
        return json == null ? null : gson.fromJson(json, User.class);
    }

    /* ================= USERS ================= */

    public boolean registerUser(String fullName, String email, String password) {
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) return false;
        if (isEmailExists(email)) return false;

        User user = new User(
                UUID.randomUUID().toString(),
                fullName,
                email,
                hashPassword(password),
                "STUDENT"
        );

        List<User> users = getAllUsers();
        users.add(user);
        saveUsers(users);
        return true;
    }

    public List<User> getAllUsers() {
        String json = preferences.getString(KEY_USERS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<User>>() {}.getType();
        List<User> users = gson.fromJson(json, type);
        return users != null ? users : new ArrayList<>();
    }

    /* ================= UPDATE USER (🔥 FIX) ================= */

    public boolean updateUser(User updatedUser) {
        List<User> users = getAllUsers();

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(updatedUser.getUserId())) {
                users.set(i, updatedUser);
                saveUsers(users);

                // If updating current user, update session
                User current = getCurrentUser();
                if (current != null && current.getUserId().equals(updatedUser.getUserId())) {
                    setCurrentUser(updatedUser);
                }
                return true;
            }
        }
        return false;
    }

    /* ================= ADMIN ================= */

    private void ensureAdminExists() {
        List<User> users = getAllUsers();

        // 🔥 REMOVE ALL OLD ADMINS (important)
        users.removeIf(User::isAdmin);

        // ✅ CREATE FRESH ADMIN (HASHED CORRECTLY)
        User admin = new User(
                UUID.randomUUID().toString(),
                "Administrator",
                ADMIN_EMAIL,
                hashPassword(ADMIN_PASSWORD),
                "ADMIN"
        );

        users.add(admin);
        saveUsers(users);
    }


    public void resetAdminAccount() {
        List<User> users = getAllUsers();
        users.removeIf(User::isAdmin);

        User admin = new User(
                UUID.randomUUID().toString(),
                "Administrator",
                ADMIN_EMAIL,
                hashPassword(ADMIN_PASSWORD),
                "ADMIN"
        );

        users.add(admin);
        saveUsers(users);
    }

    public int getTotalStudents() {
        int count = 0;
        for (User user : getAllUsers()) {
            if (user.isStudent()) count++;
        }
        return count;
    }

    public boolean deleteUser(String userId) {
        List<User> users = getAllUsers();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getUserId().equals(userId) && !user.isAdmin()) {
                users.remove(i);
                saveUsers(users);
                return true;
            }
        }
        return false;
    }

    /* ================= HELPERS ================= */

    private boolean isEmailExists(String email) {
        for (User user : getAllUsers()) {
            if (user.getEmail().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    private void saveUsers(List<User> users) {
        preferences.edit()
                .putString(KEY_USERS, gson.toJson(users))
                .apply();
    }

    private void setCurrentUser(User user) {
        preferences.edit()
                .putString(KEY_CURRENT_USER, gson.toJson(user))
                .apply();
    }

    private void setLoggedIn(boolean value) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, value)
                .apply();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();

        } catch (Exception e) {
            return password;
        }
    }
}
