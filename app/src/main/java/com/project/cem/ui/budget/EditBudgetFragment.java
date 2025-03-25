package com.project.cem.ui.budget;

import android.app.AlertDialog;
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
import android.widget.NumberPicker;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditBudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private Spinner spnCategory;
    private EditText edtAmount;
    private NumberPicker monthPicker;
    private NumberPicker yearPicker;
    private Button btnUpdate;
    private int budgetId;
    private List<ExpenseCategory> categoriesList = new ArrayList<>();
    // Use VndCurrencyFormatter
    private VndCurrencyFormatter currencyFormatter = new VndCurrencyFormatter();
    private String current = "";

    public static EditBudgetFragment newInstance(int budgetId) {
        EditBudgetFragment fragment = new EditBudgetFragment();
        Bundle args = new Bundle();
        args.putInt("budget_id", budgetId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            budgetId = getArguments().getInt("budget_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_budget, container, false);

        spnCategory = view.findViewById(R.id.edit_spn_category);
        edtAmount = view.findViewById(R.id.edit_edt_amount);
        edtAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        monthPicker = view.findViewById(R.id.edit_np_month);
        yearPicker = view.findViewById(R.id.edit_np_year);
        btnUpdate = view.findViewById(R.id.edit_btn_update);

        // Use DateUtils to set up the NumberPickers
        DateUtils.setupMonthAndYearPickers(monthPicker, yearPicker, -10, 10);


        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        budgetViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, getCategoryNames(categories));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCategory.setAdapter(adapter);

            loadBudgetInfo();
        });

        btnUpdate.setOnClickListener(v -> updateBudget());
        setupAmountEditText(); // Set up TextWatcher

        budgetViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage ->{
            if(errorMessage!= null && !errorMessage.isEmpty()){
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        budgetViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                budgetViewModel.clearMessage(); // Clear the message after displaying it
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

                    String cleanString = s.toString().replaceAll("[^\\d]", ""); // Remove
                    if (!cleanString.isEmpty()) {
                        try {
                            long parsed = Long.parseLong(cleanString);
                            String formatted = currencyFormatter.formatForEditText(parsed); // Dùng hàm mới

                            current = formatted;
                            edtAmount.setText(formatted);
                            edtAmount.setSelection(formatted.length());

                        } catch (NumberFormatException e) {
                            // Handle
                            Log.e("EditBudgetFragment", "Error parsing amount", e);
                        }
                    }
                    else{
                        current = "";
                    }
                    edtAmount.addTextChangedListener(this); // Re-attach
                }
            }
        });
    }


    private void loadBudgetInfo() {
        Budget budget = budgetViewModel.getAllBudgets().getValue().stream().filter(x -> x.getBudgetID() == budgetId).findFirst().orElse(null);

        if (budget != null) {
            // Dùng hàm formatForEditText
            edtAmount.setText(currencyFormatter.formatForEditText(budget.getAmount()));
            monthPicker.setValue(budget.getMonth());
            yearPicker.setValue(budget.getYear());

            for (int i = 0; i < categoriesList.size(); i++) {
                if (categoriesList.get(i).getCategoryID() == budget.getCategoryID()) {
                    spnCategory.setSelection(i);
                    break;
                }
            }
        } else {
            Log.d("DEBUG", "loadBudgetInfo: null");
        }
    }

    private void updateBudget() {
        // Lấy và kiểm tra dữ liệu đầu vào
        String amountStr = edtAmount.getText().toString().trim();

        if (spnCategory.getSelectedItem() == null || amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        // Remove  dots before parsing
        String cleanString = amountStr.replace("VNĐ", "").replaceAll("[^\\d]", "");
        long amount;
        try {
            amount = Long.parseLong(cleanString);
        }
        catch(NumberFormatException e){
            Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
            return;
        }
        // Minimum amount check
        if (amount < 10000) {
            Toast.makeText(getContext(), "Minimum budget amount is 10,000 VND", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = categoriesList.get(spnCategory.getSelectedItemPosition()).getCategoryID();

        // Get month and year from NumberPickers
        int month = monthPicker.getValue();
        int year = yearPicker.getValue();

        // Lấy User
        com.project.cem.model.User user = com.project.cem.utils.UserPreferences.getUser(getContext());
        if (user == null) {
            Log.e("EditBudgetFragment", "User is null. Cannot update budget.");
            Toast.makeText(getContext(), "User is null. Cannot update budget.", Toast.LENGTH_SHORT).show();
            return;
        }
        int userID = user.getUserID();

        // Tạo Budget object
        Budget updatedBudget = new Budget(budgetId, userID, categoryId, amount, month, year);

        // Gọi update trên ViewModel
        budgetViewModel.update(updatedBudget);

        // Pop back stack (quay lại BudgetFragment)
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