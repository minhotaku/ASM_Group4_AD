package com.project.cem.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.project.cem.model.User;
public class UserPreferences {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLE = "role";

    // Lưu thông tin người dùng vào SharedPreferences
    public static void saveUser(Context context, User user) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, user.getUserID());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.putString(KEY_ROLE, user.getRole());
        editor.apply(); // Áp dụng thay đổi
    }

    // Lấy thông tin người dùng từ SharedPreferences
    public static User getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userID = sharedPreferences.getInt(KEY_USER_ID, -1);
        String email = sharedPreferences.getString(KEY_EMAIL, null);
        String password = sharedPreferences.getString(KEY_PASSWORD, null);
        String role = sharedPreferences.getString(KEY_ROLE, null);

        if (userID != -1 && email != null && password != null && role != null) {
            return new User(userID, email, password, role);
        }
        return null;
    }

    // Xóa thông tin người dùng sử dụng khi đăng xuất.
    public static void clearUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}