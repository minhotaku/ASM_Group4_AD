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

public class EditBudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private Spinner spnCategory;
    private EditText edtAmount;
    private EditText edtStartDate;
    private EditText edtEndDate;
    private Button btnUpdate;
    private Button btnCancel;
    private int budgetId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private List<ExpenseCategory> categoriesList = new ArrayList<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");
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
        edtStartDate = view.findViewById(R.id.edit_edt_start_date);
        edtEndDate = view.findViewById(R.id.edit_edt_end_date);
        btnUpdate = view.findViewById(R.id.edit_btn_update);
        btnCancel = view.findViewById(R.id.edit_btn_cancel);

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

        edtStartDate.setOnClickListener(v -> showDatePickerDialog(edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePickerDialog(edtEndDate));

        btnUpdate.setOnClickListener(v -> updateBudget());
        edtAmount.addTextChangedListener(onTextChangedListener());

        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());

        budgetViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
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

    private void loadBudgetInfo() {
        Budget budget = budgetViewModel.getAllBudgets().getValue().stream().filter(x -> x.getBudgetID() == budgetId).findFirst().orElse(null);

        if (budget != null) {
            edtAmount.setText(decimalFormat.format(budget.getAmount()));
            edtStartDate.setText(dateFormat.format(budget.getStartDate()));
            edtEndDate.setText(dateFormat.format(budget.getEndDate()));

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
        String amountStr = edtAmount.getText().toString().trim();
        String startDateStr = edtStartDate.getText().toString().trim();
        String endDateStr = edtEndDate.getText().toString().trim();

        if (spnCategory.getSelectedItem() == null || amountStr.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
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

        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            com.project.cem.model.User user = com.project.cem.utils.UserPreferences.getUser(getContext());
            if (user == null) {
                Toast.makeText(getContext(), "User is null. Cannot update budget.", Toast.LENGTH_SHORT).show();
                return;
            }
            int userID = user.getUserID();

            Budget updatedBudget = new Budget(budgetId, userID, categoryId, amount, startDate, endDate);
            budgetViewModel.update(updatedBudget);
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("AddBudgetFragment", "onDestroyView called - clearing error");
        budgetViewModel.clearErrorMessage();
    }
}