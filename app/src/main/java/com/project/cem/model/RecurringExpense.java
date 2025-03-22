package com.project.cem.model;
import java.util.Date;
public class RecurringExpense {
    private int recurringExpenseID;
    private int userID;
    private int categoryID;
    private String description;
    private double amount;
    private int month;
    private int year;
    private String recurrenceFrequency;

    public RecurringExpense() {
    }

    public RecurringExpense(int recurringExpenseID, int userID, int categoryID,
                            String description, double amount, int month, int year,
                            String recurrenceFrequency) {
        this.recurringExpenseID = recurringExpenseID;
        this.userID = userID;
        this.categoryID = categoryID;
        this.description = description;
        this.amount = amount;
        this.month = month;
        this.year = year;
        this.recurrenceFrequency = recurrenceFrequency;

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getRecurrenceFrequency() {
        return recurrenceFrequency;
    }

    public void setRecurrenceFrequency(String recurrenceFrequency) {
        this.recurrenceFrequency = recurrenceFrequency;
    }
}
