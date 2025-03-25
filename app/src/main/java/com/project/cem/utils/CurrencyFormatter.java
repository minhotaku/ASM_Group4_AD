package com.project.cem.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    private static final NumberFormat vietnameseFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    public static String formatVND(double amount) {
        return vietnameseFormat.format(amount);
    }
}