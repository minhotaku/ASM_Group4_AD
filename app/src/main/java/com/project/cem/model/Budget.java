package com.project.cem.model;

import java.util.Date;

public class Budget {
    private int budgetID;
    private int userID;
    private int categoryID;
    private double amount;
    private Date startDate;
    private Date endDate;

    public Budget() {
    }
    public Budget(int categoryID, double amount, Date startDate, Date endDate) {
        this.categoryID = categoryID;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Budget(int budgetID, int categoryID, double amount, Date startDate, Date endDate) {
        this.budgetID = budgetID;
        this.categoryID = categoryID;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    // Getters and setters

    public int getBudgetID() {
        return budgetID;
    }
    public int getUserID() {
        return userID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public double getAmount() {
        return amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setBudgetID(int budgetID) {
        this.budgetID = budgetID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}