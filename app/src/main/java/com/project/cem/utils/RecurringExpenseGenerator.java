package com.project.cem.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.project.cem.model.RecurringExpense;
import com.project.cem.model.ExpenseCategory; // Import ExpenseCategory

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class RecurringExpenseGenerator {

    private final Context context;
    private final SQLiteHelper dbHelper;

    public RecurringExpenseGenerator(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);
    }

    // Method to check and generate recurring expenses
    public void generateRecurringExpenses() {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // Use writable database
        try {
            db.beginTransaction();

            // 1. Get all *active* recurring expenses
            List<RecurringExpense> activeRecurringExpenses = getAllActiveRecurringExpenses(db);

            // 2. Get the current month and year
            Calendar today = Calendar.getInstance();
            int currentMonth = today.get(Calendar.MONTH) + 1; // Month is 0-indexed
            int currentYear = today.get(Calendar.YEAR);

            // 3. Iterate through the active recurring expenses
            for (RecurringExpense recurringExpense : activeRecurringExpenses) {
                if (!hasExpenseForMonth(db, recurringExpense, currentMonth, currentYear)) {
                    createExpenseFromRecurring(db, recurringExpense, currentMonth, currentYear);
                }
            }

            db.setTransactionSuccessful(); // Commit the transaction
        } catch (Exception e){
            Log.e("RecurringExpenseGenerator", "Error generating recurring expenses", e);
        }
        finally {
            db.endTransaction();
            db.close(); // Always close the database
        }
    }
    // Method to retrieve all *active* recurring expenses
    private List<RecurringExpense> getAllActiveRecurringExpenses(SQLiteDatabase db) {
        List<RecurringExpense> recurringExpenses = new ArrayList<>();
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_RECURRING_EXPENSE + " WHERE isActive = 1"; // Only active ones
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("recurringExpenseID"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("userID"));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                String recurrenceFrequency = cursor.getString(cursor.getColumnIndexOrThrow("recurrenceFrequency"));
                boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1; // Convert to boolean

                RecurringExpense expense = new RecurringExpense(id, userId, categoryId, description, amount, month, year, recurrenceFrequency, isActive);
                recurringExpenses.add(expense);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return recurringExpenses;
    }

    // Method to check if an expense has *already* been generated for the current month
    private boolean hasExpenseForMonth(SQLiteDatabase db, RecurringExpense recurringExpense, int month, int year) {
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_EXPENSE +
                " WHERE userID = ? AND categoryID = ? AND strftime('%Y-%m', date) = ?";
        // Format date in the same format of query.
        String yearMonth = String.format(Locale.getDefault(), "%04d-%02d", year, month); // e.g., "2024-03"
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(recurringExpense.getUserID()),
                String.valueOf(recurringExpense.getCategoryID()),
                yearMonth});

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    // Method to create an Expense entry from a RecurringExpense
    private void createExpenseFromRecurring(SQLiteDatabase db, RecurringExpense recurringExpense, int month, int year) {
        // Format today's date as yyyy-MM-dd (or your desired format)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month -1, 1); // set day to 1, month is 0-based index.
        String todayStr = dateFormat.format(calendar.getTime());


        ContentValues values = new ContentValues();
        values.put("userID", recurringExpense.getUserID());
        values.put("categoryID", recurringExpense.getCategoryID());
        values.put("description", recurringExpense.getDescription() + " (Recurring)"); // Indicate it's recurring
        values.put("amount", recurringExpense.getAmount());
        values.put("date", todayStr);  // Use today's date

        db.insert(SQLiteHelper.TABLE_EXPENSE, null, values);
    }

}