package com.project.cem.repository;
import android.database.sqlite.SQLiteDatabase;
import com.project.cem.utils.SQLiteHelper;
import android.content.ContentValues;
import android.content.Context;
import com.project.cem.model.ExpenseCategory;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoryRepository {
    private SQLiteHelper dbHelper;
    public ExpenseCategoryRepository(Context context) {
        dbHelper = new SQLiteHelper(context);

    }

    public void addExpenseCategory(String categoryName, int userID ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("categoryName", categoryName);
        values.put("userID", userID);
        db.insert(SQLiteHelper.TABLE_EXPENSE_CATEGORY, null, values);
        db.close();
    }

    public void deleteExpenseCategory(int categoryID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(SQLiteHelper.TABLE_EXPENSE_CATEGORY, "categoryID = ?", new String[]{String.valueOf(categoryID)});

    }

    public List<ExpenseCategory> getAllCategories(int userID){
        List<ExpenseCategory> expenseCategories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_EXPENSE_CATEGORY + " WHERE userID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userID)});
        if (cursor.moveToFirst()) {
            do {
                ExpenseCategory expenseCategory = new ExpenseCategory();
                expenseCategory.setCategoryID(cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")));
                expenseCategory.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("categoryName")));
                expenseCategory.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow("userID")));

                expenseCategories.add(expenseCategory);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenseCategories;
    }

}
