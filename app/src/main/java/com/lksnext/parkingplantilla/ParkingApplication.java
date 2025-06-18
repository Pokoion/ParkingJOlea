package com.lksnext.parkingplantilla;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.data.local.LocalDataSource;
import com.lksnext.parkingplantilla.data.firebase.FirebaseDataSource;
import com.lksnext.parkingplantilla.data.repository.DataSource;
import com.lksnext.parkingplantilla.viewmodel.UserViewModel;

public class ParkingApplication extends Application {

    private static Context context;
    private static DataRepository repository;
    private static ParkingApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        instance = this;

        // Aplicar el tema guardado al inicio
        applyStoredTheme();

        // Initialize the data source
        DataSource dataSource = new LocalDataSource();
        //DataSource dataSource = new FirebaseDataSource();

        // Initialize repository with the data source
        repository = DataRepository.getInstance(dataSource, getApplicationContext());
    }

    private void applyStoredTheme() {
        SharedPreferences prefs = getSharedPreferences(
                UserViewModel.PREF_NAME, Context.MODE_PRIVATE);
        int themeMode = prefs.getInt(UserViewModel.PREF_THEME, UserViewModel.THEME_SYSTEM);

        switch (themeMode) {
            case UserViewModel.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case UserViewModel.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static DataRepository getRepository() {
        return repository;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}