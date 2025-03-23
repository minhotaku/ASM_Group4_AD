package com.project.cem.model;

public class CategorySpending {
    private long categoryId;
    private String categoryName;
    private double amount;
    private double percentage;
    private int colorCode;

    public CategorySpending(long categoryId, String categoryName, double amount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
        this.percentage = 0.0;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getColorCode() {
        return colorCode;
    }

    public void setColorCode(int colorCode) {
        this.colorCode = colorCode;
    }
}