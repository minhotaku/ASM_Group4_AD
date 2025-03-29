package com.project.cem.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.YearlyExpenseReport;
import com.project.cem.repository.ExpenseReportRepository;
import com.project.cem.utils.UserPreferences;
import com.project.cem.utils.VndCurrencyFormatter;

import java.util.Calendar;
import java.util.Locale;

/**
 * ViewModel class for YearExpenseReportFragment.
 * Manages data for yearly expense reports and chart displays.
 *
 * @author minhotaku
 * @version 1.0
 * @since 2025-03-28
 */
public class YearExpenseReportViewModel extends AndroidViewModel {

    private final ExpenseReportRepository repository;
    private final MutableLiveData<YearlyExpenseReport> yearlyReport = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedYear = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedChartType = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private static final VndCurrencyFormatter vndFormatter = new VndCurrencyFormatter();

    // Chart types constants
    public static final int CHART_TYPE_BAR = 0;
    public static final int CHART_TYPE_LINE = 1;
    public static final int CHART_TYPE_STACKED_BAR = 2;
    public static final int CHART_TYPE_PIE = 3;
    public static final int CHART_TYPE_COMBINED = 4;
    public static final int CHART_TYPE_RADAR = 5;
    public static final int CHART_TYPE_BUBBLE = 6;

    /**
     * Constructor initializes the ViewModel with default values and loads initial data.
     *
     * @param application Application context
     */
    public YearExpenseReportViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseReportRepository(application);

        // Initialize with current year
        Calendar calendar = Calendar.getInstance();
        selectedYear.setValue(calendar.get(Calendar.YEAR));

        // Default chart type
        selectedChartType.setValue(CHART_TYPE_BAR);

        // Load initial data
        loadYearlyReport();
    }

    /**
     * Get yearly expense report data.
     *
     * @return LiveData containing yearly expense report
     */
    public LiveData<YearlyExpenseReport> getYearlyReport() {
        return yearlyReport;
    }

    /**
     * Get selected year.
     *
     * @return LiveData containing selected year
     */
    public LiveData<Integer> getSelectedYear() {
        return selectedYear;
    }

    /**
     * Get selected chart type.
     *
     * @return LiveData containing selected chart type
     */
    public LiveData<Integer> getSelectedChartType() {
        return selectedChartType;
    }

    /**
     * Get loading state.
     *
     * @return LiveData indicating if data loading is in progress
     */
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    /**
     * Get error message.
     *
     * @return LiveData containing error message if any
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set the selected year and reload data.
     *
     * @param year Year to select
     */
    public void setSelectedYear(int year) {
        // Prevent selecting years too far in the past or future
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (year < currentYear - 10 || year > currentYear + 2) {
            errorMessage.setValue("Cannot select year " + year + ". Please choose a year between " +
                    (currentYear - 10) + " and " + (currentYear + 2));
            return;
        }

        selectedYear.setValue(year);
        loadYearlyReport();
    }

    /**
     * Set the selected chart type.
     *
     * @param chartType Chart type to select
     */
    public void setSelectedChartType(int chartType) {
        if (chartType >= CHART_TYPE_BAR && chartType <= CHART_TYPE_BUBBLE) {
            selectedChartType.setValue(chartType);
        } else {
            errorMessage.setValue("Invalid chart type selected");
        }
    }

    /**
     * Navigate to next year and reload data.
     */
    public void nextYear() {
        int currentYear = selectedYear.getValue();
        setSelectedYear(currentYear + 1);
    }

    /**
     * Navigate to previous year and reload data.
     */
    public void previousYear() {
        int currentYear = selectedYear.getValue();
        setSelectedYear(currentYear - 1);
    }

    /**
     * Load yearly expense report data from repository.
     */
    private void loadYearlyReport() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        try {
            int userId = UserPreferences.getUser(getApplication()).getUserID();
            int year = selectedYear.getValue();

            // Execute in a background thread
            new Thread(() -> {
                try {
                    YearlyExpenseReport report = repository.getYearlyExpenseReport(userId, year);
                    yearlyReport.postValue(report);
                    isLoading.postValue(false);
                } catch (Exception e) {
                    errorMessage.postValue("Error loading yearly report: " + e.getMessage());
                    isLoading.postValue(false);
                }
            }).start();

        } catch (Exception e) {
            errorMessage.setValue("Error: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    /**
     * Format currency value.
     *
     * @param amount Amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(double amount) {
        return vndFormatter.format(amount);
    }

    /**
     * Get month name.
     *
     * @param month Month number (1-12)
     * @return Month name
     */
    public static String getMonthName(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1); // Calendar months are 0-indexed

        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }

    /**
     * Get short month name.
     *
     * @param month Month number (1-12)
     * @return Short month name (e.g., Jan, Feb)
     */
    public static String getShortMonthName(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1);

        return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
    }

    /**
     * Get formatted year for display.
     *
     * @return Year as string
     */
    public String getFormattedYear() {
        return String.valueOf(selectedYear.getValue());
    }

    /**
     * Get chart type name for display.
     *
     * @return Name of the selected chart type
     */
    public String getChartTypeName() {
        switch (selectedChartType.getValue()) {
            case CHART_TYPE_BAR:
                return "Bar Chart";
            case CHART_TYPE_LINE:
                return "Line Chart";
            case CHART_TYPE_STACKED_BAR:
                return "Stacked Bar Chart";
            case CHART_TYPE_PIE:
                return "Pie Chart";
            case CHART_TYPE_COMBINED:
                return "Combined Chart";
            case CHART_TYPE_RADAR:
                return "Radar Chart";
            case CHART_TYPE_BUBBLE:
                return "Bubble Chart";
            default:
                return "Unknown Chart Type";
        }
    }

    /**
     * Get color for the given chart type.
     *
     * @param chartType Type of chart
     * @param index Color index (for multiple colors)
     * @return Color int value
     */
    public int getChartColor(int chartType, int index) {
        int[] barColors = {0xFF4CAF50, 0xFF2196F3, 0xFFFFC107, 0xFFE91E63};
        int[] lineColors = {0xFF4CAF50, 0xFF2196F3, 0xFFFFC107, 0xFFE91E63};
        int[] pieColors = {0xFF4CAF50, 0xFF2196F3, 0xFFFFC107, 0xFFE91E63, 0xFF9C27B0, 0xFF3F51B5};
        int[] radarColors = {0xAAF44336, 0xAA4CAF50};

        switch (chartType) {
            case CHART_TYPE_BAR:
            case CHART_TYPE_STACKED_BAR:
                return barColors[index % barColors.length];
            case CHART_TYPE_LINE:
            case CHART_TYPE_COMBINED:
                return lineColors[index % lineColors.length];
            case CHART_TYPE_PIE:
            case CHART_TYPE_BUBBLE:
                return pieColors[index % pieColors.length];
            case CHART_TYPE_RADAR:
                return radarColors[index % radarColors.length];
            default:
                return 0xFF000000; // Black
        }
    }

    /**
     * Calculate monthly growth rate compared to previous month.
     *
     * @param previousMonth Previous month amount
     * @param currentMonth Current month amount
     * @return Growth percentage
     */
    public static double calculateMonthlyGrowth(double previousMonth, double currentMonth) {
        if (previousMonth == 0) {
            return currentMonth > 0 ? 100.0 : 0.0;
        }
        return ((currentMonth - previousMonth) / previousMonth) * 100.0;
    }

    /**
     * Get average monthly expense for the year.
     *
     * @return Average monthly expense
     */
    public double getAverageMonthlyExpense() {
        YearlyExpenseReport report = yearlyReport.getValue();
        if (report == null || report.getMonthlyTotals() == null || report.getMonthlyTotals().isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        int count = 0;

        for (YearlyExpenseReport.MonthlyTotal monthlyTotal : report.getMonthlyTotals()) {
            if (monthlyTotal.getTotalAmount() > 0) {
                total += monthlyTotal.getTotalAmount();
                count++;
            }
        }

        return count > 0 ? total / count : 0.0;
    }

    /**
     * Find month with highest expenses.
     *
     * @return Month number with highest expenses (1-12), or 0 if no data
     */
    public int getHighestExpenseMonth() {
        YearlyExpenseReport report = yearlyReport.getValue();
        if (report == null || report.getMonthlyTotals() == null || report.getMonthlyTotals().isEmpty()) {
            return 0;
        }

        int highestMonth = 0;
        double highestAmount = 0.0;

        for (YearlyExpenseReport.MonthlyTotal monthlyTotal : report.getMonthlyTotals()) {
            if (monthlyTotal.getTotalAmount() > highestAmount) {
                highestAmount = monthlyTotal.getTotalAmount();
                highestMonth = monthlyTotal.getMonth();
            }
        }

        return highestMonth;
    }

    /**
     * Find month with lowest expenses.
     *
     * @return Month number with lowest expenses (1-12), or 0 if no data
     */
    public int getLowestExpenseMonth() {
        YearlyExpenseReport report = yearlyReport.getValue();
        if (report == null || report.getMonthlyTotals() == null || report.getMonthlyTotals().isEmpty()) {
            return 0;
        }

        int lowestMonth = 0;
        double lowestAmount = Double.MAX_VALUE;

        for (YearlyExpenseReport.MonthlyTotal monthlyTotal : report.getMonthlyTotals()) {
            if (monthlyTotal.getTotalAmount() > 0 && monthlyTotal.getTotalAmount() < lowestAmount) {
                lowestAmount = monthlyTotal.getTotalAmount();
                lowestMonth = monthlyTotal.getMonth();
            }
        }

        return lowestMonth;
    }

    /**
     * Calculate overall budget adherence percentage for the year.
     *
     * @return Percentage of budget adherence (negative if over budget)
     */
    public double getYearlyBudgetAdherence() {
        YearlyExpenseReport report = yearlyReport.getValue();
        if (report == null || report.getMonthlyTotals() == null || report.getMonthlyTotals().isEmpty()) {
            return 0.0;
        }

        double totalExpenses = 0.0;
        double totalBudget = 0.0;

        for (YearlyExpenseReport.MonthlyTotal monthlyTotal : report.getMonthlyTotals()) {
            totalExpenses += monthlyTotal.getTotalAmount();
            totalBudget += monthlyTotal.getBudgetAmount();
        }

        if (totalBudget == 0) {
            return 0.0;
        }

        // Negative if over budget, positive if under budget
        return ((totalBudget - totalExpenses) / totalBudget) * 100.0;
    }

    /**
     * Get spending trend for the year.
     *
     * @return "increasing", "decreasing", or "fluctuating"
     */
    public String getSpendingTrend() {
        YearlyExpenseReport report = yearlyReport.getValue();
        if (report == null || report.getMonthlyTotals() == null || report.getMonthlyTotals().size() < 3) {
            return "insufficient data";
        }

        int increases = 0;
        int decreases = 0;
        double previousAmount = -1;

        for (YearlyExpenseReport.MonthlyTotal monthlyTotal : report.getMonthlyTotals()) {
            double currentAmount = monthlyTotal.getTotalAmount();

            if (previousAmount >= 0 && currentAmount > 0) {
                if (currentAmount > previousAmount) {
                    increases++;
                } else if (currentAmount < previousAmount) {
                    decreases++;
                }
            }

            if (currentAmount > 0) {
                previousAmount = currentAmount;
            }
        }

        if (increases > decreases * 2) {
            return "increasing";
        } else if (decreases > increases * 2) {
            return "decreasing";
        } else {
            return "fluctuating";
        }
    }
}