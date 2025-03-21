package com.project.cem.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.project.cem.model.User;
import com.project.cem.utils.SQLiteHelper;
public class UserRepository {

    private SQLiteHelper sqLiteHelper;

    // Constructor để khởi tạo SQLiteHelper
    public UserRepository(Context context) {
        sqLiteHelper = new SQLiteHelper(context); // Khởi tạo SQLiteHelper với context
    }

    public User login(String email, String password){
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        User user = null;
        String selection = "email = ? AND password = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = null;
        try {
            cursor = db.query(
                    SQLiteHelper.TABLE_USER, // Truy vấn vào bảng TABLE_USER
                    new String[]{"userID", "email", "password", "role"},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return user; // Trả về user nếu đăng nhập thành công, null nếu thất bại
    }

    public boolean register(String email, String password) {
        if (sqLiteHelper.isEmailExists(email)) {
            return false; // Trả về false nếu email đã tồn tại
        }

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        values.put("role", "user");

        long id = -1;
        try {
            id = db.insert(SQLiteHelper.TABLE_USER, null, values);
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        } finally {
            db.close();
        }

        return id != -1; // Trả về true nếu đăng ký thành công, false nếu thất bại
    }
    public boolean changePassword(int userID, String newPassword) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword); // Make sure to hash the password before saving it

        String selection = "userID = ?"; // Query by userID
        String[] selectionArgs = {String.valueOf(userID)}; // Convert userID to string for query

        int count = -1;
        try {
            count = db.update(
                    SQLiteHelper.TABLE_USER,
                    values,
                    selection,
                    selectionArgs
            );
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        } finally {
            db.close();
        }

        return count > 0; // Return true if password was changed successfully, false if failed
    }


}
