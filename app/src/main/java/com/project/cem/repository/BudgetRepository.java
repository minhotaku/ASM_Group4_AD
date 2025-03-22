package com.project.cem.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.utils.UserPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetRepository {

    private final SQLiteHelper dbHelper;
    private final Context context;
    // No longer needed: private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public BudgetRepository(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);
    }

    public SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase() {
        return dbHelper.getReadableDatabase();
    }

    public boolean insert(SQLiteDatabase db, Budget budget) {
        ContentValues values = new ContentValues();
        values.put("categoryID", budget.getCategoryID());
        values.put("amount", budget.getAmount());
        values.put("month", budget.getMonth()); // Use month
        values.put("year", budget.getYear());   // Use year
        values.put("userID", budget.getUserID());

        long newRowId = db.insert(SQLiteHelper.TABLE_BUDGET, null, values);
        return newRowId != -1;
    }

    public List<Budget> getAllBudgets(SQLiteDatabase db) {
        List<Budget> budgetList = new ArrayList<>();
        com.project.cem.model.User user = UserPreferences.getUser(context);
        if (user == null) {
            return budgetList;
        }
        int userId = user.getUserID();

        // Modified query to select month and year
        String query = "SELECT B.budgetID, B.categoryID, B.amount, B.month, B.year, EC.categoryName " +
                "FROM " + SQLiteHelper.TABLE_BUDGET + " B " +
                "INNER JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " EC ON B.categoryID = EC.categoryID " +
                "WHERE B.userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow("budgetID"));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                int month = cursor.getInt(cursor.getColumnIndexOrThrow("month")); // Get month
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));   // Get year
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));

                Budget budget = new Budget(budgetId, userId, categoryId, amount, month, year);
                budgetList.add(budget);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return budgetList;
    }

    public int update(SQLiteDatabase db, Budget budget) {
        ContentValues values = new ContentValues();
        values.put("categoryID", budget.getCategoryID());
        values.put("amount", budget.getAmount());
        values.put("month", budget.getMonth()); // Use month
        values.put("year", budget.getYear());   // Use year
        values.put("userID", budget.getUserID());

        String selection = "budgetID = ?";
        String[] selectionArgs = {String.valueOf(budget.getBudgetID())};

        return db.update(SQLiteHelper.TABLE_BUDGET, values, selection, selectionArgs);
    }

    public List<ExpenseCategory> getAllCategories(SQLiteDatabase db) {
        List<ExpenseCategory> categories = new ArrayList<>();
        com.project.cem.model.User user = UserPreferences.getUser(context);
        if (user == null) {
            return categories;
        }
        String query = "SELECT categoryID, categoryName, userID FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " WHERE userID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(user.getUserID())});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("userID"));
                categories.add(new ExpenseCategory(id, userId, name));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return categories;
    }

    // Modified getTotalExpensesForCategory to take month and year
    public double getTotalExpensesForCategory(SQLiteDatabase db, int userId, int categoryId, int month, int year) {
        double totalExpenses = 0;

        // Create a Calendar object for the given month and year
        Calendar calendar = Calendar.getInstance();
        calendar.clear(); // Clear all fields, including time fields
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // Month is 0-indexed (January = 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of the month
        Date startDate = calendar.getTime();

        // Set the Calendar to the *end* of the month
        calendar.add(Calendar.MONTH, 1);  // Go to the *beginning* of the *next* month
        calendar.add(Calendar.DAY_OF_MONTH, -1); // Subtract one day to get the *last* day of the *current* month
        Date endDate = calendar.getTime();

        // Convert Dates to strings for the query (using a SimpleDateFormat)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDateString = dateFormat.format(startDate);
        String endDateString = dateFormat.format(endDate);

        // SQLite query to get the sum of expenses for the specified category, user, month, and year
        String query = "SELECT SUM(E.amount) " +
                "FROM " + SQLiteHelper.TABLE_EXPENSE + " E " +
                "WHERE E.userID = ? AND E.categoryID = ? AND E.date >= ? AND E.date <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(categoryId), startDateString, endDateString});

        if (cursor != null && cursor.moveToFirst()) {
            totalExpenses = cursor.getDouble(0); // The sum is in the first column (index 0)
            cursor.close();
        }

        return totalExpenses;
    }
    public List<Budget> getBudgetsByCategoryAndUser(SQLiteDatabase db, int userId, int categoryId) {
        List<Budget> budgetList = new ArrayList<>();
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_BUDGET +
                " WHERE userID = ? AND categoryID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(categoryId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow("budgetID"));
                int returnCategoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                int month = cursor.getInt(cursor.getColumnIndexOrThrow("month")); // Get month
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));   // Get year

                Budget budget = new Budget(budgetId, userId, returnCategoryId, amount, month, year);
                budgetList.add(budget);

            } while (cursor.moveToNext());
            cursor.close();

        }

        return budgetList;
    }

}