package com.project.cem.model;
import java.util.Date;

public class Expense {
    private int expenseID;
    private int userID;
    private int categoryID;
    private String description;
    private double amount;
    private Date date;

    public Expense() {
    }

    public Expense(int expenseID, int userID, String description, int categoryID, double amount, Date date) {
        this.expenseID = expenseID;
        this.userID = userID;
        this.description = description;
        this.categoryID = categoryID;
        this.amount = amount;
        this.date = date;
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
}
