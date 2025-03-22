package com.project.cem.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;
import 	android.content.DialogInterface; //add
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import  com.project.cem.R;
import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;


public class NotificationHelper {

    private static final String CHANNEL_ID = "budget_channel";
    private static final int NOTIFICATION_ID = 1;
    private Context context;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext(); // Use application context
        createNotificationChannel(); // Create the channel on initialization
    }
    public void setRequestPermissionLauncher(ActivityResultLauncher<String> requestPermissionLauncher){
        this.requestPermissionLauncher = requestPermissionLauncher; // Use setter
    }

    public void showBudgetExceededNotification(String message) {
        checkAndRequestNotificationPermission(message);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Channel";
            String description = "Channel for budget notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void checkAndRequestNotificationPermission(String message) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.baseline_circle_notifications_24) // Replace with your icon
                        .setContentTitle("Budget Exceeded!")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(NOTIFICATION_ID, builder.build());

            } else if (getActivity(context)!=null && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(context), Manifest.permission.POST_NOTIFICATIONS)) { // Changed
                new AlertDialog.Builder(context)
                        .setTitle("Notification Permission Needed")
                        .setMessage("This app needs permission to send budget notifications.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            if(requestPermissionLauncher!=null) {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                            }

                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                if(requestPermissionLauncher!=null) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }

        }else{
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_circle_notifications_24) // Replace with your icon
                    .setContentTitle("Budget Exceeded!")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    // Helper method to get Activity from Context (for shouldShowRequestPermissionRationale)
    private static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null; // Should not happen if used correctly
    }

}