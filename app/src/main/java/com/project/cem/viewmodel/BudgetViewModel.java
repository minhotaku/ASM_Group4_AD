package com.project.cem.viewmodel;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.BudgetRepository;
import com.project.cem.utils.UserPreferences;

import java.util.Date;
import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository budgetRepository;
    private final MutableLiveData<List<Budget>> allBudgets = new MutableLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ExpenseCategory>> allCategories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final SQLiteDatabase db;

    public BudgetViewModel(Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        db = budgetRepository.getWritableDatabase();
        loadCategories();
        loadBudgets();
    }

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
            if (isBudgetOverlapping(db, budget)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("A budget for this category and time period already exists.");
                });
                return;
            }

            boolean success = budgetRepository.insert(db, budget);

            new Handler(Looper.getMainLooper()).post(() -> {
                isLoading.setValue(false);
                if (success) {
                    messageLiveData.setValue("Budget added successfully!");
                    errorMessage.setValue(null);  //Clear error message
                    refreshBudgets();
                    checkBudgets();
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
            if (isBudgetOverlapping(db, budget)) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("A budget for this category and time period already exists.");
                });
                return;
            }

            int rowsAffected = budgetRepository.update(db, budget);

            new Handler(Looper.getMainLooper()).post(() -> {
                isLoading.setValue(false);
                if (rowsAffected > 0) {
                    messageLiveData.setValue("Budget updated successfully!");
                    errorMessage.setValue(null); // Clear error on success
                    refreshBudgets();
                    checkBudgets();
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

    private boolean isBudgetOverlapping(SQLiteDatabase db, Budget newBudget) {
        com.project.cem.model.User currentUser = UserPreferences.getUser(getApplication());
        if (currentUser == null) return false;

        List<Budget> existingBudgets = budgetRepository.getAllBudgets(db);
        if (existingBudgets == null) return false;

        for (Budget existingBudget : existingBudgets) {
            if (newBudget.getBudgetID() == existingBudget.getBudgetID()) {
                continue;
            }

            if (newBudget.getCategoryID() == existingBudget.getCategoryID()) {
                if (isOverlap(newBudget.getStartDate(), newBudget.getEndDate(), existingBudget.getStartDate(), existingBudget.getEndDate())) {
                    return true;
                }
            }
        }

        return false;
    }
    private boolean isOverlap(Date start1, Date end1, Date start2, Date end2) {
        return start1.compareTo(end2) < 0 && start2.compareTo(end1) < 0;
    }

    public void checkBudgets() {

        com.project.cem.model.User currentUser = UserPreferences.getUser(getApplication());
        if (currentUser == null) {
            return;
        }
        int userId = currentUser.getUserID();
        new Thread(()->{
            List<Budget> budgets = budgetRepository.getAllBudgets(db);
            if (budgets == null) {
                return;
            }

            for (Budget budget : budgets) {
                Date startDate = budget.getStartDate();
                Date endDate = budget.getEndDate();
                double totalExpenses = budgetRepository.getTotalExpensesForCategory(db, userId, budget.getCategoryID(), startDate, endDate);

                if (totalExpenses > budget.getAmount()) {
                    String message = "You have exceeded your budget for category: " + getCategoryName(db, budget.getCategoryID()) + "!";
                    sendNotification(message);
                }
            }
        }).start();
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

    private void sendNotification(String message) {
//        createNotificationChannel();
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication(), CHANNEL_ID)
//                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
//                .setContentTitle("Budget Exceeded!")
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
//        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
//            return;
//        }
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private static final String CHANNEL_ID = "budget_channel";
    private static final int NOTIFICATION_ID = 1;

    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Budget Channel";
//            String description = "Channel for budget notifications";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            NotificationManager notificationManager = getApplication().getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
    }
    public LiveData<List<ExpenseCategory>> getAllCategories() {
        return allCategories;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}