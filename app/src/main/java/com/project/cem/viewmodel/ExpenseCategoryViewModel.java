package com.project.cem.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.ExpenseCategory;
import com.project.cem.repository.ExpenseCategoryRepository;
import com.project.cem.utils.UserPreferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExpenseCategoryViewModel extends AndroidViewModel {
    private ExpenseCategoryRepository categoryRepository;
    private MutableLiveData<List<ExpenseCategory>> categories = new MutableLiveData<>();
    private MutableLiveData<Map<Integer, Integer>> categoryCounts = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    // Handler cho main thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Executor cho background operations
    private final Executor executor = Executors.newSingleThreadExecutor();

    public ExpenseCategoryViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new ExpenseCategoryRepository(application);
    }

    public LiveData<List<ExpenseCategory>> getCategories() {
        return categories;
    }

    public LiveData<Map<Integer, Integer>> getCategoryCounts() {
        return categoryCounts;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public void loadCategories() {
        mainHandler.post(() -> loading.setValue(true));

        executor.execute(() -> {
            try {
                int userID = UserPreferences.getUser(getApplication()).getUserID();
                List<ExpenseCategory> categoryList = categoryRepository.getAllCategories(userID);

                // Đếm số chi tiêu trong mỗi danh mục
                Map<Integer, Integer> counts = new HashMap<>();
                for (ExpenseCategory category : categoryList) {
                    int count = categoryRepository.getExpenseCountForCategory(category.getCategoryID());
                    counts.put(category.getCategoryID(), count);
                }

                mainHandler.post(() -> {
                    categories.setValue(categoryList);
                    categoryCounts.setValue(counts);
                    loading.setValue(false);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Lỗi khi tải danh mục: " + e.getMessage());
                    loading.setValue(false);
                });
            }
        });
    }

    public void addCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            mainHandler.post(() -> {
                errorMessage.setValue("Tên danh mục không được để trống");
                operationSuccess.setValue(false);
            });
            return;
        }

        mainHandler.post(() -> loading.setValue(true));

        executor.execute(() -> {
            try {
                int userID = UserPreferences.getUser(getApplication()).getUserID();

                // Kiểm tra trùng tên danh mục
                if (categoryRepository.isCategoryExist(userID, categoryName.trim())) {
                    mainHandler.post(() -> {
                        errorMessage.setValue("Danh mục '" + categoryName.trim() + "' đã tồn tại");
                        loading.setValue(false);
                        operationSuccess.setValue(false);
                    });
                    return;
                }

                ExpenseCategory category = new ExpenseCategory();
                category.setUserID(userID);
                category.setCategoryName(categoryName.trim());

                long id = categoryRepository.addCategory(category);

                mainHandler.post(() -> {
                    if (id > 0) {
                        loadCategories();
                        operationSuccess.setValue(true);
                    } else {
                        errorMessage.setValue("Không thể thêm danh mục");
                        loading.setValue(false);
                        operationSuccess.setValue(false);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Lỗi khi thêm danh mục: " + e.getMessage());
                    loading.setValue(false);
                    operationSuccess.setValue(false);
                });
            }
        });
    }

    public void updateCategory(ExpenseCategory category) {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            mainHandler.post(() -> {
                errorMessage.setValue("Tên danh mục không được để trống");
                operationSuccess.setValue(false);
            });
            return;
        }

        mainHandler.post(() -> loading.setValue(true));

        executor.execute(() -> {
            try {
                int userID = UserPreferences.getUser(getApplication()).getUserID();

                // Kiểm tra trùng tên danh mục (loại trừ chính nó)
                if (categoryRepository.isCategoryExistExcludeSelf(userID, category.getCategoryName().trim(), category.getCategoryID())) {
                    mainHandler.post(() -> {
                        errorMessage.setValue("Danh mục '" + category.getCategoryName().trim() + "' đã tồn tại");
                        loading.setValue(false);
                        operationSuccess.setValue(false);
                    });
                    return;
                }

                category.setCategoryName(category.getCategoryName().trim());
                int result = categoryRepository.updateCategory(category);

                mainHandler.post(() -> {
                    if (result > 0) {
                        loadCategories();
                        operationSuccess.setValue(true);
                    } else {
                        errorMessage.setValue("Không thể cập nhật danh mục");
                        loading.setValue(false);
                        operationSuccess.setValue(false);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Lỗi khi cập nhật danh mục: " + e.getMessage());
                    loading.setValue(false);
                    operationSuccess.setValue(false);
                });
            }
        });
    }

    public void deleteCategory(int categoryID) {
        mainHandler.post(() -> loading.setValue(true));

        executor.execute(() -> {
            try {
                boolean hasExpenses = categoryRepository.categoryHasExpenses(categoryID);

                if (hasExpenses) {
                    mainHandler.post(() -> {
                        errorMessage.setValue("Không thể xóa danh mục này vì còn chi tiêu liên quan");
                        loading.setValue(false);
                        operationSuccess.setValue(false);
                    });
                    return;
                }

                int result = categoryRepository.deleteCategory(categoryID);

                mainHandler.post(() -> {
                    if (result > 0) {
                        loadCategories();
                        operationSuccess.setValue(true);
                    } else {
                        errorMessage.setValue("Không thể xóa danh mục");
                        loading.setValue(false);
                        operationSuccess.setValue(false);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Lỗi khi xóa danh mục: " + e.getMessage());
                    loading.setValue(false);
                    operationSuccess.setValue(false);
                });
            }
        });
    }
    public void clearError(){
        errorMessage.setValue(null);
    }
}