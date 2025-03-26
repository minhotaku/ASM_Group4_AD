package com.project.cem.utils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ValidationUtils {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";


    private static final String PASSWORD_REGEX =
            "^(?=.*[0-9])(?=.*[a-zA-Z]).{5,}$";

    private static final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_REGEX);
    private ValidationUtils(){}
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }

        Matcher matcher = passwordPattern.matcher(password);
        return matcher.matches();
    }

}
