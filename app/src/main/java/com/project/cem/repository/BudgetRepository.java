package com.project.cem.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

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

    private SQLiteHelper dbHelper;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public BudgetRepository(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);
    }

    public long insert(Budget budget) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("categoryID", budget.getCategoryID());
        values.put("amount", budget.getAmount());
        values.put("startDate", dateFormat.format(budget.getStartDate()));
        values.put("endDate", dateFormat.format(budget.getEndDate()));

        long newRowId = db.insert(SQLiteHelper.TABLE_BUDGET, null, values);
        db.close();

        Log.d("BudgetRepository", "Insert result (newRowId): " + newRowId);

        return newRowId;
    }

    public MutableLiveData<List<Budget>> getAllBudgets() {
        MutableLiveData<List<Budget>> budgetsLiveData = new MutableLiveData<>();
        List<Budget> budgetList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        com.project.cem.model.User user = UserPreferences.getUser(context);
        if (user == null) {
            budgetsLiveData.setValue(budgetList);
            return budgetsLiveData;
        }

        int userId = user.getUserID();
        Log.d("BudgetRepository", "getAllBudgets - UserID: " + userId); // Thêm log user ID

        Log.d("BudgetRepository", "UserID: " + userId);
        String query = "SELECT DISTINCT B.budgetID, B.categoryID, B.amount, B.startDate, B.endDate, EC.categoryName " +
                "FROM " + SQLiteHelper.TABLE_BUDGET + " B " +
                "INNER JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " EC ON B.categoryID = EC.categoryID " +
                "INNER JOIN " + SQLiteHelper.TABLE_EXPENSE + " E ON B.categoryID = E.categoryID " +
                "WHERE E.userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null) {
            Log.d("BudgetRepository", "Number of budgets found: " + cursor.getCount());
        } else {
            Log.d("BudgetRepository", "Cursor is NULL!");
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow("budgetID"));
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                    String startDateStr = cursor.getString(cursor.getColumnIndexOrThrow("startDate"));
                    String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow("endDate"));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));

                    Date startDate = dateFormat.parse(startDateStr);
                    Date endDate = dateFormat.parse(endDateStr);

                    Budget budget = new Budget(budgetId, categoryId, amount, startDate, endDate);
                    budgetList.add(budget);
                } catch (ParseException e) {
                    Log.e("BudgetRepository", "Error parsing date", e);
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        Log.d("BudgetRepository", "getAllBudgets - Budget list size before setValue: " + budgetList.size());
        budgetsLiveData.setValue(budgetList);

        return budgetsLiveData;
    }

    public int update(Budget budget) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("categoryID", budget.getCategoryID());
        values.put("amount", budget.getAmount());
        values.put("startDate", dateFormat.format(budget.getStartDate()));
        values.put("endDate", dateFormat.format(budget.getEndDate()));

        String selection = "budgetID = ?";
        String[] selectionArgs = {String.valueOf(budget.getBudgetID())};

        int count = db.update(SQLiteHelper.TABLE_BUDGET, values, selection, selectionArgs);
        db.close();
        return count;
    }

    public List<ExpenseCategory> getAllCategories() {
        List<ExpenseCategory> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                categories.add(new ExpenseCategory(id, name));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return categories;
    }
}