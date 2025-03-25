package com.project.cem.ui.budget;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.utils.BudgetBroadcastReceiver;
import com.project.cem.viewmodel.BudgetViewModel;
import com.project.cem.viewmodel.SpendingOverviewViewModel;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private ImageView btnAddBudget;
    private List<ExpenseCategory> categoriesList = new ArrayList<>();
    private SpendingOverviewViewModel spendingOverviewViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        btnAddBudget = view.findViewById(R.id.img_add_budget);
        RecyclerView rclViewBudgets = view.findViewById(R.id.rcl_view_budgets);

        rclViewBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        BudgetsAdapter budgetsAdapter = new BudgetsAdapter(new ArrayList<>(), categoriesList, budgetViewModel, requireContext());
        rclViewBudgets.setAdapter(budgetsAdapter);

        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        spendingOverviewViewModel = new ViewModelProvider(requireActivity()).get(SpendingOverviewViewModel.class);

        // Observe categories
        budgetViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            budgetsAdapter.setCategories(categoriesList); // Update adapter
        });

        budgetViewModel.getAllBudgets().observe(getViewLifecycleOwner(), budgets -> {
            budgetsAdapter.setBudgets(budgets);
        });

        btnAddBudget.setOnClickListener(v -> {
            AddBudgetFragment addBudgetFragment = new AddBudgetFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, addBudgetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        budgetViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                budgetViewModel.clearMessage();
            }
        });


        budgetViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                budgetViewModel.clearErrorMessage();
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // spendingOverviewViewModel.loadCurrentMonthSpending();
        budgetViewModel.loadCategories(); // Reload categories

        // Kiểm tra và thông báo khi vào lại fragment
        Intent checkIntent = new Intent(requireContext(), BudgetBroadcastReceiver.class);
        checkIntent.setAction("com.project.cem.ACTION_CHECK_BUDGET");
        checkIntent.putExtra("is_app_launch", true); // Đặt là true vì đây là khi "vào app" (giống như MainActivity)
        requireContext().sendBroadcast(checkIntent);

    }

    private List<String> getCategoryNames(List<ExpenseCategory> categories) {
        List<String> names = new ArrayList<>();
        for (ExpenseCategory category : categories) {
            names.add(category.getCategoryName());
        }
        return names;
    }
}