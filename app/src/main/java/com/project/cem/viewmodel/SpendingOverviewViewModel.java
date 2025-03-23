package com.project.cem.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.graphics.Color;
import com.project.cem.model.CategorySpending;
import com.project.cem.repository.SpendingOverviewRepository;
import com.project.cem.utils.UserPreferences;

import java.util.Calendar;
import java.util.List;

public class SpendingOverviewViewModel extends AndroidViewModel {
    private final SpendingOverviewRepository spendingOverviewRepository;
    private final MutableLiveData<List<CategorySpending>> categorySpendingData = new MutableLiveData<>();
    private final MutableLiveData<Double> totalSpending = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalBudget = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> budgetPercentage = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SpendingOverviewViewModel(@NonNull Application application) {
        super(application);
        spendingOverviewRepository = new SpendingOverviewRepository(application);
    }

    public LiveData<List<CategorySpending>> getCategorySpendingData() {
        return categorySpendingData;
    }

    public LiveData<Double> getTotalSpending() {
        return totalSpending;
    }

    public LiveData<Double> getTotalBudget() {
        return totalBudget;
    }

    public LiveData<Double> getBudgetPercentage() {
        return budgetPercentage;
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
                // Get regular expenses
                List<CategorySpending> regularSpending = spendingOverviewRepository.getCategorySpendingByMonth(
                        userId, currentMonth, currentYear);

                // Get recurring expenses for this month
                List<CategorySpending> recurringSpending = spendingOverviewRepository.getRecurringExpenses(
                        userId, currentMonth, currentYear);

                // Merge regular and recurring expenses
                for (CategorySpending recurring : recurringSpending) {
                    boolean found = false;
                    for (CategorySpending regular : regularSpending) {
                        if (regular.getCategoryId() == recurring.getCategoryId()) {
                            // Add recurring amount to the same category
                            regular.setAmount(regular.getAmount() + recurring.getAmount());
                            found = true;
                            break;
                        }
                    }

                    // If category is not in regular expenses, add it
                    if (!found) {
                        regularSpending.add(recurring);
                    }
                }

                // Calculate total spending
                double total = 0;
                for (CategorySpending item : regularSpending) {
                    total += item.getAmount();
                }

                // Calculate percentages
                for (CategorySpending item : regularSpending) {
                    double percentage = (item.getAmount() / total) * 100;
                    item.setPercentage(Math.round(percentage * 10) / 10.0); // Round to 1 decimal place
                }

                // Define color array for categories
                int[] colorArray = new int[] {
                        Color.rgb(46, 204, 113),   // Green
                        Color.rgb(52, 152, 219),   // Blue
                        Color.rgb(149, 165, 166),  // Gray
                        Color.rgb(231, 76, 60),    // Red
                        Color.rgb(211, 84, 0),     // Dark Orange
                        Color.rgb(243, 156, 18),   // Orange
                        Color.rgb(26, 188, 156),   // Turquoise
                        Color.rgb(241, 196, 15),   // Yellow
                        Color.rgb(230, 126, 34),   // Carrot
                        Color.rgb(41, 128, 185),   // Dark Blue
                        Color.rgb(142, 68, 173),   // Dark Purple
                        Color.rgb(39, 174, 96),    // Dark Green
                        Color.rgb(192, 57, 43),    // Dark Red
                        Color.rgb(155, 89, 182),   // Purple
                        Color.rgb(44, 62, 80)      // Dark Navy
                };

                // Assign unique color to each category
                for (int i = 0; i < regularSpending.size(); i++) {
                    regularSpending.get(i).setColorCode(colorArray[i % colorArray.length]);
                }

                // Get total budget for the month
                double budget = spendingOverviewRepository.getTotalBudgetForMonth(userId, currentMonth, currentYear);

                // Calculate budget percentage
                double budgetPct = (budget > 0) ? (total / budget) * 100 : 0;

                categorySpendingData.postValue(regularSpending);
                totalSpending.postValue(total);
                totalBudget.postValue(budget);
                budgetPercentage.postValue(budgetPct);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error loading spending data: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }
}