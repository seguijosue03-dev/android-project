package com.student.learncraft;

public class User {
    // 🔥 CHANGED FROM STRING TO INT (Matches Database)
    private int userId;
    private String fullName;
    private String email;
    private String password;
    private String role; // "STUDENT" or "ADMIN"
    private long registrationDate;
    private String profilePicturePath;

    public User() {
        this.registrationDate = System.currentTimeMillis();
    }

    // --- Constructor 1: Used when Registering (No ID yet) ---
    public User(String fullName, String email, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.registrationDate = System.currentTimeMillis();
    }

    // --- Constructor 2: Used by DatabaseHelper (With ID) ---
    // 🔥 This is the one fixing your error!
    public User(int userId, String fullName, String email, String password, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.registrationDate = System.currentTimeMillis();
    }

    // --- Getters and Setters (Updated userId to int) ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
}