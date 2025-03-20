package com.project.cem.model;
import java.util.Date;
public class RecurringExpense {
    private int recurringExpenseID;
    private int userID;
    private int categoryID;
    private String description;
    private Date startDate;
    private Date endDate;
    private String recurrenceFrequency;

    public RecurringExpense() {
    }

    public RecurringExpense(int recurringExpenseID, int userID, int categoryID,
                            String description, Date startDate, Date endDate,
                            String recurrenceFrequency) {
        this.recurringExpenseID = recurringExpenseID;
        this.userID = userID;
        this.categoryID = categoryID;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public String getRecurrenceFrequency() {
        return recurrenceFrequency;
    }

    public void setRecurrenceFrequency(String recurrenceFrequency) {
        this.recurrenceFrequency = recurrenceFrequency;
    }
}
