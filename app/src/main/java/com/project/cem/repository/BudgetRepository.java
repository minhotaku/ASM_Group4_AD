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

        com.project.cem.model.User user = UserPreferences.getUser(context);
        if (user != null) {
            values.put("userID", user.getUserID());
        } else {
            Log.e("BudgetRepository", "User is null, cannot insert userID.");
            return -1;
        }

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
        Log.d("BudgetRepository", "getAllBudgets - UserID: " + userId);

        String query = "SELECT B.budgetID, B.categoryID, B.amount, B.startDate, B.endDate, EC.categoryName " +
                "FROM " + SQLiteHelper.TABLE_BUDGET + " B " +
                "INNER JOIN " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " EC ON B.categoryID = EC.categoryID " +
                "WHERE B.userID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null) {
            Log.d("BudgetRepository", "getAllBudgets - Number of budgets found: " + cursor.getCount());
        } else {
            Log.d("BudgetRepository", "getAllBudgets - Cursor is NULL!");
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

        com.project.cem.model.User user = UserPreferences.getUser(context);
        if(user != null){
            values.put("userID", user.getUserID());
        }
        else {
            Log.e("BudgetRepository", "User is null, cannot update userID.");
            return -1;
        }

        String selection = "budgetID = ?";
        String[] selectionArgs = {String.valueOf(budget.getBudgetID())};

        int count = db.update(SQLiteHelper.TABLE_BUDGET, values, selection, selectionArgs);
        db.close();
        Log.d("BudgetRepository", "Update count: " + count);
        return count;
    }

    public List<ExpenseCategory> getAllCategories() {
        List<ExpenseCategory> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        com.project.cem.model.User user = UserPreferences.getUser(context);

        if (user == null) {
            Log.e("BudgetRepository", "getAllCategories - User is NULL!");
            return categories;
        }

        Log.d("BudgetRepository", "getAllCategories - UserID: " + user.getUserID());
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY+ " WHERE userID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(user.getUserID())});

        if (cursor != null) {
            Log.d("BudgetRepository", "getAllCategories - Number of categories found: " + cursor.getCount());
        } else {
            Log.d("BudgetRepository", "getAllCategories - Cursor is NULL!");
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("userID"));
                categories.add(new ExpenseCategory(id, userId, name));
            } while (cursor.moveToNext());
            cursor.close();
        }
        else {
            Log.d("BudgetRepository", "getAllCategories - Cursor is empty or null.");
        }
        db.close();
        Log.d("BudgetRepository", "getAllCategories - Category list size: " + categories.size());
        return categories;
    }
}