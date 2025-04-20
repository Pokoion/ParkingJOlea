// UserPreferencesManager.java
package com.lksnext.parkingplantilla.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.lksnext.parkingplantilla.domain.User;

public class UserPreferencesManager {
    private static final String PREFS_NAME = "ParkingAppPrefs";
    private static final String USER_NAME_KEY = "user_name";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String IS_LOGGED_IN_KEY = "is_logged_in";

    private final SharedPreferences sharedPreferences;

    public UserPreferencesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(User user) {
        if (user == null) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME_KEY, user.getName());
        editor.putString(USER_EMAIL_KEY, user.getEmail());
        editor.apply();
    }

    public User getUser() {
        if (!isUserLoggedIn()) {
            return null;
        }

        String name = sharedPreferences.getString(USER_NAME_KEY, "");
        String email = sharedPreferences.getString(USER_EMAIL_KEY, "");
        return new User(name, email, null);
    }

    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_LOGGED_IN_KEY, isLoggedIn);
        editor.apply();
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(USER_NAME_KEY);
        editor.remove(USER_EMAIL_KEY);
        editor.putBoolean(IS_LOGGED_IN_KEY, false);
        editor.apply();
    }
}