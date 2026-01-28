package com.student.learncraft;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LearnCraft.db";
    private static final int DATABASE_VERSION = 10; // 🔥 Incremented version to force refresh

    // Table: Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULL_NAME = "fullname";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_PROFILE_PIC = "profile_pic";
    private static final String COLUMN_REG_DATE = "reg_date";

    // Table: Quiz Results
    private static final String TABLE_RESULTS = "quiz_results";
    private static final String COLUMN_RESULT_ID = "result_id";
    private static final String COLUMN_RESULT_EMAIL = "user_email";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_TOTAL_QUESTIONS = "total_questions";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_PPT_NAME = "ppt_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_FULL_NAME + " TEXT, " +
                COLUMN_ROLE + " TEXT, " +
                COLUMN_PROFILE_PIC + " TEXT, " +
                COLUMN_REG_DATE + " INTEGER)";
        db.execSQL(createUsersTable);

        String createResultsTable = "CREATE TABLE " + TABLE_RESULTS + " (" +
                COLUMN_RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RESULT_EMAIL + " TEXT, " +
                COLUMN_PPT_NAME + " TEXT, " +
                COLUMN_SCORE + " INTEGER, " +
                COLUMN_TOTAL_QUESTIONS + " INTEGER, " +
                COLUMN_DATE + " INTEGER)";
        db.execSQL(createResultsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTS);
        onCreate(db);
    }

    // ==========================================
    //              USER METHODS
    // ==========================================

    public boolean addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_ROLE, user.getRole());
        values.put(COLUMN_PROFILE_PIC, user.getProfilePicturePath());
        values.put(COLUMN_REG_DATE, user.getRegistrationDate());

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // 🔥 MATCHING USER.JAVA: Name, Email, Password, Role
            User user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
            );

            user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));

            int picIndex = cursor.getColumnIndex(COLUMN_PROFILE_PIC);
            if (picIndex != -1) user.setProfilePicturePath(cursor.getString(picIndex));

            int dateIndex = cursor.getColumnIndex(COLUMN_REG_DATE);
            if (dateIndex != -1) user.setRegistrationDate(cursor.getLong(dateIndex));

            cursor.close();
            return user;
        }
        return null;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PROFILE_PIC, user.getProfilePicturePath());

        int rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{user.getEmail()});
        return rows > 0;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 🔥 FIXED: Strict check to exclude 'admin' (case insensitive)
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE LOWER(" + COLUMN_ROLE + ") != 'admin'", null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
                );
                user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));

                int picIndex = cursor.getColumnIndex(COLUMN_PROFILE_PIC);
                if (picIndex != -1) user.setProfilePicturePath(cursor.getString(picIndex));

                int dateIndex = cursor.getColumnIndex(COLUMN_REG_DATE);
                if (dateIndex != -1) user.setRegistrationDate(cursor.getLong(dateIndex));

                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userList;
    }

    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, COLUMN_ID + "=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public int getStudentCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE LOWER(" + COLUMN_ROLE + ") != 'admin'", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // ==========================================
    //          QUIZ RESULT METHODS
    // ==========================================

    public void addResult(QuizResult result, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESULT_EMAIL, userEmail);
        values.put(COLUMN_PPT_NAME, result.getPptName());
        values.put(COLUMN_SCORE, result.getCorrectAnswers());
        values.put(COLUMN_TOTAL_QUESTIONS, result.getTotalQuestions());
        values.put(COLUMN_DATE, result.getTimestamp());
        db.insert(TABLE_RESULTS, null, values);
    }

    public void addResult(QuizResult result) {
        addResult(result, "unknown@test.com");
    }

    public List<QuizResult> getResultsForUser(String email) {
        List<QuizResult> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RESULTS, null,
                COLUMN_RESULT_EMAIL + "=?", new String[]{email},
                null, null, COLUMN_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESULT_ID));
                String pptName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PPT_NAME));
                int correct = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_QUESTIONS));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String studentEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RESULT_EMAIL));

                if(pptName == null) pptName = "General Quiz";

                QuizResult result = new QuizResult(pptName, total, correct);
                result.setId(id);
                result.setTimestamp(date);
                result.setStudentEmail(studentEmail);

                results.add(result);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }

    // Restored method for compatibility
    public List<QuizResult> getResultsByUser(String email) {
        return getResultsForUser(email);
    }

    public List<QuizResult> getAllResults() {
        List<QuizResult> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RESULTS + " ORDER BY " + COLUMN_DATE + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESULT_ID));
                String pptName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PPT_NAME));
                int correct = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_QUESTIONS));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String studentEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RESULT_EMAIL));

                if(pptName == null) pptName = "General Quiz";

                QuizResult result = new QuizResult(pptName, total, correct);
                result.setId(id);
                result.setTimestamp(date);
                result.setStudentEmail(studentEmail);

                results.add(result);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }

    public int getQuizCountForUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RESULTS + " WHERE " + COLUMN_RESULT_EMAIL + "=?", new String[]{email});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, null, null);
        db.delete(TABLE_RESULTS, null, null);
    }
}