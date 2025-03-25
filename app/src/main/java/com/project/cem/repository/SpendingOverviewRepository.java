package com.project.cem.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

import com.project.cem.model.CategorySpending;
import com.project.cem.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpendingOverviewRepository {
    private final SQLiteHelper dbHelper;
    private final int[] CHART_COLORS = {
            Color.rgb(46, 204, 113),   // Green
            Color.rgb(52, 152, 219),   // Blue
            Color.rgb(155, 89, 182),   // Purple
            Color.rgb(241, 196, 15),   // Yellow
            Color.rgb(230, 126, 34),   // Orange
            Color.rgb(231, 76, 60),    // Red
            Color.rgb(52, 73, 94),     // Dark Blue
            Color.rgb(22, 160, 133),   // Teal
            Color.rgb(142, 68, 173),   // Violet
            Color.rgb(243, 156, 18),   // Amber
            Color.rgb(211, 84, 0),     // Burnt Orange
            Color.rgb(192, 57, 43)     // Dark Red
    };

    public SpendingOverviewRepository(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public List<CategorySpending> getCategorySpendingByMonth(long userId, int month, int year) {
        List<CategorySpending> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // SQL query to get spending by category
            String query = "SELECT ec.categoryID, ec.categoryName, SUM(e.amount) as totalAmount " +
                    "FROM " + SQLiteHelper.TABLE_EXPENSE + " e " +
                    "JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " ec ON e.categoryID = ec.categoryID " +
                    "WHERE e.userID = ? " +
                    "AND strftime('%m', e.date) = ? " +
                    "AND strftime('%Y', e.date) = ? " +
                    "GROUP BY e.categoryID " +
                    "ORDER BY totalAmount DESC";

            String monthStr = String.format("%02d", month);
            String yearStr = String.valueOf(year);

            try (Cursor cursor = db.rawQuery(query, new String[]{
                    String.valueOf(userId),
                    monthStr,
                    yearStr
            })) {
                int colorIndex = 0;
                while (cursor.moveToNext()) {
                    long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("categoryID"));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("totalAmount"));

                    CategorySpending categorySpending = new CategorySpending(categoryId, categoryName, amount);

                    // Assign a color
                    categorySpending.setColorCode(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                    colorIndex++;

                    result.add(categorySpending);
                }
            }
        } finally {
            db.close();
        }

        return result;
    }

    public List<CategorySpending> getRecurringExpenses(long userId, int month, int year) {
        List<CategorySpending> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // SQL query to get recurring expenses (Not used in budget calculation)
            String query = "SELECT ec.categoryID, ec.categoryName, re.amount " +
                    "FROM " + SQLiteHelper.TABLE_RECURRING_EXPENSE + " re " +
                    "JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " ec ON re.categoryID = ec.categoryID " +
                    "WHERE re.userID = ? " +
                    "AND ((re.month = ? AND re.year = ?) " +
                    "     OR (re.recurrenceFrequency = 'Month' AND re.month <= ? AND re.year <= ?))";

            try (Cursor cursor = db.rawQuery(query, new String[]{
                    String.valueOf(userId),
                    String.valueOf(month),
                    String.valueOf(year),
                    String.valueOf(month),
                    String.valueOf(year)
            })) {
                int colorIndex = 0;
                while (cursor.moveToNext()) {
                    long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("categoryID"));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));

                    CategorySpending categorySpending = new CategorySpending(categoryId, categoryName, amount);
                    categorySpending.setColorCode(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                    colorIndex++;

                    result.add(categorySpending);
                }
            }
        } finally {
            db.close();
        }

        return result;
    }
    // Combine all expense
    public double getTotalExpensesForCategory(SQLiteDatabase db, int userId, int categoryId, int month, int year) {
        double totalExpenses = 0;

        // 1. Regular expenses (for the specific month and year)
        String regularExpenseQuery = "SELECT SUM(amount) FROM " + SQLiteHelper.TABLE_EXPENSE +
                " WHERE userID = ? AND categoryID = ? AND strftime('%Y-%m', date) = ?";
        String yearMonth = String.format(Locale.getDefault(), "%04d-%02d", year, month);
        Cursor regularCursor = db.rawQuery(regularExpenseQuery, new String[]{String.valueOf(userId), String.valueOf(categoryId), yearMonth});
        if (regularCursor != null && regularCursor.moveToFirst()) {
            totalExpenses += regularCursor.getDouble(0);
            regularCursor.close();
        }

        // 2. Recurring expenses (CORRECTED LOGIC)
        String recurringExpenseQuery = "SELECT SUM(amount) FROM " + SQLiteHelper.TABLE_RECURRING_EXPENSE +
                " WHERE userID = ? AND categoryID = ? AND isActive = 1 " +
                " AND ((recurrenceFrequency = 'Month' AND year*12+month <= ?) " + // Before or equal to the budget's month/year
                " OR (recurrenceFrequency = 'Year' AND year <= ?))";  // Before or equal to the budget year

        Cursor recurringCursor = db.rawQuery(recurringExpenseQuery, new String[]{
                String.valueOf(userId),
                String.valueOf(categoryId),
                String.valueOf(year * 12 + month),
                String.valueOf(year)
        });
        if (recurringCursor != null && recurringCursor.moveToFirst()) {
            totalExpenses += recurringCursor.getDouble(0);
            recurringCursor.close();
        }

        return totalExpenses;
    }

    public double getTotalBudgetForMonth(long userId, int month, int year) {
        double totalBudget = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // SQL query
            String query = "SELECT SUM(amount) as totalBudget " +
                    "FROM " + SQLiteHelper.TABLE_BUDGET + " " +
                    "WHERE userID = ? AND month = ? AND year = ?";

            try (Cursor cursor = db.rawQuery(query, new String[]{
                    String.valueOf(userId),
                    String.valueOf(month),
                    String.valueOf(year)
            })) {
                if (cursor.moveToFirst()) {
                    totalBudget = cursor.getDouble(cursor.getColumnIndexOrThrow("totalBudget"));
                }
            }
        } finally {
            db.close();
        }

        return totalBudget;
    }

    public double getMonthlyRecurringExpense(long userId, int month, int year) {
        double recurringExpense = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // SQL query (Not used in budget calculation)
            String query = "SELECT SUM(amount) as recurringExpense " +
                    "FROM " + SQLiteHelper.TABLE_RECURRING_EXPENSE + " " +
                    "WHERE userID = ? " +
                    "AND recurrenceFrequency = 'Month' " +
                    "AND ((month = ? AND year = ?) OR (recurrenceFrequency = 'Month'))";

            try (Cursor cursor = db.rawQuery(query, new String[]{
                    String.valueOf(userId),
                    String.valueOf(month),
                    String.valueOf(year)
            })) {
                if (cursor.moveToFirst()) {
                    recurringExpense = cursor.getDouble(cursor.getColumnIndexOrThrow("recurringExpense"));
                }
            }
        } finally {
            db.close();
        }

        return recurringExpense;
    }

    // Phương thức này CHỈ DÀNH CHO BudgetBroadcastReceiver
    public SQLiteDatabase getDbForBroadcastReceiver() {
        return dbHelper.getReadableDatabase();
    }
    public void closeDbForBroadcastReceiver(){
        dbHelper.close();
    }

}