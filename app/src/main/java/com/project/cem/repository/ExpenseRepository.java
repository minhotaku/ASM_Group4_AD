// com.project.cem.repository/ExpenseRepository.java
package com.project.cem.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    public ExpenseRepository(SQLiteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void addExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userID", expense.getUserID());
        values.put("description", expense.getDescription());
        values.put("categoryID", expense.getCategoryID());
        values.put("amount", expense.getAmount());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(expense.getDate());
        values.put("date", dateStr);
        db.insert(SQLiteHelper.TABLE_EXPENSE, null, values);
        db.close();
    }

    public void updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userID", expense.getUserID());
        values.put("description", expense.getDescription());
        values.put("categoryID", expense.getCategoryID());
        values.put("amount", expense.getAmount());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(expense.getDate());
        values.put("date", dateStr);
        db.update(SQLiteHelper.TABLE_EXPENSE, values, "expenseID = ?", new String[]{String.valueOf(expense.getExpenseID())});
        db.close();
    }

    public void deleteExpense(int expenseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLiteHelper.TABLE_EXPENSE, "expenseID = ?", new String[]{String.valueOf(expenseId)});
        db.close();
    }

    public List<Expense> getAllExpenses(int userId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_EXPENSE + " WHERE userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setExpenseID(cursor.getInt(cursor.getColumnIndexOrThrow("expenseID")));
                expense.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));
                expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                expense.setCategoryID(cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date date = sdf.parse(dateStr);
                    expense.setDate(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    public int getExpenseCountByCategory(int categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + SQLiteHelper.TABLE_EXPENSE + " WHERE categoryID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public List<AddExpenseFragment.CategoryItem> getCategoriesByUserId(int userId) {
        List<AddExpenseFragment.CategoryItem> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT categoryID, categoryName FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " WHERE userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                categories.add(new AddExpenseFragment.CategoryItem(categoryId, categoryName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public String getCategoryNameById(int categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT categoryName FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " WHERE categoryID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});
        String categoryName = null;
        if (cursor.moveToFirst()) {
            categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
        }
        cursor.close();
        db.close();
        return categoryName;
    }
}