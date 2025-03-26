package com.project.cem.ui.expense;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.ExpenseWithCategory;
import com.project.cem.utils.UserPreferences;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ExpenseDialogFragment extends DialogFragment {

    private TextInputLayout descriptionLayout;
    private TextInputLayout amountLayout;
    private TextInputLayout categoryLayout;
    private com.google.android.material.textfield.TextInputEditText descriptionEditText;
    private com.google.android.material.textfield.TextInputEditText amountEditText;
    private MaterialButton dateButton;
    private MaterialAutoCompleteTextView categorySpinner;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;

    private List<ExpenseCategory> categories;
    private Expense expenseToEdit;
    private String categoryName;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    private ExpenseDialogListener listener;

    public interface ExpenseDialogListener {
        void onExpenseAdded(Expense expense);
        void onExpenseUpdated(Expense expense);
    }

    public static ExpenseDialogFragment newInstance(List<ExpenseCategory> categories) {
        ExpenseDialogFragment fragment = new ExpenseDialogFragment();
        fragment.categories = new ArrayList<>(categories);
        return fragment;
    }

    public static ExpenseDialogFragment newInstance(List<ExpenseCategory> categories, ExpenseWithCategory expense) {
        ExpenseDialogFragment fragment = new ExpenseDialogFragment();
        fragment.categories = new ArrayList<>(categories);
        fragment.expenseToEdit = new Expense();
        fragment.expenseToEdit.setExpenseID(expense.getExpenseID());
        fragment.expenseToEdit.setUserID(expense.getUserID());
        fragment.expenseToEdit.setCategoryID(expense.getCategoryID());
        fragment.expenseToEdit.setDescription(expense.getDescription());
        fragment.expenseToEdit.setAmount(expense.getAmount());
        fragment.expenseToEdit.setDate(expense.getDate());
        fragment.categoryName = expense.getCategoryName();
        return fragment;
    }

    public void setExpenseDialogListener(ExpenseDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_expense, null);

        descriptionLayout = view.findViewById(R.id.description_layout);
        amountLayout = view.findViewById(R.id.amount_layout);
        descriptionEditText = view.findViewById(R.id.edit_description);
        amountEditText = view.findViewById(R.id.edit_amount);
        dateButton = view.findViewById(R.id.button_date);
        categoryLayout = view.findViewById(R.id.category_layout);
        categorySpinner = view.findViewById(R.id.spinner_category);
        saveButton = view.findViewById(R.id.button_save);
        cancelButton = view.findViewById(R.id.button_cancel);

        setupCategorySpinner();

        amountEditText.addTextChangedListener(amountTextWatcher());

        updateDateDisplay();

        dateButton.setOnClickListener(v -> showDatePicker());

        if (expenseToEdit != null) {
            descriptionEditText.setText(expenseToEdit.getDescription());
            String formattedAmount = decimalFormat.format((long) expenseToEdit.getAmount());
            amountEditText.setText(formattedAmount);

            calendar.setTime(expenseToEdit.getDate());
            updateDateDisplay();

            if (categoryName != null && !categoryName.isEmpty()) {
                categorySpinner.setText(categoryName, false);
            } else {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getCategoryID() == expenseToEdit.getCategoryID()) {
                        categorySpinner.setText(categories.get(i).getCategoryName(), false);
                        break;
                    }
                }
            }
        }

        saveButton.setOnClickListener(v -> {
            if (validateInput()) {
                saveExpense();
                dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        builder.setTitle(expenseToEdit != null ? "Edit Expense" : "Add New Expense");

        return builder.create();
    }

    private TextWatcher amountTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                amountEditText.removeTextChangedListener(this);
                try {
                    String originalString = s.toString();
                    if (originalString.isEmpty()) {
                        amountEditText.addTextChangedListener(this);
                        return;
                    }

                    // Remove existing commas
                    String cleanString = originalString.replaceAll("[,]", "");
                    long longval = Long.parseLong(cleanString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,###");
                    String formattedString = formatter.format(longval);

                    amountEditText.setText(formattedString);
                    amountEditText.setSelection(formattedString.length());
                } catch (NumberFormatException e) {
                }
                amountEditText.addTextChangedListener(this);
            }
        };
    }

    private void setupCategorySpinner() {
        try {
            List<String> categoryNames = new ArrayList<>();
            for (ExpenseCategory category : categories) {
                categoryNames.add(category.getCategoryName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
            categorySpinner.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi khi tải danh sách danh mục", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDateDisplay() {
        dateButton.setText(dateFormat.format(calendar.getTime()));
    }

    private void showDatePicker() {
        try {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày chi tiêu")
                    .setSelection(calendar.getTimeInMillis())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utcCalendar.setTimeInMillis(selection);

                calendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH));

                updateDateDisplay();
            });

            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        } catch (Exception e) {
            e.printStackTrace();
            Calendar now = Calendar.getInstance();
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplay();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        if (descriptionEditText.getText().toString().trim().isEmpty()) {
            descriptionLayout.setError("Vui lòng nhập mô tả");
            isValid = false;
        } else {
            descriptionLayout.setError(null);
        }

        String amountText = amountEditText.getText().toString().trim().replaceAll("[,]", "");
        if (amountText.isEmpty()) {
            amountLayout.setError("Vui lòng nhập số tiền");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    amountLayout.setError("Số tiền phải lớn hơn 0");
                    isValid = false;
                } else {
                    amountLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                amountLayout.setError("Số tiền không hợp lệ");
                isValid = false;
            }
        }

        String selectedCategory = categorySpinner.getText().toString();
        if (selectedCategory.isEmpty()) {
            categoryLayout.setError("Vui lòng chọn danh mục");
            isValid = false;
        } else {
            categoryLayout.setError(null);
        }

        return isValid;
    }

    private void saveExpense() {
        try {
            String description = descriptionEditText.getText().toString().trim();
            String amountText = amountEditText.getText().toString().trim().replaceAll("[,]", "");
            double amount = Double.parseDouble(amountText);
            Date date = calendar.getTime();

            String selectedCategoryName = categorySpinner.getText().toString();
            int categoryID = -1;

            for (ExpenseCategory category : categories) {
                if (category.getCategoryName().equals(selectedCategoryName)) {
                    categoryID = category.getCategoryID();
                    break;
                }
            }

            if (categoryID == -1) {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            Expense expense = new Expense();
            expense.setDescription(description);
            expense.setAmount(amount);
            expense.setDate(date);
            expense.setCategoryID(categoryID);
            expense.setUserID(UserPreferences.getUser(requireContext()).getUserID());

            if (expenseToEdit != null) {
                expense.setExpenseID(expenseToEdit.getExpenseID());
                listener.onExpenseUpdated(expense);
            } else {
                listener.onExpenseAdded(expense);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi khi lưu chi tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}