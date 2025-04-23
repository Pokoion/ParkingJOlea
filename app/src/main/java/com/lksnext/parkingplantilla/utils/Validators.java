package com.lksnext.parkingplantilla.utils;

import android.util.Patterns;

public class Validators {

    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }

    public static boolean isValidPassword(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }

    public static boolean areLoginFieldsValid(String email, String password) {
        return !email.trim().isEmpty() && !password.trim().isEmpty();
    }
}