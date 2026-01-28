package com.student.learncraft;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class UserManager {

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";

    // 🔥 1. Added Key for Profile Picture
    private static final String KEY_USER_PIC = "userPic";

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
        return dbHelper.addUser(user);
    }

    // --- 2. LOGIN ---
    public User login(String email, String password) {
        String securePass = hashPassword(password);

        if (dbHelper.checkUser(email, securePass)) {
            User user = dbHelper.getUser(email);
            if (user != null) {
                // 🔥 2. Save the picture path during login
                createSession(user.getFullName(), email, user.getRole(), user.getProfilePicturePath());
                return user;
            }
        }
        return null;
    }

    // --- 3. UPDATE USER ---
    public boolean updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(hashPassword(user.getPassword()));
        }

        boolean success = dbHelper.updateUser(user);

        if (success) {
            // 🔥 3. Update the session immediately with the new picture
            createSession(user.getFullName(), user.getEmail(), user.getRole(), user.getProfilePicturePath());
        }
        return success;
    }

    // --- 4. ADMIN SETUP ---
    public void resetAdminAccount() {
        String adminEmail = "admin@learncraft.com";
        String adminPass = "Admin@123";
        User existingAdmin = dbHelper.getUser(adminEmail);

        if (existingAdmin == null) {
            User admin = new User("Administrator", adminEmail, hashPassword(adminPass), "ADMIN");
            dbHelper.addUser(admin);
        }
    }

    // ==========================================
    //           SESSION MANAGEMENT
    // ==========================================

    // 🔥 4. Updated to accept Picture Path
    public void createSession(String name, String email, String role, String picPath) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_PIC, picPath); // Save the path!
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
        String picPath = pref.getString(KEY_USER_PIC, null); // Load the path!

        User user = new User(name, email, "", role);

        // 🔥 5. Set the picture path on the object
        if (picPath != null) {
            user.setProfilePicturePath(picPath);
        }

        return user;
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

    // --- EXTRAS ---
    public int getTotalStudents() {
        return dbHelper.getStudentCount();
    }

    public void saveQuizResult(QuizResult result) {
        dbHelper.addResult(result);
    }

    public List<QuizResult> getAllQuizResults() {
        return dbHelper.getAllResults();
    }

    public List<User> getAllUsers() {
        return dbHelper.getAllUsers();
    }

    public boolean deleteUser(int userId) {
        return dbHelper.deleteUser(userId);
    }
}