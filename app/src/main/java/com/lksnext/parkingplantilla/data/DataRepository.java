package com.lksnext.parkingplantilla.data;

import android.content.Context;

import com.lksnext.parkingplantilla.data.repository.DataSource;
import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;

import java.util.List;

public class DataRepository {
    private static DataRepository instance;
    private final DataSource dataSource;
    private final UserPreferencesManager preferencesManager;

    private DataRepository(DataSource dataSource, Context context) {
        this.dataSource = dataSource;
        this.preferencesManager = new UserPreferencesManager(context);
    }

    public static DataRepository getInstance(DataSource dataSource, Context context) {
        if (instance == null) {
            instance = new DataRepository(dataSource, context);
        }
        return instance;
    }

    public void logout() {
        preferencesManager.clearUserData();
    }

    public User getCurrentUser() {
        return preferencesManager.getUser();
    }

    public void saveUser(User user) {
        preferencesManager.saveUser(user);
    }

    public boolean isUserLoggedIn() {
        return preferencesManager.isUserLoggedIn();
    }

    private void setLoggedIn(boolean isLoggedIn) {
        preferencesManager.setLoggedIn(isLoggedIn);
    }

    public void login(String email, String password, Callback callback) {
        dataSource.login(email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // Save the authenticated user
                preferencesManager.saveUser(user);
                preferencesManager.setLoggedIn(true);
                callback.onSuccess();
            }

            @Override
            public void onError(Exception e) {
                callback.onFailure();
            }
        });
    }

    public void getReservations(String userId, DataCallback<List<Reserva>> callback) {
        dataSource.getReservations(userId, callback);
    }

    public void getAllReservations(DataCallback<List<Reserva>> callback) {
        dataSource.getAllReservations(callback);
    }

    public void createReservation(Reserva reserva, Callback callback) {
        dataSource.createReservation(reserva, callback);
    }

    public void cancelReservation(String reservationId, Callback callback) {
        dataSource.cancelReservation(reservationId, callback);
    }
}