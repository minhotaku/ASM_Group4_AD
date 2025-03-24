package com.project.cem.ui.expenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.Expense;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.SQLiteHelper;
import com.project.cem.viewmodel.ExpenseViewModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private final ExpenseViewModel viewModel;
    private final int containerId;

    public ExpenseAdapter(ExpenseViewModel viewModel, int containerId) {
        this.viewModel = viewModel;
        this.containerId = containerId;
        this.expenseList = new ArrayList<>();
    }

    public void setExpenseList(List<Expense> expenseList) {
        this.expenseList = expenseList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAmount;
        private final TextView tvCategory;
        private final TextView tvDescription;
        private final TextView tvDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);

            // Xử lý nhấn giữ (long press) để hiển thị menu ngữ cảnh
            itemView.setOnLongClickListener(v -> {
                CharSequence[] options = new CharSequence[]{"Edit", "Delete"};
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Choose an action")
                        .setItems(options, (dialog, which) -> {
                            Expense expense = expenseList.get(getAdapterPosition());
                            if (which == 0) { // Edit
                                FragmentActivity activity = (FragmentActivity) itemView.getContext();
                                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                                transaction.replace(containerId, EditExpenseFragment.newInstance(expense));
                                transaction.addToBackStack(null);
                                transaction.commit();
                            } else if (which == 1) { // Delete
                                new AlertDialog.Builder(itemView.getContext())
                                        .setTitle("Delete Expense")
                                        .setMessage("Are you sure you want to delete this expense?")
                                        .setPositiveButton("Yes", (deleteDialog, deleteWhich) -> {
                                            SQLiteHelper dbHelper = new SQLiteHelper(itemView.getContext());
                                            ExpenseRepository repository = new ExpenseRepository(dbHelper);
                                            repository.deleteExpense(expense.getExpenseID());

                                            Bundle result = new Bundle();
                                            result.putBoolean("expense_deleted", true);
                                            ((FragmentActivity) itemView.getContext()).getSupportFragmentManager()
                                                    .setFragmentResult("expense_deleted_request", result);
                                        })
                                        .setNegativeButton("No", (deleteDialog, deleteWhich) -> deleteDialog.dismiss())
                                        .show();
                            }
                        })
                        .show();
                return true;
            });
        }

        public void bind(Expense expense) {
            // Định dạng amount với phân cách phần nghìn và thêm "VNĐ"
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            String formattedAmount = decimalFormat.format(expense.getAmount()) + " VNĐ";
            tvAmount.setText(formattedAmount);

            // Lấy categoryName từ categoryID
            ExpenseCategoryRepository categoryRepository = new ExpenseCategoryRepository(itemView.getContext());
            String categoryName = categoryRepository.getCategoryNameById(expense.getCategoryID());
            tvCategory.setText(categoryName != null ? categoryName : "Unknown Category");

            // Hiển thị description
            tvDescription.setText(expense.getDescription() != null ? expense.getDescription() : "No description");

            // Hiển thị ngày
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvDate.setText(dateFormat.format(expense.getDate()));
        }
    }
}