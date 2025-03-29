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
import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import com.google.android.material.chip.ChipGroup;
import com.project.cem.R;
import com.project.cem.model.YearlyExpenseReport;
import com.project.cem.utils.VndCurrencyFormatter;
import com.project.cem.viewmodel.YearExpenseReportViewModel;

import java.util.ArrayList;
import java.util.List;

public class YearExpenseReportFragment extends Fragment {

    private YearExpenseReportViewModel viewModel;
    private BarChart monthlyBarChart;
    private LineChart monthlyLineChart;
    private BarChart monthlyStackedBarChart;
    private PieChart monthlyPieChart;
    private RadarChart monthlyRadarChart;
    private CombinedChart monthlyCombinedChart;
    private BubbleChart monthlyBubbleChart;
    private BarChart categoryTotalsChart;
    private TextView tvYear;
    private TextView tvTotalYearlyExpenses;
    private TableLayout tableMonthlyComparison;
    private MaterialButton btnToggleMonthlyReport;
    private MaterialButton btnExportYearlyReport;
    private ImageButton btnPreviousYear;
    private ImageButton btnNextYear;
    private ChipGroup chartTypeChipGroup;
    private Chip chipBarChart;
    private Chip chipLineChart;
    private Chip chipStackedBarChart;
    private Chip chipPieChart;
    private Chip chipRadarChart;
    private Chip chipCombinedChart;
    private Chip chipBubbleChart;
    private VndCurrencyFormatter vndFormatter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_year_expense_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(YearExpenseReportViewModel.class);
        vndFormatter = new VndCurrencyFormatter();

        // Initialize UI components
        initializeViews(view);
        setupListeners();
        setupObservers();
    }

    private void initializeViews(View view) {
        monthlyBarChart = view.findViewById(R.id.chart_monthly_expenses_bar);
        monthlyLineChart = view.findViewById(R.id.chart_monthly_expenses_line);
        monthlyStackedBarChart = view.findViewById(R.id.chart_monthly_expenses_stacked);
        monthlyPieChart = view.findViewById(R.id.chart_monthly_expenses_pie);
        monthlyRadarChart = view.findViewById(R.id.chart_monthly_expenses_radar);
        monthlyCombinedChart = view.findViewById(R.id.chart_monthly_expenses_combined);
        monthlyBubbleChart = view.findViewById(R.id.chart_monthly_expenses_bubble);
        categoryTotalsChart = view.findViewById(R.id.chart_category_totals);
        tvYear = view.findViewById(R.id.tv_year);
        tvTotalYearlyExpenses = view.findViewById(R.id.tv_total_yearly_expenses);
        tableMonthlyComparison = view.findViewById(R.id.table_monthly_comparison);
        btnToggleMonthlyReport = view.findViewById(R.id.btn_toggle_monthly_report);
        btnExportYearlyReport = view.findViewById(R.id.btn_export_yearly_report);
        btnPreviousYear = view.findViewById(R.id.btn_previous_year);
        btnNextYear = view.findViewById(R.id.btn_next_year);
        chartTypeChipGroup = view.findViewById(R.id.chart_type_chip_group);
        chipBarChart = view.findViewById(R.id.chip_bar_chart);
        chipLineChart = view.findViewById(R.id.chip_line_chart);
        chipStackedBarChart = view.findViewById(R.id.chip_stacked_bar_chart);
        chipPieChart = view.findViewById(R.id.chip_pie_chart);
        chipRadarChart = view.findViewById(R.id.chip_radar_chart);
        chipCombinedChart = view.findViewById(R.id.chip_combined_chart);
        chipBubbleChart = view.findViewById(R.id.chip_bubble_chart);

        // Set up charts
        setupMonthlyBarChart();
        setupMonthlyLineChart();
        setupMonthlyStackedBarChart();
        setupMonthlyPieChart();
        setupMonthlyRadarChart();
        setupMonthlyCombinedChart();
        setupMonthlyBubbleChart();
        setupCategoryTotalsChart();
    }

    private void setupListeners() {
        btnPreviousYear.setOnClickListener(v -> viewModel.previousYear());
        btnNextYear.setOnClickListener(v -> viewModel.nextYear());

        btnToggleMonthlyReport.setOnClickListener(v -> {
            ExpenseReportFragment expenseReportFragment = new ExpenseReportFragment();

            // Thay thế fragment hiện tại bằng YearExpenseReportFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, expenseReportFragment)  // fragment_container là ID của container chứa fragment
                    .addToBackStack(null)  // Cho phép quay lại bằng nút Back
                    .commit();
        });

        btnExportYearlyReport.setOnClickListener(v ->{
            showChartOptionsDialog();
        });

        // Chart type chip group listener
        chartTypeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_bar_chart) {
                viewModel.setSelectedChartType(YearExpenseReportViewModel.CHART_TYPE_BAR);
                showSelectedChart(YearExpenseReportViewModel.CHART_TYPE_BAR);
            } else if (checkedId == R.id.chip_line_chart) {
                viewModel.setSelectedChartType(YearExpenseReportViewModel.CHART_TYPE_LINE);
                showSelectedChart(YearExpenseReportViewModel.CHART_TYPE_LINE);
            } else if (checkedId == R.id.chip_stacked_bar_chart) {
                viewModel.setSelectedChartType(YearExpenseReportViewModel.CHART_TYPE_STACKED_BAR);
                showSelectedChart(YearExpenseReportViewModel.CHART_TYPE_STACKED_BAR);
            } else if (checkedId == R.id.chip_pie_chart) {
                viewModel.setSelectedChartType(YearExpenseReportViewModel.CHART_TYPE_PIE);
                showSelectedChart(YearExpenseReportViewModel.CHART_TYPE_PIE);
            } else if (checkedId == R.id.chip_radar_chart) {
                viewModel.setSelectedChartType(YearExpenseReportViewModel.CHART_TYPE_RADAR);
                showSelectedChart(YearExpenseReportViewModel.CHART_TYPE_RADAR);
            } else if (checkedId == R.id.chip_combined_chart) {
                viewModel.setSelectedChartType(YearExpenseReportViewModel.CHART_TYPE_COMBINED);
                showSelectedChart(YearExpenseReportViewModel.CHART_TYPE_COMBINED);
            } else if (checkedId == R.id.chip_bubble_chart) {
                viewModel.setSelectedChartType(YearExpenseReportViewModel.CHART_TYPE_BUBBLE);
                showSelectedChart(YearExpenseReportViewModel.CHART_TYPE_BUBBLE);
            }
        });
    }

    private void showSelectedChart(int chartType) {
        monthlyBarChart.setVisibility(chartType == YearExpenseReportViewModel.CHART_TYPE_BAR ? View.VISIBLE : View.GONE);
        monthlyLineChart.setVisibility(chartType == YearExpenseReportViewModel.CHART_TYPE_LINE ? View.VISIBLE : View.GONE);
        monthlyStackedBarChart.setVisibility(chartType == YearExpenseReportViewModel.CHART_TYPE_STACKED_BAR ? View.VISIBLE : View.GONE);
        monthlyPieChart.setVisibility(chartType == YearExpenseReportViewModel.CHART_TYPE_PIE ? View.VISIBLE : View.GONE);
        monthlyRadarChart.setVisibility(chartType == YearExpenseReportViewModel.CHART_TYPE_RADAR ? View.VISIBLE : View.GONE);
        monthlyCombinedChart.setVisibility(chartType == YearExpenseReportViewModel.CHART_TYPE_COMBINED ? View.VISIBLE : View.GONE);
        monthlyBubbleChart.setVisibility(chartType == YearExpenseReportViewModel.CHART_TYPE_BUBBLE ? View.VISIBLE : View.GONE);
    }

    private void setupObservers() {
        viewModel.getSelectedYear().observe(getViewLifecycleOwner(), year -> {
            tvYear.setText(viewModel.getFormattedYear());
        });

        viewModel.getSelectedChartType().observe(getViewLifecycleOwner(), chartType -> {
            showSelectedChart(chartType);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator if needed
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getYearlyReport().observe(getViewLifecycleOwner(), this::updateReportUI);
    }

    private void updateReportUI(YearlyExpenseReport report) {
        if (report == null) return;

        // Update total expenses
        tvTotalYearlyExpenses.setText(YearExpenseReportViewModel.formatCurrency(report.getTotalYearlyExpenses()));

        // Update charts based on selected chart type
        updateMonthlyBarChart(report.getMonthlyTotals());
        updateMonthlyLineChart(report.getMonthlyTotals());
        updateMonthlyStackedBarChart(report.getCategoryTotals());
        updateMonthlyPieChart(report.getCategoryTotals());
        updateMonthlyRadarChart(report.getMonthlyTotals());
        updateMonthlyCombinedChart(report.getMonthlyTotals());
        updateMonthlyBubbleChart(report.getCategoryTotals());

        // Update category totals chart
        updateCategoryTotalsChart(report.getCategoryTotals());

        // Update monthly comparison table
        updateMonthlyComparisonTable(report.getMonthlyTotals());
    }

    private void setupMonthlyBarChart() {
        monthlyBarChart.getDescription().setEnabled(false);
        monthlyBarChart.setDrawGridBackground(false);
        monthlyBarChart.setDrawBarShadow(false);
        monthlyBarChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = monthlyBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = monthlyBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        monthlyBarChart.getAxisRight().setEnabled(false);
        monthlyBarChart.getLegend().setEnabled(true);
        monthlyBarChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        monthlyBarChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
    }

    private void setupMonthlyLineChart() {
        monthlyLineChart.getDescription().setEnabled(false);
        monthlyLineChart.setDrawGridBackground(false);

        XAxis xAxis = monthlyLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = monthlyLineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        monthlyLineChart.getAxisRight().setEnabled(false);
        monthlyLineChart.getLegend().setEnabled(true);
        monthlyLineChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        monthlyLineChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
    }

    private void setupMonthlyStackedBarChart() {
        monthlyStackedBarChart.getDescription().setEnabled(false);
        monthlyStackedBarChart.setDrawGridBackground(false);
        monthlyStackedBarChart.setDrawBarShadow(false);
        monthlyStackedBarChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = monthlyStackedBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = monthlyStackedBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        monthlyStackedBarChart.getAxisRight().setEnabled(false);
        monthlyStackedBarChart.getLegend().setEnabled(true);
        monthlyStackedBarChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        monthlyStackedBarChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
    }

    private void setupMonthlyPieChart() {
        monthlyPieChart.getDescription().setEnabled(false);
        monthlyPieChart.setUsePercentValues(false);
        monthlyPieChart.setDrawHoleEnabled(true);
        monthlyPieChart.setHoleColor(Color.WHITE);
        monthlyPieChart.setTransparentCircleRadius(61f);

        monthlyPieChart.setRotationAngle(0);
        monthlyPieChart.setRotationEnabled(true);
        monthlyPieChart.setHighlightPerTapEnabled(true);

        monthlyPieChart.getLegend().setEnabled(true);
        monthlyPieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        monthlyPieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        monthlyPieChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
    }

    private void setupMonthlyRadarChart() {
        monthlyRadarChart.getDescription().setEnabled(false);
        monthlyRadarChart.setWebLineWidth(1f);
        monthlyRadarChart.setWebColor(Color.LTGRAY);
        monthlyRadarChart.setWebLineWidthInner(1f);
        monthlyRadarChart.setWebColorInner(Color.LTGRAY);
        monthlyRadarChart.setWebAlpha(100);

        // Cấu hình thêm cho radar chart...
        monthlyRadarChart.getLegend().setEnabled(true);
        monthlyRadarChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        monthlyRadarChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
    }

    private void setupMonthlyCombinedChart() {
        monthlyCombinedChart.getDescription().setEnabled(false);
        monthlyCombinedChart.setDrawGridBackground(false);
        monthlyCombinedChart.setDrawBarShadow(false);

        // Kết hợp biểu đồ cột và đường
        monthlyCombinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        YAxis leftAxis = monthlyCombinedChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        monthlyCombinedChart.getAxisRight().setEnabled(false);
        monthlyCombinedChart.getLegend().setEnabled(true);
        monthlyCombinedChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        monthlyCombinedChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);

        XAxis xAxis = monthlyCombinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void setupMonthlyBubbleChart() {
        monthlyBubbleChart.getDescription().setEnabled(false);
        monthlyBubbleChart.setDrawGridBackground(false);

        monthlyBubbleChart.setScaleEnabled(true);

        YAxis leftAxis = monthlyBubbleChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        monthlyBubbleChart.getAxisRight().setEnabled(false);
        monthlyBubbleChart.getLegend().setEnabled(true);
        monthlyBubbleChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        monthlyBubbleChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);

        XAxis xAxis = monthlyBubbleChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void setupCategoryTotalsChart() {
        categoryTotalsChart.getDescription().setEnabled(false);
        categoryTotalsChart.setDrawGridBackground(false);
        categoryTotalsChart.setDrawBarShadow(false);
        categoryTotalsChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = categoryTotalsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5);

        YAxis leftAxis = categoryTotalsChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        categoryTotalsChart.getAxisRight().setEnabled(false);
        categoryTotalsChart.getLegend().setEnabled(false);
    }

    private void updateMonthlyBarChart(List<YearlyExpenseReport.MonthlyTotal> monthlyTotals) {
        if (monthlyTotals == null || monthlyTotals.isEmpty()) {
            monthlyBarChart.clear();
            monthlyBarChart.invalidate();
            return;
        }

        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < monthlyTotals.size(); i++) {
            YearlyExpenseReport.MonthlyTotal monthly = monthlyTotals.get(i);
            expenseEntries.add(new BarEntry(i, (float) monthly.getTotalAmount()));
            labels.add(YearExpenseReportViewModel.getShortMonthName(monthly.getMonth()));
        }

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Chi tiêu");
        expenseDataSet.setColor(Color.rgb(76, 175, 80)); // Green
        expenseDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value > 0) {
                    return vndFormatter.formatInMillions(value);
                }
                return "";
            }
        });

        BarData barData = new BarData(expenseDataSet);
        barData.setBarWidth(0.7f); // Adjusted width now that we only have one dataset
        barData.setValueTextSize(10f);

        monthlyBarChart.setData(barData);
        monthlyBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        monthlyBarChart.getXAxis().setLabelCount(labels.size());

        // Refresh chart
        monthlyBarChart.invalidate();
    }
    private void updateMonthlyLineChart(List<YearlyExpenseReport.MonthlyTotal> monthlyTotals) {
        if (monthlyTotals == null || monthlyTotals.isEmpty()) {
            monthlyLineChart.clear();
            monthlyLineChart.invalidate();
            return;
        }

        List<Entry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < monthlyTotals.size(); i++) {
            YearlyExpenseReport.MonthlyTotal monthly = monthlyTotals.get(i);
            expenseEntries.add(new Entry(i, (float) monthly.getTotalAmount()));
            labels.add(YearExpenseReportViewModel.getShortMonthName(monthly.getMonth()));
        }

        LineDataSet expenseDataSet = new LineDataSet(expenseEntries, "Chi tiêu");
        expenseDataSet.setColor(Color.rgb(76, 175, 80)); // Green
        expenseDataSet.setCircleColor(Color.rgb(76, 175, 80));
        expenseDataSet.setLineWidth(2f);
        expenseDataSet.setCircleRadius(4f);
        expenseDataSet.setDrawCircleHole(false);
        expenseDataSet.setValueTextSize(10f);
        expenseDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        LineData lineData = new LineData(expenseDataSet);

        monthlyLineChart.setData(lineData);
        monthlyLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        monthlyLineChart.getXAxis().setLabelCount(labels.size());

        // Refresh chart
        monthlyLineChart.invalidate();
    }

    private void updateMonthlyStackedBarChart(List<YearlyExpenseReport.CategoryTotal> categoryTotals) {
        if (categoryTotals == null || categoryTotals.isEmpty()) {
            monthlyStackedBarChart.clear();
            monthlyStackedBarChart.invalidate();
            return;
        }

        // We'll create a stacked bar chart with one stack per month
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            labels.add(YearExpenseReportViewModel.getShortMonthName(i));
        }

        List<IBarDataSet> dataSets = new ArrayList<>();
        int[] colors = getColors(categoryTotals.size());

        // For each category, create a dataset with 12 values (one for each month)
        for (int i = 0; i < categoryTotals.size(); i++) {
            YearlyExpenseReport.CategoryTotal category = categoryTotals.get(i);
            List<BarEntry> entries = new ArrayList<>();

            // Convert the monthly amounts for this category into bar entries
            for (int month = 0; month < 12; month++) {
                float amount = category.getMonthlyAmounts().get(month).floatValue();
                if (entries.size() > month) {
                    // Add to existing entry (stack)
                    BarEntry existing = entries.get(month);
                    float[] vals = existing.getYVals();
                    if (vals != null) {
                        float[] newVals = new float[vals.length + 1];
                        System.arraycopy(vals, 0, newVals, 0, vals.length);
                        newVals[vals.length] = amount;
                        entries.set(month, new BarEntry(month, newVals));
                    }
                } else {
                    // Create new entry
                    entries.add(new BarEntry(month, new float[]{amount}));
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, category.getCategoryName());
            dataSet.setColor(colors[i % colors.length]);
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return value > 0 ? vndFormatter.formatInMillions(value) : "";
                }
            });
            dataSets.add(dataSet);
        }

        BarData barData = new BarData(dataSets);
        barData.setBarWidth(0.7f);
        barData.setValueTextSize(8f);

        monthlyStackedBarChart.setData(barData);
        monthlyStackedBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        monthlyStackedBarChart.getXAxis().setLabelCount(labels.size());

        // Refresh chart
        monthlyStackedBarChart.invalidate();
    }

    private void updateMonthlyPieChart(List<YearlyExpenseReport.CategoryTotal> categoryTotals) {
        if (categoryTotals == null || categoryTotals.isEmpty()) {
            monthlyPieChart.clear();
            monthlyPieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();

        // Take top 5 categories by total amount
        int limit = Math.min(5, categoryTotals.size());
        double otherAmount = 0;

        for (int i = 0; i < categoryTotals.size(); i++) {
            YearlyExpenseReport.CategoryTotal category = categoryTotals.get(i);

            if (i < limit) {
                entries.add(new PieEntry((float) category.getTotalAmount(), category.getCategoryName()));
            } else {
                otherAmount += category.getTotalAmount();
            }
        }

        // Add "Other" category if there are more than 5 categories
        if (otherAmount > 0) {
            entries.add(new PieEntry((float) otherAmount, "Other"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.WHITE);

        monthlyPieChart.setData(pieData);

        // Refresh chart
        monthlyPieChart.invalidate();
    }

    private void updateMonthlyRadarChart(List<YearlyExpenseReport.MonthlyTotal> monthlyTotals) {
        if (monthlyTotals == null || monthlyTotals.isEmpty()) {
            monthlyRadarChart.clear();
            monthlyRadarChart.invalidate();
            return;
        }

        List<RadarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (YearlyExpenseReport.MonthlyTotal monthly : monthlyTotals) {
            expenseEntries.add(new RadarEntry((float) monthly.getTotalAmount()));
            labels.add(YearExpenseReportViewModel.getShortMonthName(monthly.getMonth()));
        }

        RadarDataSet expenseDataSet = new RadarDataSet(expenseEntries, "Chi tiêu");
        expenseDataSet.setColor(Color.rgb(76, 175, 80));
        expenseDataSet.setFillColor(Color.rgb(76, 175, 80));
        expenseDataSet.setDrawFilled(true);
        expenseDataSet.setFillAlpha(180);
        expenseDataSet.setLineWidth(2f);
        expenseDataSet.setDrawValues(true);


        expenseDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        RadarData radarData = new RadarData(expenseDataSet);
        radarData.setValueTextSize(10f);


        monthlyRadarChart.setDrawWeb(true);
        monthlyRadarChart.getYAxis().setDrawLabels(false);
        monthlyRadarChart.getYAxis().setDrawGridLines(false);
        monthlyRadarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        monthlyRadarChart.setData(radarData);
        monthlyRadarChart.invalidate();
    }

    private void updateMonthlyCombinedChart(List<YearlyExpenseReport.MonthlyTotal> monthlyTotals) {
        if (monthlyTotals == null || monthlyTotals.isEmpty()) {
            monthlyCombinedChart.clear();
            monthlyCombinedChart.invalidate();
            return;
        }

        List<Entry> lineEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < monthlyTotals.size(); i++) {
            YearlyExpenseReport.MonthlyTotal monthly = monthlyTotals.get(i);
            // Sử dụng dữ liệu chi tiêu cho cả biểu đồ cột và đường
            lineEntries.add(new Entry(i, (float) monthly.getTotalAmount()));

            // Tính giá trị orther để biểu đồ cột hiển thị 80% của chi tiêu
            float otherValue = (float) (monthly.getTotalAmount() * 0.8);
            barEntries.add(new BarEntry(i, otherValue));

            labels.add(YearExpenseReportViewModel.getShortMonthName(monthly.getMonth()));
        }

        // Dữ liệu biểu đồ cột
        BarDataSet barDataSet = new BarDataSet(barEntries, "Mức chi trung bình");
        barDataSet.setColor(Color.rgb(33, 150, 243));
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f);

        // Dữ liệu biểu đồ đường
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Chi tiêu");
        lineDataSet.setColor(Color.rgb(76, 175, 80));
        lineDataSet.setLineWidth(2.5f);
        lineDataSet.setCircleColor(Color.rgb(76, 175, 80));
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });
        LineData lineData = new LineData(lineDataSet);

        // Kết hợp dữ liệu
        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);

        monthlyCombinedChart.setData(combinedData);
        monthlyCombinedChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        monthlyCombinedChart.getXAxis().setLabelCount(labels.size());

        monthlyCombinedChart.invalidate();
    }

    private void updateMonthlyBubbleChart(List<YearlyExpenseReport.CategoryTotal> categoryTotals) {
        if (categoryTotals == null || categoryTotals.isEmpty()) {
            monthlyBubbleChart.clear();
            monthlyBubbleChart.invalidate();
            return;
        }

        // Lấy 3 danh mục chi tiêu lớn nhất
        int limit = Math.min(3, categoryTotals.size());
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            labels.add(YearExpenseReportViewModel.getShortMonthName(i));
        }

        // Thay đổi từ ArrayList<BubbleDataSet> thành List<IBubbleDataSet>
        List<IBubbleDataSet> dataSets = new ArrayList<>();
        int[] colors = {Color.rgb(76, 175, 80), Color.rgb(33, 150, 243), Color.rgb(255, 152, 0)};

        for (int categoryIndex = 0; categoryIndex < limit; categoryIndex++) {
            YearlyExpenseReport.CategoryTotal category = categoryTotals.get(categoryIndex);
            ArrayList<BubbleEntry> entries = new ArrayList<>();

            double totalForCategory = category.getTotalAmount();

            for (int month = 0; month < 12; month++) {
                double monthAmount = category.getMonthlyAmounts().get(month);
                if (monthAmount > 0) {
                    // X: tháng, Y: số tiền, Kích thước: tỷ lệ % trong tổng danh mục
                    float bubbleSize = (float) ((monthAmount / totalForCategory) * 10);
                    entries.add(new BubbleEntry(month, (float) monthAmount, bubbleSize));
                }
            }

            BubbleDataSet dataSet = new BubbleDataSet(entries, category.getCategoryName());
            dataSet.setColor(colors[categoryIndex]);
            dataSet.setValueTextSize(10f);
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return vndFormatter.formatInMillions(value);
                }
            });

            // Thêm vào danh sách IBubbleDataSet, không phải ArrayList<BubbleDataSet>
            dataSets.add(dataSet);
        }

        // Tạo BubbleData với List<IBubbleDataSet>
        BubbleData bubbleData = new BubbleData(dataSets);

        monthlyBubbleChart.setData(bubbleData);
        monthlyBubbleChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        monthlyBubbleChart.getXAxis().setLabelCount(labels.size());

        monthlyBubbleChart.invalidate();
    }
    private void updateCategoryTotalsChart(List<YearlyExpenseReport.CategoryTotal> categoryTotals) {
        if (categoryTotals == null || categoryTotals.isEmpty()) {
            categoryTotalsChart.clear();
            categoryTotalsChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Take top 5 categories by total amount
        int limit = Math.min(5, categoryTotals.size());

        for (int i = 0; i < limit; i++) {
            YearlyExpenseReport.CategoryTotal category = categoryTotals.get(i);
            entries.add(new BarEntry(i, (float) category.getTotalAmount()));

            // Truncate long category names
            String categoryName = category.getCategoryName();
            if (categoryName.length() > 12) {
                categoryName = categoryName.substring(0, 10) + "...";
            }
            labels.add(categoryName);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Top Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormatter.formatInMillions(value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        barData.setValueTextSize(10f);

        categoryTotalsChart.setData(barData);
        categoryTotalsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        categoryTotalsChart.getXAxis().setLabelCount(labels.size());
        categoryTotalsChart.getXAxis().setLabelRotationAngle(45f); // Rotate labels for better visibility

        // Refresh chart
        categoryTotalsChart.invalidate();
    }

    private void updateMonthlyComparisonTable(List<YearlyExpenseReport.MonthlyTotal> monthlyTotals) {
        // Clear existing rows except header
        int childCount = tableMonthlyComparison.getChildCount();
        if (childCount > 1) {
            tableMonthlyComparison.removeViews(1, childCount - 1);
        }

        if (monthlyTotals == null || monthlyTotals.isEmpty()) {
            return;
        }

        for (YearlyExpenseReport.MonthlyTotal monthly : monthlyTotals) {
            TableRow row = new TableRow(getContext());

            // Month column
            TextView tvMonth = new TextView(getContext());
            tvMonth.setText(YearExpenseReportViewModel.getMonthName(monthly.getMonth()));
            tvMonth.setPadding(8, 8, 8, 8);
            row.addView(tvMonth);

            // Expenses column
            TextView tvExpenses = new TextView(getContext());
            tvExpenses.setText(YearExpenseReportViewModel.formatCurrency(monthly.getTotalAmount()));
            tvExpenses.setPadding(8, 8, 8, 8);
            row.addView(tvExpenses);

            // Budget column
            TextView tvBudget = new TextView(getContext());
            tvBudget.setText(YearExpenseReportViewModel.formatCurrency(monthly.getBudgetAmount()));
            tvBudget.setPadding(8, 8, 8, 8);
            row.addView(tvBudget);

            // Difference column
            TextView tvDifference = new TextView(getContext());
            double difference = monthly.getBudgetAmount() - monthly.getTotalAmount();
            tvDifference.setText(YearExpenseReportViewModel.formatCurrency(difference));
            // Color the text based on whether over/under budget
            tvDifference.setTextColor(difference < 0 ? Color.RED : Color.rgb(76, 175, 80));
            tvDifference.setPadding(8, 8, 8, 8);
            row.addView(tvDifference);

            // Add row to table
            tableMonthlyComparison.addView(row);
        }
    }

    private int[] getColors(int count) {
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i % ColorTemplate.MATERIAL_COLORS.length];
        }
        return colors;
    }

    // Add a method to get the class instance for use in a ViewPager or Navigation component
    public static YearExpenseReportFragment newInstance() {
        return new YearExpenseReportFragment();
    }

    private void showBubbleChartInfo() {
        // Tạo dialog giải thích
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thông tin biểu đồ bong bóng");

        String message = "Biểu đồ bong bóng hiển thị:\n\n" +
                "• Trục X: Tháng trong năm\n" +
                "• Trục Y: Tổng chi tiêu\n" +
                "• Kích thước bong bóng: Tỷ lệ phần trăm chi tiêu trong danh mục\n\n" +
                "Hiển thị 3 danh mục chi tiêu lớn nhất với màu sắc khác nhau.";

        builder.setMessage(message);
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    /**
     * Tạo và hiển thị dialog chọn các tùy chọn xem biểu đồ
     */
    private void showChartOptionsDialog() {
        String[] options = {"Đổi màu biểu đồ", "Xuất biểu đồ", "Thông tin chi tiết biểu đồ"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tùy chọn biểu đồ");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Đổi màu biểu đồ
                    showColorPickerDialog();
                    break;
                case 1: // Xuất biểu đồ
                    exportCurrentChart();
                    break;
                case 2: // Thông tin chi tiết biểu đồ
                    showChartInfo();
                    break;
            }
        });
        builder.show();
    }

    /**
     * Hiển thị dialog chọn màu cho biểu đồ
     */
    private void showColorPickerDialog() {
        // Giả định bạn đã có một ColorPickerDialog từ thư viện hoặc tự xây dựng
        Toast.makeText(requireContext(), "Tính năng đổi màu sẽ được triển khai sau", Toast.LENGTH_SHORT).show();
    }

    /**
     * Xuất biểu đồ hiện tại thành hình ảnh
     */
    private void exportCurrentChart() {
        int chartType = viewModel.getSelectedChartType().getValue();
        Bitmap chartBitmap = null;

        switch (chartType) {
            case YearExpenseReportViewModel.CHART_TYPE_BAR:
                monthlyBarChart.setDrawingCacheEnabled(true);
                chartBitmap = monthlyBarChart.getChartBitmap();
                break;
            case YearExpenseReportViewModel.CHART_TYPE_LINE:
                monthlyLineChart.setDrawingCacheEnabled(true);
                chartBitmap = monthlyLineChart.getChartBitmap();
                break;
            case YearExpenseReportViewModel.CHART_TYPE_STACKED_BAR:
                monthlyStackedBarChart.setDrawingCacheEnabled(true);
                chartBitmap = monthlyStackedBarChart.getChartBitmap();
                break;
            case YearExpenseReportViewModel.CHART_TYPE_PIE:
                monthlyPieChart.setDrawingCacheEnabled(true);
                chartBitmap = monthlyPieChart.getChartBitmap();
                break;
            case YearExpenseReportViewModel.CHART_TYPE_COMBINED:
                monthlyCombinedChart.setDrawingCacheEnabled(true);
                chartBitmap = monthlyCombinedChart.getChartBitmap();
                break;
            case YearExpenseReportViewModel.CHART_TYPE_RADAR:
                monthlyRadarChart.setDrawingCacheEnabled(true);
                chartBitmap = monthlyRadarChart.getChartBitmap();
                break;
            case YearExpenseReportViewModel.CHART_TYPE_BUBBLE:
                monthlyBubbleChart.setDrawingCacheEnabled(true);
                chartBitmap = monthlyBubbleChart.getChartBitmap();
                break;
        }

        if (chartBitmap != null) {
            // Lưu biểu đồ vào tệp
            try {
                // Kiểm tra quyền truy cập bộ nhớ ngoài
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Yêu cầu cấp quyền
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }

                // Tạo tên tệp duy nhất
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "Expenses_" + timeStamp + ".png";

                // Tạo đường dẫn đến thư mục Pictures
                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imageFile = new File(storageDir, fileName);

                // Lưu bitmap vào tệp
                FileOutputStream fos = new FileOutputStream(imageFile);
                chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                // Thông báo Media Scanner về tệp mới
                MediaScannerConnection.scanFile(requireContext(),
                        new String[]{imageFile.toString()},
                        null,
                        (path, uri) -> {
                            // Hiển thị thông báo thành công
                            Toast.makeText(requireContext(),
                                    "Đã lưu biểu đồ vào " + path,
                                    Toast.LENGTH_LONG).show();
                        });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(),
                        "Lỗi khi lưu biểu đồ: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Hiển thị thông tin về biểu đồ hiện tại
     */
    private void showChartInfo() {
        int chartType = viewModel.getSelectedChartType().getValue();

        // Tạo dialog giải thích
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thông tin biểu đồ");

        String message = "";
        switch (chartType) {
            case YearExpenseReportViewModel.CHART_TYPE_BAR:
                message = "Biểu đồ cột hiển thị chi tiêu theo từng tháng trong năm. " +
                        "Giúp bạn nhìn thấy rõ sự biến động chi tiêu theo từng tháng.";
                break;
            case YearExpenseReportViewModel.CHART_TYPE_LINE:
                message = "Biểu đồ đường thể hiện xu hướng chi tiêu qua các tháng. " +
                        "Giúp bạn phân tích sự thay đổi trong chi tiêu theo thời gian.";
                break;
            case YearExpenseReportViewModel.CHART_TYPE_STACKED_BAR:
                message = "Biểu đồ cột chồng hiển thị sự đóng góp của từng danh mục vào tổng chi tiêu hàng tháng. " +
                        "Giúp bạn hiểu rõ cơ cấu chi tiêu theo danh mục trong từng tháng.";
                break;
            case YearExpenseReportViewModel.CHART_TYPE_PIE:
                message = "Biểu đồ tròn thể hiện tỷ lệ phần trăm của từng danh mục trong tổng chi tiêu năm. " +
                        "Giúp bạn dễ dàng nhận biết các danh mục chiếm tỷ trọng lớn nhất trong chi tiêu.";
                break;
            case YearExpenseReportViewModel.CHART_TYPE_COMBINED:
                message = "Biểu đồ kết hợp thể hiện chi tiêu thực tế (đường) và mức chi trung bình (cột) hàng tháng. " +
                        "Cho phép so sánh trực quan các tháng bạn chi tiêu nhiều hơn hoặc ít hơn mức trung bình.";
                break;
            case YearExpenseReportViewModel.CHART_TYPE_RADAR:
                message = "Biểu đồ radar thể hiện chi tiêu hàng tháng dưới dạng đa giác. " +
                        "Giúp phát hiện các tháng có chi tiêu cao hoặc thấp một cách trực quan.";
                break;
            case YearExpenseReportViewModel.CHART_TYPE_BUBBLE:
                message = "Biểu đồ bong bóng hiển thị chi tiêu theo danh mục và tháng. " +
                        "Kích thước bong bóng thể hiện tỷ lệ đóng góp của chi tiêu vào tổng danh mục. " +
                        "Màu sắc khác nhau tương ứng với các danh mục khác nhau.";
                break;
        }

        builder.setMessage(message);
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }
}