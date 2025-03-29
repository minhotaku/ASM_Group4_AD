package com.project.cem.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class VndCurrencyFormatter {
    private final DecimalFormat formatter;
    private final DecimalFormat millionFormatter;

    public VndCurrencyFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        formatter = new DecimalFormat("#,###", symbols);
        formatter.setGroupingSize(3);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);

        millionFormatter = new DecimalFormat("#,##0.00", symbols);
        millionFormatter.setGroupingSize(3);
    }

    public String format(double amount) {
        return formatter.format(amount) + " VNĐ";
    }

    public String formatForEditText(double amount) {
        return formatter.format(amount);
    }


    public String formatInMillions(double amount) {
        if (amount == 0) {
            return "";
        }
        double millions = amount / 1_000_000;

        DecimalFormat millionFormatter = new DecimalFormat("#,##0.##");

        return millionFormatter.format(millions) + "M";
    }


    public String formatInMillionsForChart(double amount) {
        double millions = amount / 1_000_000;
        return millionFormatter.format(millions);
    }


    public String formatAutoUnit(double amount) {
        if (amount >= 1_000_000) {
            return formatInMillions(amount);
        } else {
            return format(amount);
        }
    }
}