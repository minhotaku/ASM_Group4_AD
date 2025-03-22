package com.project.cem.model;
import java.util.Date;
public class RecurringExpense {
    private int recurringExpenseID;
    private int userID;
    private int categoryID;
    private String description;
    private int year;
    private int month;

    public RecurringExpense() {
    }

    public RecurringExpense(int recurringExpenseID, int userID, int categoryID, String description, int year, int month) {
        this.recurringExpenseID = recurringExpenseID;
        this.userID = userID;
        this.categoryID = categoryID;
        this.description = description;
        this.year = year;
    }

    public int getRecurringExpenseID() {
        return recurringExpenseID;
    }

    public void setRecurringExpenseID(int recurringExpenseID) {
        this.recurringExpenseID = recurringExpenseID;
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
