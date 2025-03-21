package com.project.cem.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    // SQL to create tables
    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    "userID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email VARCHAR(255) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "role VARCHAR(50) DEFAULT 'User'" +
                    ")";

    private static final String CREATE_TABLE_EXPENSE_CATEGORY =
            "CREATE TABLE " + TABLE_EXPENSE_CATEGORY + " (" +
                    "categoryID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userID INTEGER, " +
                    "categoryName VARCHAR(255), " +
                    "FOREIGN KEY (userID) REFERENCES " + TABLE_USER + "(userID) ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_TABLE_EXPENSE =
            "CREATE TABLE " + TABLE_EXPENSE + " (" +
                    "expenseID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userID INTEGER, " +
                    "categoryID INTEGER, " +
                    "description TEXT, " +
                    "amount DECIMAL(10,2), " +
                    "date DATE, " +
                    "FOREIGN KEY (userID) REFERENCES " + TABLE_USER + "(userID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (categoryID) REFERENCES " + TABLE_EXPENSE_CATEGORY + "(categoryID) ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_TABLE_BUDGET =
            "CREATE TABLE " + TABLE_BUDGET + " (" +
                    "budgetID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userID INTEGER, " +
                    "categoryID INTEGER, " +
                    "amount DECIMAL(10,2), " +
                    "startDate DATE, " +
                    "endDate DATE, " +
                    "FOREIGN KEY (userID) REFERENCES " + TABLE_USER + "(userID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (categoryID) REFERENCES " + TABLE_EXPENSE_CATEGORY + "(categoryID) ON DELETE CASCADE" +
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
                    "FOREIGN KEY (userID) REFERENCES " + TABLE_USER + "(userID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (categoryID) REFERENCES " + TABLE_EXPENSE_CATEGORY + "(categoryID) ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_TABLE_EXPENSE_REPORT =
            "CREATE TABLE " + TABLE_EXPENSE_REPORT + " (" +
                    "reportID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userID INTEGER, " +
                    "generatedDate DATETIME, " +
                    "reportData TEXT, " +
                    "FOREIGN KEY (userID) REFERENCES " + TABLE_USER + "(userID) ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_INDEX_EXPENSE_USER =
            "CREATE INDEX idx_expense_user ON " + TABLE_EXPENSE + "(userID);";
    private static final String CREATE_INDEX_EXPENSE_CATEGORY =
            "CREATE INDEX idx_expense_category ON " + TABLE_EXPENSE + "(categoryID);";
    private static final String CREATE_INDEX_BUDGET_USER =
            "CREATE INDEX idx_budget_user ON " + TABLE_BUDGET + "(userID);";
    private static final String CREATE_INDEX_RECURRING_EXPENSE_USER =
            "CREATE INDEX idx_recurring_expense_user ON " + TABLE_RECURRING_EXPENSE + "(userID);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_EXPENSE_CATEGORY);
        db.execSQL(CREATE_TABLE_EXPENSE);
        db.execSQL(CREATE_TABLE_BUDGET);
        db.execSQL(CREATE_TABLE_RECURRING_EXPENSE);
        db.execSQL(CREATE_TABLE_EXPENSE_REPORT);

        // Thêm chỉ mục
        db.execSQL(CREATE_INDEX_EXPENSE_USER);
        db.execSQL(CREATE_INDEX_EXPENSE_CATEGORY);
        db.execSQL(CREATE_INDEX_BUDGET_USER);
        db.execSQL(CREATE_INDEX_RECURRING_EXPENSE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_EXPENSE + " ADD COLUMN notes TEXT;");
        }
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USER,
                new String[]{"email"},
                "email = ?",
                new String[]{email},
                null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        } finally {
            db.close();
        }
    }
}
