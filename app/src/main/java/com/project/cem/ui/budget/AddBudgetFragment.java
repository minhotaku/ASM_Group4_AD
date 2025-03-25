package com.project.cem.ui.budget;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.project.cem.R;
import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.utils.DateUtils;
import com.project.cem.utils.VndCurrencyFormatter;
import com.project.cem.viewmodel.BudgetViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddBudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private Spinner spnCategory;
    private EditText edtAmount;
    private NumberPicker monthPicker;
    private NumberPicker yearPicker;
    private Button btnSave;
    private ProgressBar progressBar;
    private List<ExpenseCategory> categoriesList = new ArrayList<>();
    // Use the VndCurrencyFormatter
    private VndCurrencyFormatter currencyFormatter = new VndCurrencyFormatter();
    private String current = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_budget, container, false);

        spnCategory = view.findViewById(R.id.add_spn_category);
        edtAmount = view.findViewById(R.id.add_edt_amount);
        monthPicker = view.findViewById(R.id.add_np_month);
        yearPicker = view.findViewById(R.id.add_np_year);
        btnSave = view.findViewById(R.id.add_btn_save);
        progressBar = view.findViewById(R.id.progress_bar);


        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        budgetViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, getCategoryNames(categories));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCategory.setAdapter(adapter);
        });

        DateUtils.setupMonthAndYearPickers(monthPicker, yearPicker, -10, 10);

        btnSave.setOnClickListener(v -> saveBudget());
        setupAmountEditText(); // Set up TextWatcher

        budgetViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        budgetViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }

    private void setupAmountEditText() {
        edtAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    edtAmount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^\\d]", ""); // Remove non-digits
                    if (!cleanString.isEmpty()) {
                        try {
                            long parsed = Long.parseLong(cleanString);
                            String formatted = currencyFormatter.formatForEditText(parsed); // Dùng hàm mới

                            current = formatted;
                            edtAmount.setText(formatted);
                            edtAmount.setSelection(formatted.length()); // Keep cursor at the end

                        } catch (NumberFormatException e) {
                            // Handle parsing error
                            Log.e("AddBudgetFragment", "Error parsing amount", e);
                        }
                    }
                    else{
                        current = ""; // Reset
                    }
                    edtAmount.addTextChangedListener(this); // Re-attach
                }
            }
        });
    }


    private void saveBudget() {
        String amountStr = edtAmount.getText().toString().trim();

        if (spnCategory.getSelectedItem() == null || amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoriesList == null || spnCategory.getSelectedItemPosition() < 0 || spnCategory.getSelectedItemPosition() >= categoriesList.size()) {
            Toast.makeText(getContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Parse the amount
        long amount;
        try {
            // Remove non-digit characters before parsing
            String cleanString = amountStr.replace("VNĐ", "").replaceAll("[^\\d]", "");
            amount = Long.parseLong(cleanString);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (amount < 10000) {
            Toast.makeText(getContext(), "Minimum budget amount is 10,000 VND", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = categoriesList.get(spnCategory.getSelectedItemPosition()).getCategoryID();

        int month = monthPicker.getValue();
        int year = yearPicker.getValue();

        com.project.cem.model.User user = com.project.cem.utils.UserPreferences.getUser(getContext());
        if (user == null) {
            Toast.makeText(getContext(), "User is null. Cannot save budget.", Toast.LENGTH_SHORT).show();
            return;
        }
        int userID = user.getUserID();

        Budget newBudget = new Budget(0, userID, categoryId, amount, month, year);
        budgetViewModel.insert(newBudget);
        requireActivity().getSupportFragmentManager().popBackStack();

    }


    private List<String> getCategoryNames(List<ExpenseCategory> categories) {
        List<String> names = new ArrayList<>();
        for (ExpenseCategory category : categories) {
            names.add(category.getCategoryName());
        }
        return names;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        budgetViewModel.clearErrorMessage();
    }
}