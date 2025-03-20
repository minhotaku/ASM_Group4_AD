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
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                // 1. Dữ liệu mẫu cho bảng ExpenseCategory
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_CATEGORY +
                        " (categoryName) VALUES ('Food')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_CATEGORY +
                        " (categoryName) VALUES ('Transportation')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_CATEGORY +
                        " (categoryName) VALUES ('Entertainment')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_CATEGORY +
                        " (categoryName) VALUES ('Housing')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_CATEGORY +
                        " (categoryName) VALUES ('Utilities')");

                // 2. Dữ liệu mẫu cho bảng User
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_USER +
                        " (email, password, role) VALUES ('test@example.com', 'password123', 'user')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_USER +
                        " (email, password, role) VALUES ('admin@example.com', 'admin123', 'admin')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_USER +
                        " (email, password, role) VALUES ('user2@example.com', 'pass456', 'user')");

                // 3. Dữ liệu mẫu cho bảng Expense (Đã sửa lỗi "penal amount" thành "amount")
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE +
                        " (userID, categoryID, description, amount, date) " +  // Sửa ở đây
                        "VALUES (1, 1, 'Lunch at Cafe', 15.50, '2025-03-20')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE +
                        " (userID, categoryID, description, amount, date) " +
                        "VALUES (1, 2, 'Bus ticket', 2.75, '2025-03-20')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE +
                        " (userID, categoryID, description, amount, date) " +
                        "VALUES (2, 3, 'Movie night', 12.00, '2025-03-19')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE +
                        " (userID, categoryID, description, amount, date) " +
                        "VALUES (3, 4, 'Rent payment', 800.00, '2025-03-01')");

                // 4. Dữ liệu mẫu cho bảng Budget
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_BUDGET +
                        " (categoryID, amount, startDate, endDate) " +
                        "VALUES (1, 200.00, '2025-03-01', '2025-03-31')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_BUDGET +
                        " (categoryID, amount, startDate, endDate) " +
                        "VALUES (2, 100.00, '2025-03-01', '2025-03-31')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_BUDGET +
                        " (categoryID, amount, startDate, endDate) " +
                        "VALUES (3, 150.00, '2025-03-01', '2025-03-31')");

                // 5. Dữ liệu mẫu cho bảng RecurringExpense
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_RECURRING_EXPENSE +
                        " (userID, categoryID, description, startDate, endDate, recurrenceFrequency) " +
                        "VALUES (1, 5, 'Electricity bill', '2025-03-01', '2025-12-31', 'Monthly')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_RECURRING_EXPENSE +
                        " (userID, categoryID, description, startDate, endDate, recurrenceFrequency) " +
                        "VALUES (2, 4, 'Monthly rent', '2025-03-01', '2025-12-31', 'Monthly')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_RECURRING_EXPENSE +
                        " (userID, categoryID, description, startDate, endDate, recurrenceFrequency) " +
                        "VALUES (3, 2, 'Weekly bus pass', '2025-03-01', '2025-06-30', 'Weekly')");

                // 6. Dữ liệu mẫu cho bảng ExpenseReport
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_REPORT +
                        " (userID, generatedDate, reportData) " +
                        "VALUES (1, '2025-03-20 10:00:00', 'Monthly expense report for March')");
                db.execSQL("INSERT INTO " + SQLiteHelper.TABLE_EXPENSE_REPORT +
                        " (userID, generatedDate, reportData) " +
                        "VALUES (2, '2025-03-19 15:30:00', 'Weekly expense summary')");

                // Đánh dấu đã khởi tạo trong SharedPreferences
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_INITIALIZED, true)
                        .apply();

                isDataInitialized = true;
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    public boolean isDataInitialized() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_INITIALIZED, false) || isDataInitialized;
    }
}