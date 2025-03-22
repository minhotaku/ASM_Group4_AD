package com.project.cem.ui.expenses;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.viewmodel.ExpenseViewModel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ExpensesFragment extends Fragment {
    private ExpenseAdapter[] expenseAdapters;
    private ExpenseViewModel expenseViewModel;
    private Button btnAddExpense;
    private Calendar selectedDate = Calendar.getInstance();

    private static final String[] CATEGORY_NAMES = {"Food", "Transportation", "Entertainment", "Housing", "Utilities", "Health", "Education"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        expenseAdapters = new ExpenseAdapter[CATEGORY_NAMES.length];

        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            expenseAdapters[i] = new ExpenseAdapter(new ArrayList<>());
            RecyclerView recyclerView = view.findViewById(getResources().getIdentifier("recycler_view_" + (i + 1), "id", requireContext().getPackageName()));
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(expenseAdapters[i]);

            TextView categoryTitle = view.findViewById(getResources().getIdentifier("category_title_" + (i + 1), "id", requireContext().getPackageName()));
            categoryTitle.setText(CATEGORY_NAMES[i]);
        }

        btnAddExpense = view.findViewById(R.id.btn_add_expense);
        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseViewModel.getAllExpenses().observe(getViewLifecycleOwner(), this::updateExpenses);

        return view;
    }

    private void updateExpenses(List<Expense> expenses) {
        if (expenses != null) {
            Log.d("ExpenseFragment", "Cập nhật danh sách Expense: " + expenses.size());

            Map<Integer, List<Expense>> categorizedExpenses = expenses.stream()
                    .collect(Collectors.groupingBy(Expense::getCategoryID));

            for (int i = 0; i < CATEGORY_NAMES.length; i++) {
                List<Expense> categoryExpenses = categorizedExpenses.getOrDefault(i + 1, new ArrayList<>());
                expenseAdapters[i].updateList(categoryExpenses);
            }
        }
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_expense, null);
        builder.setView(dialogView);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        Button buttonSelectDate = dialogView.findViewById(R.id.buttonSelectDate);
        TextView textViewSelectedDate = dialogView.findViewById(R.id.textViewSelectedDate);

        updateDateDisplay(textViewSelectedDate);
        buttonSelectDate.setOnClickListener(v -> showDatePicker(textViewSelectedDate));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, CATEGORY_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String description = editTextDescription.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();
            int categoryID = spinnerCategory.getSelectedItemPosition() + 1;

            if (description.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            Expense expense = new Expense(0, 1, description, categoryID, amount, selectedDate.getTime());
            expenseViewModel.insertExpense(expense);
            Toast.makeText(getContext(), "Expense added!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showDatePicker(TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateDisplay(textView);
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay(TextView textView) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        textView.setText(sdf.format(selectedDate.getTime()));
    }
}
