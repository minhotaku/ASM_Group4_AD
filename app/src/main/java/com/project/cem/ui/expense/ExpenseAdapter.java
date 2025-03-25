package com.project.cem.ui.expense;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.project.cem.R;
import com.project.cem.model.ExpenseWithCategory;
import com.project.cem.utils.VndCurrencyFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items = new ArrayList<>();
    private Map<String, List<ExpenseWithCategory>> groupedExpenses;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private VndCurrencyFormatter currencyFormatter = new VndCurrencyFormatter();
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseLongClick(ExpenseWithCategory expense);
    }

    public ExpenseAdapter(Map<String, List<ExpenseWithCategory>> groupedExpenses, OnExpenseClickListener listener) {
        this.groupedExpenses = groupedExpenses;
        this.listener = listener;
        processGroupedData();
    }

    public void updateData(Map<String, List<ExpenseWithCategory>> groupedExpenses) {
        this.groupedExpenses = groupedExpenses;
        processGroupedData();
        notifyDataSetChanged();
    }

    private void processGroupedData() {
        items.clear();

        for (Map.Entry<String, List<ExpenseWithCategory>> entry : groupedExpenses.entrySet()) {
            // Thêm header tháng
            items.add(entry.getKey());

            // mỗi nhóm được sắp xếp theo ngày
            List<ExpenseWithCategory> expensesInMonth = entry.getValue();
            items.addAll(expensesInMonth);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String monthYear = (String) items.get(position);
            headerHolder.monthYearTextView.setText(monthYear);
        } else if (holder instanceof ExpenseViewHolder) {
            ExpenseViewHolder expenseHolder = (ExpenseViewHolder) holder;
            ExpenseWithCategory expense = (ExpenseWithCategory) items.get(position);

            expenseHolder.descriptionTextView.setText(expense.getDescription());
            expenseHolder.dateTextView.setText(dateFormat.format(expense.getDate()));
            expenseHolder.amountTextView.setText(currencyFormatter.format(expense.getAmount()));

            expenseHolder.categoryChip.setText(expense.getCategoryName());

            expenseHolder.itemView.setOnLongClickListener(v -> {
                listener.onExpenseLongClick(expense);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView monthYearTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            monthYearTextView = itemView.findViewById(R.id.text_month_year);
        }
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        TextView dateTextView;
        TextView amountTextView;
        Chip categoryChip;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.text_description);
            dateTextView = itemView.findViewById(R.id.text_date);
            amountTextView = itemView.findViewById(R.id.text_amount);
            categoryChip = itemView.findViewById(R.id.text_category);
        }
    }
}