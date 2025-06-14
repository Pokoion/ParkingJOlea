package com.lksnext.parkingplantilla.data.local;

import com.lksnext.parkingplantilla.data.repository.DataSource;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;
import com.lksnext.parkingplantilla.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
        // Plazas Standard (filas A-D, 10 plazas por fila)
        for (char fila = 'A'; fila <= 'D'; fila++) {
            for (int numero = 1; numero <= 10; numero++) {
                String id = fila + "-" + numero;
                plazas.add(new Plaza(id, Plaza.TIPO_STANDARD));
            }
        }

        // Plazas para motos (fila E, 10 plazas)
        for (int numero = 1; numero <= 10; numero++) {
            String id = "E-" + numero;
            plazas.add(new Plaza(id, Plaza.TIPO_MOTORCYCLE));
        }

        // Plazas para discapacitados (fila F, 5 plazas)
        for (int numero = 1; numero <= 5; numero++) {
            String id = "F-" + numero;
            plazas.add(new Plaza(id, Plaza.TIPO_DISABLED));
        }

        // Plazas con cargador eléctrico (fila G, 10 plazas)
        for (int numero = 1; numero <= 10; numero++) {
            String id = "G-" + numero;
            plazas.add(new Plaza(id, Plaza.TIPO_CV_CHARGER));
        }
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
    }

    private void initReservations() {
        String today = DateUtils.getCurrentDateForApi();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String tomorrow = DateUtils.formatDateForApi(calendar);

        long currentTimeMs = DateUtils.getCurrentTimeMs();

        Hora horaReservaActual = new Hora(currentTimeMs, currentTimeMs + 3600000);

        Hora horaReservaMañana = new Hora(8 * 3600000, 9 * 3600000);

        // Create sample reservations for the admin user
        List<Reserva> adminReservas = new ArrayList<>();

        Reserva r1 = new Reserva(today, ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(0), horaReservaActual);
        adminReservas.add(r1);
        todasReservas.add(r1);

        Reserva r2 = new Reserva(tomorrow, ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(1), horaReservaMañana);
        adminReservas.add(r2);
        todasReservas.add(r2);

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = DateUtils.formatDateForApi(calendar);

        Reserva r3 = new Reserva(yesterday, ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(2), horas.get(0));
        adminReservas.add(r3);
        todasReservas.add(r3);

        Reserva r4 = new Reserva("2023-06-16", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(3), horas.get(2));
        adminReservas.add(r4);
        todasReservas.add(r4);

        reservasPorUsuario.put(ADMINEMAIL, adminReservas);

        // Create sample reservations for a regular user
        List<Reserva> userReservas = new ArrayList<>();

        Reserva r8 = new Reserva(today, USEREMAIL,
                UUID.randomUUID().toString(), plazas.get(5), horaReservaActual);
        userReservas.add(r8);
        todasReservas.add(r8);

        Reserva r9 = new Reserva(tomorrow, USEREMAIL,
                UUID.randomUUID().toString(), plazas.get(6), horaReservaMañana);
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
    public void getHistoricReservations(String userId, DataCallback<List<Reserva>> callback) {
        try {
            List<Reserva> userReservations = reservasPorUsuario.get(userId);
            List<Reserva> historicReservations = new ArrayList<>();

            if (userReservations != null && !userReservations.isEmpty()) {
                for (Reserva reserva : userReservations) {
                    if (DateUtils.isHistoricReservation(reserva)) {
                        historicReservations.add(reserva);
                    }
                }
            }

            callback.onSuccess(historicReservations);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void getCurrentReservation(String userId, DataCallback<Reserva> callback) {
        List<Reserva> userReservations = reservasPorUsuario.get(userId);
        if (userReservations == null || userReservations.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        Reserva currentReservation = null;

        for (Reserva reserva : userReservations) {
            if (DateUtils.isOngoingReservation(reserva)) {
                currentReservation = reserva;
                break;
            }
        }

        callback.onSuccess(currentReservation);
    }

    @Override
    public void getNextReservation(String userId, DataCallback<Reserva> callback) {
        List<Reserva> userReservations = reservasPorUsuario.get(userId);
        if (userReservations == null || userReservations.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        Date now = new Date();
        Reserva nextReservation = null;
        Date nextDate = null;

        for (Reserva reserva : userReservations) {
            Date reservaDateTime = DateUtils.getReservaDateTime(reserva);

            if (reservaDateTime.after(now)) {
                if (nextReservation == null || reservaDateTime.before(nextDate)) {
                    nextReservation = reserva;
                    nextDate = reservaDateTime;
                }
            }
        }

        callback.onSuccess(nextReservation);
    }

    @Override
    public void deleteReservation(String reservationId, DataCallback<Boolean> callback) {
        try {
            Reserva reservaToDelete = null;
            for (Reserva reserva : todasReservas) {
                if (reserva.getId().equals(reservationId)) {
                    reservaToDelete = reserva;
                    break;
                }
            }

            if (reservaToDelete == null) {
                callback.onSuccess(false);
                return;
            }

            // Cambia el estado a CANCELADA en vez de eliminar
            reservaToDelete.setEstado(Reserva.Estado.CANCELADA);

            String userId = reservaToDelete.getUsuario();
            List<Reserva> userReservations = reservasPorUsuario.get(userId);
            if (userReservations != null) {
                for (Reserva r : userReservations) {
                    if (r.getId().equals(reservaToDelete.getId())) {
                        r.setEstado(Reserva.Estado.CANCELADA);
                        break;
                    }
                }
            }

            callback.onSuccess(true);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    private void actualizarEstadosFinalizadas() {
        long ahora = System.currentTimeMillis();
        for (Reserva r : todasReservas) {
            if (r.getEstado() == Reserva.Estado.ACTIVA && r.getHora() != null) {
                long fin = r.getHora().getHoraFin();
                if (fin < ahora) {
                    r.setEstado(Reserva.Estado.FINALIZADA);
                }
            }
        }
        for (List<Reserva> lista : reservasPorUsuario.values()) {
            for (Reserva r : lista) {
                if (r.getEstado() == Reserva.Estado.ACTIVA && r.getHora() != null) {
                    long fin = r.getHora().getHoraFin();
                    if (fin < ahora) {
                        r.setEstado(Reserva.Estado.FINALIZADA);
                    }
                }
            }
        }
    }

    @Override
    public void updateReservation(Reserva reserva, DataCallback<Boolean> callback) {
        actualizarEstadosFinalizadas();
        // Solo permitir actualizar si no hay solapamiento con reservas ACTIVAS (excepto la propia)
        for (Reserva r : todasReservas) {
            if (!r.getId().equals(reserva.getId()) && r.getPlaza() != null && r.getPlaza().getId().equals(reserva.getPlaza().getId()) && r.getFecha().equals(reserva.getFecha())
                && r.getEstado() == Reserva.Estado.ACTIVA) {
                long ini = r.getHora().getHoraInicio();
                long fin = r.getHora().getHoraFin();
                long nIni = reserva.getHora().getHoraInicio();
                long nFin = reserva.getHora().getHoraFin();
                if (!(nFin <= ini || nIni >= fin)) {
                    callback.onFailure(new Exception("Plaza ocupada en ese horario"));
                    return;
                }
            }
        }
        Reserva reservaToUpdate = null;
        for (Reserva r : todasReservas) {
            if (r.getId().equals(reserva.getId())) {
                reservaToUpdate = r;
                break;
            }
        }
        if (reservaToUpdate == null) {
            callback.onFailure(new Exception("Reserva no encontrada"));
            return;
        }
        reservaToUpdate.setFecha(reserva.getFecha());
        reservaToUpdate.setHora(reserva.getHora());
        reservaToUpdate.setPlaza(reserva.getPlaza());
        reservaToUpdate.setEstado(Reserva.Estado.ACTIVA);
        callback.onSuccess(true);
    }

    @Override
    public void createReservation(Reserva reserva, DataCallback<Boolean> callback) {
        actualizarEstadosFinalizadas();
        // Solo permitir crear si no hay solapamiento con reservas ACTIVAS
        if (reserva.getPlaza() != null && reserva.getPlaza().getId() != null) {
            for (Reserva r : todasReservas) {
                if (r.getPlaza() != null && r.getPlaza().getId().equals(reserva.getPlaza().getId()) && r.getFecha().equals(reserva.getFecha())
                    && r.getEstado() == Reserva.Estado.ACTIVA) {
                    long ini = r.getHora().getHoraInicio();
                    long fin = r.getHora().getHoraFin();
                    long nIni = reserva.getHora().getHoraInicio();
                    long nFin = reserva.getHora().getHoraFin();
                    if (!(nFin <= ini || nIni >= fin)) {
                        callback.onFailure(new Exception("Plaza ocupada en ese horario"));
                        return;
                    }
                }
            }
        }
        if (reserva.getId() == null || reserva.getId().isEmpty()) {
            reserva.setId(UUID.randomUUID().toString());
        }
        reserva.setEstado(Reserva.Estado.ACTIVA);
        todasReservas.add(reserva);
        String userId = reserva.getUsuario();
        List<Reserva> userReservations = reservasPorUsuario.get(userId);
        if (userReservations == null) {
            userReservations = new ArrayList<>();
            reservasPorUsuario.put(userId, userReservations);
        }
        userReservations.add(reserva);
        callback.onSuccess(true);
    }

    private Map<String, List<Reserva>> getReservasPorPlaza(String fecha, long horaInicio, long horaFin, String tipo) {
        Map<String, List<Reserva>> mapa = new HashMap<>();
        for (Plaza plaza : plazas) {
            if (tipo == null || plaza.getTipo().equals(tipo)) {
                mapa.put(plaza.getId(), new ArrayList<>());
            }
        }
        for (Reserva r : todasReservas) {
            if (r.getFecha().equals(fecha)) {
                long ini = r.getHora().getHoraInicio();
                long fin = r.getHora().getHoraFin();
                if (!(horaFin <= ini || horaInicio >= fin)) {
                    String plazaId = r.getPlaza().getId();
                    if (mapa.containsKey(plazaId)) {
                        mapa.get(plazaId).add(r);
                    }
                }
            }
        }
        return mapa;
    }

    @Override
    public void getAvailablePlazas(String tipo, String fecha, long horaInicio, long horaFin, DataCallback<List<String>> callback) {
        actualizarEstadosFinalizadas();
        Map<String, List<Reserva>> mapa = getReservasPorPlaza(fecha, horaInicio, horaFin, tipo);
        List<String> disponibles = new ArrayList<>();
        for (Map.Entry<String, List<Reserva>> entry : mapa.entrySet()) {
            boolean libre = true;
            for (Reserva r : entry.getValue()) {
                if (r.getEstado() == Reserva.Estado.ACTIVA) {
                    libre = false;
                    break;
                }
            }
            if (libre) {
                disponibles.add(entry.getKey());
            }
        }
        callback.onSuccess(disponibles);
    }

    @Override
    public void assignRandomPlaza(String tipo, String fecha, long horaInicio, long horaFin, DataCallback<String> callback) {
        getAvailablePlazas(tipo, fecha, horaInicio, horaFin, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> disponibles) {
                if (disponibles.isEmpty()) {
                    callback.onSuccess(null);
                } else {
                    int idx = (int) (Math.random() * disponibles.size());
                    callback.onSuccess(disponibles.get(idx));
                }
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void checkAvailability(Reserva reserva, DataCallback<Boolean> callback) {
        actualizarEstadosFinalizadas();
        String fecha = reserva.getFecha();
        long horaInicio = reserva.getHora().getHoraInicio();
        long horaFin = reserva.getHora().getHoraFin();
        String plazaId = reserva.getPlaza().getId();
        Map<String, List<Reserva>> mapa = getReservasPorPlaza(fecha, horaInicio, horaFin, null);
        boolean isAvailable = true;
        if (mapa.containsKey(plazaId)) {
            for (Reserva r : mapa.get(plazaId)) {
                if (r.getEstado() == Reserva.Estado.ACTIVA) {
                    isAvailable = false;
                    break;
                }
            }
        }
        callback.onSuccess(isAvailable);
    }

    @Override
    public void hasReservationOnDate(String userId, String date, DataCallback<Boolean> callback) {
        try {
            List<Reserva> userReservations = reservasPorUsuario.get(userId);
            if (userReservations == null || userReservations.isEmpty()) {
                callback.onSuccess(false);
                return;
            }

            boolean hasReservation = false;
            for (Reserva reserva : userReservations) {
                if (reserva.getFecha().equals(date)) {
                    hasReservation = true;
                    break;
                }
            }

            callback.onSuccess(hasReservation);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void getAvailableNumbers(String tipo, String row, String fecha, long horaInicio, long horaFin, DataCallback<List<String>> callback) {
        actualizarEstadosFinalizadas();
        List<String> disponibles = new ArrayList<>();
        int maxNumber = 10;
        if (tipo.equals(Plaza.TIPO_MOTORCYCLE) && row.equals("E")) maxNumber = 8;
        if (tipo.equals(Plaza.TIPO_DISABLED) && row.equals("F")) maxNumber = 5;
        if (tipo.equals(Plaza.TIPO_CV_CHARGER) && row.equals("G")) maxNumber = 6;
        for (int i = 1; i <= maxNumber; i++) {
            String plazaId = row + "-" + i;
            boolean reservada = false;
            for (Reserva r : todasReservas) {
                if (r.getPlaza() != null && plazaId.equals(r.getPlaza().getId()) && r.getFecha().equals(fecha)
                    && r.getEstado() == Reserva.Estado.ACTIVA) {
                    long ini = r.getHora().getHoraInicio();
                    long fin = r.getHora().getHoraFin();
                    if (!(horaFin <= ini || horaInicio >= fin)) {
                        reservada = true;
                        break;
                    }
                }
            }
            if (!reservada) disponibles.add(String.valueOf(i));
        }
        callback.onSuccess(disponibles);
    }
}
