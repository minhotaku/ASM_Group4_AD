package com.project.cem.viewmodel;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.RecurringExpense;
import com.project.cem.model.User;
import com.project.cem.repository.BudgetRepository;
import com.project.cem.repository.RecurringExpenseRepository; // Import
import com.project.cem.utils.UserPreferences;

import java.util.Date;
import java.util.List;

public class RecurringExpenseViewModel extends AndroidViewModel {

    private RecurringExpenseRepository recurringExpenseRepository; // Use the repository
    private MutableLiveData<List<RecurringExpense>> allRecurringExpenses = new MutableLiveData<>();
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private SQLiteDatabase db;
    // Add LiveData for categories
    private final MutableLiveData<List<ExpenseCategory>> allCategories = new MutableLiveData<>();
    private final BudgetRepository budgetRepository;


    public RecurringExpenseViewModel(Application application) {
        super(application);
        recurringExpenseRepository = new RecurringExpenseRepository(application);
        budgetRepository = new BudgetRepository(application); // Initialize BudgetRepository
        db = recurringExpenseRepository.getWritableDatabase(); // Open db
        loadCategories(); // Load categories on creation

    }

    // Load recurring expenses
    public LiveData<List<RecurringExpense>> getAllRecurringExpenses(int userID) {
        loadRecurringExpenses(userID); // Load when requested
        return allRecurringExpenses;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }
    public void clearMessage() {
        messageLiveData.setValue(null);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
    public void loadRecurringExpenses(int userID) {
        new Thread(() -> {
            try {
                List<RecurringExpense> recurringExpenses = recurringExpenseRepository.getAllRecurringExpenses(db,userID); // Pass db
                new Handler(Looper.getMainLooper()).post(() -> {
                    allRecurringExpenses.setValue(recurringExpenses);
                });
            } finally {

            }
        }).start();
    }

    // Method to load categories
    public void loadCategories() {
        new Thread(() -> {
            List<ExpenseCategory> categories = budgetRepository.getAllCategories(db);
            new Handler(Looper.getMainLooper()).post(() -> {
                allCategories.setValue(categories);
            });
        }).start();
    }

    // LiveData for categories
    public LiveData<List<ExpenseCategory>> getAllCategories() {
        return allCategories;
    }

    public void insert(RecurringExpense recurringExpense) {
        isLoading.postValue(true);
        errorMessage.postValue(null); // Clear error
        messageLiveData.postValue(null);

        new Thread(() -> {
            // Check for overlapping
            if (recurringExpenseRepository.isRecurringExpenseOverlapping(db, recurringExpense)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("A recurring expense with this description, month, and year already exists."); // Sửa thông báo
                });
                return; //
            }
            try {
                long result = recurringExpenseRepository.insert(db, recurringExpense);
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    if (result != -1) {
                        messageLiveData.setValue("Recurring Expense added successfully!");
                        errorMessage.setValue(null);
                        loadRecurringExpenses(recurringExpense.getUserID()); // Reload
                        loadCategories(); // Reload categories
                    } else {
                        errorMessage.setValue("Failed to add recurring expense.");
                    }
                });
            } finally {

            }
        }).start();
    }

    public void update(RecurringExpense recurringExpense) {
        isLoading.postValue(true);
        errorMessage.postValue(null);  // Clear error
        messageLiveData.postValue(null);

        new Thread(() -> {
            if (recurringExpenseRepository.isRecurringExpenseOverlapping(db, recurringExpense)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("A recurring expense with this description, month, and year already exists."); // Sửa thông báo
                });
                return; // Exit if overlapping
            }
            try {

                int rowsAffected = recurringExpenseRepository.update(db, recurringExpense);

                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    if (rowsAffected > 0) {
                        messageLiveData.setValue("Recurring Expense updated successfully!");
                        errorMessage.setValue(null);  // Clear
                        loadRecurringExpenses(recurringExpense.getUserID()); // Reload
                        loadCategories(); // Reload categories
                    } else {
                        errorMessage.setValue("Failed to update recurring expense.");
                    }
                });
            } finally {
                // Don't close db here
            }

        }).start();
    }
    public void deleteRecurringExpense(int recurringExpenseId) {
        isLoading.postValue(true);
        messageLiveData.postValue(null); // Clear message
        errorMessage.postValue(null);

        new Thread(() -> {
            try {
                recurringExpenseRepository.deleteRecurringExpenseAndExpenses(db, recurringExpenseId); // Call method
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    messageLiveData.setValue("Recurring expense and associated expenses deleted.");
                    // Reload.  Get the current user ID.
                    loadRecurringExpenses(UserPreferences.getUser(getApplication()).getUserID());

                });
            } catch (Exception e) {
                Log.e("RecurringExpenseVM", "Error deleting", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to delete recurring expense: " + e.getMessage());
                });

            }

        }).start();
    }


    // get a recurring expense by ID
    public LiveData<RecurringExpense> getRecurringExpenseById(int recurringExpenseId) {
        MutableLiveData<RecurringExpense> recurringExpenseLiveData = new MutableLiveData<>();
        new Thread(() -> {
            RecurringExpense expense = recurringExpenseRepository.getRecurringExpenseById(db, recurringExpenseId);
            new Handler(Looper.getMainLooper()).post(() -> {
                recurringExpenseLiveData.setValue(expense); // Use setValue
            });
        }).start();
        return recurringExpenseLiveData;
    }

    // Close db
    @Override
    protected void onCleared() {
        super.onCleared();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

}