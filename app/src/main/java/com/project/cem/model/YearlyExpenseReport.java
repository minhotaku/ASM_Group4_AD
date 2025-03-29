package com.project.cem.model;

import java.util.List;

public class YearlyExpenseReport {

    private int year;
    private double totalYearlyExpenses;
    private List<MonthlyTotal> monthlyTotals;
    private List<CategoryTotal> categoryTotals;

    public YearlyExpenseReport(int year, double totalYearlyExpenses,
                               List<MonthlyTotal> monthlyTotals,
                               List<CategoryTotal> categoryTotals) {
        this.year = year;
        this.totalYearlyExpenses = totalYearlyExpenses;
        this.monthlyTotals = monthlyTotals;
        this.categoryTotals = categoryTotals;
    }

    public int getYear() {
        return year;
    }

    public double getTotalYearlyExpenses() {
        return totalYearlyExpenses;
    }

    public List<MonthlyTotal> getMonthlyTotals() {
        return monthlyTotals;
    }

    public List<CategoryTotal> getCategoryTotals() {
        return categoryTotals;
    }

    // Inner class for monthly totals
    public static class MonthlyTotal {
        private int month; // 1-12
        private double totalAmount;
        private double budgetAmount;

        public MonthlyTotal(int month, double totalAmount, double budgetAmount) {
            this.month = month;
            this.totalAmount = totalAmount;
            this.budgetAmount = budgetAmount;
        }

        public int getMonth() {
            return month;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public double getBudgetAmount() {
            return budgetAmount;
        }
    }

    // Inner class for category totals across the year
    public static class CategoryTotal {
        private int categoryId;
        private String categoryName;
        private double totalAmount;
        private List<Double> monthlyAmounts; // Amount per month (12 values)

        public CategoryTotal(int categoryId, String categoryName, double totalAmount,
                             List<Double> monthlyAmounts) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
            this.monthlyAmounts = monthlyAmounts;
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

        public List<Double> getMonthlyAmounts() {
            return monthlyAmounts;
        }
    }
}