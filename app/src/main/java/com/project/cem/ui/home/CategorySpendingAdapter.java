package com.project.cem.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.project.cem.R;
import com.project.cem.model.CategorySpending;

import java.text.NumberFormat;
import java.util.Locale;

public class CategorySpendingAdapter extends ListAdapter<CategorySpending, CategorySpendingAdapter.ViewHolder> {

    public CategorySpendingAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_spending, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategorySpending item = getItem(position);
        holder.bind(item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryNameText;
        private final TextView percentageText;
        private final TextView amountText;
        private final View colorIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.categoryNameText);
            percentageText = itemView.findViewById(R.id.percentageText);
            amountText = itemView.findViewById(R.id.amountText);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }

        public void bind(CategorySpending item) {
            categoryNameText.setText(item.getCategoryName());
            percentageText.setText(String.format(Locale.getDefault(), "%.1f%%", item.getPercentage()));

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
            amountText.setText(currencyFormat.format(item.getAmount()));

            colorIndicator.setBackgroundColor(item.getColorCode());
        }
    }

    private static final DiffUtil.ItemCallback<CategorySpending> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CategorySpending>() {
                @Override
                public boolean areItemsTheSame(@NonNull CategorySpending oldItem, @NonNull CategorySpending newItem) {
                    return oldItem.getCategoryId() == newItem.getCategoryId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull CategorySpending oldItem, @NonNull CategorySpending newItem) {
                    return oldItem.getAmount() == newItem.getAmount() &&
                            oldItem.getPercentage() == newItem.getPercentage();
                }
            };
}