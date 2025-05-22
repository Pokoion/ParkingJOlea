package com.lksnext.parkingplantilla.data.repository;

import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;

import java.util.List;

public interface DataSource {

    void getReservations(String userId, DataCallback<List<Reserva>> callback);
    void getHistoricReservations(String userId, DataCallback<List<Reserva>> callback);
    void getAllReservations(DataCallback<List<Reserva>> callback);
    void createReservation(Reserva reserva, Callback callback);
    void cancelReservation(String reservationId, Callback callback);

    void login(String email, String password, DataCallback<User> callback);
    void register(String name, String email, String password, DataCallback<User> callback);
}