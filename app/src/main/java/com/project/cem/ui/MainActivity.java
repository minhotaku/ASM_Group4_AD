package com.project.cem.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.project.cem.R;
import com.project.cem.databinding.ActivityStudentHomeBinding;
import com.project.cem.ui.budget.BudgetFragment;
import com.project.cem.ui.home.HomeFragment;
import com.project.cem.ui.setting.SettingFragment;
import com.project.cem.ui.expense.ExpenseFragment;

public class MainActivity extends AppCompatActivity {

    ActivityStudentHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Logic chuyển đổi giữa các fragment dựa vào bottom navigation
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

    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

    }
}