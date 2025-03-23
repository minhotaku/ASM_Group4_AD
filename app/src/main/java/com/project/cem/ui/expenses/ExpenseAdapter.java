// com.project.cem.ui.expenses/ExpenseAdapter.java
package com.project.cem.ui.expenses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.viewmodel.ExpenseViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expenseList = new ArrayList<>();
    private ExpenseViewModel viewModel;
    private OnExpenseClickListener onExpenseClickListener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public ExpenseAdapter(ExpenseViewModel viewModel, OnExpenseClickListener listener) {
        this.viewModel = viewModel;
        this.onExpenseClickListener = listener;
    }

    public void setExpenseList(List<Expense> expenses) {
        this.expenseList = expenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.tvDescription.setText(expense.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (expense.getDate() != null) {
            holder.tvDate.setText("Date: " + sdf.format(expense.getDate()));
        } else {
            holder.tvDate.setText("Date: N/A");
        }

        holder.tvAmount.setText("Amount: $" + String.format(Locale.getDefault(), "%.2f", expense.getAmount()));

        String categoryName = viewModel.getCategoryName(expense.getCategoryID());
        holder.tvCategory.setText("Category: " + categoryName);

        // Thêm sự kiện nhấn vào mục chi tiêu
        holder.itemView.setOnClickListener(v -> {
            if (onExpenseClickListener != null) {
                onExpenseClickListener.onExpenseClick(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvDate, tvAmount, tvCategory;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}