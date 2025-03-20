package com.project.cem.model;
import java.util.Date;
public class ExpenseReport {
    private int reportID;
    private int userID;
    private Date generatedDate;
    private String reportData;

    public ExpenseReport() {
    }

    public ExpenseReport(int reportID, int userID, Date generatedDate, String reportData) {
        this.reportID = reportID;
        this.userID = userID;
        this.generatedDate = generatedDate;
        this.reportData = reportData;
    }

    public int getReportID() {
        return reportID;
    }

    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }
}
