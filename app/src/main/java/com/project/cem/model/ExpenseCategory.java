package com.project.cem.model;

public class ExpenseCategory {
    private int categoryID;
    private int userID;
    private String categoryName;

    public ExpenseCategory() {
    }

    public ExpenseCategory(int categoryID, int userID, String categoryName) {
        this.categoryID = categoryID;
        this.userID = userID;
        this.categoryName = categoryName;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
