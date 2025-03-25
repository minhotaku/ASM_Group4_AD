package com.project.cem.ui.setting.recurring;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.project.cem.R;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.RecurringExpense;
import com.project.cem.model.User;
import com.project.cem.utils.UserPreferences;
import com.project.cem.utils.VndCurrencyFormatter;
import com.project.cem.viewmodel.BudgetViewModel;
import com.project.cem.viewmodel.RecurringExpenseViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddEditRecurringExpenseFragment extends Fragment {

    private RecurringExpenseViewModel viewModel;
    private BudgetViewModel budgetViewModel; // Add BudgetViewModel
    // UI elements
    private EditText edtDescription;
    private EditText edtAmount;
    private Spinner spnCategory;
    private NumberPicker npMonth;
    private NumberPicker npYear;
    private Spinner spnFrequency;

    private Switch swActive;
    private Button btnSave;
    private List<ExpenseCategory> categoriesList = new ArrayList<>();
    private VndCurrencyFormatter currencyFormatter = new VndCurrencyFormatter(); // Instance of formatter

    private int recurringExpenseId = -1; // -1 indicates "add" mode, otherwise "edit" mode.
    private static final String ARG_EXPENSE_ID = "expense_id";


    public static AddEditRecurringExpenseFragment newInstance(int expenseId) {
        AddEditRecurringExpenseFragment fragment = new AddEditRecurringExpenseFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EXPENSE_ID, expenseId);
        fragment.setArguments(args);
        return fragment;
    }
    public static AddEditRecurringExpenseFragment newInstance() {
        AddEditRecurringExpenseFragment fragment = new AddEditRecurringExpenseFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(RecurringExpenseViewModel.class);
        // Get BudgetViewModel
        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        // Check if we are in edit mode
        if (getArguments() != null) {
            recurringExpenseId = getArguments().getInt(ARG_EXPENSE_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_recurring_expense, container, false);

        // Initialize UI elements
        edtDescription = view.findViewById(R.id.edt_recurring_description);
        edtAmount = view.findViewById(R.id.edt_recurring_amount);
        edtAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Use number input
        spnCategory = view.findViewById(R.id.spn_recurring_category);
        npMonth = view.findViewById(R.id.np_recurring_month);
        npYear = view.findViewById(R.id.np_recurring_year);
        spnFrequency = view.findViewById(R.id.spn_recurring_frequency);
        swActive = view.findViewById(R.id.sw_recurring_active);
        btnSave = view.findViewById(R.id.btn_recurring_save);

        // Set up NumberPickers, Spinners, and TextWatcher
        setupNumberPickers();
        setupFrequencySpinner();
        setupAmountEditText();
        // Set up listeners
        btnSave.setOnClickListener(v -> saveRecurringExpense());

        // Get and observe categories for the spinner (Use BudgetViewModel)
        budgetViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, getCategoryNames(categories));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCategory.setAdapter(adapter);

            // If editing, load existing data *after* categories are loaded
            if (recurringExpenseId != -1) {
                loadRecurringExpenseData();
            }
        });
        viewModel.getMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage(); // Clear the message after displaying it
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage(); // Clear the error after displaying
            }
        });
        return view;
    }
    private void setupFrequencySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.recurrence_frequencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFrequency.setAdapter(adapter);
    }

    // Method to set up the NumberPickers (month and year)
    private void setupNumberPickers() {
        // Month NumberPicker
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setDisplayedValues(months); // Display month names
        npMonth.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1); // Set current month


        // Year NumberPicker
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        npYear.setMinValue(currentYear - 10); // Go back 10 years
        npYear.setMaxValue(currentYear + 10); // Go forward 10 years
        npYear.setValue(currentYear);        // Set current year
    }

    // Helper method to get category names for the spinner
    private List<String> getCategoryNames(List<ExpenseCategory> categories) {
        List<String> names = new ArrayList<>();
        for (ExpenseCategory category : categories) {
            names.add(category.getCategoryName());
        }
        return names;
    }
    private void setupAmountEditText() {
        edtAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

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
                            // Format *with* VndCurrencyFormatter, but without " VNĐ"
                            String formatted = currencyFormatter.formatForEditText(parsed);

                            current = formatted;
                            edtAmount.setText(formatted);
                            edtAmount.setSelection(formatted.length()); // cursor

                        } catch (NumberFormatException e) {
                            // Handle
                            Log.e("AddEditRecurring", "Error parsing amount", e);
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
    private void loadRecurringExpenseData() {
        viewModel.getRecurringExpenseById(recurringExpenseId).observe(getViewLifecycleOwner(), recurringExpense -> {
            if (recurringExpense != null) {
                // Populate
                edtDescription.setText(recurringExpense.getDescription());
                edtAmount.setText(currencyFormatter.formatForEditText(recurringExpense.getAmount())); // Dùng hàm mới
                npMonth.setValue(recurringExpense.getMonth());
                npYear.setValue(recurringExpense.getYear());
                swActive.setChecked(recurringExpense.isActive());

                // spinner
                for (int i = 0; i < categoriesList.size(); i++) {
                    if (categoriesList.get(i).getCategoryID() == recurringExpense.getCategoryID()) {
                        spnCategory.setSelection(i);
                        break;
                    }
                }
                //set selection for frequency
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spnFrequency.getAdapter();
                if(adapter != null){
                    int position = adapter.getPosition(recurringExpense.getRecurrenceFrequency());
                    if(position != -1){
                        spnFrequency.setSelection(position);
                    }
                }

            }
        });
    }


    private void saveRecurringExpense() {
        // 1. Get values from UI elements
        String description = edtDescription.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();

        // Check if spnCategory.getSelectedItemPosition() is within the valid range
        if (spnCategory.getSelectedItemPosition() < 0 || spnCategory.getSelectedItemPosition() >= categoriesList.size()) {
            Toast.makeText(requireContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
            return; // Exit the method, preventing further processing
        }

        int categoryId = categoriesList.get(spnCategory.getSelectedItemPosition()).getCategoryID(); // Get category ID
        int month = npMonth.getValue();
        int year = npYear.getValue();
        String frequency = spnFrequency.getSelectedItem().toString(); // Get selected frequency
        boolean isActive = swActive.isChecked(); // Get switch state


        // 2. Validate input
        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount;
        try {
            // Remove non-digit characters before parsing
            String cleanString = amountStr.replace("VNĐ", "").replaceAll("[^\\d]", "");
            amount = Long.parseLong(cleanString);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount format.", Toast.LENGTH_SHORT).show();
            return; // Exit if the amount is not a valid number
        }

        // Get the user ID
        User user = UserPreferences.getUser(getContext());
        if (user == null) {
            Toast.makeText(getContext(), "User is null. Cannot save budget.", Toast.LENGTH_SHORT).show();
            return; // Exit if no user is logged in
        }
        int userID = user.getUserID();

        // 3. Create a RecurringExpense object
        RecurringExpense recurringExpense = new RecurringExpense(recurringExpenseId, userID, categoryId, description, amount, month, year, frequency, isActive); // Use -1 for ID when adding

        // 4. Call viewModel.insert() or viewModel.update()
        if (recurringExpenseId == -1) {
            viewModel.insert(recurringExpense); // Add new
        } else {
            viewModel.update(recurringExpense); // Update existing
        }

        // 6. Delay the popBackStack()
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) { // Check if the fragment is still added to its activity
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        }, 300); // increased time
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.clearErrorMessage();
    }
}