package com.lksnext.parkingplantilla.data.repository;

import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;

import java.util.List;

public interface DataSource {
    // Reservation-related methods
    void getReservations(String userId, DataCallback<List<Reserva>> callback);
    void getHistoricReservations(String userId, DataCallback<List<Reserva>> callback);
    void getCurrentReservation(String userId, DataCallback<Reserva> callback);
    void getNextReservation(String userId, DataCallback<Reserva> callback);
    void createReservation(Reserva reserva, DataCallback<Boolean> callback);
    void deleteReservation(String reservationId, DataCallback<Boolean> callback);
    void updateReservation(Reserva reserva, DataCallback<Boolean> callback);
    void checkAvailability(Reserva reserva, DataCallback<Boolean> callback);

    // User-related methods
    void login(String email, String password, DataCallback<User> callback);
    void register(String name, String email, String password, DataCallback<User> callback);
    void hasReservationOnDate(String userId, String date, DataCallback<Boolean> callback);
}