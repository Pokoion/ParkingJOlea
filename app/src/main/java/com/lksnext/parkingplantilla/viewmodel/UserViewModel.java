package com.lksnext.parkingplantilla.viewmodel;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.data.UserPreferencesManager;
import com.lksnext.parkingplantilla.domain.User;

public class UserViewModel extends ViewModel {

    private final DataRepository repository;
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(null);
    private LoginViewModel loginViewModel;

    public UserViewModel() {
        repository = ParkingApplication.getInstance().getRepository();
    }
    public UserViewModel(DataRepository repository) {
        this.repository = repository;
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
                .getSharedPreferences(UserPreferencesManager.PREF_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(UserPreferencesManager.PREF_THEME, themeMode);
        editor.apply();

        applyTheme(themeMode);
    }

    public int getThemeMode() {
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(UserPreferencesManager.PREF_NAME, 0);
        return prefs.getInt(UserPreferencesManager.PREF_THEME, UserPreferencesManager.THEME_SYSTEM); // Por defecto, tema del sistema
    }

    public void applyTheme(int themeMode) {
        // Guardar preferencia primero
        SharedPreferences prefs = ParkingApplication.getAppContext()
                .getSharedPreferences(UserPreferencesManager.PREF_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(UserPreferencesManager.PREF_THEME, themeMode);
        editor.apply();

        // Luego aplicar el tema
        switch (themeMode) {
            case UserPreferencesManager.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case UserPreferencesManager.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case UserPreferencesManager.THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}