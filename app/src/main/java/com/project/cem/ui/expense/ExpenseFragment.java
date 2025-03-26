package com.project.cem.ui.expense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.project.cem.R;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.ExpenseWithCategory;
import com.project.cem.ui.setting.report.ExpenseReportFragment;
import com.project.cem.viewmodel.ExpenseCategoryViewModel;
import com.project.cem.viewmodel.ExpenseViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseFragment extends Fragment {

    private ExpenseViewModel expenseViewModel;
    private ExpenseCategoryViewModel categoryViewModel;
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private com.google.android.material.textfield.TextInputLayout filterLayout;
    private com.google.android.material.textfield.MaterialAutoCompleteTextView categoryFilterSpinner;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FloatingActionButton fabAddExpense;
    private MaterialButton manageCategoriesButton;

    private List<ExpenseCategory> categories = new ArrayList<>();
    private Map<String, List<ExpenseWithCategory>> groupedExpenses = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        try {
            // Khởi tạo các view
            recyclerView = view.findViewById(R.id.expense_recycler_view);
            filterLayout = view.findViewById(R.id.filter_layout);
            categoryFilterSpinner = view.findViewById(R.id.category_filter_spinner);
            progressBar = view.findViewById(R.id.progress_bar);
            emptyView = view.findViewById(R.id.empty_view);
            fabAddExpense = view.findViewById(R.id.fab_add_expense);
            manageCategoriesButton = view.findViewById(R.id.btn_manage_categories);

            // Thiết lập RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new ExpenseAdapter(groupedExpenses, new ExpenseAdapter.OnExpenseClickListener() {
                @Override
                public void onExpenseLongClick(ExpenseWithCategory expense) {
                    showExpenseOptionsBottomSheet(expense);
                }
            });
            recyclerView.setAdapter(adapter);

            // Thiết lập FAB
            fabAddExpense.setOnClickListener(v -> {
                showAddExpenseDialog();
            });

            // Thiết lập nút quản lý danh mục
            manageCategoriesButton.setOnClickListener(v -> {
                navigateToCategoryManagement();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Khởi tạo ViewModel
            expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
            categoryViewModel = new ViewModelProvider(this).get(ExpenseCategoryViewModel.class);

            // Quan sát dữ liệu chi tiêu
            expenseViewModel.getGroupedExpenses().observe(getViewLifecycleOwner(), groupedExpenseData -> {
                this.groupedExpenses = groupedExpenseData;
                adapter.updateData(groupedExpenseData);
                checkEmptyState();
            });

            // Quan sát trạng thái loading
            expenseViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            });

            // Quan sát thông báo lỗi
            expenseViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
                if (message != null && !message.isEmpty()) {
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
                }
            });

            // Quan sát danh sách danh mục
            categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categoryList -> {
                this.categories = categoryList;
                setupCategorySpinner();
            });

            // Tải dữ liệu
            categoryViewModel.loadCategories();
            expenseViewModel.loadExpenses();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi khởi tạo ViewModel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCategorySpinner() {
        try {
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("All Categories");

            for (ExpenseCategory category : categories) {
                categoryNames.add(category.getCategoryName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
            categoryFilterSpinner.setAdapter(adapter);

            categoryFilterSpinner.setOnItemClickListener((parent, view, position, id) -> {
                if (position == 0) {
                    // "All Categories" is selected
                    expenseViewModel.clearFilter();
                } else {
                    // Một danh mục cụ thể được chọn
                    int categoryID = categories.get(position - 1).getCategoryID();
                    expenseViewModel.setFilter(categoryID);
                }
            });

            // Mặc định hiển thị tất cả
            categoryFilterSpinner.setText("All Categories", false);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi thiết lập bộ lọc danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkEmptyState() {
        boolean isEmpty = groupedExpenses.isEmpty();
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showExpenseOptionsBottomSheet(ExpenseWithCategory expense) {
        try {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_expense_options, null);

            MaterialButton editButton = bottomSheetView.findViewById(R.id.btn_edit);
            MaterialButton deleteButton = bottomSheetView.findViewById(R.id.btn_delete);

            editButton.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                showEditExpenseDialog(expense);
            });

            deleteButton.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                showDeleteConfirmDialog(expense);
            });

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi hiển thị tùy chọn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmDialog(ExpenseWithCategory expense) {
        try {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        expenseViewModel.deleteExpense(expense.getExpenseID());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi hiển thị xác nhận xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddExpenseDialog() {
        try {
            if (categories.isEmpty()) {
                Toast.makeText(getContext(), "Danh sách danh mục trống", Toast.LENGTH_SHORT).show();
                return;
            }

            ExpenseDialogFragment dialogFragment = ExpenseDialogFragment.newInstance(categories);
            dialogFragment.setExpenseDialogListener(new ExpenseDialogFragment.ExpenseDialogListener() {
                @Override
                public void onExpenseAdded(com.project.cem.model.Expense expense) {
                    expenseViewModel.addExpense(expense);
                    Snackbar.make(requireView(), "Đã thêm khoản chi tiêu mới", Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onExpenseUpdated(com.project.cem.model.Expense expense) {
                }
            });

            dialogFragment.show(getChildFragmentManager(), "AddExpenseDialog");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi hiển thị form thêm chi tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditExpenseDialog(ExpenseWithCategory expense) {
        try {
            ExpenseDialogFragment dialogFragment = ExpenseDialogFragment.newInstance(categories, expense);
            dialogFragment.setExpenseDialogListener(new ExpenseDialogFragment.ExpenseDialogListener() {
                @Override
                public void onExpenseAdded(com.project.cem.model.Expense expense) {
                }

                @Override
                public void onExpenseUpdated(com.project.cem.model.Expense expense) {
                    expenseViewModel.updateExpense(expense);
                    Snackbar.make(requireView(), "Đã cập nhật khoản chi tiêu", Snackbar.LENGTH_SHORT).show();
                }
            });

            dialogFragment.show(getChildFragmentManager(), "EditExpenseDialog");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi hiển thị form sửa chi tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void navigateToCategoryManagement(){
        Fragment categoryFragment = new ExpenseCategoryFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, categoryFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


}