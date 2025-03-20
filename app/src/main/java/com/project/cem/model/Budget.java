package com.project.cem.model;
import java.util.Date;
public class Budget {
    private int budgetID;
    private int categoryID;
    private double amount;
    private Date startDate;
    private Date endDate;

    public Budget() {
    }

    public Budget(int budgetID, int categoryID, double amount, Date startDate, Date endDate) {
        this.budgetID = budgetID;
        this.categoryID = categoryID;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getBudgetID() {
        return budgetID;
    }

    public void setBudgetID(int budgetID) {
        this.budgetID = budgetID;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
