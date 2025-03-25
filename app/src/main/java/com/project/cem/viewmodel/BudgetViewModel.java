package com.project.cem.viewmodel;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // Import Log

import androidx.activity.result.ActivityResultLauncher;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.User;
import com.project.cem.repository.BudgetRepository;
import com.project.cem.utils.UserPreferences;

import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    // Constants (Group together)
    private static final long CHECK_INTERVAL = 60000; // Milliseconds - Check every 60 seconds
    private static final double WARNING_THRESHOLD = 0.8; // 80% -  Warn when 80% of budget is reached

    // Repositories and Database
    private final BudgetRepository budgetRepository;
    private final SQLiteDatabase db;

    // LiveData (For UI updates)
    private final MutableLiveData<List<Budget>> allBudgets = new MutableLiveData<>();          // List of all budgets
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();             // For success/info messages
    private final MutableLiveData<List<ExpenseCategory>> allCategories = new MutableLiveData<>(); // List of all expense categories
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);           // Flag to indicate loading state
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();            // For error messages


    // Notification related
    private ActivityResultLauncher<String> requestPermissionLauncher; // For requesting notification permission

    // Handler for periodic budget checks
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable budgetCheckRunnable;

    public BudgetViewModel(Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        db = budgetRepository.getWritableDatabase();
        loadCategories();  // Load expense categories when ViewModel is created
        loadBudgets();     // Load budgets when ViewModel is created
        startBudgetChecks(); // Start the periodic budget checks
    }

    // LiveData Getters (Used by the UI to observe data changes)
    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }
    public LiveData<String> getErrorMessage(){
        return  errorMessage;
    }

    public LiveData<List<ExpenseCategory>> getAllCategories() {
        return allCategories;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    // Clear LiveData values (Used to reset messages after they've been displayed)
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void clearMessage() {
        messageLiveData.setValue(null);
    }

    // Load data (Called to initially load data and to refresh)
    public void loadBudgets() {
        new Thread(() -> {
            List<Budget> budgets = budgetRepository.getAllBudgets(db);
            handler.post(() -> allBudgets.setValue(budgets)); // Update LiveData on the main thread
        }).start();
    }

    public void loadCategories() {
        new Thread(() -> {
            List<ExpenseCategory> categories = budgetRepository.getAllCategories(db);
            handler.post(() -> allCategories.setValue(categories)); // Update LiveData on the main thread
        }).start();
    }

    // Budget operations (Insert, Update, Delete - though you don't have delete for Budget)
    public void insert(Budget budget) {
        isLoading.postValue(true); // Set loading state to true
        errorMessage.postValue(null); // Clear any previous error message
        messageLiveData.postValue(null); // Clear any previous message

        new Thread(() -> {
            // Check for overlapping budgets (same category, month, year, user)
            if (isBudgetOverlapping(db, budget)) {
                handler.post(() -> {
                    isLoading.setValue(false); // Set loading to false
                    errorMessage.setValue("A budget for this category and month/year already exists."); // Set error message
                });
                return; // Exit if there's an overlap
            }


            boolean success = budgetRepository.insert(db, budget);  // Insert the new budget into the database

            handler.post(() -> {
                isLoading.setValue(false); // Set loading to false
                if (success) {
                    messageLiveData.setValue("Budget added successfully!");  // Set success message
                    errorMessage.setValue(null);  // Clear error message
                    refreshBudgets();      // Reload the budget list
                    loadCategories();    // Reload categories (in case a new category was added)

                } else {
                    errorMessage.setValue("Failed to add budget."); // Set error message
                }
            });
        }).start();
    }


    public void update(Budget budget) {
        isLoading.postValue(true); // Set loading state
        errorMessage.postValue(null);  // Clear error
        messageLiveData.postValue(null); // Clear message

        new Thread(() -> {
            // Check for overlapping budgets (but exclude the budget being updated itself)
            if (isBudgetOverlapping(db, budget)) {
                handler.post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("A budget for this category and month/year already exists.");
                });
                return; // Exit if overlapping
            }

            int rowsAffected = budgetRepository.update(db, budget); // Update the budget in the database

            handler.post(() -> {
                isLoading.setValue(false); // Set loading to false
                if (rowsAffected > 0) {
                    messageLiveData.setValue("Budget updated successfully!"); // Set success message
                    errorMessage.setValue(null); // Clear error message
                    refreshBudgets();     // Reload the budget list
                    loadCategories();   // Reload categories.

                } else {
                    errorMessage.setValue("Failed to update budget."); // Set error message
                }
            });
        }).start();
    }

    // Helper method to check for overlapping budgets
    private boolean isBudgetOverlapping(SQLiteDatabase db, Budget newBudget) {
        User currentUser = UserPreferences.getUser(getApplication());
        if (currentUser == null) return false;  // No user, can't overlap

        List<Budget> existingBudgets = budgetRepository.getAllBudgets(db);
        if (existingBudgets == null) return false; // No budgets, can't overlap

        for (Budget existingBudget : existingBudgets) {
            // IMPORTANT: Exclude the budget *being updated* from the overlap check.
            if (newBudget.getBudgetID() == existingBudget.getBudgetID()) {
                continue; // Skip to the next budget if it's the one being updated
            }

            // Check if user, category, month, and year are the same
            if (newBudget.getUserID() == existingBudget.getUserID() &&
                    newBudget.getCategoryID() == existingBudget.getCategoryID() &&
                    newBudget.getMonth() == existingBudget.getMonth() &&
                    newBudget.getYear() == existingBudget.getYear()) {
                return true; // Overlap found!
            }
        }

        return false; // No overlap found
    }

    // Reload the budget list (Called after insert/update)
    public void refreshBudgets() {
        new Thread(() -> {
            List<Budget> updatedList = budgetRepository.getAllBudgets(db);
            handler.post(() -> allBudgets.setValue(updatedList)); // Update LiveData
        }).start();
    }


    // Helper method to get the category name (Used for notifications)
    private String getCategoryName(SQLiteDatabase db, int categoryId) {
        List<ExpenseCategory> allCategories = budgetRepository.getAllCategories(db);
        for (ExpenseCategory category : allCategories) {
            if (category.getCategoryID() == categoryId) {
                return category.getCategoryName();
            }
        }
        return "Unknown Category"; // Return a default value if not found
    }


    // Start the periodic budget checks
    private void startBudgetChecks() {
        budgetCheckRunnable = new Runnable() {
            @Override
            public void run() {
                User currentUser = UserPreferences.getUser(getApplication());
                if (currentUser == null) {
                    return;
                }
                int userId = currentUser.getUserID();

                List<Budget> budgets = budgetRepository.getAllBudgets(db);
                if (budgets == null) {
                    return;
                }

                for (Budget budget : budgets) {
                    double totalExpenses = budgetRepository.getTotalExpensesForCategory(db, userId, budget.getCategoryID(), budget.getMonth(), budget.getYear());

                    // ADD THESE LOG STATEMENTS:
                    Log.d("BudgetCheck", "Category ID: " + budget.getCategoryID() + ", Month/Year: " + budget.getMonth() + "/" + budget.getYear() +
                            ", Budget Amount: " + budget.getAmount() + ", Total Expenses: " + totalExpenses);


                    if (totalExpenses > budget.getAmount()) {
                        String message = "You have exceeded your budget for category: " + getCategoryName(db, budget.getCategoryID()) + "!";
                        Log.d("BudgetCheck", "Notification sent (exceeded): " + message); // Add this log
                    } else if (totalExpenses > budget.getAmount() * WARNING_THRESHOLD) {
                        String message = "You are close to exceeding your budget for category: " + getCategoryName(db, budget.getCategoryID()) + "!";
                        Log.d("BudgetCheck", "Notification sent (warning): " + message); // Add this log
                    }
                }
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(budgetCheckRunnable);
    }


    // Stop the periodic budget checks (Called when ViewModel is no longer needed)
    private void stopBudgetChecks() {
        if (budgetCheckRunnable != null) {
            handler.removeCallbacks(budgetCheckRunnable); // Remove any pending checks
        }
    }


    // Called when the ViewModel is being destroyed
    @Override
    protected void onCleared() {
        super.onCleared();
        stopBudgetChecks(); // Stop the budget checks
        if (db != null && db.isOpen()) {
            db.close();    // Close the database connection
        }
    }
}