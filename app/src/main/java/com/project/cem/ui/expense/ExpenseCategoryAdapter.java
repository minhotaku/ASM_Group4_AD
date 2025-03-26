package com.project.cem.ui.expense;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.project.cem.R;
import com.project.cem.model.ExpenseCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseCategoryAdapter extends RecyclerView.Adapter<ExpenseCategoryAdapter.CategoryViewHolder> {

    private List<ExpenseCategory> categories = new ArrayList<>();
    private Map<Integer, Integer> categoryCounts = new HashMap<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryEditClick(int categoryID, String currentName);
        void onCategoryDeleteClick(int categoryID);
    }

    public ExpenseCategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void updateCategories(List<ExpenseCategory> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void updateCounts(Map<Integer, Integer> counts) {
        this.categoryCounts = counts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_modern, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        ExpenseCategory category = categories.get(position);
        holder.categoryNameTextView.setText(category.getCategoryName());

        int count = categoryCounts.getOrDefault(category.getCategoryID(), 0);
        String expenseText = count <= 1 ? " expense" : " expenses";
        holder.expenseCountChip.setText(count + expenseText);

        // Disable nút xóa nếu danh mục có chi tiêu
        holder.deleteButton.setEnabled(count == 0);
        holder.deleteButton.setAlpha(count == 0 ? 1.0f : 0.5f);

        holder.editButton.setOnClickListener(v -> {
            listener.onCategoryEditClick(category.getCategoryID(), category.getCategoryName());
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (count == 0) {
                listener.onCategoryDeleteClick(category.getCategoryID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        Chip expenseCountChip;
        MaterialButton editButton;
        MaterialButton deleteButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.text_category_name);
            expenseCountChip = itemView.findViewById(R.id.chip_expense_count);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}