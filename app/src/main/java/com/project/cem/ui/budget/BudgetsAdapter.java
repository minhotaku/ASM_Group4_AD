package com.project.cem.ui.budget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Import for ContextCompat
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.BudgetRepository;
import com.project.cem.viewmodel.BudgetViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.BudgetViewHolder> {

    private List<Budget> budgets;
    private List<ExpenseCategory> categoriesList;
    private final BudgetViewModel budgetViewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final Context context;
    private final BudgetRepository budgetRepository;

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

        // Combine start and end date into a single string
        String dateRange = String.format(
                "From %s to %s",
                dateFormat.format(currentBudget.getStartDate()),
                dateFormat.format(currentBudget.getEndDate())
        );
        holder.tvDateRange.setText(dateRange); // Use the combined date range

        SQLiteDatabase db = budgetRepository.getReadableDatabase();
        double totalExpenses = budgetRepository.getTotalExpensesForCategory(
                db,
                currentBudget.getUserID(),
                currentBudget.getCategoryID(),
                currentBudget.getStartDate(),
                currentBudget.getEndDate()
        );

        double percentage = (currentBudget.getAmount() > 0) ? (totalExpenses / currentBudget.getAmount()) * 100 : 0;
        percentage = Math.min(percentage, 100);

        String progressText;
        int textColor;

        if (totalExpenses > currentBudget.getAmount()) {
            progressText = String.format(Locale.getDefault(), "Overspent! (%.0f VND)", totalExpenses - currentBudget.getAmount());
            textColor = ContextCompat.getColor(context, R.color.red);
        } else {
            progressText = String.format(Locale.getDefault(), "Spent: %.0f / %.0f VND (%.2f%%)", totalExpenses, currentBudget.getAmount(), percentage);
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

    @Override
    public int getItemCount() {
        return budgets == null ? 0 : budgets.size();
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = new ArrayList<>(budgets); // Tạo bản sao để tránh thay đổi danh sách gốc

        // Sắp xếp danh sách theo startDate mới nhất trước
        Collections.sort(this.budgets, (b1, b2) -> b2.getStartDate().compareTo(b1.getStartDate()));

        notifyDataSetChanged(); // Cập nhật RecyclerView
    }


    public void setCategories(List<ExpenseCategory> categories) {
        this.categoriesList = categories;
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange; // Combined date range TextView
        TextView tvCategoryName;
        TextView tvBudgetProgress;

        public BudgetViewHolder(View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.tv_date_range); // Use a single TextView for the date range
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvBudgetProgress = itemView.findViewById(R.id.tv_budget_progress);
        }
    }
}