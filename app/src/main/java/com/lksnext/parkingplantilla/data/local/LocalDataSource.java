package com.lksnext.parkingplantilla.data.local;

import com.lksnext.parkingplantilla.data.repository.DataSource;
import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LocalDataSource implements DataSource {
    public static final String ADMINEMAIL = "admin@example.com";
    public static final String USEREMAIL = "user@example.com";
    private final Map<String, User> fakeDatabase;
    private final List<Plaza> plazas;
    private final List<Hora> horas;
    private final Map<String, List<Reserva>> reservasPorUsuario;
    private final List<Reserva> todasReservas;

    public LocalDataSource() {
        // Initialize fake users
        this.fakeDatabase = new HashMap<>();
        fakeDatabase.put(ADMINEMAIL, new User("Admin", ADMINEMAIL, "admin123"));
        fakeDatabase.put(USEREMAIL, new User("Regular User", USEREMAIL, "password123"));
        fakeDatabase.put("test@example.com", new User("Test User", "test@example.com", "test123"));

        // Initialize collections
        this.plazas = new ArrayList<>();
        this.horas = new ArrayList<>();
        this.reservasPorUsuario = new HashMap<>();
        this.todasReservas = new ArrayList<>();

        // Create parking spots
        initPlazas();

        // Create time slots
        initHoras();

        // Create sample reservations
        initReservations();
    }

    private void initPlazas() {
        plazas.add(new Plaza("A-1", Plaza.TIPO_STANDARD));
        plazas.add(new Plaza("A-2", Plaza.TIPO_STANDARD));
        plazas.add(new Plaza("A-3", Plaza.TIPO_STANDARD));
        plazas.add(new Plaza("B-1", Plaza.TIPO_MOTORCYCLE));
        plazas.add(new Plaza("B-2", Plaza.TIPO_MOTORCYCLE));
        plazas.add(new Plaza("C-1", Plaza.TIPO_CV_CHARGER));
        plazas.add(new Plaza("C-2", Plaza.TIPO_CV_CHARGER));
        plazas.add(new Plaza("D-1", Plaza.TIPO_DISABLED));
        plazas.add(new Plaza("D-2", Plaza.TIPO_DISABLED));
        plazas.add(new Plaza("E-1", Plaza.TIPO_STANDARD));
    }

    private void initHoras() {
        long oneHour = 3600000; // 1 hour in milliseconds
        long eightAM = 8 * oneHour;

        horas.add(new Hora(eightAM, eightAM + oneHour));              // 8:00 - 9:00
        horas.add(new Hora(eightAM + oneHour, eightAM + 2*oneHour));  // 9:00 - 10:00
        horas.add(new Hora(eightAM + 2*oneHour, eightAM + 3*oneHour)); // 10:00 - 11:00
        horas.add(new Hora(eightAM + 3*oneHour, eightAM + 4*oneHour)); // 11:00 - 12:00
        horas.add(new Hora(eightAM + 4*oneHour, eightAM + 5*oneHour)); // 12:00 - 13:00
        horas.add(new Hora(eightAM + 5*oneHour, eightAM + 6*oneHour)); // 13:00 - 14:00
        horas.add(new Hora(eightAM + 6*oneHour, eightAM + 7*oneHour)); // 14:00 - 15:00
        horas.add(new Hora(eightAM + 7*oneHour, eightAM + 8*oneHour)); // 15:00 - 16:00
    }

    private void initReservations() {
        // Admin reservations
        List<Reserva> adminReservas = new ArrayList<>();

        Reserva r1 = new Reserva("2023-06-15", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(0), horas.get(0));
        adminReservas.add(r1);
        todasReservas.add(r1);

        Reserva r2 = new Reserva("2023-06-16", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(1), horas.get(2));
        adminReservas.add(r2);
        todasReservas.add(r2);

        Reserva r3 = new Reserva("2023-06-17", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(2), horas.get(1));
        adminReservas.add(r3);
        todasReservas.add(r3);

        Reserva r4 = new Reserva("2023-06-18", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(4), horas.get(3));
        adminReservas.add(r4);
        todasReservas.add(r4);

        Reserva r5 = new Reserva("2023-06-19", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(6), horas.get(4));
        adminReservas.add(r5);
        todasReservas.add(r5);

        Reserva r6 = new Reserva("2023-06-20", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(8), horas.get(5));
        adminReservas.add(r6);
        todasReservas.add(r6);

        Reserva r7 = new Reserva("2023-06-21", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(9), horas.get(6));
        adminReservas.add(r7);
        todasReservas.add(r7);

        reservasPorUsuario.put(ADMINEMAIL, adminReservas);

        // User reservations
        List<Reserva> userReservas = new ArrayList<>();

        Reserva r8 = new Reserva("2023-06-15", USEREMAIL,
                UUID.randomUUID().toString(), plazas.get(3), horas.get(1));
        userReservas.add(r8);
        todasReservas.add(r8);

        Reserva r9 = new Reserva("2023-06-17", USEREMAIL,
                UUID.randomUUID().toString(), plazas.get(5), horas.get(4));
        userReservas.add(r9);
        todasReservas.add(r9);

        reservasPorUsuario.put(USEREMAIL, userReservas);
    }

    @Override
    public void login(String email, String password, DataCallback<User> callback) {
        User storedUser = fakeDatabase.get(email);

        if (storedUser != null && storedUser.getPassword().equals(password)) {
            callback.onSuccess(storedUser); // Return the user object
        } else {
            callback.onFailure(new Exception("Invalid credentials"));
        }
    }

    @Override
    public void register(String name, String email, String password, DataCallback<User> callback) {
        if (fakeDatabase.containsKey(email)) {
            callback.onFailure(new Exception("User already exists"));
        } else {
            User newUser = new User(name, email, password);
            fakeDatabase.put(email, newUser);
            callback.onSuccess(newUser);
        }
    }

    @Override
    public void getReservations(String userId, DataCallback<List<Reserva>> callback) {
        List<Reserva> userReservations = reservasPorUsuario.get(userId);
        if (userReservations != null) {
            callback.onSuccess(new ArrayList<>(userReservations));
        } else {
            callback.onSuccess(new ArrayList<>());
        }
    }

    @Override
    public void createReservation(Reserva reserva, Callback callback) {
        // Implementation for creating a reservation
    }

    @Override
    public void cancelReservation(String reservationId, Callback callback) {
        // Implementation for cancelling a reservation
    }

    @Override
    public void getAllReservations(DataCallback<List<Reserva>> callback) {
        // Implementation for retrieving all reservations
    }
}