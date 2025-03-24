package com.project.cem.ui.expenses;

import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.SQLiteHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditExpenseFragment extends Fragment {

    private static final String ARG_EXPENSE = "expense";

    private Expense expense;
    private EditText etAmount;
    private Spinner spinnerCategory;
    private EditText etDescription;
    private EditText etDate;
    private Button btnSave;
    private ExpenseRepository expenseRepository;
    private ExpenseCategoryRepository categoryRepository;
    private List<ExpenseCategory> allCategories; // Danh sách tất cả danh mục
    private List<String> categoryNames; // Danh sách tên danh mục để hiển thị trong Spinner

    public EditExpenseFragment() {
        // Required empty public constructor
    }

    public static EditExpenseFragment newInstance(Expense expense) {
        EditExpenseFragment fragment = new EditExpenseFragment();
        Bundle args = new Bundle();
        args.putInt("expense_id", expense.getExpenseID());
        args.putDouble("amount", expense.getAmount());
        args.putInt("category_id", expense.getCategoryID());
        args.putString("description", expense.getDescription());
        args.putLong("date", expense.getDate().getTime());
        args.putInt("user_id", expense.getUserID());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
        expenseRepository = new ExpenseRepository(dbHelper);
        categoryRepository = new ExpenseCategoryRepository(dbHelper);

        if (getArguments() != null) {
            expense = new Expense();
            expense.setExpenseID(getArguments().getInt("expense_id"));
            expense.setAmount(getArguments().getDouble("amount"));
            expense.setDescription(getArguments().getString("description"));
            expense.setDate(new Date(getArguments().getLong("date")));
            expense.setCategoryID(getArguments().getInt("category_id"));
            expense.setUserID(getArguments().getInt("user_id"));
        }

        // Lấy danh sách tất cả danh mục
        allCategories = categoryRepository.getAllCategories(expense.getUserID());
        categoryNames = new ArrayList<>();
        for (ExpenseCategory cat : allCategories) {
            categoryNames.add(cat.getCategoryName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_expense, container, false);

        etAmount = view.findViewById(R.id.etAmount);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        etDescription = view.findViewById(R.id.etDescription);
        etDate = view.findViewById(R.id.etDate);
        btnSave = view.findViewById(R.id.btnSave);

        // Thiết lập Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Hiển thị thông tin chi tiêu hiện tại
        if (expense != null) {
            etAmount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));
            etDescription.setText(expense.getDescription() != null ? expense.getDescription() : "");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etDate.setText(dateFormat.format(expense.getDate()));

            // Đặt danh mục hiện tại làm lựa chọn mặc định trong Spinner
            String currentCategoryName = categoryRepository.getCategoryNameById(expense.getCategoryID());
            if (currentCategoryName != null) {
                int position = categoryNames.indexOf(currentCategoryName);
                if (position != -1) {
                    spinnerCategory.setSelection(position);
                }
            }
        }

        // Thiết lập Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle("Edit Expense");
        }
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Xử lý sự kiện nhấn nút Save
        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            int selectedCategoryPosition = spinnerCategory.getSelectedItemPosition();
            String description = etDescription.getText().toString().trim();
            String dateStr = etDate.getText().toString().trim();

            if (amountStr.isEmpty() || selectedCategoryPosition == -1 || dateStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = dateFormat.parse(dateStr);

                // Lấy categoryID từ danh mục được chọn
                int categoryId = allCategories.get(selectedCategoryPosition).getCategoryID();

                // Cập nhật chi tiêu
                expense.setAmount(amount);
                expense.setCategoryID(categoryId);
                expense.setDescription(description);
                expense.setDate(date);
                expenseRepository.updateExpense(expense);

                // Gửi kết quả về ExpensesFragment
                Bundle result = new Bundle();
                result.putBoolean("expense_updated", true);
                getParentFragmentManager().setFragmentResult("expense_updated_request", result);

                // Quay lại ExpensesFragment
                getParentFragmentManager().popBackStack();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Invalid date format (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}