package com.project.cem.ui.setting.recurring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.model.RecurringExpense;
import com.project.cem.utils.VndCurrencyFormatter;
import com.project.cem.utils.DateUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.ViewHolder> {

    private List<RecurringExpense> recurringExpenses;
    private final Context context;
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;
    private final VndCurrencyFormatter currencyFormatter = new VndCurrencyFormatter();


    public interface OnItemClickListener {
        void onItemClick(RecurringExpense expense);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(RecurringExpense expense);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }
    private List<ExpenseCategory> categoriesList;
    // Add this setter method
    public void setCategories(List<ExpenseCategory> categories) {
        this.categoriesList = categories;
        notifyDataSetChanged(); // Notify adapter of the change
    }


    public RecurringExpenseAdapter(List<RecurringExpense> recurringExpenses,  List<ExpenseCategory> categories, Context context) {
        this.recurringExpenses = recurringExpenses;
        this.context = context;
        this.categoriesList = categories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recurring_expense_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringExpense expense = recurringExpenses.get(position);

        holder.tvDescription.setText(expense.getDescription());
        holder.tvAmount.setText(currencyFormatter.format(expense.getAmount()));
        // Use DateUtils to get month name
        holder.tvMonthYear.setText(String.format(Locale.getDefault(), "%s %d", DateUtils.getMonthName(expense.getMonth()), expense.getYear()));
        holder.tvFrequency.setText(String.format(Locale.getDefault(), "%s", expense.getRecurrenceFrequency())); //Added
        holder.tvActive.setText(String.format("Active: %s", expense.isActive()));
        // Set other fields as needed
        String categoryName = getCategoryName(expense.getCategoryID());
        holder.tvCategoryName.setText(categoryName);
        // Set up click listener
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(expense);
            }
        });

        // Set up long-click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (itemLongClickListener != null) {
                itemLongClickListener.onItemLongClick(expense);
                return true; // Consume the long-click event
            }
            return false;
        });
    }
    private String getCategoryName(int categoryId) {
        for (ExpenseCategory category : categoriesList) {
            if (category.getCategoryID() == categoryId) {
                return category.getCategoryName();
            }
        }
        return "Unknown Category";
    }

    @Override
    public int getItemCount() {
        return recurringExpenses == null ? 0 : recurringExpenses.size();
    }

    public void setRecurringExpenses(List<RecurringExpense> recurringExpenses) {
        this.recurringExpenses = recurringExpenses;
        // Sort by year, then by month (descending order)
        Collections.sort(this.recurringExpenses, (r1, r2) -> {
            if (r2.getYear() != r1.getYear()) {
                return Integer.compare(r2.getYear(), r1.getYear()); // Sort by year first
            }
            return Integer.compare(r2.getMonth(), r1.getMonth()); // Then sort by month
        });
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription;
        TextView tvAmount;
        TextView tvMonthYear;
        TextView tvFrequency;
        TextView tvActive;
        TextView tvCategoryName;


        public ViewHolder(View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_recurring_description);
            tvAmount = itemView.findViewById(R.id.tv_recurring_amount);
            tvMonthYear = itemView.findViewById(R.id.tv_recurring_month_year);
            tvFrequency = itemView.findViewById(R.id.tv_recurring_frequency);
            tvActive =  itemView.findViewById(R.id.tv_recurring_active);
            tvCategoryName = itemView.findViewById(R.id.tv_recurring_category_name);
        }
    }
}
