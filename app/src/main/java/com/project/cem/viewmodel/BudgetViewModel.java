package com.project.cem.viewmodel;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;

import com.project.cem.model.User;
import com.project.cem.repository.BudgetRepository;

import java.util.Date;
import java.util.List;
import  com.project.cem.R;

import android.Manifest;
import com.project.cem.utils.UserPreferences;


public class BudgetViewModel extends AndroidViewModel {

    private BudgetRepository budgetRepository;
    private MutableLiveData<List<Budget>> allBudgets = new MutableLiveData<>();
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();;
    private MutableLiveData<List<ExpenseCategory>> allCategories = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private SQLiteDatabase db;

    public BudgetViewModel(Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        //getAllCategories need SQLiteDatabase object

        db = budgetRepository.getWritableDatabase();
        loadCategories();
        loadBudgets();
    }
    public void loadCategories() {
        new Thread(() -> {
            try {
                List<ExpenseCategory> categories = budgetRepository.getAllCategories(db);
                new Handler(Looper.getMainLooper()).post(() -> {
                    allCategories.setValue(categories);
                });
            } finally {
            }
        }).start();
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void loadBudgets() {
        new Thread(() -> {
            try {
                List<Budget> budgets = budgetRepository.getAllBudgets(db);
                new Handler(Looper.getMainLooper()).post(() -> {
                    allBudgets.setValue(budgets);
                });
            } finally {
            }
        }).start();
    }
    public void insert(Budget budget) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        new Thread(() -> {
            try {
                if (isBudgetOverlapping(db, budget)) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("A budget for this category and time period already exists.");
                    });
                    return;
                }

                long result = budgetRepository.insert(db, budget);
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    if (result != -1) {
                        messageLiveData.setValue("Budget added successfully!");
                        refreshBudgets();
                        checkBudgets();

                    } else {
                        errorMessage.setValue("Failed to add budget.");
                    }
                });
            } finally {
            }
        }).start();
    }
    public void update(Budget budget) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        new Thread(() -> {
            try {
                if (isBudgetOverlapping(db, budget)) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("A budget for this category and time period already exists.");
                    });
                    return;
                }

                int rowsAffected = budgetRepository.update(db, budget); // Truyền db

                new Handler(Looper.getMainLooper()).post(() -> {
                    //Xóa delay
                    isLoading.setValue(false);
                    if (rowsAffected > 0) {
                        messageLiveData.setValue("Budget updated successfully!");
                        refreshBudgets();
                        checkBudgets();
                    } else {
                        errorMessage.setValue("Failed to update budget.");
                    }
                });
            } finally {
            }
        }).start();
    }

    public void refreshBudgets() {
        new Thread(() -> {
            try {
                List<Budget> updatedList = budgetRepository.getAllBudgets(db);
                new Handler(Looper.getMainLooper()).post(() -> allBudgets.setValue(updatedList));
            } finally {
            }
        }).start();
    }

    private boolean isBudgetOverlapping(SQLiteDatabase db, Budget newBudget) {
        User currentUser = UserPreferences.getUser(getApplication());
        if(currentUser == null) return false;

        List<Budget> existingBudgets = budgetRepository.getAllBudgets(db);
        if(existingBudgets == null) return false;

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
        User currentUser = UserPreferences.getUser(getApplication());
        if (currentUser == null) {
            return;
        }
        int userId = currentUser.getUserID();
        // Không mở database ở đây nữa, dùng this.db
        try {
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
        } finally {
            //Không đóng
        }
    }
    private String getCategoryName(SQLiteDatabase db, int categoryId) {

        List<ExpenseCategory> allCategories =  budgetRepository.getAllCategories(db);
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
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//
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
//
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
            db.close(); // Đóng database khi ViewModel bị hủy
        }
    }

}