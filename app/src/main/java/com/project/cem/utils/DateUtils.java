package com.project.cem.utils;

import android.widget.NumberPicker;
import java.util.Calendar;

public class DateUtils {

    // Prevent instantiation
    private DateUtils() {}

    public static final String[] MONTH_NAMES = new String[] {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    // Sets up a NumberPicker for month selection
    public static void setupMonthPicker(NumberPicker monthPicker) {
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(MONTH_NAMES);
        monthPicker.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1); // Current month
    }
    // Sets up a NumberPicker for year selection, current year, min, max
    public static void setupYearPicker(NumberPicker yearPicker, int minYearOffset, int maxYearOffset) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear + minYearOffset);
        yearPicker.setMaxValue(currentYear + maxYearOffset);
        yearPicker.setValue(currentYear);
    }

    // Gets the month name (e.g., "January") from the month number (1-12)
    public static String getMonthName(int month) {
        if (month >= 1 && month <= 12) {
            return MONTH_NAMES[month - 1]; // Month is 1-indexed
        }
        return "Invalid Month"; // Or throw an exception
    }

    // You could also add a convenience method to set both:
    public static void setupMonthAndYearPickers(NumberPicker monthPicker, NumberPicker yearPicker, int minYearOffset, int maxYearOffset) {
        setupMonthPicker(monthPicker);
        setupYearPicker(yearPicker, minYearOffset, maxYearOffset);
    }
}