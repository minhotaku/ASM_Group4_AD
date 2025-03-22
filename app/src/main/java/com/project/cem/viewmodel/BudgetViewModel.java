package com.project.cem.viewmodel;

import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.User;
import com.project.cem.repository.BudgetRepository;
import com.project.cem.utils.NotificationHelper;
import com.project.cem.utils.UserPreferences;

import java.util.Date;
import java.util.List;
import  com.project.cem.R;

import android.Manifest;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository budgetRepository;
    private final MutableLiveData<List<Budget>> allBudgets = new MutableLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ExpenseCategory>> allCategories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final SQLiteDatabase db;
    //Added
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable budgetCheckRunnable; // Add this
    private static final long CHECK_INTERVAL = 60000; // Check every 60 seconds (adjust as needed)
    private NotificationHelper notificationHelper;  // Add this
    private ActivityResultLauncher<String> requestPermissionLauncher; // Moved here

    public BudgetViewModel(Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        db = budgetRepository.getWritableDatabase();
        // Initialize NotificationHelper
        notificationHelper = new NotificationHelper(application);
        loadCategories();
        loadBudgets();
        startBudgetChecks();
    }
    // Changed to public.  Needs to be called from Fragment.
    public  void setRequestPermissionLauncher(ActivityResultLauncher<String> requestPermissionLauncher){
        this.requestPermissionLauncher = requestPermissionLauncher; // Use setter
        //Pass the permission to notification helper.
        notificationHelper.setRequestPermissionLauncher(requestPermissionLauncher);
    }

    // ... (rest of your methods - loadCategories, getAllBudgets, getMessageLiveData, etc.) ...
    public void loadCategories() {
        new Thread(() -> {
            List<ExpenseCategory> categories = budgetRepository.getAllCategories(db);
            new Handler(Looper.getMainLooper()).post(() -> {
                allCategories.setValue(categories);
            });
        }).start();
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void clearMessage() {
        messageLiveData.setValue(null);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void loadBudgets() {
        new Thread(() -> {
            List<Budget> budgets = budgetRepository.getAllBudgets(db);
            new Handler(Looper.getMainLooper()).post(() -> {
                allBudgets.setValue(budgets);
            });
        }).start();
    }

    public void insert(Budget budget) {
        isLoading.postValue(true);
        errorMessage.postValue(null); // Clear previous error
        messageLiveData.postValue(null);

        new Thread(() -> {
            // Check for overlapping budgets (same user, category, month, year)
            if (isBudgetOverlapping(db, budget)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("A budget for this category and month/year already exists.");
                });
                return; // Exit if overlapping
            }


            boolean success = budgetRepository.insert(db, budget);

            new Handler(Looper.getMainLooper()).post(() -> {
                isLoading.setValue(false);
                if (success) {
                    messageLiveData.setValue("Budget added successfully!");
                    errorMessage.setValue(null);  //Clear error message
                    refreshBudgets();
                    //checkBudgets(); No longer call here, now we use handler.
                } else {
                    errorMessage.setValue("Failed to add budget.");
                }
            });
        }).start();
    }


    public void update(Budget budget) {
        isLoading.postValue(true);
        errorMessage.postValue(null);  //Clear error
        messageLiveData.postValue(null);

        new Thread(() -> {
            // Check for overlapping budgets (excluding the current budget being updated)
            if (isBudgetOverlapping(db, budget)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("A budget for this category and month/year already exists.");
                });
                return; // Exit if overlapping
            }

            int rowsAffected = budgetRepository.update(db, budget);

            new Handler(Looper.getMainLooper()).post(() -> {
                isLoading.setValue(false);
                if (rowsAffected > 0) {
                    messageLiveData.setValue("Budget updated successfully!");
                    errorMessage.setValue(null); // Clear error on success
                    refreshBudgets();
                    // checkBudgets(); No longer call here
                } else {
                    errorMessage.setValue("Failed to update budget.");
                }
            });
        }).start();
    }
    public void refreshBudgets() {
        new Thread(() -> {
            List<Budget> updatedList = budgetRepository.getAllBudgets(db);
            new Handler(Looper.getMainLooper()).post(() -> allBudgets.setValue(updatedList));
        }).start();
    }
    // RE-ADD isBudgetOverlapping (but simplified)
    private boolean isBudgetOverlapping(SQLiteDatabase db, Budget newBudget) {
        com.project.cem.model.User currentUser = UserPreferences.getUser(getApplication());
        if (currentUser == null) return false;

        List<Budget> existingBudgets = budgetRepository.getAllBudgets(db);
        if (existingBudgets == null) return false;

        for (Budget existingBudget : existingBudgets) {
            // IMPORTANT: Exclude the current budget being updated from the check
            if (newBudget.getBudgetID() == existingBudget.getBudgetID()) {
                continue;
            }

            if (newBudget.getUserID() == existingBudget.getUserID() &&
                    newBudget.getCategoryID() == existingBudget.getCategoryID() &&
                    newBudget.getMonth() == existingBudget.getMonth() &&
                    newBudget.getYear() == existingBudget.getYear()) {
                return true; // Overlap found (same user, category, month, year)
            }
        }

        return false; // No overlap
    }
    private String getCategoryName(SQLiteDatabase db, int categoryId) {

        List<ExpenseCategory> allCategories = budgetRepository.getAllCategories(db);
        for (ExpenseCategory category : allCategories) {
            if (category.getCategoryID() == categoryId) {
                return category.getCategoryName();
            }
        }
        return "Unknown Category";
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        stopBudgetChecks(); // Stop the checks when the ViewModel is cleared
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    private void startBudgetChecks() {
        budgetCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // Get the current user
                User currentUser = UserPreferences.getUser(getApplication());
                if (currentUser == null) {
                    return; // No user, don't check
                }
                int userId = currentUser.getUserID();

                // Get all budgets
                List<Budget> budgets = budgetRepository.getAllBudgets(db);
                if (budgets == null) {
                    return;
                }

                // Check each budget
                for (Budget budget : budgets) {
                    double totalExpenses = budgetRepository.getTotalExpensesForCategory(db, userId, budget.getCategoryID(), budget.getMonth(), budget.getYear());

                    if (totalExpenses > budget.getAmount()) {
                        String message = "You have exceeded your budget for category: " + getCategoryName(db, budget.getCategoryID()) + "!";
                        // Use the NotificationHelper to send the notification
                        notificationHelper.showBudgetExceededNotification(message);
                    }
                }

                // Schedule the next check
                handler.postDelayed(this, CHECK_INTERVAL); // Important: re-post the runnable
            }
        };
        handler.post(budgetCheckRunnable); // Start the first check immediately
    }
    private void stopBudgetChecks() {
        if (budgetCheckRunnable != null) {
            handler.removeCallbacks(budgetCheckRunnable); // Remove any pending checks
        }
    }
    public LiveData<List<ExpenseCategory>> getAllCategories() {
        return allCategories;
    }

}