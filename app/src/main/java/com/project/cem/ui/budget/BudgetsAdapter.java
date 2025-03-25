package com.project.cem.ui.budget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.BudgetRepository;
import com.project.cem.repository.SpendingOverviewRepository;
import com.project.cem.utils.DateUtils;
import com.project.cem.utils.VndCurrencyFormatter;
import com.project.cem.viewmodel.BudgetViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.BudgetViewHolder> {

    private List<Budget> budgets;
    private List<ExpenseCategory> categoriesList;
    private final BudgetViewModel budgetViewModel;
    private final Context context;
    private final SpendingOverviewRepository spendingOverviewRepository;
    private final BudgetRepository budgetRepository; // Add

    // Use formatter
    private final VndCurrencyFormatter currencyFormatter = new VndCurrencyFormatter();

    public BudgetsAdapter(List<Budget> budgets, List<ExpenseCategory> categories, BudgetViewModel budgetViewModel, Context context) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        this.categoriesList = categories != null ? categories : new ArrayList<>();
        this.budgetViewModel = budgetViewModel;
        this.context = context;
        this.spendingOverviewRepository = new SpendingOverviewRepository(context);
        this.budgetRepository = new BudgetRepository(context); // Initialize
    }
    public void setCategories(List<ExpenseCategory> categories) {
        this.categoriesList = categories;
        notifyDataSetChanged();
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
        String monthYear = DateUtils.getMonthName(currentBudget.getMonth()) + " " + currentBudget.getYear();
        holder.tvDateRange.setText(monthYear);


        // Get total expenses.  Get database from BudgetRepository.
        SQLiteDatabase db = budgetRepository.getReadableDatabase();
        double totalExpenses = spendingOverviewRepository.getTotalExpensesForCategory(
                db,
                currentBudget.getUserID(),
                currentBudget.getCategoryID(),
                currentBudget.getMonth(),
                currentBudget.getYear()
        );
        db.close();

        double percentage = (currentBudget.getAmount() > 0) ? (totalExpenses / currentBudget.getAmount()) * 100 : 0;
        percentage = Math.min(percentage, 100);


        // Set progress bar
        holder.progressBar.setProgress((int) percentage);
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));

        if (percentage < 50) {
            holder.progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(context, R.color.blue), PorterDuff.Mode.SRC_IN);
        } else if (percentage < 100) {
            holder.progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(context, R.color.yellow), PorterDuff.Mode.SRC_IN);
        } else {
            holder.progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN);
        }

        // overspent logic
        if (totalExpenses > currentBudget.getAmount()) {
            holder.tvBudgetProgress.setText(String.format(Locale.getDefault(), "%s / %s", currencyFormatter.format(totalExpenses), currencyFormatter.format(currentBudget.getAmount())));
            holder.tvBudgetProgress.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.tvBudgetProgress.setText(String.format(Locale.getDefault(), "%s / %s", currencyFormatter.format(totalExpenses), currencyFormatter.format(currentBudget.getAmount())));
            holder.tvBudgetProgress.setTextColor(ContextCompat.getColor(context, R.color.green));
        }

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

    @Override
    public int getItemCount() {
        return budgets == null ? 0 : budgets.size();
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        // Sort by year
        Collections.sort(this.budgets, (b1, b2) -> {
            if (b2.getYear() != b1.getYear()) {
                return Integer.compare(b2.getYear(), b1.getYear());
            }
            return Integer.compare(b2.getMonth(), b1.getMonth());
        });
        notifyDataSetChanged();
    }


    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange;
        TextView tvCategoryName;
        TextView tvBudgetProgress;
        ProgressBar progressBar;
        TextView tvPercentage;


        public BudgetViewHolder(View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.tv_date_range);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvBudgetProgress = itemView.findViewById(R.id.tv_budget_progress);
            progressBar = itemView.findViewById(R.id.pb_item_progress);
            tvPercentage = itemView.findViewById(R.id.tv_item_percentage);
        }
    }
}