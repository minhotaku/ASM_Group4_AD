package com.project.cem.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.project.cem.model.User;

public class SQLiteHelper extends SQLiteOpenHelper {

    // Database version and name
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ExpenseManager.db";

    // Table names
    public static final String TABLE_USER = "User";
    public static final String TABLE_EXPENSE = "Expense";
    public static final String TABLE_BUDGET = "Budget";
    public static final String TABLE_RECURRING_EXPENSE = "RecurringExpense";
    public static final String TABLE_EXPENSE_REPORT = "ExpenseReport";
    public static final String TABLE_EXPENSE_CATEGORY = "ExpenseCategory";

    // Common column names
    private static final String COLUMN_ID = "id";

    // SQL to create tables
    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    "userID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email VARCHAR(255), " + // Đổi từ username thành email
                    "password VARCHAR(255), " +
                    "role VARCHAR(50) DEFAULT 'User'" +
                    ")";

    private static final String CREATE_TABLE_EXPENSE =
            "CREATE TABLE " + TABLE_EXPENSE + " (" +
                    "expenseID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userID INTEGER, " +
                    "categoryID INTEGER, " +
                    "description TEXT, " +
                    "amount DECIMAL(10,2), " +
                    "date DATE, " +
                    "FOREIGN KEY (userID) REFERENCES " + TABLE_USER + "(userID), " +
                    "FOREIGN KEY (categoryID) REFERENCES " + TABLE_EXPENSE_CATEGORY + "(categoryID)" +
                    ")";

    private static final String CREATE_TABLE_BUDGET =
            "CREATE TABLE " + TABLE_BUDGET + " (" +
                    "budgetID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "categoryID INTEGER, " +
                    "amount DECIMAL(10,2), " +
                    "startDate DATE, " +
                    "endDate DATE, " +
                    "FOREIGN KEY (categoryID) REFERENCES " + TABLE_EXPENSE_CATEGORY + "(categoryID)" +
                    ")";

    private static final String CREATE_TABLE_RECURRING_EXPENSE =
            "CREATE TABLE " + TABLE_RECURRING_EXPENSE + " (" +
                    "recurringExpenseID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userID INTEGER, " +
                    "categoryID INTEGER, " +
                    "description TEXT, " +
                    "startDate DATE, " +
                    "endDate DATE, " +
                    "recurrenceFrequency VARCHAR(50), " +
                    "FOREIGN KEY (userID) REFERENCES " + TABLE_USER + "(userID), " +
                    "FOREIGN KEY (categoryID) REFERENCES " + TABLE_EXPENSE_CATEGORY + "(categoryID)" +
                    ")";

    private static final String CREATE_TABLE_EXPENSE_REPORT =
            "CREATE TABLE " + TABLE_EXPENSE_REPORT + " (" +
                    "reportID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userID INTEGER, " +
                    "generatedDate DATETIME, " +
                    "reportData TEXT, " +
                    "FOREIGN KEY (userID) REFERENCES " + TABLE_USER + "(userID)" +
                    ")";

    private static final String CREATE_TABLE_EXPENSE_CATEGORY =
            "CREATE TABLE " + TABLE_EXPENSE_CATEGORY + " (" +
                    "categoryID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "categoryName VARCHAR(255)" +
                    ")";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_EXPENSE);
        db.execSQL(CREATE_TABLE_BUDGET);
        db.execSQL(CREATE_TABLE_RECURRING_EXPENSE);
        db.execSQL(CREATE_TABLE_EXPENSE_REPORT);
        db.execSQL(CREATE_TABLE_EXPENSE_CATEGORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_CATEGORY);
        onCreate(db);
    }

    // Method to add a user
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", user.getEmail()); // Đổi từ username thành email
        values.put("password", user.getPassword());
        values.put("role", user.getRole());
        long id = db.insert(TABLE_USER, null, values);
        db.close();
        return id;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "email = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.query(
                TABLE_USER,
                new String[]{"email"}, // Chỉ cần cột email để kiểm tra
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }
}