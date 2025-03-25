package com.project.cem.ui.expense;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.project.cem.R;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.viewmodel.ExpenseCategoryViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExpenseCategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextInputLayout categoryNameInputLayout;
    private EditText categoryNameEditText;
    private Button addButton;
    private LinearProgressIndicator progressIndicator;
    private View emptyView;

    private ExpenseCategoryViewModel viewModel;
    private ExpenseCategoryAdapter adapter;

    // Map để lưu trữ observers với key là dialogId
    private Map<String, DialogObservers> dialogObserversMap = new HashMap<>();

    // Class để nhóm observers của một dialog
    private static class DialogObservers {
        String dialogId; // Unique ID for each dialog
        Observer<Boolean> successObserver;
        Observer<String> errorObserver;
        AlertDialog dialog; // Reference to the dialog
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_category, container, false);

        // Khởi tạo các View
        recyclerView = view.findViewById(R.id.category_recycler_view);
        categoryNameInputLayout = view.findViewById(R.id.category_name_input_layout);
        categoryNameEditText = view.findViewById(R.id.edit_category_name);
        addButton = view.findViewById(R.id.button_add_category);
        progressIndicator = view.findViewById(R.id.progress_indicator);
        emptyView = view.findViewById(R.id.empty_view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ExpenseCategoryViewModel.class);

        // Thiết lập adapter
        adapter = new ExpenseCategoryAdapter(new ExpenseCategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryEditClick(int categoryID, String currentName) {
                showEditCategoryDialog(categoryID, currentName);
                viewModel.clearError();
            }

            @Override
            public void onCategoryDeleteClick(int categoryID) {
                showDeleteConfirmDialog(categoryID);
            }
        });
        recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.updateCategories(categories);
            checkEmptyState(categories.isEmpty());
        });

        viewModel.getCategoryCounts().observe(getViewLifecycleOwner(), counts -> {
            adapter.updateCounts(counts);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            addButton.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
                categoryNameInputLayout.setError(message);

            } else {
                categoryNameInputLayout.setError(null);
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                // Xóa input và error
                categoryNameEditText.setText("");
                categoryNameInputLayout.setError(null);
            }
        });

        // Xử lý sự kiện nút thêm
        addButton.setOnClickListener(v -> {
            String categoryName = categoryNameEditText.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                categoryNameInputLayout.setError(null);
                viewModel.addCategory(categoryName);
            } else {
                categoryNameInputLayout.setError("Vui lòng nhập tên danh mục");
            }
        });

        // Tải dữ liệu
        viewModel.loadCategories();
    }

    private void checkEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showEditCategoryDialog(int categoryID, String currentName) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Sửa loại chi tiêu");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_category, null);
        TextInputLayout editTextLayout = dialogView.findViewById(R.id.edit_category_layout);
        EditText input = dialogView.findViewById(R.id.edit_category_name);
        input.setText(currentName);

        builder.setView(dialogView);

        // Tạo dialog với kiểu đúng
        final AlertDialog dialog = builder.setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", (dialog1, which) -> dialog1.cancel())
                .create();

        // Tạo unique ID cho dialog này
        final String dialogId = UUID.randomUUID().toString();

        // Tạo đối tượng DialogObservers mới
        DialogObservers observers = new DialogObservers();
        observers.dialogId = dialogId;
        observers.dialog = dialog;

        // Tạo observers mới với tham chiếu đến dialogId
        observers.successObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success != null && success) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    // Xóa observers khi dialog đóng
                    removeDialogObservers(dialogId);
                }
            }
        };

        observers.errorObserver = new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    editTextLayout.setError(message);
                } else {
                    editTextLayout.setError(null);
                }
            }
        };

        // Đăng ký observers với lifecycle của Fragment
        if (isAdded() && !isRemoving() && !isDetached()) {
            viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), observers.successObserver);
            viewModel.getErrorMessage().observe(getViewLifecycleOwner(), observers.errorObserver);
        }

        // Lưu các observers vào map với dialogId làm key
        dialogObserversMap.put(dialogId, observers);

        // Đăng ký listener để xóa observers khi dialog bị đóng
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                removeDialogObservers(dialogId);
            }
        });

        // Hiển thị dialog
        dialog.show();

        // Ghi đè xử lý nút Lưu để không đóng dialog khi có lỗi
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) {
                    editTextLayout.setError("Tên danh mục không được để trống");
                    return;
                }

                ExpenseCategory category = new ExpenseCategory();
                category.setCategoryID(categoryID);
                category.setCategoryName(newName);

                viewModel.updateCategory(category);
            }
        });
    }

    private void removeDialogObservers(String dialogId) {
        DialogObservers observers = dialogObserversMap.get(dialogId);
        if (observers != null) {
            if (observers.successObserver != null && isAdded() && !isRemoving() && !isDetached()) {
                try {
                    viewModel.getOperationSuccess().removeObserver(observers.successObserver);
                } catch (Exception e) {
                    // Ngăn chặn lỗi khi observer đã bị remove hoặc Fragment không còn hợp lệ
                }
            }
            if (observers.errorObserver != null && isAdded() && !isRemoving() && !isDetached()) {
                try {
                    viewModel.getErrorMessage().removeObserver(observers.errorObserver);
                } catch (Exception e) {
                    // Ngăn chặn lỗi khi observer đã bị remove hoặc Fragment không còn hợp lệ
                }
            }
            // Xóa khỏi map
            dialogObserversMap.remove(dialogId);
        }
    }

    private void showDeleteConfirmDialog(int categoryID) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa loại chi tiêu này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteCategory(categoryID);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        // Xóa tất cả observers trước khi hủy view
        for (String dialogId : dialogObserversMap.keySet()) {
            removeDialogObservers(dialogId);
        }
        dialogObserversMap.clear();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        // Đảm bảo tất cả dialog đều bị đóng khi Fragment bị hủy
        for (DialogObservers observers : dialogObserversMap.values()) {
            if (observers.dialog != null && observers.dialog.isShowing()) {
                observers.dialog.dismiss();
            }
        }
        dialogObserversMap.clear();
        super.onDestroy();
    }
}