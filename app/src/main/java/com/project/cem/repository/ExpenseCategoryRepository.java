package com.project.cem.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.project.cem.model.ExpenseCategory;
import com.project.cem.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoryRepository {

    private final SQLiteHelper dbHelper;

    public ExpenseCategoryRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public ExpenseCategoryRepository(android.content.Context context) {
        this.dbHelper = new SQLiteHelper(context);
    }

    public void insertExpenseCategory(ExpenseCategory category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("categoryName", category.getCategoryName());
        values.put("userID", category.getUserID());
        long id = db.insert("ExpenseCategory", null, values);
        category.setCategoryID((int) id);
        db.close();
    }

    public void updateExpenseCategory(ExpenseCategory category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("categoryName", category.getCategoryName());
        values.put("userID", category.getUserID());
        db.update("ExpenseCategory", values,
                "categoryID = ?",
                new String[]{String.valueOf(category.getCategoryID())});
        db.close();
    }

    public void deleteExpenseCategory(int categoryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("ExpenseCategory",
                "categoryID = ?",
                new String[]{String.valueOf(categoryId)});
        db.close();
    }

    public List<ExpenseCategory> getAllCategories(int userId) {
        List<ExpenseCategory> categoryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("ExpenseCategory",
                null,
                "userID = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ExpenseCategory category = new ExpenseCategory();
                category.setCategoryID(cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")));
                category.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("categoryName")));
                category.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));
                categoryList.add(category);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return categoryList;
    }

    public int getExpenseCountForCategory(int categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Expense WHERE categoryID = ?",
                new String[]{String.valueOf(categoryId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    // Phương thức lấy categoryName từ categoryID
    public String getCategoryNameById(int categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("ExpenseCategory",
                new String[]{"categoryName"},
                "categoryID = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null);

        String categoryName = null;
        if (cursor != null && cursor.moveToFirst()) {
            categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
            cursor.close();
        }
        db.close();
        return categoryName;
    }
}