package com.project.cem.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.RecurringExpense;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.utils.UserPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseRepository {

    private final SQLiteHelper dbHelper;
    private final Context context;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public RecurringExpenseRepository(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);
    }

    public SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase() {
        return dbHelper.getReadableDatabase();
    }

    // Method to create a new expense from a recurring expense
    private long createExpenseFromRecurring(SQLiteDatabase db, RecurringExpense recurringExpense) {
        // Get current date
        Date currentDate = new Date(); // Creates a Date object with the current date/time

        ContentValues values = new ContentValues();
        values.put("userID", recurringExpense.getUserID());
        values.put("categoryID", recurringExpense.getCategoryID());
        values.put("description", recurringExpense.getDescription() + " (Recurring)"); // Mark as recurring
        values.put("amount", recurringExpense.getAmount());

        // Format the date for SQLite storage (YYYY-MM-DD format)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        values.put("date", formattedDate);

        // No need to put expenseID as it's autoincrement

        return db.insert(SQLiteHelper.TABLE_EXPENSE, null, values);
    }


    // Method to add a recurring expense
// Method to add a recurring expense and automatically create an expense entry
    public long insert(SQLiteDatabase db, RecurringExpense recurringExpense) {
        long result = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("userID", recurringExpense.getUserID());
            values.put("categoryID", recurringExpense.getCategoryID());
            values.put("description", recurringExpense.getDescription());
            values.put("amount", recurringExpense.getAmount());
            values.put("month", recurringExpense.getMonth());
            values.put("year", recurringExpense.getYear());
            values.put("recurrenceFrequency", recurringExpense.getRecurrenceFrequency());
            values.put("isActive", recurringExpense.isActive() ? 1 : 0);

            // Insert the recurring expense
            result = db.insert(SQLiteHelper.TABLE_RECURRING_EXPENSE, null, values);

            // If insertion was successful and the recurring expense is active
            if (result != -1 && recurringExpense.isActive()) {
                // Set the ID of the newly created recurring expense
                recurringExpense.setRecurringExpenseID((int) result);

                // Create the first expense entry for current month
                Calendar today = Calendar.getInstance();
                int currentMonth = today.get(Calendar.MONTH) + 1; // 0-indexed to 1-indexed
                int currentYear = today.get(Calendar.YEAR);

                // Only create expense if the recurring expense is for the current month/year or in the future
                if ((recurringExpense.getYear() > currentYear) ||
                        (recurringExpense.getYear() == currentYear && recurringExpense.getMonth() >= currentMonth)) {
                    createExpenseFromRecurring(db, recurringExpense);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("RecurringExpenseRepo", "Error creating recurring expense: " + e.getMessage());
            result = -1;
        } finally {
            db.endTransaction();
        }

        return result;
    }
    // Method to get all recurring expenses for a user
    public List<RecurringExpense> getAllRecurringExpenses(SQLiteDatabase db, int userId) {
        List<RecurringExpense> recurringExpenses = new ArrayList<>();
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_RECURRING_EXPENSE + " WHERE userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("recurringExpenseID"));
                int user = cursor.getInt(cursor.getColumnIndexOrThrow("userID"));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
                String recurrenceFrequency = cursor.getString(cursor.getColumnIndexOrThrow("recurrenceFrequency"));
                // Get isActive from the database and convert to boolean
                boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1;

                RecurringExpense expense = new RecurringExpense(id, user, categoryId, description, amount, month, year, recurrenceFrequency, isActive);
                recurringExpenses.add(expense);

            } while (cursor.moveToNext());
            cursor.close();
        }
        return recurringExpenses;
    }

    // Method to update a recurring expense.
    public int update(SQLiteDatabase db, RecurringExpense recurringExpense) {
        ContentValues values = new ContentValues();
        values.put("userID", recurringExpense.getUserID());
        values.put("categoryID", recurringExpense.getCategoryID());
        values.put("description", recurringExpense.getDescription());
        values.put("amount", recurringExpense.getAmount());
        values.put("month", recurringExpense.getMonth());
        values.put("year", recurringExpense.getYear());
        values.put("recurrenceFrequency", recurringExpense.getRecurrenceFrequency());
        // Update isActive
        values.put("isActive", recurringExpense.isActive() ? 1 : 0); // Convert boolean to int

        String selection = "recurringExpenseID = ?";
        String[] selectionArgs = { String.valueOf(recurringExpense.getRecurringExpenseID()) };

        return db.update(SQLiteHelper.TABLE_RECURRING_EXPENSE, values, selection, selectionArgs);
    }

    public RecurringExpense getRecurringExpenseById(SQLiteDatabase db, int recurringExpenseId) {
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_RECURRING_EXPENSE + " WHERE recurringExpenseID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(recurringExpenseId)});

        RecurringExpense expense = null;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("recurringExpenseID"));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("userID"));
            int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
            int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
            String recurrenceFrequency = cursor.getString(cursor.getColumnIndexOrThrow("recurrenceFrequency"));
            boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1;

            expense = new RecurringExpense(id, userId, categoryId, description, amount, month, year, recurrenceFrequency, isActive);
            cursor.close();
        }
        return expense;
    }


    // Check for overlapping recurring expenses
    public boolean isRecurringExpenseOverlapping(SQLiteDatabase db, RecurringExpense newExpense) {
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_RECURRING_EXPENSE +
                " WHERE userID = ? AND description = ? AND month = ? AND year = ? AND recurringExpenseID != ?";
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(newExpense.getUserID()),
                newExpense.getDescription(), // Kiểm tra description
                String.valueOf(newExpense.getMonth()),
                String.valueOf(newExpense.getYear()),
                String.valueOf(newExpense.getRecurringExpenseID()) // Exclude the current expense (for updates)
        });

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }


    // Method to *completely* delete a recurring expense AND its associated expenses
    public void deleteRecurringExpenseAndExpenses(SQLiteDatabase db, int recurringExpenseId) {
        db.beginTransaction();
        try {
            // 1. Get the recurring expense details (userID, categoryID, month, year)
            RecurringExpense recurringExpense = getRecurringExpenseById(db, recurringExpenseId);
            if (recurringExpense == null) {
                return; // Or throw an exception, depending on your error handling
            }

            // 2. Delete associated expenses
            String expenseSelection = "userID = ? AND categoryID = ? AND strftime('%Y-%m', date) = ?";
            Calendar calendar = Calendar.getInstance(); // Use calendar
            calendar.clear();
            calendar.set(Calendar.MONTH, recurringExpense.getMonth() -1); // get month
            calendar.set(Calendar.YEAR, recurringExpense.getYear()); //get year
            String yearMonth = dateFormat.format(calendar.getTime()); // Format date

            String[] expenseSelectionArgs = {
                    String.valueOf(recurringExpense.getUserID()),
                    String.valueOf(recurringExpense.getCategoryID()),
                    yearMonth // Use format string
            };

            //Use delete method from SQLiteDatabase to delete related expense
            db.delete(SQLiteHelper.TABLE_EXPENSE, expenseSelection, expenseSelectionArgs);

            // 3. Delete the recurring expense itself
            String recurringSelection = "recurringExpenseID = ?";
            String[] recurringSelectionArgs = {String.valueOf(recurringExpenseId)};
            db.delete(SQLiteHelper.TABLE_RECURRING_EXPENSE, recurringSelection, recurringSelectionArgs);

            db.setTransactionSuccessful();
        }
        catch (Exception e){
            Log.e("DELETE RE", "deleteRecurringExpenseAndExpenses: " + e.getMessage() );
        }
        finally {
            db.endTransaction();
        }
    }
}