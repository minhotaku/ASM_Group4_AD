package com.project.cem.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.project.cem.model.Expense;
import com.project.cem.model.ExpenseWithCategory;
import com.project.cem.utils.SQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseRepository {
    private SQLiteHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    public ExpenseRepository(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    // Lấy tất cả chi tiêu của người dùng
    public List<ExpenseWithCategory> getAllExpenses(int userID) {
        List<ExpenseWithCategory> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT e.*, c.categoryName FROM " + SQLiteHelper.TABLE_EXPENSE + " e " +
                "LEFT JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " c ON e.categoryID = c.categoryID " +
                "WHERE e.userID = ? ORDER BY e.date DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userID)});

        if (cursor.moveToFirst()) {
            do {
                try {
                    Expense expense = new Expense();
                    expense.setExpenseID(cursor.getInt(cursor.getColumnIndexOrThrow("expenseID")));
                    expense.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));
                    expense.setCategoryID(cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")));
                    expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));

                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    Date date = dateFormat.parse(dateStr);
                    expense.setDate(date);

                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));

                    // Tạo monthYear từ date để nhóm
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    String monthYear = monthYearFormat.format(date);

                    expenses.add(new ExpenseWithCategory(expense, categoryName, monthYear));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    // Lấy chi tiêu theo danh mục
    public List<ExpenseWithCategory> getExpensesByCategory(int userID, int categoryID) {
        List<ExpenseWithCategory> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT e.*, c.categoryName FROM " + SQLiteHelper.TABLE_EXPENSE + " e " +
                "LEFT JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " c ON e.categoryID = c.categoryID " +
                "WHERE e.userID = ? AND e.categoryID = ? ORDER BY e.date DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userID), String.valueOf(categoryID)});

        if (cursor.moveToFirst()) {
            do {
                try {
                    Expense expense = new Expense();
                    expense.setExpenseID(cursor.getInt(cursor.getColumnIndexOrThrow("expenseID")));
                    expense.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));
                    expense.setCategoryID(cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")));
                    expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));

                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    Date date = dateFormat.parse(dateStr);
                    expense.setDate(date);

                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));

                    // Tạo monthYear từ date để nhóm
                    String monthYear = monthYearFormat.format(date);

                    expenses.add(new ExpenseWithCategory(expense, categoryName, monthYear));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    // Thêm chi tiêu mới
    public long addExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userID", expense.getUserID());
        values.put("categoryID", expense.getCategoryID());
        values.put("description", expense.getDescription());
        values.put("amount", expense.getAmount());
        values.put("date", dateFormat.format(expense.getDate()));

        long id = db.insert(SQLiteHelper.TABLE_EXPENSE, null, values);
        db.close();
        return id;
    }

    // Cập nhật chi tiêu
    public int updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("categoryID", expense.getCategoryID());
        values.put("description", expense.getDescription());
        values.put("amount", expense.getAmount());
        values.put("date", dateFormat.format(expense.getDate()));

        int rowsAffected = db.update(SQLiteHelper.TABLE_EXPENSE, values, "expenseID = ?",
                new String[]{String.valueOf(expense.getExpenseID())});
        db.close();
        return rowsAffected;
    }

    // Xóa chi tiêu
    public int deleteExpense(int expenseID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(SQLiteHelper.TABLE_EXPENSE, "expenseID = ?",
                new String[]{String.valueOf(expenseID)});
        db.close();
        return rowsAffected;
    }

    // Lấy chi tiêu theo ID
    public Expense getExpenseById(int expenseID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Expense expense = null;

        Cursor cursor = db.query(SQLiteHelper.TABLE_EXPENSE, null, "expenseID = ?",
                new String[]{String.valueOf(expenseID)}, null, null, null);

        if (cursor.moveToFirst()) {
            try {
                expense = new Expense();
                expense.setExpenseID(cursor.getInt(cursor.getColumnIndexOrThrow("expenseID")));
                expense.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));
                expense.setCategoryID(cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")));
                expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));

                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                expense.setDate(dateFormat.parse(dateStr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        db.close();
        return expense;
    }
}