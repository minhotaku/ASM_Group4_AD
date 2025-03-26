package com.project.cem.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.project.cem.R;
import com.project.cem.ui.home.CategorySpendingAdapter;
import com.project.cem.model.CategorySpending;
import com.project.cem.utils.VndCurrencyFormatter;
import com.project.cem.viewmodel.SpendingOverviewViewModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private SpendingOverviewViewModel viewModel;
    private PieChart pieChart;
    private RecyclerView categoryRecyclerView;
    private CategorySpendingAdapter categoryAdapter;
    private ProgressBar loadingProgressBar;
    private TextView monthYearTextView;
    private TextView totalSpendingTextView,totalSpendingTextView2;
    private TextView totalBudgetTextView;
    private TextView budgetComparisonTextView;
    private ProgressBar budgetProgressBar;
    private LinearLayout emptyStateLayout;
    private VndCurrencyFormatter currencyFormatter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize currency formatter
        currencyFormatter = new VndCurrencyFormatter();

        // Initialize views
        pieChart = view.findViewById(R.id.spendingPieChart);
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        monthYearTextView = view.findViewById(R.id.monthYearTextView);
        totalSpendingTextView = view.findViewById(R.id.totalSpendingTextView);
        totalSpendingTextView2 = view.findViewById(R.id.totalSpendingTextView2);
        totalBudgetTextView = view.findViewById(R.id.totalBudgetTextView);
        budgetComparisonTextView = view.findViewById(R.id.budgetComparisonTextView);
        budgetProgressBar = view.findViewById(R.id.budgetProgressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        // Set current month and year
        Calendar calendar = Calendar.getInstance();
        String[] months = new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String currentMonth = months[calendar.get(Calendar.MONTH)];
        int currentYear = calendar.get(Calendar.YEAR);
        // Hiển thị tháng trên giao diện người dùng
        monthYearTextView.setText(String.format("%s %d", currentMonth, currentYear));

        // Initialize RecyclerView
        categoryAdapter = new CategorySpendingAdapter(currencyFormatter);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Initialize and configure PieChart
        setupPieChart();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SpendingOverviewViewModel.class);

        // Observe LiveData
        viewModel.getCategorySpendingData().observe(getViewLifecycleOwner(), this::updateChartData);
        viewModel.getTotalSpending().observe(getViewLifecycleOwner(), this::updateTotalSpending);
        viewModel.getTotalBudget().observe(getViewLifecycleOwner(), this::updateTotalBudget);
        viewModel.getBudgetPercentage().observe(getViewLifecycleOwner(), this::updateBudgetComparison);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::showLoading);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showError);

        // Load data
        viewModel.loadCurrentMonthSpending();
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Spending");
        pieChart.animateY(1000);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false); // Disable default legend as we'll show categories in RecyclerView
    }

    private void updateChartData(List<CategorySpending> categorySpendingList) {
        if (categorySpendingList == null || categorySpendingList.isEmpty()) {
            showEmptyState(true);
            return;
        }

        showEmptyState(false);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (CategorySpending item : categorySpendingList) {
            entries.add(new PieEntry((float) item.getAmount(), item.getCategoryName()));
            colors.add(item.getColorCode());
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending Categories");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // Refresh chart

        // Update the RecyclerView
        categoryAdapter.submitList(categorySpendingList);
    }

    private void updateTotalSpending(Double total) {
        totalSpendingTextView.setText(currencyFormatter.format(total));
        totalSpendingTextView2.setText(currencyFormatter.format(total));
    }

    private void updateTotalBudget(Double budget) {
        totalBudgetTextView.setText(currencyFormatter.format(budget));
    }

    private void updateBudgetComparison(Double percentage) {
        int progress = percentage.intValue();
        if (progress > 100) progress = 100;

        budgetProgressBar.setProgress(progress);

        // Set color based on percentage
        int color;
        if (percentage <= 50) {
            color = Color.rgb(46, 204, 113); // Green
        } else if (percentage <= 80) {
            color = Color.rgb(243, 156, 18); // Orange
        } else {
            color = Color.rgb(231, 76, 60); // Red
        }

        budgetProgressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

        // Format the percentage
        DecimalFormat df = new DecimalFormat("#.#");
        budgetComparisonTextView.setText(df.format(percentage) + "%");

        if (percentage > 100) {
            budgetComparisonTextView.setTextColor(Color.rgb(231, 76, 60)); // Red if over budget
        } else {
            budgetComparisonTextView.setTextColor(Color.BLACK);
        }
    }

    private void showLoading(Boolean isLoading) {
        if (isLoading) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            categoryRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            pieChart.setVisibility(View.VISIBLE);
            categoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            categoryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            pieChart.setVisibility(View.VISIBLE);
            categoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}