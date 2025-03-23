// com.project.cem.ui.expenses/AddExpenseFragment.java
package com.project.cem.ui.expenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.model.User;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.utils.UserPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {

    private EditText etDescription, etAmount, etDate;
    private Spinner spinnerCategory;
    private Button btnSave;
    private ExpenseRepository expenseRepository;
    private List<CategoryItem> categoryList;
    private Calendar selectedDate; // Lưu ngày được chọn

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
        expenseRepository = new ExpenseRepository(dbHelper);
        selectedDate = Calendar.getInstance(); // Khởi tạo ngày mặc định là ngày hiện tại
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        etDescription = view.findViewById(R.id.etDescription);
        etAmount = view.findViewById(R.id.etAmount);
        etDate = view.findViewById(R.id.etDate);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnSave = view.findViewById(R.id.btnSave);

        // Load danh sách danh mục vào Spinner
        loadCategories();

        // Thiết lập DatePickerDialog khi nhấn vào EditText ngày
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý sự kiện nhấn nút Save
        btnSave.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void showDatePickerDialog() {
        // Lấy ngày hiện tại làm mặc định
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    // Cập nhật ngày được chọn
                    selectedDate.set(yearSelected, monthOfYear, dayOfMonth);
                    // Hiển thị ngày trong EditText
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
        // Lấy dữ liệu từ các trường nhập liệu
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        int selectedCategoryPosition = spinnerCategory.getSelectedItemPosition();

        // Kiểm tra dữ liệu đầu vào
        if (description.isEmpty() || amountStr.isEmpty() || etDate.getText().toString().isEmpty() || selectedCategoryPosition == -1) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi dữ liệu
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy ngày từ selectedDate
        Date date = selectedDate.getTime();

        // Lấy userID từ UserPreferences
        User currentUser = UserPreferences.getUser(requireContext());
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        int userId = currentUser.getUserID();

        // Lấy categoryID từ danh mục được chọn
        int categoryId = categoryList.get(selectedCategoryPosition).getCategoryId();

        // Tạo đối tượng Expense
        Expense expense = new Expense();
        expense.setUserID(userId);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setCategoryID(categoryId);

        // Lưu vào cơ sở dữ liệu
        expenseRepository.addExpense(expense);

        // Gửi kết quả để thông báo rằng chi tiêu đã được thêm
        Bundle result = new Bundle();
        result.putBoolean("expense_added", true);
        getParentFragmentManager().setFragmentResult("expense_added_request", result);

        Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    // Lớp phụ để lưu trữ thông tin danh mục
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