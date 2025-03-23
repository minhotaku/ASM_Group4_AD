package com.project.cem.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

import com.project.cem.model.CategorySpending;
import com.project.cem.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ExpenseRepository {
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

    public ExpenseRepository(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public List<CategorySpending> getCategorySpendingByMonth(long userId, int month, int year) {
        List<CategorySpending> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // SQL query to get spending by category for a specific month and year
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

                    // Assign a color from our predefined color array
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
}