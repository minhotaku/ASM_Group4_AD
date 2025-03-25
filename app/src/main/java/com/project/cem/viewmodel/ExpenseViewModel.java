package com.project.cem.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.cem.model.Expense;
import com.project.cem.model.ExpenseWithCategory;
import com.project.cem.repository.ExpenseRepository;
import com.project.cem.utils.UserPreferences;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExpenseViewModel extends AndroidViewModel {
    private ExpenseRepository expenseRepository;
    private MutableLiveData<List<ExpenseWithCategory>> expenses = new MutableLiveData<>();
    private MutableLiveData<Map<String, List<ExpenseWithCategory>>> groupedExpenses = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private int currentCategoryFilter = -1; // -1 means no filter

    // Handler cho main thread để cập nhật LiveData an toàn
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Executor để xử lý các thao tác database trên background
    private final Executor executor = Executors.newSingleThreadExecutor();

    // Format để chuyển đổi chuỗi tháng-năm sang Date để so sánh
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
    }

    public LiveData<List<ExpenseWithCategory>> getExpenses() {
        return expenses;
    }

    public LiveData<Map<String, List<ExpenseWithCategory>>> getGroupedExpenses() {
        return groupedExpenses;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadExpenses() {
        // Đặt loading lên UI thread
        mainHandler.post(() -> loading.setValue(true));

        executor.execute(() -> {
            try {
                int userID = UserPreferences.getUser(getApplication()).getUserID();
                List<ExpenseWithCategory> expenseList;

                // Tải theo bộ lọc nếu có
                if (currentCategoryFilter > 0) {
                    expenseList = expenseRepository.getExpensesByCategory(userID, currentCategoryFilter);
                } else {
                    expenseList = expenseRepository.getAllExpenses(userID);
                }

                // Nhóm các chi tiêu theo tháng/năm
                Map<String, List<ExpenseWithCategory>> grouped = new HashMap<>();
                for (ExpenseWithCategory expense : expenseList) {
                    String monthYear = expense.getMonthYear();
                    if (!grouped.containsKey(monthYear)) {
                        grouped.put(monthYear, new ArrayList<>());
                    }
                    grouped.get(monthYear).add(expense);
                }

                // Sắp xếp các nhóm theo thời gian giảm dần
                Map<String, List<ExpenseWithCategory>> sortedGrouped = sortMonthYearMap(grouped);

                // Đặt kết quả trên UI thread
                mainHandler.post(() -> {
                    expenses.setValue(expenseList);
                    groupedExpenses.setValue(sortedGrouped);
                    loading.setValue(false);
                });
            } catch (Exception e) {
                // Xử lý lỗi trên UI thread
                mainHandler.post(() -> {
                    errorMessage.setValue("Lỗi khi tải chi tiêu: " + e.getMessage());
                    loading.setValue(false);
                });
            }
        });
    }

    // Sắp xếp Map theo tháng-năm giảm dần
    private Map<String, List<ExpenseWithCategory>> sortMonthYearMap(Map<String, List<ExpenseWithCategory>> unsorted) {
        // Sử dụng custom Comparator để so sánh tháng-năm
        Comparator<String> monthYearComparator = (monthYear1, monthYear2) -> {
            try {
                Date date1 = monthYearFormat.parse(monthYear1);
                Date date2 = monthYearFormat.parse(monthYear2);
                // Sắp xếp giảm dần (mới nhất trước)
                return date2.compareTo(date1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0; // Nếu có lỗi, giữ nguyên thứ tự
            }
        };

        Map<String, List<ExpenseWithCategory>> sortedMap = new LinkedHashMap<>();

        // Lấy danh sách các key và sắp xếp
        List<String> keys = new ArrayList<>(unsorted.keySet());
        Collections.sort(keys, monthYearComparator);

        // Thêm lại vào LinkedHashMap để giữ thứ tự
        for (String key : keys) {
            sortedMap.put(key, unsorted.get(key));
        }

        return sortedMap;
    }

    public void setFilter(int categoryID) {
        currentCategoryFilter = categoryID;
        loadExpenses();
    }

    public void clearFilter() {
        currentCategoryFilter = -1;
        loadExpenses();
    }

    public void addExpense(Expense expense) {
        mainHandler.post(() -> loading.setValue(true));

        executor.execute(() -> {
            try {
                long id = expenseRepository.addExpense(expense);

                mainHandler.post(() -> {
                    if (id > 0) {
                        loadExpenses();
                    } else {
                        errorMessage.setValue("Không thể thêm chi tiêu");
                        loading.setValue(false);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Lỗi khi thêm chi tiêu: " + e.getMessage());
                    loading.setValue(false);
                });
            }
        });
    }

    public void updateExpense(Expense expense) {
        mainHandler.post(() -> loading.setValue(true));

        executor.execute(() -> {
            try {
                int result = expenseRepository.updateExpense(expense);

                mainHandler.post(() -> {
                    if (result > 0) {
                        loadExpenses();
                    } else {
                        errorMessage.setValue("Không thể cập nhật chi tiêu");
                        loading.setValue(false);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Lỗi khi cập nhật chi tiêu: " + e.getMessage());
                    loading.setValue(false);
                });
            }
        });
    }

    public void deleteExpense(int expenseID) {
        mainHandler.post(() -> loading.setValue(true));

        executor.execute(() -> {
            try {
                int result = expenseRepository.deleteExpense(expenseID);

                mainHandler.post(() -> {
                    if (result > 0) {
                        loadExpenses();
                    } else {
                        errorMessage.setValue("Không thể xóa chi tiêu");
                        loading.setValue(false);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Lỗi khi xóa chi tiêu: " + e.getMessage());
                    loading.setValue(false);
                });
            }
        });
    }
}