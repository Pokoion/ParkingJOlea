package com.lksnext.parkingplantilla.viewmodel;

import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.User;

public class UserViewModel extends ViewModel {

    private final DataRepository repository;
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(null);
    private LoginViewModel loginViewModel;

    // Constantes para el tema
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_SYSTEM = 3;

    public static final String PREF_NAME = "theme_preferences";
    public static final String PREF_THEME = "selected_theme";
    public static final String PREF_START_REMINDER = "start_reminder_enabled";
    public static final String PREF_END_REMINDER = "end_reminder_enabled";

    public UserViewModel() {
        this.repository = ParkingApplication.getRepository();
    }

    public void setLoginViewModel(LoginViewModel loginViewModel) {
        this.loginViewModel = loginViewModel;
    }

    public User getCurrentUser() {
        return repository.getCurrentUser();
    }

    public LiveData<Boolean> isLogoutSuccessful() {
        return logoutSuccess;
    }

    public void logout() {
        // Clear user session in repository if needed
        if (repository != null) {
            repository.logout();
        }

        // Use LoginViewModel to clear credentials and update logged state
        if (loginViewModel != null) {
            loginViewModel.logout();
        }

        // Signal logout success
        logoutSuccess.setValue(true);
    }

    // Reset logout state (call this after navigation)
    public void resetLogoutState() {
        logoutSuccess.setValue(null);
    }

    // Métodos para gestionar el tema
    public void setThemeMode(int themeMode) {
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_THEME, themeMode);
        editor.apply();

        applyTheme(themeMode);
    }

    public int getThemeMode() {
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(PREF_NAME, 0);
        return prefs.getInt(PREF_THEME, THEME_SYSTEM); // Por defecto, tema del sistema
    }

    public void applyTheme(int themeMode) {
        // Guardar preferencia primero
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_THEME, themeMode);
        editor.apply();

        // Luego aplicar el tema
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public void setStartReminderEnabled(boolean enabled) {
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_START_REMINDER, enabled);
        editor.apply();
    }

    public boolean isStartReminderEnabled() {
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(PREF_NAME, 0);
        return prefs.getBoolean(PREF_START_REMINDER, false); // Por defecto, desactivado
    }

    public void setEndReminderEnabled(boolean enabled) {
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_END_REMINDER, enabled);
        editor.apply();
    }

    public boolean isEndReminderEnabled() {
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(PREF_NAME, 0);
        return prefs.getBoolean(PREF_END_REMINDER, false); // Por defecto, desactivado
    }
}