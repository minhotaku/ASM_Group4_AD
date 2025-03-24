package com.project.cem.ui.expenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.model.User;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.utils.UserPreferences;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {

    private EditText etDescription, etAmount, etDate;
    private Spinner spinnerCategory;
    private Button btnSave, btnBack;
    private ExpenseRepository expenseRepository;
    private List<CategoryItem> categoryList;
    private Calendar selectedDate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
        expenseRepository = new ExpenseRepository(dbHelper);
        selectedDate = Calendar.getInstance(); // Khởi tạo với ngày hiện tại
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // Khởi tạo các view
        etDescription = view.findViewById(R.id.etDescription);
        etAmount = view.findViewById(R.id.etAmount);
        etDate = view.findViewById(R.id.etDate);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnSave = view.findViewById(R.id.btnSave);
        btnBack = view.findViewById(R.id.btnBack);

        // Thiết lập Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("Add Expense");
        }
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Load danh sách danh mục vào Spinner
        loadCategories();

        // Thiết lập ngày mặc định là ngày hôm nay
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(selectedDate.getTime()));

        // Thiết lập DatePickerDialog khi nhấn vào EditText ngày
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Thêm TextWatcher để định dạng số tiền khi nhập
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            long parsed = Long.parseLong(cleanString);
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
                            symbols.setGroupingSeparator('.');
                            DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
                            decimalFormat.setMaximumFractionDigits(0);
                            String formatted = decimalFormat.format(parsed);
                            current = formatted;
                            etAmount.setText(formatted);
                            etAmount.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            etAmount.setText("");
                        }
                    } else {
                        current = "";
                    }

                    etAmount.addTextChangedListener(this);
                }
            }
        });

        // Xử lý sự kiện nhấn nút Save
        btnSave.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void showDatePickerDialog() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(yearSelected, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    etDate.setText(sdf.format(selectedDate.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadCategories() {
        categoryList = new ArrayList<>();
        User currentUser = UserPreferences.getUser(requireContext());
        if (currentUser != null) {
            int userId = currentUser.getUserID();
            categoryList = expenseRepository.getCategoriesByUserId(userId);
        }

        List<String> categoryNames = new ArrayList<>();
        for (CategoryItem category : categoryList) {
            categoryNames.add(category.getCategoryName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void saveExpense() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().replaceAll("[^\\d]", "").trim(); // Loại bỏ dấu chấm để parse
        int selectedCategoryPosition = spinnerCategory.getSelectedItemPosition();

        if (description.isEmpty() || amountStr.isEmpty() || etDate.getText().toString().isEmpty() || selectedCategoryPosition == -1) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = selectedDate.getTime();

        User currentUser = UserPreferences.getUser(requireContext());
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        int userId = currentUser.getUserID();

        int categoryId = categoryList.get(selectedCategoryPosition).getCategoryId();

        Expense expense = new Expense();
        expense.setUserID(userId);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setCategoryID(categoryId);

        expenseRepository.addExpense(expense);

        Bundle result = new Bundle();
        result.putBoolean("expense_added", true);
        getParentFragmentManager().setFragmentResult("expense_added_request", result);

        Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    public static class CategoryItem {
        private int categoryId;
        private String categoryName;

        public CategoryItem(int categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }
    }
}