package com.project.cem.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.project.cem.viewmodel.BudgetViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private ProgressBar progressBar;
    private ImageView btnAddBudget;
    private List<ExpenseCategory> categoriesList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Add this
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        progressBar = view.findViewById(R.id.progress_bar);
        btnAddBudget = view.findViewById(R.id.img_add_budget);
        RecyclerView rclViewBudgets = view.findViewById(R.id.rcl_view_budgets);

        rclViewBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        BudgetsAdapter budgetsAdapter = new BudgetsAdapter(new ArrayList<>(), categoriesList, budgetViewModel, requireContext());
        rclViewBudgets.setAdapter(budgetsAdapter);

        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        budgetViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            budgetsAdapter.setCategories(categoriesList);
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
                budgetViewModel.clearMessage(); // Clear after displaying
            }
        });

        budgetViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        budgetViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                budgetViewModel.clearErrorMessage(); // Clear after displaying
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //budgetViewModel.checkBudgets(); // NO LONGER NEEDED HERE - handled by the Handler
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        // Register the permission callback, which handles the user's response
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permission granted.  You can now post notifications.
                Toast.makeText(requireContext(), "Notifications permission granted", Toast.LENGTH_SHORT).show();
                // You might want to call checkBudgets() here, or enable a setting
            } else {
                // Permission denied.  Explain to the user why you need the permission
                Toast.makeText(requireContext(), "Notifications permission denied", Toast.LENGTH_LONG).show();
                // You should disable notification-related functionality, or provide
                // a way for the user to grant the permission later.
                // You might want to show a dialog explaining why you need the permission.
            }
        });
        budgetViewModel.setRequestPermissionLauncher(requestPermissionLauncher);
    }
    private List<String> getCategoryNames(List<ExpenseCategory> categories) {
        List<String> names = new ArrayList<>();
        for (ExpenseCategory category : categories) {
            names.add(category.getCategoryName());
        }
        return names;
    }
}