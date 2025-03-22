package com.project.cem.model;

import java.util.Date;

public class Budget {
    private int budgetID;
    private int userID;
    private int categoryID;
    private double amount;
    private int month;
    private int year;

    public Budget() {
    }

    public Budget(int budgetID, int userID, int categoryID, double amount, int month, int year) {
        this.budgetID = budgetID;
        this.userID = userID;
        this.categoryID = categoryID;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }

    public int getBudgetID() {
        return budgetID;
    }

    public void setBudgetID(int budgetID) {
        this.budgetID = budgetID;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
}