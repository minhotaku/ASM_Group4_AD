package com.project.cem.viewmodel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.project.cem.model.User;
import com.project.cem.repository.UserRepository;
import com.project.cem.utils.UserPreferences;

public class ChangePasswordViewModel extends ViewModel {
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void changePassword(Context context, String oldPassword, String newPassword, String confirmPassword) {
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            statusMessage.setValue("Fields cannot be empty");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            statusMessage.setValue("New passwords do not match");
            return;
        }

        User loggedInUser = UserPreferences.getUser(context);
        if (loggedInUser == null) {
            statusMessage.setValue("User not logged in");
            return;
        }

        SQLiteDatabase db = context.openOrCreateDatabase("UserDB", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE user_id = ?", new String[]{String.valueOf(loggedInUser.getUserID())});

        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            if (!storedPassword.equals(oldPassword)) {
                statusMessage.setValue("Incorrect old password");
                cursor.close();
                return;
            }
        } else {
            statusMessage.setValue("User not found");
            cursor.close();
            return;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        int result = db.update("users", values, "user_id=?", new String[]{String.valueOf(loggedInUser.getUserID())});
        db.close();

        if (result > 0) {
            statusMessage.setValue("Password changed successfully");
        } else {
            statusMessage.setValue("Password change failed");
        }
    }
}
