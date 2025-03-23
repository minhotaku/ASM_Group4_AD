package com.project.cem.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.CategorySpending;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.UserPreferences;

import java.util.Calendar;
import java.util.List;

public class SpendingOverviewViewModel extends AndroidViewModel {
    private final ExpenseRepository expenseRepository;
    private final MutableLiveData<List<CategorySpending>> categorySpendingData = new MutableLiveData<>();
    private final MutableLiveData<Double> totalSpending = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SpendingOverviewViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
    }

    public LiveData<List<CategorySpending>> getCategorySpendingData() {
        return categorySpendingData;
    }

    public LiveData<Double> getTotalSpending() {
        return totalSpending;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadCurrentMonthSpending() {
        isLoading.setValue(true);

        // Get current user
        long userId = UserPreferences.getUser(getApplication()).getUserID();

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Calendar months are 0-based
        int currentYear = calendar.get(Calendar.YEAR);

        // Load data in background thread
        new Thread(() -> {
            try {
                List<CategorySpending> spending = expenseRepository.getCategorySpendingByMonth(
                        userId, currentMonth, currentYear);

                // Calculate total spending
                double total = 0;
                for (CategorySpending item : spending) {
                    total += item.getAmount();
                }

                // Calculate percentages
                for (CategorySpending item : spending) {
                    double percentage = (item.getAmount() / total) * 100;
                    item.setPercentage(Math.round(percentage * 10) / 10.0); // Round to 1 decimal place
                }

                categorySpendingData.postValue(spending);
                totalSpending.postValue(total);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error loading spending data: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }
}