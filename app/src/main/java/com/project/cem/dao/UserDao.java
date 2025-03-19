package com.project.cem.dao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.project.cem.model.User;
import com.project.cem.utils.SQLiteHelper;
public class UserDao {
    private SQLiteHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new SQLiteHelper(context);
    }
    public long insertUser(User user) {
        if (dbHelper.isEmailExists(user.getEmail())) {
            return -1; // Email đã tồn tại
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", user.getEmail());
        values.put("password", user.getPassword());
        values.put("role", user.getRole());
        long id = db.insert(SQLiteHelper.TABLE_USER, null, values);
        db.close();
        return id;
    }

    public User getUserByEmailAndPassword(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;
        String selection = "email = ? AND password = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(
                SQLiteHelper.TABLE_USER, // Bây giờ có thể truy cập vì TABLE_USER là public
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
            cursor.close();
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return user;
    }

}
