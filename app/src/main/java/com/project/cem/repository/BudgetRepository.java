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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetRepository {

    private final SQLiteHelper dbHelper;
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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

    public boolean insert(SQLiteDatabase db, Budget budget) { // Returns boolean
        ContentValues values = new ContentValues();
        values.put("categoryID", budget.getCategoryID());
        values.put("amount", budget.getAmount());
        values.put("startDate", dateFormat.format(budget.getStartDate()));
        values.put("endDate", dateFormat.format(budget.getEndDate()));
        values.put("userID", budget.getUserID());

        long newRowId = db.insert(SQLiteHelper.TABLE_BUDGET, null, values);
        return newRowId != -1; // true if successful, false otherwise
    }
    public List<Budget> getAllBudgets(SQLiteDatabase db) {
        List<Budget> budgetList = new ArrayList<>();
        com.project.cem.model.User user = UserPreferences.getUser(context);
        if (user == null) {
            return budgetList;
        }
        int userId = user.getUserID();
        String query = "SELECT B.budgetID, B.categoryID, B.amount, B.startDate, B.endDate, EC.categoryName " +
                "FROM " + SQLiteHelper.TABLE_BUDGET + " B " +
                "INNER JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " EC ON B.categoryID = EC.categoryID " +
                "WHERE B.userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow("budgetID"));
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                    String startDateStr = cursor.getString(cursor.getColumnIndexOrThrow("startDate"));
                    String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow("endDate"));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName")); // Corrected line
                    Date startDate = dateFormat.parse(startDateStr);
                    Date endDate = dateFormat.parse(endDateStr);
                    Budget budget = new Budget(budgetId, userId, categoryId, amount, startDate, endDate);
                    budgetList.add(budget);
                } catch (ParseException e) {
                    Log.e("BudgetRepository", "Error parsing date", e);
                }
            } while (cursor.moveToNext());
            cursor.close(); // Close cursor
        }
        return budgetList;
    }


    public int update(SQLiteDatabase db, Budget budget) {
        ContentValues values = new ContentValues();
        values.put("categoryID", budget.getCategoryID());
        values.put("amount", budget.getAmount());
        values.put("startDate", dateFormat.format(budget.getStartDate()));
        values.put("endDate", dateFormat.format(budget.getEndDate()));
        values.put("userID", budget.getUserID());

        String selection = "budgetID = ?";
        String[] selectionArgs = {String.valueOf(budget.getBudgetID())};

        return db.update(SQLiteHelper.TABLE_BUDGET, values, selection, selectionArgs);

    }

    public List<ExpenseCategory> getAllCategories(SQLiteDatabase db) {
        List<ExpenseCategory> categories = new ArrayList<>();
        com.project.cem.model.User user = UserPreferences.getUser(context);
        if(user == null){
            return  categories;
        }
        String query = "SELECT categoryID, categoryName, userID FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY+ " WHERE userID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(user.getUserID())});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("userID"));
                categories.add(new ExpenseCategory(id, userId, name)); // Sửa constructor
            } while (cursor.moveToNext());
            cursor.close(); // Close cursor
        }

        return categories;
    }

    public double getTotalExpensesForCategory(SQLiteDatabase db, int userId, int categoryId, Date startDate, Date endDate) {
        double totalExpenses = 0;
        String query = "SELECT SUM(E.amount) " +
                "FROM " + SQLiteHelper.TABLE_EXPENSE + " E " +
                "WHERE E.userID = ? AND E.categoryID = ? AND E.date >= ? AND E.date <= ?";

        String startDateString = dateFormat.format(startDate);
        String endDateString = dateFormat.format(endDate);

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(categoryId), startDateString, endDateString});

        if (cursor != null && cursor.moveToFirst()) {
            totalExpenses = cursor.getDouble(0);
            cursor.close(); // Close cursor
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
                try {
                    int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow("budgetID"));
                    int returnCategoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                    String startDateStr = cursor.getString(cursor.getColumnIndexOrThrow("startDate"));
                    String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow("endDate"));

                    Date startDate = dateFormat.parse(startDateStr);
                    Date endDate = dateFormat.parse(endDateStr);

                    Budget budget = new Budget(budgetId, userId, returnCategoryId, amount, startDate, endDate);
                    budgetList.add(budget);
                }
                catch (ParseException e) {
                    Log.e("BudgetRepository", "Error parsing date", e);
                }
            } while (cursor.moveToNext());
            cursor.close();

        }

        return budgetList;
    }

}