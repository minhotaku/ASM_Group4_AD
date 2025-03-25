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
import com.project.cem.utils.VndCurrencyFormatter;

import java.text.ParseException;
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
    private String categoryName; // Để lưu trữ tạm tên danh mục khi chỉnh sửa
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private VndCurrencyFormatter currencyFormatter = new VndCurrencyFormatter();

    // Flag để kiểm soát việc cập nhật EditText
    private boolean isUpdatingAmountText = false;

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

        // Khởi tạo các View
        descriptionLayout = view.findViewById(R.id.description_layout);
        amountLayout = view.findViewById(R.id.amount_layout);
        descriptionEditText = view.findViewById(R.id.edit_description);
        amountEditText = view.findViewById(R.id.edit_amount);
        dateButton = view.findViewById(R.id.button_date);
        categoryLayout = view.findViewById(R.id.category_layout);
        categorySpinner = view.findViewById(R.id.spinner_category);
        saveButton = view.findViewById(R.id.button_save);
        cancelButton = view.findViewById(R.id.button_cancel);

        // Thêm TextWatcher để định dạng số tiền
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần xử lý
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingAmountText) return;

                isUpdatingAmountText = true;

                String input = s.toString().trim();
                // Loại bỏ tất cả các ký tự không phải số
                String digitsOnly = input.replaceAll("[^\\d]", "");

                if (!digitsOnly.isEmpty()) {
                    try {
                        // Chuyển đổi thành số và định dạng lại
                        double amount = Double.parseDouble(digitsOnly);
                        // Chỉ định dạng, không thêm "VNĐ" vào EditText
                        String formatted = digitsOnly.isEmpty() ? "" : currencyFormatter.format(amount)
                                .replace(" VNĐ", ""); // Loại bỏ đơn vị tiền để không tích lũy

                        s.replace(0, s.length(), formatted);
                    } catch (NumberFormatException e) {
                        s.replace(0, s.length(), "");
                    }
                }

                isUpdatingAmountText = false;
            }
        });

        // Thiết lập spinner danh mục
        setupCategorySpinner();

        // Thiết lập ngày mặc định (hôm nay)
        updateDateDisplay();

        // Xử lý sự kiện click vào ngày để hiển thị date picker
        dateButton.setOnClickListener(v -> showDatePicker());

        // Điền thông tin nếu đang sửa
        if (expenseToEdit != null) {
            descriptionEditText.setText(expenseToEdit.getDescription());

            // Định dạng số tiền với VndCurrencyFormatter
            String formattedAmount = currencyFormatter.format(expenseToEdit.getAmount())
                    .replace(" VNĐ", ""); // Loại bỏ đơn vị tiền

            isUpdatingAmountText = true;
            amountEditText.setText(formattedAmount);
            isUpdatingAmountText = false;

            calendar.setTime(expenseToEdit.getDate());
            updateDateDisplay();

            // Chọn danh mục
            if (categoryName != null && !categoryName.isEmpty()) {
                categorySpinner.setText(categoryName, false);
            } else {
                // Tìm tên danh mục dựa trên ID nếu không có sẵn
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getCategoryID() == expenseToEdit.getCategoryID()) {
                        categorySpinner.setText(categories.get(i).getCategoryName(), false);
                        break;
                    }
                }
            }
        }

        // Xử lý sự kiện nút lưu
        saveButton.setOnClickListener(v -> {
            if (validateInput()) {
                saveExpense();
                dismiss();
            }
        });

        // Xử lý sự kiện nút hủy
        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        builder.setTitle(expenseToEdit != null ? "Sửa chi tiêu" : "Thêm chi tiêu mới");

        return builder.create();
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
            // Sử dụng MaterialDatePicker
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày chi tiêu")
                    .setSelection(calendar.getTimeInMillis())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Đặt múi giờ về UTC để tránh vấn đề offset
                Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utcCalendar.setTimeInMillis(selection);

                // Chuyển giá trị ngày về múi giờ local
                calendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH));

                updateDateDisplay();
            });

            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback nếu MaterialDatePicker gặp lỗi
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

        String amountText = amountEditText.getText().toString().trim();
        if (amountText.isEmpty()) {
            amountLayout.setError("Vui lòng nhập số tiền");
            isValid = false;
        } else {
            try {
                // Chuyển đổi dữ liệu từ định dạng sang số
                double amount = getCleanAmount(amountText);
                if (amount <= 0) {
                    amountLayout.setError("Số tiền phải lớn hơn 0");
                    isValid = false;
                } else {
                    amountLayout.setError(null);
                }
            } catch (Exception e) {
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

    private double getCleanAmount(String formattedAmount) {
        // Loại bỏ tất cả dấu chấm (phân cách hàng nghìn) và đơn vị tiền
        String digitsOnly = formattedAmount.replaceAll("[^\\d]", "");
        if (digitsOnly.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(digitsOnly);
    }

    private void saveExpense() {
        try {
            String description = descriptionEditText.getText().toString().trim();
            double amount = getCleanAmount(amountEditText.getText().toString());
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