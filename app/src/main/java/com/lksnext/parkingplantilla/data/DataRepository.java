package com.lksnext.parkingplantilla.data;

import com.lksnext.parkingplantilla.data.repository.DataSource;
import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;

import java.util.List;

public class DataRepository {

    private static DataRepository instance;
    private final DataSource dataSource;

    /**
     * Private constructor to enforce singleton pattern.
     * @param dataSource The data source to be used.
     */
    private DataRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Singleton pattern to get the instance of DataRepository.
     * @param dataSource The data source to be used.
     * @return The instance of DataRepository.
     */
    public static DataRepository getInstance(DataSource dataSource) {
        if (instance == null) {
            instance = new DataRepository(dataSource);
        }
        return instance;
    }

    public void login(String email, String password, Callback callback) {
        dataSource.login(email, password, callback);
    }

    public void logout() {
        dataSource.logout();
    }

    public User getCurrentUser() {
        return dataSource.getCurrentUser();
    }

    public void saveUser(User user) {
        dataSource.saveUser(user);
    }

    public boolean isUserLoggedIn() {
        return dataSource.isUserLoggedIn();
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