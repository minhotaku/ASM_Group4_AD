package com.project.cem.ui.expenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.viewmodel.ExpenseCategoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<ExpenseCategoryViewModel.CategoryWithCount> categoryList;
    private final int containerId; // Thêm biến để lưu container ID

    public CategoryAdapter(int containerId) {
        this.containerId = containerId;
        this.categoryList = new ArrayList<>();
    }

    public void setCategoryList(List<ExpenseCategoryViewModel.CategoryWithCount> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        ExpenseCategoryViewModel.CategoryWithCount categoryWithCount = categoryList.get(position);
        holder.bind(categoryWithCount);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryName;
        private final TextView tvExpenseCount;
        private final ImageButton btnEdit;
        private final ImageButton btnMenu;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvExpenseCount = itemView.findViewById(R.id.tvExpenseCount);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }

        public void bind(ExpenseCategoryViewModel.CategoryWithCount categoryWithCount) {
            tvCategoryName.setText(categoryWithCount.getCategory().getCategoryName());
            int count = categoryWithCount.getCount();
            if (count > 0) {
                tvExpenseCount.setText("(" + count + ")");
                tvExpenseCount.setVisibility(View.VISIBLE);
            } else {
                tvExpenseCount.setVisibility(View.GONE);
            }

            // Xử lý sự kiện nhấn nút Edit
            btnEdit.setOnClickListener(v -> {
                FragmentActivity activity = (FragmentActivity) itemView.getContext();
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.replace(containerId, EditCategoryFragment.newInstance(categoryWithCount.getCategory())); // Sử dụng containerId
                transaction.addToBackStack(null);
                transaction.commit();
            });

            // Xử lý sự kiện nhấn nút Delete
            btnMenu.setOnClickListener(v -> {
                // Kiểm tra số lượng bản ghi chi tiêu liên quan đến danh mục
                SQLiteHelper dbHelper = new SQLiteHelper(itemView.getContext());
                ExpenseRepository expenseRepository = new ExpenseRepository(dbHelper);
                int expenseCount = expenseRepository.getExpenseCountByCategory(categoryWithCount.getCategory().getCategoryID());

                if (expenseCount > 0) {
                    // Nếu danh mục có bản ghi chi tiêu, hiển thị thông báo và không cho phép xóa
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Cannot Delete")
                            .setMessage("Cannot delete category because it has associated expenses")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();                } else {
                    // Nếu không có bản ghi chi tiêu, hiển thị dialog xác nhận xóa
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Category")
                            .setMessage("Are you sure you want to delete this category?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Xóa danh mục
                                ExpenseCategoryRepository repository = new ExpenseCategoryRepository(itemView.getContext());
                                repository.deleteExpenseCategory(categoryWithCount.getCategory().getCategoryID());

                                // Gửi kết quả về CategoryFragment
                                Bundle result = new Bundle();
                                result.putBoolean("category_deleted", true);
                                ((FragmentActivity) itemView.getContext()).getSupportFragmentManager()
                                        .setFragmentResult("category_deleted_request", result);
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        }
    }
}