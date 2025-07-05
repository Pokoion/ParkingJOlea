package com.lksnext.parkingplantilla;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.data.UserPreferencesManager;
import com.lksnext.parkingplantilla.data.firebase.FirebaseDataSource;
import com.lksnext.parkingplantilla.data.repository.DataSource;

public class ParkingApplication extends Application {

    private static ParkingApplication instance;
    private DataRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        applyStoredTheme();
        DataSource dataSource = new FirebaseDataSource();
        repository = DataRepository.getInstance(dataSource, getApplicationContext());
    }

    private void applyStoredTheme() {
        SharedPreferences prefs = getSharedPreferences(
                UserPreferencesManager.PREF_NAME, Context.MODE_PRIVATE);
        int themeMode = prefs.getInt(UserPreferencesManager.PREF_THEME, UserPreferencesManager.THEME_SYSTEM);

        switch (themeMode) {
            case UserPreferencesManager.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case UserPreferencesManager.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public static ParkingApplication getInstance() {
        return instance;
    }

    public DataRepository getRepository() {
        return repository;
    }
}
