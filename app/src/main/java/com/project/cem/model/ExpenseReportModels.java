package com.project.cem.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExpenseReportModels {

    public static class ExpenseItem {
        private int expenseId;
        private int userId;
        private int categoryId;
        private String categoryName;
        private String description;
        private double amount;
        private Date date;

        public ExpenseItem(int expenseId, int userId, int categoryId, String categoryName, String description, double amount, Date date) {
            this.expenseId = expenseId;
            this.userId = userId;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.description = description;
            this.amount = amount;
            this.date = date;
        }

        public int getExpenseId() {
            return expenseId;
        }

        public int getUserId() {
            return userId;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public String getDescription() {
            return description;
        }

        public double getAmount() {
            return amount;
        }

        public Date getDate() {
            return date;
        }
    }

    public static class MonthlyBudget {
        private int budgetId;
        private int userId;
        private int categoryId;
        private String categoryName;
        private double amount;
        private int month;
        private int year;

        public MonthlyBudget(int budgetId, int userId, int categoryId, String categoryName, double amount, int month, int year) {
            this.budgetId = budgetId;
            this.userId = userId;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.amount = amount;
            this.month = month;
            this.year = year;
        }

        public int getBudgetId() {
            return budgetId;
        }

        public int getUserId() {
            return userId;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getAmount() {
            return amount;
        }

        public int getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }
    }

    public static class DailyExpenseSummary {
        private Date date;
        private double totalAmount;

        public DailyExpenseSummary(Date date, double totalAmount) {
            this.date = date;
            this.totalAmount = totalAmount;
        }

        public Date getDate() {
            return date;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }

    public static class CategoryExpenseSummary {
        private int categoryId;
        private String categoryName;
        private double totalAmount;

        public CategoryExpenseSummary(int categoryId, String categoryName, double totalAmount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }

    public static class MonthlyExpenseReport {
        private List<ExpenseItem> expenses;
        private List<MonthlyBudget> budgets;
        private List<DailyExpenseSummary> dailyExpenses;
        private List<CategoryExpenseSummary> categoryExpenses;
        private double totalExpenses;
        private double totalBudget;
        private int month;
        private int year;

        public MonthlyExpenseReport(List<ExpenseItem> expenses, List<MonthlyBudget> budgets,
                                    List<DailyExpenseSummary> dailyExpenses,
                                    List<CategoryExpenseSummary> categoryExpenses,
                                    double totalExpenses, double totalBudget,
                                    int month, int year) {
            this.expenses = expenses;
            this.budgets = budgets;
            this.dailyExpenses = dailyExpenses;
            this.categoryExpenses = categoryExpenses;
            this.totalExpenses = totalExpenses;
            this.totalBudget = totalBudget;
            this.month = month;
            this.year = year;
        }

        public List<ExpenseItem> getExpenses() {
            return expenses;
        }

        public List<MonthlyBudget> getBudgets() {
            return budgets;
        }

        public List<DailyExpenseSummary> getDailyExpenses() {
            return dailyExpenses;
        }

        public List<CategoryExpenseSummary> getCategoryExpenses() {
            return categoryExpenses;
        }

        public double getTotalExpenses() {
            return totalExpenses;
        }

        public double getTotalBudget() {
            return totalBudget;
        }

        public int getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }
    }
}