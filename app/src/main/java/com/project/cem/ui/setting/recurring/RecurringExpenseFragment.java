package com.project.cem.ui.setting.recurring;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.ui.setting.recurring.AddEditRecurringExpenseFragment;
import com.project.cem.ui.setting.recurring.RecurringExpenseAdapter;
import com.project.cem.utils.BudgetBroadcastReceiver;
import com.project.cem.utils.UserPreferences;
import com.project.cem.viewmodel.BudgetViewModel;
import com.project.cem.viewmodel.RecurringExpenseViewModel;

import java.util.ArrayList;
import java.util.List;

public class RecurringExpenseFragment extends Fragment {

    private RecurringExpenseViewModel viewModel;
    private BudgetViewModel budgetViewModel;
    private RecyclerView recyclerView;
    private RecurringExpenseAdapter adapter;
    private ImageView btnAdd;
    private List<ExpenseCategory> categoriesList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewModel NGAY TRONG onCreate
        viewModel = new ViewModelProvider(requireActivity()).get(RecurringExpenseViewModel.class);
        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        // Observe messageLiveData NGAY TRONG onCreate
        viewModel.getMessageLiveData().observe(this, message -> { // Observe using 'this'
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage(); // Xóa message sau khi hiển thị
            }
        });
        viewModel.getErrorMessage().observe(this, error -> { // Observe error messages as well
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recurring_expense, container, false);

        recyclerView = view.findViewById(R.id.recurring_expense_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Initialize adapter
        adapter = new RecurringExpenseAdapter(new ArrayList<>(), categoriesList, requireContext());
        recyclerView.setAdapter(adapter);

        btnAdd = view.findViewById(R.id.btn_add_recurring_expense);
        btnAdd.setOnClickListener(v -> {
            // Navigate
            AddEditRecurringExpenseFragment addEditFragment = new AddEditRecurringExpenseFragment().newInstance();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, addEditFragment); // container ID
            transaction.addToBackStack(null); // Optional
            transaction.commit();
        });

        // item click listener
        adapter.setOnItemClickListener(recurringExpense -> {
            // Navigate
            AddEditRecurringExpenseFragment editFragment = AddEditRecurringExpenseFragment.newInstance(recurringExpense.getRecurringExpenseID());
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, editFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // long click
        adapter.setOnItemLongClickListener(recurringExpense -> {
            showDeleteConfirmationDialog(recurringExpense.getRecurringExpenseID());
        });

        // Observe categories.  Use the RecurringExpenseViewModel.
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> { // Changed to getViewLifecycleOwner()
            categoriesList.clear();
            categoriesList.addAll(categories);
            adapter.setCategories(categoriesList); // Update
        });

        // Observe recurring expenses
        int userId = UserPreferences.getUser(requireContext()).getUserID();
        viewModel.getAllRecurringExpenses(userId).observe(getViewLifecycleOwner(), recurringExpenses -> {
            adapter.setRecurringExpenses(recurringExpenses);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadCategories(); // Reload categories here
        int userId = UserPreferences.getUser(requireContext()).getUserID();
        viewModel.getAllRecurringExpenses(userId).observe(getViewLifecycleOwner(), recurringExpenses -> {
            // Update the UI (RecyclerView) with the new list of recurring expenses
            adapter.setRecurringExpenses(recurringExpenses);
        });

        // Kiểm tra và thông báo khi vào fragment
        Intent checkIntent = new Intent(requireContext(), BudgetBroadcastReceiver.class);
        checkIntent.setAction("com.project.cem.ACTION_CHECK_BUDGET");
        checkIntent.putExtra("is_app_launch", true);
        requireContext().sendBroadcast(checkIntent);
    }

    private void showDeleteConfirmationDialog(final int recurringExpenseId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Recurring Expense")
                .setMessage("Are you sure you want to delete this recurring expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteRecurringExpense(recurringExpenseId);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}