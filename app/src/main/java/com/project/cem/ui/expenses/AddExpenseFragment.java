package com.project.cem.ui.expenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.utils.UserPreferences;
import com.project.cem.viewmodel.ExpenseViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {
    private ExpenseViewModel expenseViewModel;
    private Spinner spinnerCategory;
    private EditText editTextDescription, editTextAmount;
    private TextView textViewSelectedDate;
    private Button buttonSelectDate, buttonAddExpense;
    private String selectedDate = "";
    private Date expenseDate;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // Khởi tạo ViewModel
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // Ánh xạ View
        spinnerCategory = root.findViewById(R.id.spinnerCategory);
        editTextDescription = root.findViewById(R.id.editTextDescription);
        editTextAmount = root.findViewById(R.id.editTextAmount);
        textViewSelectedDate = root.findViewById(R.id.textViewSelectedDate);
        buttonSelectDate = root.findViewById(R.id.buttonSelectDate);
        buttonAddExpense = root.findViewById(R.id.buttonAddExpense);

        // Danh sách loại chi tiêu
        String[] categories = {"Transportation", "Entertainment", "Housing", "Utilities", "Health", "Education"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        // Xử lý chọn ngày
        buttonSelectDate.setOnClickListener(v -> showDatePicker());

        // Xử lý thêm chi tiêu
        buttonAddExpense.setOnClickListener(v -> addExpense());

        return root;
    }

    // Hiển thị DatePicker
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    textViewSelectedDate.setText(selectedDate);
                    expenseDate = calendar.getTime(); // Lưu lại ngày đã chọn
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    // Xử lý thêm chi tiêu
    private void addExpense() {
        String category = spinnerCategory.getSelectedItem().toString();
        String description = editTextDescription.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();

        if (description.isEmpty() || amountStr.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int userID = UserPreferences.getUser(requireContext()).getUserID();
        int categoryID = getCategoryID(category);

        Expense newExpense = new Expense(0, userID, description, categoryID, amount, expenseDate);

        // Gửi dữ liệu đến ViewModel để lưu vào DB
        expenseViewModel.insertExpense(newExpense);

        // Thông báo thành công
        Toast.makeText(requireContext(), "Expense Added!", Toast.LENGTH_SHORT).show();

        // Reset form
        editTextDescription.setText("");
        editTextAmount.setText("");
        textViewSelectedDate.setText("No date selected");
        selectedDate = "";
        expenseDate = null;
    }

    // Chuyển đổi từ tên danh mục sang categoryID
    private int getCategoryID(String category) {
        switch (category) {
            case "Food": return 1;
            case "Transportation": return 2;
            case "Entertainment": return 3;
            case "Housing": return 4;
            case "Utilities": return 5;
            case "Health": return 6;
            case "Education": return 7;
            default: return 0;
        }
    }

}