package com.project.cem.model;

import java.util.Date;

// Lớp để nhóm chi tiêu theo tháng/năm và lưu tên danh mục
public class ExpenseWithCategory {
    private int expenseID;
    private int userID;
    private int categoryID;
    private String description;
    private double amount;
    private Date date;
    private String categoryName;
    private String monthYear;
    
    public ExpenseWithCategory(Expense expense, String categoryName, String monthYear) {
        this.expenseID = expense.getExpenseID();
        this.userID = expense.getUserID();
        this.categoryID = expense.getCategoryID();
        this.description = expense.getDescription();
        this.amount = expense.getAmount();
        this.date = expense.getDate();
        this.categoryName = categoryName;
        this.monthYear = monthYear;
    }

    public int getExpenseID() {
        return expenseID;
    }

    public void setExpenseID(int expenseID) {
        this.expenseID = expenseID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getMonthYear() {
        return monthYear;
    }
    
    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }
}