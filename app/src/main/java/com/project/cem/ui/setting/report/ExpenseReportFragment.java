package com.project.cem.ui.setting.report;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.project.cem.R;
import com.project.cem.model.ExpenseReportModels;
import com.project.cem.utils.VndCurrencyFormatter;
import com.project.cem.viewmodel.ExpenseReportViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseReportFragment extends Fragment {

    private ExpenseReportViewModel viewModel;
    private BarChart dailyExpensesChart;
    private BarChart categoryExpensesChart;
    private TextView tvMonthYear;
    private TextView tvTotalExpenses;
    private TextView tvExpenseListTotal;
    private TextView tvExpenseListBudget;
    private TableLayout tableExpenses;
    private MaterialButton btnToggleYearlyReport;
    private MaterialButton btnExportReport;
    private ImageButton btnPreviousMonth;
    private ImageButton btnNextMonth;
    private VndCurrencyFormatter vndFormatter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ExpenseReportViewModel.class);
        vndFormatter = new VndCurrencyFormatter();

        // Initialize UI components
        initializeViews(view);
        setupListeners();
        setupObservers();
    }

    private void initializeViews(View view) {
        dailyExpensesChart = view.findViewById(R.id.chart_daily_expenses);
        categoryExpensesChart = view.findViewById(R.id.chart_category_expenses);
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        tvTotalExpenses = view.findViewById(R.id.tv_total_expenses);
        tvExpenseListTotal = view.findViewById(R.id.tv_expense_list_total);
        tvExpenseListBudget = view.findViewById(R.id.tv_expense_list_budget);
        tableExpenses = view.findViewById(R.id.table_expenses);
        btnToggleYearlyReport = view.findViewById(R.id.btn_toggle_yearly_report);
        btnExportReport = view.findViewById(R.id.btn_export_report);
        btnPreviousMonth = view.findViewById(R.id.btn_previous_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);

        // Set up charts
        setupDailyExpensesChart();
        setupCategoryExpensesChart();
    }

    private void setupListeners() {
        btnPreviousMonth.setOnClickListener(v -> viewModel.previousMonth());
        btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        btnToggleYearlyReport.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Yearly report will be implemented later", Toast.LENGTH_SHORT).show();
        });

        btnExportReport.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Export functionality will be implemented later", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupObservers() {
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), month -> {
            tvMonthYear.setText(viewModel.getFormattedMonthYear());
        });

        viewModel.getSelectedYear().observe(getViewLifecycleOwner(), year -> {
            tvMonthYear.setText(viewModel.getFormattedMonthYear());
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator if needed
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getMonthlyReport().observe(getViewLifecycleOwner(), this::updateReportUI);
    }

    private void updateReportUI(ExpenseReportModels.MonthlyExpenseReport report) {
        if (report == null) return;

        // Update total expenses
        tvTotalExpenses.setText(ExpenseReportViewModel.formatCurrency(report.getTotalExpenses()));
        tvExpenseListTotal.setText(ExpenseReportViewModel.formatCurrency(report.getTotalExpenses()));
        tvExpenseListBudget.setText(ExpenseReportViewModel.formatCurrency(report.getTotalBudget()));

        // Update charts
        updateDailyExpensesChart(report.getDailyExpenses());
        updateCategoryExpensesChart(report.getCategoryExpenses());

        // Update expense table
        updateExpenseTable(report.getExpenses());
    }

    private void setupDailyExpensesChart() {
        dailyExpensesChart.getDescription().setEnabled(false);
        dailyExpensesChart.setDrawGridBackground(false);
        dailyExpensesChart.setDrawBarShadow(false);
        dailyExpensesChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = dailyExpensesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = dailyExpensesChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.format(value);
            }
        });

        dailyExpensesChart.getAxisRight().setEnabled(false);
        dailyExpensesChart.getLegend().setEnabled(false);
    }

    private void setupCategoryExpensesChart() {
        categoryExpensesChart.getDescription().setEnabled(false);
        categoryExpensesChart.setDrawGridBackground(false);
        categoryExpensesChart.setDrawBarShadow(false);
        categoryExpensesChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = categoryExpensesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = categoryExpensesChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.format(value);
            }
        });

        categoryExpensesChart.getAxisRight().setEnabled(false);
        categoryExpensesChart.getLegend().setEnabled(false);
    }

    private void updateDailyExpensesChart(List<ExpenseReportModels.DailyExpenseSummary> dailyExpenses) {
        if (dailyExpenses == null || dailyExpenses.isEmpty()) {
            dailyExpensesChart.clear();
            dailyExpensesChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());

        // Get the current month and year
        int month = viewModel.getSelectedMonth().getValue();
        int year = viewModel.getSelectedYear().getValue();

        // Get days in month
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // Month is 0-based in Calendar
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Create a map to store daily expenses for quick lookup
        java.util.Map<Integer, Double> dailyExpenseMap = new java.util.HashMap<>();
        for (ExpenseReportModels.DailyExpenseSummary daily : dailyExpenses) {
            calendar.setTime(daily.getDate());
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            dailyExpenseMap.put(dayOfMonth, daily.getTotalAmount());
        }

        // Create entries for all days in the month
        for (int day = 1; day <= daysInMonth; day++) {
            float amount = dailyExpenseMap.getOrDefault(day, 0.0).floatValue();
            entries.add(new BarEntry(day - 1, amount));
            labels.add(String.valueOf(day));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Expenses");
        dataSet.setColor(Color.parseColor("#4CAF50"));  // Green color
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value > 0) {
                    return vndFormatter.format(value);
                }
                return "";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        barData.setValueTextSize(10f);

        dailyExpensesChart.setData(barData);
        dailyExpensesChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        dailyExpensesChart.getXAxis().setLabelCount(labels.size());
        dailyExpensesChart.getXAxis().setLabelRotationAngle(90f); // Rotate labels for better visibility

        // Refresh chart
        dailyExpensesChart.invalidate();
    }

    private void updateCategoryExpensesChart(List<ExpenseReportModels.CategoryExpenseSummary> categoryExpenses) {
        if (categoryExpenses == null || categoryExpenses.isEmpty()) {
            categoryExpensesChart.clear();
            categoryExpensesChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < categoryExpenses.size(); i++) {
            ExpenseReportModels.CategoryExpenseSummary category = categoryExpenses.get(i);
            entries.add(new BarEntry(i, (float) category.getTotalAmount()));
            labels.add(category.getCategoryName());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Category Expenses");
        dataSet.setColor(Color.parseColor("#2196F3"));  // Blue color
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.format(value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        barData.setValueTextSize(10f);

        categoryExpensesChart.setData(barData);
        categoryExpensesChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        categoryExpensesChart.getXAxis().setLabelCount(labels.size());
        categoryExpensesChart.getXAxis().setLabelRotationAngle(45f); // Rotate labels for better visibility

        // Refresh chart
        categoryExpensesChart.invalidate();
    }

    private void updateExpenseTable(List<ExpenseReportModels.ExpenseItem> expenses) {
        // Clear existing rows except header
        int childCount = tableExpenses.getChildCount();
        if (childCount > 1) {
            tableExpenses.removeViews(1, childCount - 1);
        }

        if (expenses == null || expenses.isEmpty()) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (ExpenseReportModels.ExpenseItem expense : expenses) {
            TableRow row = new TableRow(getContext());

            // Date column
            TextView tvDate = new TextView(getContext());
            tvDate.setText(dateFormat.format(expense.getDate()));
            tvDate.setPadding(8, 8, 8, 8);
            row.addView(tvDate);

            // Category column
            TextView tvCategory = new TextView(getContext());
            tvCategory.setText(expense.getCategoryName());
            tvCategory.setPadding(8, 8, 8, 8);
            row.addView(tvCategory);

            // Description column
            TextView tvDescription = new TextView(getContext());
            tvDescription.setText(expense.getDescription());
            tvDescription.setPadding(8, 8, 8, 8);
            row.addView(tvDescription);

            // Amount column
            TextView tvAmount = new TextView(getContext());
            tvAmount.setText(ExpenseReportViewModel.formatCurrency(expense.getAmount()));
            tvAmount.setPadding(8, 8, 8, 8);
            row.addView(tvAmount);

            // Add row to table
            tableExpenses.addView(row);
        }
    }

    // Add a method to get the class instance for use in a ViewPager or Navigation component
    public static ExpenseReportFragment newInstance() {
        return new ExpenseReportFragment();
    }
}