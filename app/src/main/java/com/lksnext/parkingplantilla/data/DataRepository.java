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

    public boolean isUserLoggedIn() {
        return preferencesManager.isUserLoggedIn();
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
            public void onFailure(Exception e) {
                callback.onFailure();
            }
        });
    }

    public void register(String name, String email, String password, DataCallback<User> callback) {
        dataSource.register(name, email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // Save the registered user
                preferencesManager.saveUser(user);
                preferencesManager.setLoggedIn(true);
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getReservations(String userId, DataCallback<List<Reserva>> callback) {
        dataSource.getReservations(userId, callback);
    }

    public void getHistoricReservations(String userId, DataCallback<List<Reserva>> callback) {
        dataSource.getHistoricReservations(userId, callback);
    }

    public void getCurrentReservation(String userId, DataCallback<Reserva> callback) {
        dataSource.getCurrentReservation(userId, callback);
    }

    public void getNextReservation(String userId, DataCallback<Reserva> callback) {
        dataSource.getNextReservation(userId, callback);
    }

    public void createReservation(Reserva reserva, DataCallback<Boolean> callback) {
        dataSource.createReservation(reserva, callback);
    }

    public void deleteReservation(String reservationId, DataCallback<Boolean> callback) {
        dataSource.deleteReservation(reservationId, callback);
    }

    public void updateReservation(Reserva reserva, DataCallback<Boolean> callback) {
        dataSource.updateReservation(reserva, callback);
    }

    public void checkAvailability(Reserva reserva, DataCallback<Boolean> callback) {
        dataSource.checkAvailability(reserva, callback);
    }

    public void hasReservationOnDate(String userId, String date, DataCallback<Boolean> callback){
        dataSource.hasReservationOnDate(userId, date, callback);
    }

    public void assignRandomPlaza(String tipo, String fecha, long horaInicio, long horaFin, DataCallback<String> callback) {
        dataSource.assignRandomPlaza(tipo, fecha, horaInicio, horaFin, callback);
    }

    public void getAvailableNumbers(String tipo, String row, String fecha, long horaInicio, long horaFin, DataCallback<List<String>> callback) {
        dataSource.getAvailableNumbers(tipo, row, fecha, horaInicio, horaFin, callback);
    }

    public void checkUserExists(String email, DataCallback<Boolean> callback) {
        dataSource.checkUserExists(email, callback);
    }

    public void sendPasswordResetEmail(String email, DataCallback<Boolean> callback) {
        dataSource.sendPasswordResetEmail(email, callback);
    }
}
