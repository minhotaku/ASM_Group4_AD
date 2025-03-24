package com.project.cem.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.project.cem.model.ExpenseReportModels;
import com.project.cem.utils.SQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseReportRepository {
    private final SQLiteHelper dbHelper;
    private static final String TAG = "ExpenseReportRepository";

    public ExpenseReportRepository(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public ExpenseReportModels.MonthlyExpenseReport getMonthlyExpenseReport(int userId, int month, int year) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get all expenses for the month
        List<ExpenseReportModels.ExpenseItem> expenses = getMonthlyExpenses(db, userId, month, year);

        // Get all budgets for the month
        List<ExpenseReportModels.MonthlyBudget> budgets = getMonthlyBudgets(db, userId, month, year);

        // Get daily expense summaries
        List<ExpenseReportModels.DailyExpenseSummary> dailyExpenses = getDailyExpenseSummaries(db, userId, month, year);

        // Get category expense summaries
        List<ExpenseReportModels.CategoryExpenseSummary> categoryExpenses = getCategoryExpenseSummaries(db, userId, month, year);

        // Calculate totals
        double totalExpenses = calculateTotalExpenses(expenses);
        double totalBudget = calculateTotalBudget(budgets);

        db.close();

        return new ExpenseReportModels.MonthlyExpenseReport(
                expenses, budgets, dailyExpenses, categoryExpenses,
                totalExpenses, totalBudget, month, year);
    }

    private List<ExpenseReportModels.ExpenseItem> getMonthlyExpenses(SQLiteDatabase db, int userId, int month, int year) {
        List<ExpenseReportModels.ExpenseItem> expenses = new ArrayList<>();

        String query = "SELECT e.expenseID, e.userID, e.categoryID, c.categoryName, e.description, e.amount, e.date " +
                "FROM " + SQLiteHelper.TABLE_EXPENSE + " e " +
                "LEFT JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " c ON e.categoryID = c.categoryID " +
                "WHERE e.userID = ? AND strftime('%m', e.date) = ? AND strftime('%Y', e.date) = ? " +
                "ORDER BY e.date DESC";

        String monthStr = String.format(Locale.US, "%02d", month);
        String yearStr = String.valueOf(year);

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), monthStr, yearStr})) {
            while (cursor.moveToNext()) {
                int expenseId = cursor.getInt(cursor.getColumnIndexOrThrow("expenseID"));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                Date date;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    date = dateFormat.parse(dateStr);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing date: " + dateStr, e);
                    date = new Date(); // Fallback to current date
                }

                expenses.add(new ExpenseReportModels.ExpenseItem(
                        expenseId, userId, categoryId, categoryName, description, amount, date));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting monthly expenses", e);
        }

        return expenses;
    }

    private List<ExpenseReportModels.MonthlyBudget> getMonthlyBudgets(SQLiteDatabase db, int userId, int month, int year) {
        List<ExpenseReportModels.MonthlyBudget> budgets = new ArrayList<>();

        String query = "SELECT b.budgetID, b.userID, b.categoryID, c.categoryName, b.amount, b.month, b.year " +
                "FROM " + SQLiteHelper.TABLE_BUDGET + " b " +
                "LEFT JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " c ON b.categoryID = c.categoryID " +
                "WHERE b.userID = ? AND b.month = ? AND b.year = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId), String.valueOf(month), String.valueOf(year)})) {

            while (cursor.moveToNext()) {
                int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow("budgetID"));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                int budgetMonth = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
                int budgetYear = cursor.getInt(cursor.getColumnIndexOrThrow("year"));

                budgets.add(new ExpenseReportModels.MonthlyBudget(
                        budgetId, userId, categoryId, categoryName, amount, budgetMonth, budgetYear));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting monthly budgets", e);
        }

        return budgets;
    }

    private List<ExpenseReportModels.DailyExpenseSummary> getDailyExpenseSummaries(SQLiteDatabase db, int userId, int month, int year) {
        List<ExpenseReportModels.DailyExpenseSummary> dailySummaries = new ArrayList<>();

        // Calculate start date (30 days before the end of the month)
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);

        Calendar startCal = (Calendar) calendar.clone();
        startCal.add(Calendar.DAY_OF_MONTH, -29); // 30 days including the last day

        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String endDateStr = dbDateFormat.format(calendar.getTime());
        String startDateStr = dbDateFormat.format(startCal.getTime());

        String query = "SELECT date(e.date) as expense_date, SUM(e.amount) as total_amount " +
                "FROM " + SQLiteHelper.TABLE_EXPENSE + " e " +
                "WHERE e.userID = ? AND e.date BETWEEN ? AND ? " +
                "GROUP BY date(e.date) " +
                "ORDER BY e.date ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId), startDateStr, endDateStr})) {

            while (cursor.moveToNext()) {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("expense_date"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));

                Date date;
                try {
                    date = dbDateFormat.parse(dateStr);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing date: " + dateStr, e);
                    continue;
                }

                dailySummaries.add(new ExpenseReportModels.DailyExpenseSummary(date, totalAmount));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting daily expense summaries", e);
        }

        return dailySummaries;
    }

    private List<ExpenseReportModels.CategoryExpenseSummary> getCategoryExpenseSummaries(SQLiteDatabase db, int userId, int month, int year) {
        List<ExpenseReportModels.CategoryExpenseSummary> categorySummaries = new ArrayList<>();

        String query = "SELECT e.categoryID, c.categoryName, SUM(e.amount) as total_amount " +
                "FROM " + SQLiteHelper.TABLE_EXPENSE + " e " +
                "LEFT JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " c ON e.categoryID = c.categoryID " +
                "WHERE e.userID = ? AND strftime('%m', e.date) = ? AND strftime('%Y', e.date) = ? " +
                "GROUP BY e.categoryID " +
                "ORDER BY total_amount DESC";

        String monthStr = String.format(Locale.US, "%02d", month);
        String yearStr = String.valueOf(year);

        try (Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId), monthStr, yearStr})) {

            while (cursor.moveToNext()) {
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));

                categorySummaries.add(new ExpenseReportModels.CategoryExpenseSummary(
                        categoryId, categoryName, totalAmount));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting category expense summaries", e);
        }

        return categorySummaries;
    }

    private double calculateTotalExpenses(List<ExpenseReportModels.ExpenseItem> expenses) {
        double total = 0;
        for (ExpenseReportModels.ExpenseItem expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    private double calculateTotalBudget(List<ExpenseReportModels.MonthlyBudget> budgets) {
        double total = 0;
        for (ExpenseReportModels.MonthlyBudget budget : budgets) {
            total += budget.getAmount();
        }
        return total;
    }
}