package com.project.cem.ui.budget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.BudgetRepository;
import com.project.cem.viewmodel.BudgetViewModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.BudgetViewHolder> {

    private List<Budget> budgets;
    private List<ExpenseCategory> categoriesList;
    private final BudgetViewModel budgetViewModel;
    private final Context context;
    private final BudgetRepository budgetRepository;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public BudgetsAdapter(List<Budget> budgets, List<ExpenseCategory> categories, BudgetViewModel budgetViewModel, Context context) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        this.categoriesList = categories != null ? categories : new ArrayList<>();
        this.budgetViewModel = budgetViewModel;
        this.context = context;
        this.budgetRepository = new BudgetRepository(context);
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.budget_item, parent, false);
        return new BudgetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget currentBudget = budgets.get(position);

        String categoryName = getCategoryName(currentBudget.getCategoryID());
        holder.tvCategoryName.setText(categoryName);

        // Display month and year
        String monthYear = getMonthName(currentBudget.getMonth()) + " " + currentBudget.getYear();
        holder.tvDateRange.setText(monthYear);


        SQLiteDatabase db = budgetRepository.getReadableDatabase();
        // Use new getTotalExpensesForCategory method with month and year
        double totalExpenses = budgetRepository.getTotalExpensesForCategory(
                db,
                currentBudget.getUserID(),
                currentBudget.getCategoryID(),
                currentBudget.getMonth(), // Pass month
                currentBudget.getYear()   // Pass year
        );

        double percentage = (currentBudget.getAmount() > 0) ? (totalExpenses / currentBudget.getAmount()) * 100 : 0;
        percentage = Math.min(percentage, 100);

        String progressText;
        int textColor;

        if (totalExpenses > currentBudget.getAmount()) {
            progressText = String.format(Locale.getDefault(), "Overspent! (%s / %s VND)", decimalFormat.format(totalExpenses), decimalFormat.format(currentBudget.getAmount()));
            textColor = ContextCompat.getColor(context, R.color.red);
        } else {
            progressText = String.format(Locale.getDefault(), "Spent: %s / %s VND (%.2f%%)", decimalFormat.format(totalExpenses), decimalFormat.format(currentBudget.getAmount()), percentage);
            textColor = ContextCompat.getColor(context, R.color.green);
        }

        holder.tvBudgetProgress.setText(progressText);
        holder.tvBudgetProgress.setTextColor(textColor);

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                EditBudgetFragment editFragment = EditBudgetFragment.newInstance(currentBudget.getBudgetID());
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, editFragment)
                        .addToBackStack(null)
                        .commit();
            }
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
    // Helper method to get month name from month number
    private String getMonthName(int month) {
        String[] monthNames = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        if (month >= 1 && month <= 12) {
            return monthNames[month - 1]; // Month is 1-indexed
        }
        return "Invalid Month";
    }

    @Override
    public int getItemCount() {
        return budgets == null ? 0 : budgets.size();
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        // Sort by year, then by month (descending order)
        Collections.sort(this.budgets, (b1, b2) -> {
            if (b2.getYear() != b1.getYear()) {
                return Integer.compare(b2.getYear(), b1.getYear()); // Sort by year first
            }
            return Integer.compare(b2.getMonth(), b1.getMonth()); // Then sort by month
        });
        notifyDataSetChanged();
    }


    public void setCategories(List<ExpenseCategory> categories) {
        this.categoriesList = categories;
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange;
        TextView tvCategoryName;
        TextView tvBudgetProgress;

        public BudgetViewHolder(View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.tv_date_range); // Use the combined date range TextView
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvBudgetProgress = itemView.findViewById(R.id.tv_budget_progress);
        }
    }
}