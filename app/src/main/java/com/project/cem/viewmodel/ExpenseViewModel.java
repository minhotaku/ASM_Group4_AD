package com.project.cem.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.project.cem.model.Expense;
import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.UserPreferences;
import java.util.ArrayList;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final MutableLiveData<List<ExpenseCategory>> expenseCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Expense>> allExpenses = new MutableLiveData<>();

    public ExpenseViewModel(Application application) {
        super(application);
        categoryRepository = new ExpenseCategoryRepository(application);
        expenseRepository = new ExpenseRepository(application);

        loadCategories();
        refreshExpenses(); // Load dữ liệu ban đầu
    }

    public void insertExpense(Expense expense) {
        new Thread(() -> {
            expenseRepository.insertExpense(expense);
            refreshExpenses(); // Cập nhật danh sách sau khi thêm mới
        }).start();
    }

    private void refreshExpenses() {
        List<Expense> expenses = expenseRepository.getAllExpenses(1); // userID = 1
        if (expenses.isEmpty()) {
            Log.d("ExpenseViewModel", "Không có dữ liệu Expense nào sau khi cập nhật!");
        } else {
            Log.d("ExpenseViewModel", "Số lượng Expense mới: " + expenses.size());
        }
        allExpenses.postValue(expenses);
    }



    private void loadCategories() {
        new Thread(() -> {
            int userID = UserPreferences.getUser(getApplication()).getUserID();
            expenseCategories.postValue(categoryRepository.getAllCategories(userID));
        }).start();
    }

    public LiveData<List<ExpenseCategory>> getExpenseCategories() {
        return expenseCategories;
    }

    public LiveData<List<Expense>> getExpensesByCategory(int categoryID) {
        MutableLiveData<List<Expense>> filteredExpenses = new MutableLiveData<>();
        allExpenses.observeForever(expenses -> {
            List<Expense> filteredList = new ArrayList<>();
            for (Expense expense : expenses) {
                if (expense.getCategoryID() == categoryID) {
                    filteredList.add(expense);
                }
            }
            filteredExpenses.postValue(filteredList);
        });
        return filteredExpenses;
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses; // Trả về LiveData thay vì tạo mới mỗi lần gọi
    }
}