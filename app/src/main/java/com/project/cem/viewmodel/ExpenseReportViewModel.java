package com.project.cem.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.ExpenseReportModels;
import com.project.cem.repository.ExpenseReportRepository;
import com.project.cem.utils.UserPreferences;
import com.project.cem.utils.VndCurrencyFormatter;

import java.util.Calendar;
import java.util.Locale;

public class ExpenseReportViewModel extends AndroidViewModel {

    private final ExpenseReportRepository repository;
    private final MutableLiveData<ExpenseReportModels.MonthlyExpenseReport> monthlyReport = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedMonth = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedYear = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private static final VndCurrencyFormatter vndFormatter = new VndCurrencyFormatter();

    public ExpenseReportViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseReportRepository(application);

        // Initialize with current month and year
        Calendar calendar = Calendar.getInstance();
        selectedMonth.setValue(calendar.get(Calendar.MONTH) + 1); // Calendar months are 0-indexed
        selectedYear.setValue(calendar.get(Calendar.YEAR));

        // Load initial data
        loadMonthlyReport();
    }

    public LiveData<ExpenseReportModels.MonthlyExpenseReport> getMonthlyReport() {
        return monthlyReport;
    }

    public LiveData<Integer> getSelectedMonth() {
        return selectedMonth;
    }

    public LiveData<Integer> getSelectedYear() {
        return selectedYear;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setSelectedMonth(int month) {
        if (month < 1) {
            selectedMonth.setValue(12);
            selectedYear.setValue(selectedYear.getValue() - 1);
        } else if (month > 12) {
            selectedMonth.setValue(1);
            selectedYear.setValue(selectedYear.getValue() + 1);
        } else {
            selectedMonth.setValue(month);
        }
        loadMonthlyReport();
    }

    public void setSelectedYear(int year) {
        selectedYear.setValue(year);
        loadMonthlyReport();
    }

    public void nextMonth() {
        int currentMonth = selectedMonth.getValue();
        setSelectedMonth(currentMonth + 1);
    }

    public void previousMonth() {
        int currentMonth = selectedMonth.getValue();
        setSelectedMonth(currentMonth - 1);
    }

    private void loadMonthlyReport() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        try {
            int userId = UserPreferences.getUser(getApplication()).getUserID();
            int month = selectedMonth.getValue();
            int year = selectedYear.getValue();

            // Execute in a background thread
            new Thread(() -> {
                try {
                    ExpenseReportModels.MonthlyExpenseReport report =
                            repository.getMonthlyExpenseReport(userId, month, year);

                    monthlyReport.postValue(report);
                    isLoading.postValue(false);
                } catch (Exception e) {
                    errorMessage.postValue("Error loading report: " + e.getMessage());
                    isLoading.postValue(false);
                }
            }).start();

        } catch (Exception e) {
            errorMessage.setValue("Error: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    // Currency formatter helper
    public static String formatCurrency(double amount) {
        return vndFormatter.format(amount);
    }

    // Get month name
    public static String getMonthName(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1); // Calendar months are 0-indexed

        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }

    // Get month and year formatted string
    public String getFormattedMonthYear() {
        int month = selectedMonth.getValue();
        int year = selectedYear.getValue();

        return getMonthName(month) + " " + year;
    }
}