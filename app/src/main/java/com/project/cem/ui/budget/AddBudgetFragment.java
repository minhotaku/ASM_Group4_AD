package com.project.cem.ui.budget;

import android.app.DatePickerDialog;
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
import android.widget.NumberPicker; // Import NumberPicker
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
import com.project.cem.viewmodel.BudgetViewModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddBudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private Spinner spnCategory;
    private EditText edtAmount;
    // Replace Date EditTexts with NumberPickers
    private NumberPicker monthPicker;
    private NumberPicker yearPicker;
    private Button btnSave;
    private ProgressBar progressBar;
    private List<ExpenseCategory> categoriesList = new ArrayList<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Format for display
    private String current = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_budget, container, false);

        spnCategory = view.findViewById(R.id.add_spn_category);
        edtAmount = view.findViewById(R.id.add_edt_amount);
        // Initialize NumberPickers
        monthPicker = view.findViewById(R.id.add_np_month);
        yearPicker = view.findViewById(R.id.add_np_year);
        btnSave = view.findViewById(R.id.add_btn_save);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set up NumberPickers
        setupNumberPickers();

        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        budgetViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, getCategoryNames(categories));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCategory.setAdapter(adapter);
        });


        btnSave.setOnClickListener(v -> saveBudget());
        edtAmount.addTextChangedListener(onTextChangedListener());

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
    private TextWatcher onTextChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                edtAmount.removeTextChangedListener(this);
                try {
                    String originalString = s.toString();
                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    if (originalString.contains(".")) {
                        originalString = originalString.replaceAll("\\.", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,###,###,###");
                    String formattedString = formatter.format(longval);

                    edtAmount.setText(formattedString);
                    edtAmount.setSelection(edtAmount.getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                edtAmount.addTextChangedListener(this);
            }
        };
    }

    private void setupNumberPickers() {
        // Month NumberPicker
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(months); // Display month names
        monthPicker.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1); // Set current month


        // Year NumberPicker
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 10); // Go back 10 years
        yearPicker.setMaxValue(currentYear + 10); // Go forward 10 years
        yearPicker.setValue(currentYear);        // Set current year
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

        String cleanString = amountStr.replaceAll("[$,.]", "");
        double amount;
        try {
            amount = Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount < 10000) {
            Toast.makeText(getContext(), "Minimum budget amount is 10,000 VND", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = categoriesList.get(spnCategory.getSelectedItemPosition()).getCategoryID();

        // Get selected month and year from NumberPickers
        int month = monthPicker.getValue();
        int year = yearPicker.getValue();


        com.project.cem.model.User user = com.project.cem.utils.UserPreferences.getUser(getContext());
        if (user == null) {
            Toast.makeText(getContext(), "User is null. Cannot save budget.", Toast.LENGTH_SHORT).show();
            return;
        }
        int userID = user.getUserID();

        // Create Budget object with month and year
        Budget newBudget = new Budget(0, userID, categoryId, amount, month, year); // Use new constructor
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