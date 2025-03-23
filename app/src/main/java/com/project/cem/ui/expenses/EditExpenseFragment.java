// com.project.cem.ui.expenses/EditExpenseFragment.java
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
import android.widget.ImageButton;
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

public class EditExpenseFragment extends Fragment {

    private static final String ARG_EXPENSE_ID = "expense_id";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_DATE = "date";

    private EditText etDescription, etAmount, etDate;
    private Spinner spinnerCategory;
    private Button btnSave, btnDelete;
    private ImageButton btnPickDate;
    private ExpenseRepository expenseRepository;
    private List<AddExpenseFragment.CategoryItem> categoryList;
    private Calendar selectedDate;
    private Expense expense; // Chi tiêu được truyền vào để chỉnh sửa

    public static EditExpenseFragment newInstance(Expense expense) {
        EditExpenseFragment fragment = new EditExpenseFragment();
        Bundle args = new Bundle();
        // Truyền từng thuộc tính của Expense qua Bundle
        args.putInt(ARG_EXPENSE_ID, expense.getExpenseID());
        args.putInt(ARG_USER_ID, expense.getUserID());
        args.putString(ARG_DESCRIPTION, expense.getDescription());
        args.putInt(ARG_CATEGORY_ID, expense.getCategoryID());
        args.putDouble(ARG_AMOUNT, expense.getAmount());
        // Truyền Date dưới dạng long (milliseconds)
        args.putLong(ARG_DATE, expense.getDate() != null ? expense.getDate().getTime() : -1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
        expenseRepository = new ExpenseRepository(dbHelper);

        // Tái tạo đối tượng Expense từ các giá trị trong Bundle
        if (getArguments() != null) {
            int expenseId = getArguments().getInt(ARG_EXPENSE_ID);
            int userId = getArguments().getInt(ARG_USER_ID);
            String description = getArguments().getString(ARG_DESCRIPTION);
            int categoryId = getArguments().getInt(ARG_CATEGORY_ID);
            double amount = getArguments().getDouble(ARG_AMOUNT);
            long dateMillis = getArguments().getLong(ARG_DATE);
            Date date = dateMillis != -1 ? new Date(dateMillis) : null;

            expense = new Expense(expenseId, userId, description, categoryId, amount, date);
        }

        selectedDate = Calendar.getInstance();
        if (expense.getDate() != null) {
            selectedDate.setTime(expense.getDate());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_expense, container, false);

        etDescription = view.findViewById(R.id.etDescription);
        etAmount = view.findViewById(R.id.etAmount);
        etDate = view.findViewById(R.id.etDate);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnPickDate = view.findViewById(R.id.btnPickDate);

        // Điền dữ liệu hiện tại vào các trường
        etDescription.setText(expense.getDescription());
        etAmount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(expense.getDate() != null ? sdf.format(expense.getDate()) : "");

        // Load danh sách danh mục
        loadCategories();

        // Thiết lập DatePickerDialog
        btnPickDate.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý sự kiện nhấn nút Save
        btnSave.setOnClickListener(v -> confirmSave());

        // Xử lý sự kiện nhấn nút Delete
        btnDelete.setOnClickListener(v -> confirmDelete());

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
        for (AddExpenseFragment.CategoryItem category : categoryList) {
            categoryNames.add(category.getCategoryName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Chọn danh mục hiện tại của chi tiêu
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getCategoryId() == expense.getCategoryID()) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void confirmSave() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Save")
                .setMessage("Are you sure you want to save changes?")
                .setPositiveButton("Yes", (dialog, which) -> saveExpense())
                .setNegativeButton("No", null)
                .show();
    }

    private void saveExpense() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
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
        int categoryId = categoryList.get(selectedCategoryPosition).getCategoryId();

        // Cập nhật thông tin chi tiêu
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setCategoryID(categoryId);

        // Lưu vào cơ sở dữ liệu
        expenseRepository.updateExpense(expense);

        // Gửi kết quả để thông báo rằng chi tiêu đã được chỉnh sửa
        Bundle result = new Bundle();
        result.putBoolean("expense_updated", true);
        getParentFragmentManager().setFragmentResult("expense_updated_request", result);

        Toast.makeText(requireContext(), "Expense updated successfully", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes", (dialog, which) -> deleteExpense())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteExpense() {
        // Xóa chi tiêu khỏi cơ sở dữ liệu
        expenseRepository.deleteExpense(expense.getExpenseID());

        // Gửi kết quả để thông báo rằng chi tiêu đã bị xóa
        Bundle result = new Bundle();
        result.putBoolean("expense_deleted", true);
        getParentFragmentManager().setFragmentResult("expense_deleted_request", result);

        Toast.makeText(requireContext(), "Expense deleted successfully", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}