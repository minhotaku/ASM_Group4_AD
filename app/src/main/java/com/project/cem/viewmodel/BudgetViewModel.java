package com.project.cem.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.Budget;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.BudgetRepository;

import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private BudgetRepository budgetRepository;
    private MutableLiveData<List<Budget>> allBudgets;
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private LiveData<List<ExpenseCategory>> allCategories;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BudgetViewModel(Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        allBudgets = budgetRepository.getAllBudgets();
        allCategories = new MutableLiveData<>(budgetRepository.getAllCategories());
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void insert(Budget budget) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        new Thread(() -> {
            long result = budgetRepository.insert(budget);
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                isLoading.setValue(false);
                if (result != -1) {
                    messageLiveData.setValue("Budget added successfully!");
                    List<Budget> updatedList = budgetRepository.getAllBudgets().getValue();
                    Log.d("BudgetViewModel", "insert - Updated budget list size: " + (updatedList != null ? updatedList.size() : "null"));
                    allBudgets.setValue(updatedList);

                } else {
                    errorMessage.setValue("Failed to add budget.");
                }
            });
        }).start();
    }

    public void refreshBudgets() {
        allBudgets.setValue(budgetRepository.getAllBudgets().getValue());
    }

    public void update(Budget budget) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        new Thread(() -> {
            int rowsAffected = budgetRepository.update(budget);
            new Handler(Looper.getMainLooper()).post(() -> {

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                isLoading.setValue(false);
                if (rowsAffected > 0) {
                    messageLiveData.setValue("Budget updated successfully!");
                    List<Budget> updatedList = budgetRepository.getAllBudgets().getValue();
                    Log.d("BudgetViewModel", "update - Updated budget list size: " + (updatedList != null ? updatedList.size() : "null"));
                    allBudgets.setValue(updatedList);
                } else {
                    errorMessage.setValue("Failed to update budget.");
                }
            });
        }).start();
    }
    public LiveData<List<ExpenseCategory>> getAllCategories() {
        return allCategories;
    }

}