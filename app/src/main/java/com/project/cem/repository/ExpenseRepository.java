package com.project.cem.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.project.cem.model.Expense;
import com.project.cem.utils.SQLiteHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExpenseRepository {
    private SQLiteHelper dbHelper;

    public ExpenseRepository(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public void insertExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userID", expense.getUserID());
        values.put("description", expense.getDescription());
        values.put("categoryID", expense.getCategoryID());
        values.put("amount", expense.getAmount());
        values.put("date", expense.getDate().getTime());

        long result = db.insert(SQLiteHelper.TABLE_EXPENSE, null, values);
        db.close();

        if (result == -1) {
            Log.e("ExpenseRepository", "Lỗi khi thêm Expense vào database!");
        } else {
            Log.d("ExpenseRepository", "Đã thêm Expense thành công với ID: " + result);
        }
    }



    public List<Expense> getAllExpenses(int userID) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + SQLiteHelper.TABLE_EXPENSE + " WHERE userID = ? ORDER BY date DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userID)});

        if (cursor.moveToFirst()) {
            do {
                int expenseID = cursor.getInt(cursor.getColumnIndexOrThrow("expenseID"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                Date date = new Date(dateMillis);
                int categoryID = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));

                Expense expense = new Expense(expenseID, userID, description, categoryID, amount, date);
                expenses.add(expense);
            } while (cursor.moveToNext());

            Log.d("ExpenseRepository", "Lấy được " + expenses.size() + " Expense từ database!");
        } else {
            Log.d("ExpenseRepository", "Không tìm thấy Expense nào!");
        }

        cursor.close();
        db.close();
        return expenses;
    }



}
