package com.project.cem.ui.budget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.viewmodel.BudgetViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.BudgetViewHolder> {

    private List<Budget> budgets;
    private List<ExpenseCategory> categoriesList;
    private BudgetViewModel budgetViewModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Context context;

    public BudgetsAdapter(List<Budget> budgets, List<ExpenseCategory> categories, BudgetViewModel budgetViewModel, Context context) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        this.categoriesList = categories != null ? categories : new ArrayList<>();
        this.budgetViewModel = budgetViewModel;
        this.context = context;
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

        holder.tvAmount.setText(String.format(Locale.getDefault(), "%.0f VND", currentBudget.getAmount()));
        holder.tvStartDate.setText(dateFormat.format(currentBudget.getStartDate()));
        holder.tvEndDate.setText(dateFormat.format(currentBudget.getEndDate()));

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
        return budgets == null ? 0 : budgets.size(); // Sửa để tránh NullPointerException
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    public void setCategories(List<ExpenseCategory> categories) {
        this.categoriesList = categories;
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount;
        TextView tvStartDate;
        TextView tvEndDate;
        TextView tvCategoryName;

        public BudgetViewHolder(View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
            tvEndDate = itemView.findViewById(R.id.tv_end_date);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}