package com.project.cem.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class VndCurrencyFormatter {
    private final DecimalFormat formatter;

    public VndCurrencyFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');

        formatter = new DecimalFormat("#,###", symbols);
        formatter.setGroupingSize(3);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
    }

    public String format(double amount) {
        return formatter.format(amount) + " VNĐ";
    }
}