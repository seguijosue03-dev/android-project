package com.student.learncraft;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private DatabaseHelper dbHelper;

    public UserManager(Context context) {
        this.context = context;
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
        this.dbHelper = new DatabaseHelper(context);
    }

    // --- 1. REGISTER ---
    public boolean registerUser(String fullName, String email, String password) {
        if (dbHelper.checkUser(email, hashPassword(password))) {
            return false;
        }
        User user = new User(fullName, email, hashPassword(password), "STUDENT");
        return dbHelper.registerUser(user);
    }

    // --- 2. LOGIN ---
    public User login(String email, String password) {
        String securePass = hashPassword(password);
        if (dbHelper.checkUser(email, securePass)) {
            User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                createSession(user.getFullName(), email, user.getRole());
                return user;
            }
        }
        return null;
    }

    // --- 3. UPDATE USER (🔥 Fixes ProfileFragment Error) ---
    public boolean updateUser(User user) {
        // If the user is setting a new password, we must hash it before sending to DB
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(hashPassword(user.getPassword()));
        }

        boolean success = dbHelper.updateUser(user);

        // If DB update worked, update the local session too so the app feels responsive
        if (success) {
            createSession(user.getFullName(), user.getEmail(), user.getRole());
        }
        return success;
    }

    // --- 4. ADMIN SETUP ---
    public void resetAdminAccount() {
        String adminEmail = "admin@learncraft.com";
        String adminPass = "Admin@123";
        User existingAdmin = dbHelper.getUserByEmail(adminEmail);
        if (existingAdmin == null) {
            User admin = new User("Administrator", adminEmail, hashPassword(adminPass), "ADMIN");
            dbHelper.registerUser(admin);
        }
    }

    // --- SESSION HELPERS ---
    public void createSession(String name, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) return null;
        String name = pref.getString(KEY_USER_NAME, null);
        String email = pref.getString(KEY_USER_EMAIL, null);
        String role = pref.getString(KEY_USER_ROLE, null);
        // Note: Password is empty here for security.
        return new User(0, name, email, "", role);
    }

    public boolean isAdmin() {
        String role = pref.getString(KEY_USER_ROLE, "STUDENT");
        return "ADMIN".equalsIgnoreCase(role);
    }

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
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
}