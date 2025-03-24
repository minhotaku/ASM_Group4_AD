package com.project.cem.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoryViewModel extends AndroidViewModel {

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final MutableLiveData<List<CategoryWithCount>> categoriesWithCountLiveData;

    public ExpenseCategoryViewModel(@NonNull Application application) {
        super(application);
        SQLiteHelper dbHelper = new SQLiteHelper(application);
        categoryRepository = new ExpenseCategoryRepository(dbHelper); // Sửa constructor
        expenseRepository = new ExpenseRepository(dbHelper);
        categoriesWithCountLiveData = new MutableLiveData<>();
    }

    public void fetchCategoriesWithCount(int userId) {
        List<ExpenseCategory> categories = categoryRepository.getAllCategories(userId);
        List<CategoryWithCount> categoriesWithCount = new ArrayList<>();

        for (ExpenseCategory category : categories) {
            int count = expenseRepository.getExpenseCountByCategory(category.getCategoryID());
            categoriesWithCount.add(new CategoryWithCount(category, count));
        }

        categoriesWithCountLiveData.setValue(categoriesWithCount);
    }

    public LiveData<List<CategoryWithCount>> getCategoriesWithCountLiveData() {
        return categoriesWithCountLiveData;
    }

    // Thêm phương thức để thêm danh mục mới
    public void addCategory(ExpenseCategory category) {
        categoryRepository.insertExpenseCategory(category); // Sửa thành insertExpenseCategory
    }

    // Lớp nội bộ để lưu danh mục cùng với số lượng chi tiêu
    public static class CategoryWithCount {
        private final ExpenseCategory category;
        private final int count;

        public CategoryWithCount(ExpenseCategory category, int count) {
            this.category = category;
            this.count = count;
        }

        public ExpenseCategory getCategory() {
            return category;
        }

        public int getCount() {
            return count;
        }
    }
}