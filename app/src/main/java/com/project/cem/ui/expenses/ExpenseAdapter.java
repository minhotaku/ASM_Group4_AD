package com.project.cem.ui.expenses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project.cem.R;
import com.project.cem.model.Expense;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private final List<Expense> expenses;

    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.txtDescription.setText(expense.getDescription());
        holder.txtAmount.setText(String.format("$%.2f", expense.getAmount()));

        // Format ngày từ Date object (giả sử expense có phương thức getDate())
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(expense.getDate()); // Sử dụng getDate() của Expense
        holder.txtDate.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }
    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses.clear();
        this.expenses.addAll(newExpenses);
        notifyDataSetChanged(); // Cập nhật UI
    }


    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView txtDescription, txtAmount, txtDate;

        public ExpenseViewHolder(View itemView) {
            super(itemView);
            txtDescription = itemView.findViewById(R.id.textDescription);
            txtAmount = itemView.findViewById(R.id.textAmount);
            txtDate = itemView.findViewById(R.id.textDate);
        }
    }
    public void updateList(List<Expense> newList) {
        expenses.clear();
        expenses.addAll(newList);
        notifyDataSetChanged();
    }

}