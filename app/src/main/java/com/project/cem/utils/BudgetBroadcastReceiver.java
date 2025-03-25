package com.project.cem.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.project.cem.R;
import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.User;
import com.project.cem.repository.BudgetRepository;
import com.project.cem.repository.SpendingOverviewRepository;
import com.project.cem.ui.MainActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BudgetBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "budget_channel_01";
    private static final int NOTIFICATION_ID = 101;
    private static final String PREFS_NAME = "budget_prefs";
    private static final String KEY_NOTIFIED_BUDGETS = "notified_budgets"; // Key for SharedPreferences

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BudgetBroadcastReceiver", "onReceive triggered");

        User currentUser = UserPreferences.getUser(context);
        if (currentUser == null) {
            Log.d("BudgetBroadcastReceiver", "No user logged in");
            return;
        }

        BudgetRepository budgetRepository = new BudgetRepository(context);
        SpendingOverviewRepository spendingOverviewRepository = new SpendingOverviewRepository(context);

        List<Budget> budgets = budgetRepository.getAllBudgets(budgetRepository.getReadableDatabase());
        if (budgets.isEmpty()) {
            Log.d("BudgetBroadcastReceiver", "No budgets found");
            budgetRepository.getReadableDatabase().close();
            return;
        }

        List<ExpenseCategory> allCategories = budgetRepository.getAllCategories(budgetRepository.getReadableDatabase());

        createNotificationChannel(context);


        handleBudgetNotifications(context, budgets, spendingOverviewRepository, allCategories); // Gộp chung

        budgetRepository.getReadableDatabase().close();
        spendingOverviewRepository.closeDbForBroadcastReceiver();
    }

    private void handleBudgetNotifications(Context context, List<Budget> budgets, SpendingOverviewRepository spendingOverviewRepository, List<ExpenseCategory> allCategories) {
        // Lấy danh sách các budget ID đã được thông báo từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> notifiedBudgets = prefs.getStringSet(KEY_NOTIFIED_BUDGETS, new HashSet<>());

        StringBuilder exceededBudgetsMessage = new StringBuilder(); // Chỉ thông báo các khoản vượt

        for (Budget budget : budgets) {
            double totalExpenses = spendingOverviewRepository.getTotalExpensesForCategory(
                    spendingOverviewRepository.getDbForBroadcastReceiver(),
                    UserPreferences.getUser(context).getUserID(), //Sửa ở đây.
                    budget.getCategoryID(),
                    budget.getMonth(),
                    budget.getYear()
            );

            String categoryName = getCategoryName(budget.getCategoryID(), allCategories);

            String budgetKey = String.valueOf(budget.getBudgetID()); // Key để lưu trạng thái thông báo

            if (totalExpenses > budget.getAmount()) {
                // Vượt quá
                if (!notifiedBudgets.contains(budgetKey)) { // Kiểm tra đã thông báo vượt quá chưa
                    exceededBudgetsMessage.append(String.format("• %s\n", categoryName)); // Thêm vào string builder

                    // Lưu trạng thái đã thông báo (thêm vào set)
                    notifiedBudgets.add(budgetKey);
                    prefs.edit().putStringSet(KEY_NOTIFIED_BUDGETS, notifiedBudgets).apply();
                }
            } else {
                //Xóa các key đã thông báo
                notifiedBudgets.remove(budgetKey);
                prefs.edit().putStringSet(KEY_NOTIFIED_BUDGETS, notifiedBudgets).apply();
            }
        }

        // Nếu có bất kỳ budget nào bị vượt, hiển thị thông báo
        if (exceededBudgetsMessage.length() > 0) {
            String title = "Budget Exceeded!";
            String message = "You have exceeded your budget for the following categories:\n" + exceededBudgetsMessage.toString();
            showNotification(context, title, message);
        }
    }


    private String getCategoryName(int categoryId, List<ExpenseCategory> categories) {
        for (ExpenseCategory ec : categories) {
            if (ec.getCategoryID() == categoryId) {
                return ec.getCategoryName();
            }
        }
        return "Unknown Category";
    }


    private void showNotification(Context context, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            Log.d("BudgetBroadcastReceiver", "About to show notification: " + message);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

            // Kiểm tra và hiển thị thông báo nếu "notification dot" được bật
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
                if (channel != null && channel.canShowBadge()) { // canShowBadge() kiểm tra "notification dot"
                    showNotificationDotWarning(context);
                }
            }

        } catch (SecurityException e) {
            Log.e("BudgetBroadcastReceiver", "SecurityException: " + e.getMessage());
            Toast.makeText(context, "Notification permission is required. Please enable it in app settings.", Toast.LENGTH_LONG).show();

            Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            settingsIntent.setData(uri);
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(settingsIntent);
            } catch (ActivityNotFoundException e1) {
                Log.e("BudgetBroadcastReceiver", "ActivityNotFoundException: " + e1.getMessage());
            }
        }
    }

    private void showNotificationDotWarning(Context context) {
        Toast.makeText(context, "Please turn off 'Allow notification dot' in settings for better notification experience.", Toast.LENGTH_LONG).show();
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Notifications";
            String description = "Notifications for budget overspending";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}