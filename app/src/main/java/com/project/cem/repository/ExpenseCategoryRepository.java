package com.project.cem.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.project.cem.model.ExpenseCategory;
import com.project.cem.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoryRepository {
    private SQLiteHelper dbHelper;

    public ExpenseCategoryRepository(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    // Phương thức mới: Kiểm tra danh mục đã tồn tại chưa
    public boolean isCategoryExist(int userID, String categoryName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists = false;

        String query = "SELECT COUNT(*) FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY +
                " WHERE userID = ? AND LOWER(categoryName) = LOWER(?)";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userID),
                categoryName.toLowerCase()
        });

        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }

        cursor.close();
        db.close();
        return exists;
    }

    // Phương thức mới: Kiểm tra danh mục đã tồn tại chưa (loại trừ ID hiện tại - dùng khi update)
    public boolean isCategoryExistExcludeSelf(int userID, String categoryName, int excludeCategoryID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists = false;

        String query = "SELECT COUNT(*) FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY +
                " WHERE userID = ? AND LOWER(categoryName) = LOWER(?) AND categoryID != ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userID),
                categoryName.toLowerCase(),
                String.valueOf(excludeCategoryID)
        });

        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }

        cursor.close();
        db.close();
        return exists;
    }

    // Các phương thức khác giữ nguyên...
    // Lấy tất cả danh mục chi tiêu của người dùng
    public List<ExpenseCategory> getAllCategories(int userID) {
        List<ExpenseCategory> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(SQLiteHelper.TABLE_EXPENSE_CATEGORY, null, "userID = ?",
                new String[]{String.valueOf(userID)}, null, null, "categoryName ASC");

        if (cursor.moveToFirst()) {
            do {
                ExpenseCategory category = new ExpenseCategory();
                category.setCategoryID(cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")));
                category.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));
                category.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("categoryName")));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    // Lấy danh mục theo ID
    public ExpenseCategory getCategoryById(int categoryID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ExpenseCategory category = null;

        Cursor cursor = db.query(SQLiteHelper.TABLE_EXPENSE_CATEGORY, null, "categoryID = ?",
                new String[]{String.valueOf(categoryID)}, null, null, null);

        if (cursor.moveToFirst()) {
            category = new ExpenseCategory();
            category.setCategoryID(cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")));
            category.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));
            category.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("categoryName")));
        }
        cursor.close();
        db.close();
        return category;
    }

    // Thêm danh mục mới
    public long addCategory(ExpenseCategory category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userID", category.getUserID());
        values.put("categoryName", category.getCategoryName());

        long id = db.insert(SQLiteHelper.TABLE_EXPENSE_CATEGORY, null, values);
        db.close();
        return id;
    }

    // Cập nhật danh mục
    public int updateCategory(ExpenseCategory category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("categoryName", category.getCategoryName());

        int rowsAffected = db.update(SQLiteHelper.TABLE_EXPENSE_CATEGORY, values, "categoryID = ?",
                new String[]{String.valueOf(category.getCategoryID())});
        db.close();
        return rowsAffected;
    }

    // Xóa danh mục
    public int deleteCategory(int categoryID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(SQLiteHelper.TABLE_EXPENSE_CATEGORY, "categoryID = ?",
                new String[]{String.valueOf(categoryID)});
        db.close();
        return rowsAffected;
    }

    // Kiểm tra xem danh mục có chi tiêu nào không
    public boolean categoryHasExpenses(int categoryID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean hasExpenses = false;

        Cursor cursor = db.query(SQLiteHelper.TABLE_EXPENSE, new String[]{"COUNT(*)"},
                "categoryID = ?", new String[]{String.valueOf(categoryID)}, null, null, null);

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            hasExpenses = count > 0;
        }
        cursor.close();
        db.close();
        return hasExpenses;
    }

    // Đếm số lượng chi tiêu của mỗi danh mục
    public int getExpenseCountForCategory(int categoryID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;

        Cursor cursor = db.query(SQLiteHelper.TABLE_EXPENSE, new String[]{"COUNT(*)"},
                "categoryID = ?", new String[]{String.valueOf(categoryID)}, null, null, null);

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}