package com.project.cem.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.project.cem.R;
import com.project.cem.databinding.ActivityStudentHomeBinding;
import com.project.cem.ui.budget.BudgetFragment;
import com.project.cem.ui.expense.ExpenseFragment;
import com.project.cem.ui.home.HomeFragment;
import com.project.cem.ui.setting.SettingFragment;
import com.project.cem.utils.BudgetBroadcastReceiver;
import com.project.cem.utils.RecurringExpenseGenerator;
import com.project.cem.viewmodel.BudgetViewModel;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ActivityStudentHomeBinding binding;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Yêu cầu quyền thông báo
        requestNotificationPermission();


        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.expenses:
                    replaceFragment(new ExpenseFragment());
                    break;
                case R.id.budget:
                    replaceFragment(new BudgetFragment());

                    break;
                case R.id.setting:
                    replaceFragment(new SettingFragment());
                    break;
            }
            return true;
        });

        // Kiểm tra xem có phải ngày đầu tháng không và tạo các recurring expense nếu cần
        Calendar today = Calendar.getInstance();
        if (today.get(Calendar.DAY_OF_MONTH) == 1) {
            RecurringExpenseGenerator generator = new RecurringExpenseGenerator(this);
            generator.generateRecurringExpenses();
        }

        // Kích hoạt BroadcastReceiver khi vào app
        Intent intent = new Intent(this, BudgetBroadcastReceiver.class);
        intent.setAction("com.project.cem.ACTION_CHECK_BUDGET");
        sendBroadcast(intent);

        // Lên lịch kiểm tra định kỳ (12 tiếng)
        scheduleBudgetCheck(this);

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

    }
    public static void scheduleBudgetCheck(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BudgetBroadcastReceiver.class); // Sử dụng BroadcastReceiver
        intent.setAction("com.project.cem.ACTION_CHECK_BUDGET"); // Đặt action

        // Sử dụng FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Lặp lại mỗi 12 tiếng
        long intervalMillis = 12 * 60 * 60 * 1000;
        // long intervalMillis = 60 * 1000; // test 60s

        // RTC_WAKEUP: Đánh thức thiết bị nếu đang ngủ
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, pendingIntent);
    }

    // yêu cầu quyền thông báo
    private void requestNotificationPermission() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Quyền đã được cấp
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();

            } else {
                // Quyền bị từ chối, hiển thị thông báo giải thích
                showPermissionRationale();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission Needed")
                .setMessage("This app needs the notification permission to alert you about your budget. Please enable it in the app settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this,"Notification are disable", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}