// com.project.cem.repository/ExpenseRepository.java
package com.project.cem.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.project.cem.model.Expense;
import com.project.cem.ui.expenses.AddExpenseFragment;
import com.project.cem.utils.SQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseRepository {
    private SQLiteHelper dbHelper;
    private static final String TAG = "ExpenseRepository";

    public ExpenseRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Expense> getExpensesByUserId(int userId) {
        List<Expense> expenseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + SQLiteHelper.TABLE_EXPENSE + " WHERE userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int expenseID = cursor.getInt(cursor.getColumnIndexOrThrow("expenseID"));
                int userID = cursor.getInt(cursor.getColumnIndexOrThrow("userID"));
                int categoryID = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = null;
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        date = sdf.parse(dateStr);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing date: " + dateStr, e);
                    }
                } else {
                    Log.w(TAG, "Date is null or empty for expenseID: " + expenseID);
                }

                Expense expense = new Expense(expenseID, userID, description, categoryID, amount, date);
                expenseList.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenseList;
    }

    public String getCategoryNameById(int categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String categoryName = "Unknown";
        String query = "SELECT categoryName FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " WHERE categoryID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});

        if (cursor.moveToFirst()) {
            categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
        }
        cursor.close();
        db.close();
        return categoryName;
    }

    public List<AddExpenseFragment.CategoryItem> getCategoriesByUserId(int userId) {
        List<AddExpenseFragment.CategoryItem> categoryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT categoryID, categoryName FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " WHERE userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                categoryList.add(new AddExpenseFragment.CategoryItem(categoryId, categoryName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categoryList;
    }

    public void addExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("userID", expense.getUserID());
        values.put("categoryID", expense.getCategoryID());
        values.put("description", expense.getDescription());
        values.put("amount", expense.getAmount());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = expense.getDate() != null ? sdf.format(expense.getDate()) : null;
        values.put("date", dateStr);

        db.insert(SQLiteHelper.TABLE_EXPENSE, null, values);
        db.close();
    }

    // Cập nhật chi tiêu
    public void updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("userID", expense.getUserID());
        values.put("categoryID", expense.getCategoryID());
        values.put("description", expense.getDescription());
        values.put("amount", expense.getAmount());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = expense.getDate() != null ? sdf.format(expense.getDate()) : null;
        values.put("date", dateStr);

        db.update(SQLiteHelper.TABLE_EXPENSE, values, "expenseID = ?",
                new String[]{String.valueOf(expense.getExpenseID())});
        db.close();
    }

    // Xóa chi tiêu
    public void deleteExpense(int expenseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLiteHelper.TABLE_EXPENSE, "expenseID = ?",
                new String[]{String.valueOf(expenseId)});
        db.close();
    }
}