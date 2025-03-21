package com.project.cem.ui.budget;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
    private int budgetId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private List<ExpenseCategory> categoriesList = new ArrayList<>();

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

        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        budgetViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, getCategoryNames(categories));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCategory.setAdapter(adapter);

            loadBudgetInfo(); // Load sau khi đã có danh sách categories
        });



        edtStartDate.setOnClickListener(v -> showDatePickerDialog(edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePickerDialog(edtEndDate));

        btnUpdate.setOnClickListener(v -> updateBudget());
        budgetViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage ->{
            if(errorMessage!= null && !errorMessage.isEmpty()){
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private void loadBudgetInfo() {
        // Lấy danh sách budgets từ LiveData
        List<Budget> budgets = budgetViewModel.getAllBudgets().getValue();

        if (budgets != null) {
            // Tìm budget có budgetId tương ứng
            Budget budget = null;
            for (Budget b : budgets) {
                if (b.getBudgetID() == budgetId) {
                    budget = b;
                    break;
                }
            }
            if (budget != null) {
                edtAmount.setText(String.valueOf(budget.getAmount()));
                edtStartDate.setText(dateFormat.format(budget.getStartDate()));
                edtEndDate.setText(dateFormat.format(budget.getEndDate()));

                for (int i = 0; i < categoriesList.size(); i++) {
                    if (categoriesList.get(i).getCategoryID() == budget.getCategoryID()) {
                        spnCategory.setSelection(i);
                        break;
                    }
                }
            } else {
                Log.d("DEBUG", "loadBudgetInfo: Budget not found for budgetId: " + budgetId);
            }

        } else {
            Log.d("DEBUG", "loadBudgetInfo: null"); // Log nếu budget list là null.
        }

    }


    private void updateBudget() {

        // Log để debug
        Log.d("EditBudgetFragment", "categoriesList: " + categoriesList);
        Log.d("EditBudgetFragment", "spnCategory: " + spnCategory);

        String amountStr = edtAmount.getText().toString().trim();
        String startDateStr = edtStartDate.getText().toString().trim();
        String endDateStr = edtEndDate.getText().toString().trim();


        if (spnCategory.getSelectedItem() == null || amountStr.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            Log.d("EditBudgetFragment", "spnCategory.getSelectedItem(): " + spnCategory.getSelectedItem());
            return;
        }
        //Kiểm tra null
        if (categoriesList == null || categoriesList.isEmpty() || spnCategory.getSelectedItemPosition() < 0 || spnCategory.getSelectedItemPosition() >= categoriesList.size()) {
            Log.e("EditBudgetFragment", "categoriesList is null or empty, or invalid position");
            Toast.makeText(getContext(), "No categories available.", Toast.LENGTH_SHORT).show();
            return;
        }
        int categoryId = categoriesList.get(spnCategory.getSelectedItemPosition()).getCategoryID();
        double amount = Double.parseDouble(amountStr);

        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            com.project.cem.model.User user = com.project.cem.utils.UserPreferences.getUser(getContext());
            Log.d("EditBudgetFragment", "User: "+ user);
            if (user == null) {
                Log.e("EditBudgetFragment", "User is null. Cannot update budget.");
                Toast.makeText(getContext(), "User is null. Cannot update budget.", Toast.LENGTH_SHORT).show();
                return;
            }
            int userID = user.getUserID();

            Budget updatedBudget = new Budget(budgetId, userID, categoryId, amount, startDate, endDate);

            budgetViewModel.update(updatedBudget);

            requireActivity().getSupportFragmentManager().popBackStack();

        } catch (ParseException e) {
            Log.e("EditBudgetFragment", "Error parsing date", e);
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return; //Thêm return
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
    private List<String> getCategoryNames(List<ExpenseCategory> categories) {
        List<String> names = new ArrayList<>();
        for (ExpenseCategory category : categories) {
            names.add(category.getCategoryName());
        }
        return names;
    }

}