package com.lksnext.parkingplantilla.data.repository;

import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Plaza;
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
    void checkAvailability(Reserva reserva, String excludeReservationId, DataCallback<Boolean> callback);

    // User-related methods
    void login(String email, String password, DataCallback<User> callback);
    void register(String name, String email, String password, DataCallback<User> callback);
    void hasReservationOnDate(String userId, String date, DataCallback<Boolean> callback);
    void deleteUser(String email, DataCallback<Boolean> callback);
    void deleteUserReservations(String email, DataCallback<Boolean> callback);

    // Plaza-related methods
    void getAvailablePlazas(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<List<String>> callback);
    void assignRandomPlaza(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<String> callback);
    void addPlaza(Plaza plaza, DataCallback<Boolean> callback);
    void deletePlaza(String plazaId, DataCallback<Boolean> callback);
    void deleteReserva(String reservaId, DataCallback<Boolean> callback);

    // Números disponibles en una fila concreta
    void getAvailableNumbers(String tipo, String row, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<List<String>> callback);
    void checkUserExists(String email, DataCallback<Boolean> callback);
    void sendPasswordResetEmail(String email, DataCallback<Boolean> callback);
    void getAvailableRows(String tipo, DataCallback<List<String>> callback);
}
