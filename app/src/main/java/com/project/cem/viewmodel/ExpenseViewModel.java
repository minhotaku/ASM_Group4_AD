// com.project.cem.viewmodel/ExpenseViewModel.java
package com.project.cem.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.cem.model.Expense;
import com.project.cem.repository.ExpenseRepository;

import java.util.List;

public class ExpenseViewModel extends ViewModel {
    private ExpenseRepository expenseRepository;
    private MutableLiveData<List<Expense>> expensesLiveData = new MutableLiveData<>();

    public ExpenseViewModel(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // Lấy danh sách chi tiêu theo userID
    public void fetchExpenses(int userId) {
        List<Expense> expenses = expenseRepository.getExpensesByUserId(userId);
        expensesLiveData.setValue(expenses);
    }

    // Cung cấp LiveData cho Fragment
    public LiveData<List<Expense>> getExpensesLiveData() {
        return expensesLiveData;
    }

    // Lấy tên danh mục
    public String getCategoryName(int categoryId) {
        return expenseRepository.getCategoryNameById(categoryId);
    }
}