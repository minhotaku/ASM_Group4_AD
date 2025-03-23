package com.project.cem.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class SampleDataInitializer {
    private final SQLiteHelper dbHelper;
    private final Context context;
    private static volatile boolean isDataInitialized = false;
    private static final String PREFS_NAME = "SampleDataPrefs";
    private static final String KEY_INITIALIZED = "isInitialized";

    public SampleDataInitializer(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);
    }

    public synchronized void initializeSampleData() {
        boolean isInitializedInPrefs = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_INITIALIZED, false);

        if (!isInitializedInPrefs && !isDataInitialized) {
            try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                db.beginTransaction();
                try {
                    // 1. Dữ liệu mẫu cho bảng User (không thay đổi)
                    db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_USER +
                            " (email, password, role) VALUES " +
                            "('test@example.com', 'password123', 'user'), " +
                            "('admin@example.com', 'admin123', 'admin'), " +
                            "('user2@example.com', 'pass456', 'user'), " +
                            "('user3@example.com', 'secure789', 'user'), " +
                            "('admin2@example.com', 'admin2025', 'admin')");

                    // 2. Dữ liệu mẫu cho bảng ExpenseCategory (không thay đổi)
                    db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_CATEGORY +
                            " (userID, categoryName) VALUES " +
                            "(1, 'Food'), " +
                            "(1, 'Transportation'), " +
                            "(2, 'Entertainment'), " +
                            "(3, 'Housing'), " +
                            "(3, 'Utilities'), " +
                            "(4, 'Health'), " +
                            "(5, 'Education')");

                    // 3. Dữ liệu mẫu cho bảng Expense (không thay đổi)
                    db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE +
                            " (userID, categoryID, description, amount, date) VALUES " +
                            "(1, 1, 'Lunch at Cafe', 15.50, '2025-03-20'), " +
                            "(1, 2, 'Bus ticket', 2.75, '2025-03-20'), " +
                            "(2, 3, 'Movie night', 12.00, '2025-03-19'), " +
                            "(3, 4, 'Rent payment', 800.00, '2025-03-01'), " +
                            "(4, 5, 'University tuition', 1500.00, '2025-02-25'), " +
                            "(5, 6, 'Medical checkup', 120.00, '2025-03-10')");

                    // 4. Dữ liệu mẫu cho bảng Budget (không thay đổi)
                    db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_BUDGET +
                            " (userID, categoryID, amount, month, year) VALUES " +
                            "(1, 1, 200.00, 3, 2025), " +
                            "(1, 2, 100.00, 3, 2025), " +
                            "(2, 3, 150.00, 3, 2025), " +
                            "(3, 4, 1000.00, 3, 2025), " +
                            "(4, 5, 2000.00, 3, 2025), " +
                            "(5, 6, 500.00, 3, 2025)");

                    // 5. Dữ liệu mẫu cho bảng RecurringExpense (thêm isActive)
                    db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_RECURRING_EXPENSE +
                            " (userID, categoryID, description, amount, month, year, recurrenceFrequency, isActive) VALUES " +
                            "(1, 5, 'Electricity bill', 50.00, 3, 2025, 'Month', 1), " +
                            "(2, 4, 'Monthly rent', 800.00, 3, 2025, 'Month', 1), " +
                            "(3, 2, 'Monthly bus pass', 30.00, 3, 2025, 'Month', 1), " +
                            "(4, 5, 'Annual education fee', 1500.00, 3, 2025, 'Year', 1), " +
                            "(5, 6, 'Health insurance', 1200.00, 3, 2025, 'Year', 0)"); // Ví dụ: 1 record không active

                    // 6. Dữ liệu mẫu cho bảng ExpenseReport (không thay đổi)
                    db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_REPORT +
                            " (userID, generatedDate, reportData) VALUES " +
                            "(1, '2025-03-20 10:00:00', 'Monthly expense report for March'), " +
                            "(2, '2025-03-19 15:30:00', 'Weekly expense summary'), " +
                            "(3, '2025-03-25 09:00:00', 'Rent and utilities report'), " +
                            "(4, '2025-03-15 14:30:00', 'University expenses'), " +
                            "(5, '2025-03-28 18:00:00', 'Health and insurance expenses')");

                    // Đánh dấu đã khởi tạo trong SharedPreferences
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_INITIALIZED, true)
                            .apply();

                    isDataInitialized = true;
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        }
    }

    public boolean isDataInitialized() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_INITIALIZED, false) || isDataInitialized;
    }
}