package com.project.cem.ui.expenses;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.viewmodel.ExpenseViewModel;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditExpenseFragment extends Fragment {

    private ExpenseViewModel expenseViewModel;
    private Spinner spnCategory;
    private EditText edtAmount, edtDate, edtDescription;
    private Button btnUpdate, btnCancel;
    private int expenseId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private List<ExpenseCategory> categoriesList = new ArrayList<>();

    public static EditExpenseFragment newInstance(int expenseId) {
        EditExpenseFragment fragment = new EditExpenseFragment();
        Bundle args = new Bundle();
        args.putInt("expense_id", expenseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            expenseId = getArguments().getInt("expense_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_expense, container, false);

        spnCategory = view.findViewById(R.id.edit_spn_category);
        edtAmount = view.findViewById(R.id.edit_edt_amount);
        edtDate = view.findViewById(R.id.edit_edt_date);
        edtDescription = view.findViewById(R.id.edit_edt_description);
        btnUpdate = view.findViewById(R.id.edit_btn_update);
        btnCancel = view.findViewById(R.id.edit_btn_cancel);

        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        expenseViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, getCategoryNames(categories));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCategory.setAdapter(adapter);

            loadExpenseInfo();
        });

        edtDate.setOnClickListener(v -> showDatePickerDialog(edtDate));
        btnUpdate.setOnClickListener(v -> updateExpense());
        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());

        return view;
    }

    private void loadExpenseInfo() {
        Expense expense = expenseViewModel.getExpenseById(expenseId);
        if (expense != null) {
            edtAmount.setText(decimalFormat.format(expense.getAmount()));
            edtDate.setText(dateFormat.format(expense.getDate()));
            edtDescription.setText(expense.getDescription());

            for (int i = 0; i < categoriesList.size(); i++) {
                if (categoriesList.get(i).getCategoryID() == expense.getCategoryID()) {
                    spnCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateExpense() {
        String amountStr = edtAmount.getText().toString().trim();
        String dateStr = edtDate.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (spnCategory.getSelectedItem() == null || amountStr.isEmpty() || dateStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String cleanString = amountStr.replaceAll("[$,."]", "");
        double amount;
        try {
            amount = Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format.", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = categoriesList.get(spnCategory.getSelectedItemPosition()).getCategoryID();

        try {
            Date date = dateFormat.parse(dateStr);
            Expense updatedExpense = new Expense(expenseId, categoryId, amount, date, description);
            expenseViewModel.update(updatedExpense);
            requireActivity().getSupportFragmentManager().popBackStack();
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year1);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editText.setText(sdf.format(calendar.getTime()));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Update")
                .setMessage("Are you sure you want to cancel the update?")
                .setPositiveButton("Yes", (dialog, which) -> requireActivity().getSupportFragmentManager().popBackStack())
                .setNegativeButton("No", null)
                .show();
    }

    private List<String> getCategoryNames(List<ExpenseCategory> categories) {
        List<String> names = new ArrayList<>();
        for (ExpenseCategory category : categories) {
            names.add(category.getCategoryName());
        }
        return names;
    }
}
