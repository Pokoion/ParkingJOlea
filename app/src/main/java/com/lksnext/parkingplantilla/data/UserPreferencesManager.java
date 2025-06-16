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
    private static final String FIRST_TIME_USER_FRAGMENT_KEY = "first_time_user_fragment";
    private static final String START_REMINDER_ENABLED_KEY = "start_reminder_enabled";
    private static final String END_REMINDER_ENABLED_KEY = "end_reminder_enabled";

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
        editor.clear();
        editor.apply();
    }

    public boolean isFirstTimeUserFragment() {
        return sharedPreferences.getBoolean(FIRST_TIME_USER_FRAGMENT_KEY, true);
    }

    public void setFirstTimeUserFragment(boolean isFirstTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_TIME_USER_FRAGMENT_KEY, isFirstTime);
        editor.apply();
    }

    public boolean isStartReminderEnabled() {
        return sharedPreferences.getBoolean(START_REMINDER_ENABLED_KEY, false);
    }

    public void setStartReminderEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(START_REMINDER_ENABLED_KEY, enabled);
        editor.apply();
    }

    public boolean isEndReminderEnabled() {
        return sharedPreferences.getBoolean(END_REMINDER_ENABLED_KEY, false);
    }

    public void setEndReminderEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(END_REMINDER_ENABLED_KEY, enabled);
        editor.apply();
    }
}

