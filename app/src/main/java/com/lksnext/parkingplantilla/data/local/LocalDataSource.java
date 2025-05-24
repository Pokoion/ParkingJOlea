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
    }

    private void initReservations() {
        // Obtener fecha y hora actual para hacer pruebas dinámicas
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // Formatear la fecha actual para usarla en las reservas
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(now);

        // Calcular fecha de mañana
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String tomorrow = dateFormat.format(calendar.getTime());

        // Calcular hora actual en milisegundos desde medianoche
        calendar.setTime(now);
        int horaActual = calendar.get(Calendar.HOUR_OF_DAY);
        int minutoActual = calendar.get(Calendar.MINUTE);
        long currentTimeMs = horaActual * 3600000L + minutoActual * 60000L;

        // Crear hora para reserva actual (desde ahora hasta 1 hora después)
        Hora horaReservaActual = new Hora(currentTimeMs, currentTimeMs + 3600000);

        // Crear hora para reserva de mañana (8:00 - 9:00)
        Hora horaReservaMañana = new Hora(8 * 3600000, 9 * 3600000);

        // Admin reservations
        List<Reserva> adminReservas = new ArrayList<>();

        // Reserva ACTUAL del admin (en curso ahora mismo)
        Reserva r1 = new Reserva(today, ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(0), horaReservaActual);
        adminReservas.add(r1);
        todasReservas.add(r1);

        // Reserva PRÓXIMA del admin (para mañana)
        Reserva r2 = new Reserva(tomorrow, ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(1), horaReservaMañana);
        adminReservas.add(r2);
        todasReservas.add(r2);

        // Algunas reservas históricas
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = dateFormat.format(calendar.getTime());

        Reserva r3 = new Reserva(yesterday, ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(2), horas.get(0));
        adminReservas.add(r3);
        todasReservas.add(r3);

        Reserva r4 = new Reserva("2023-06-16", ADMINEMAIL,
                UUID.randomUUID().toString(), plazas.get(3), horas.get(2));
        adminReservas.add(r4);
        todasReservas.add(r4);

        reservasPorUsuario.put(ADMINEMAIL, adminReservas);

        // User reservations
        List<Reserva> userReservas = new ArrayList<>();

        // Reserva actual para usuario normal
        Reserva r8 = new Reserva(today, USEREMAIL,
                UUID.randomUUID().toString(), plazas.get(5), horaReservaActual);
        userReservas.add(r8);
        todasReservas.add(r8);

        // Reserva próxima para usuario normal
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
            if (userReservations != null) {
                List<Reserva> historicReservations = new ArrayList<>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String currentDateStr = dateFormat.format(new Date());

                Calendar calendar = Calendar.getInstance();
                int horaActual = calendar.get(Calendar.HOUR_OF_DAY);
                int minutoActual = calendar.get(Calendar.MINUTE);
                long currentTimeMs = horaActual * 3600000L + minutoActual * 60000L;

                for (Reserva reserva : userReservations) {
                    if (reserva.getFecha().compareTo(currentDateStr) < 0) {
                        historicReservations.add(reserva);
                    }
                    else if (reserva.getFecha().equals(currentDateStr) &&
                            reserva.getHora().getHoraFin() < currentTimeMs) {
                        historicReservations.add(reserva);
                    }
                }

                callback.onSuccess(historicReservations);
            } else {
                callback.onSuccess(new ArrayList<>());
            }
        } catch (Exception e) {
            System.out.println("Error al obtener reservas históricas: " + e.getMessage());
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

        // Obtener la fecha y hora actual
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        // Obtener fecha actual en formato "yyyy-MM-dd"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(now);

        // Obtener hora actual en milisegundos desde medianoche
        int horaActual = cal.get(Calendar.HOUR_OF_DAY);
        int minutoActual = cal.get(Calendar.MINUTE);
        long currentTimeMs = horaActual * 3600000L + minutoActual * 60000L;

        // Inicializar reserva actual
        Reserva currentReservation = null;

        // Recorrer todas las reservas del usuario
        for (Reserva reserva : userReservations) {
            // Verificar si es del día actual
            if (reserva.getFecha().equals(currentDate)) {
                // Verificar si la hora actual está dentro del rango de la reserva
                if (currentTimeMs >= reserva.getHora().getHoraInicio() &&
                        currentTimeMs <= reserva.getHora().getHoraFin()) {
                    currentReservation = reserva;
                    break;
                }
            }
        }

        // Imprimir para depuración
        System.out.println("Reserva actual encontrada: " + (currentReservation != null ?
                currentReservation.getId() : "ninguna"));

        callback.onSuccess(currentReservation);
    }

    @Override
    public void getNextReservation(String userId, DataCallback<Reserva> callback) {
        List<Reserva> userReservations = reservasPorUsuario.get(userId);
        if (userReservations == null || userReservations.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        // Obtener la fecha y hora actual
        Date now = new Date();
        Reserva nextReservation = null;
        Date nextDate = null;

        for (Reserva reserva : userReservations) {
            // Convertir fecha y hora de la reserva a Date para comparación
            Date reservaDateTime = DateUtils.getReservaDateTime(reserva);

            // Comprobar si es una reserva futura
            if (reservaDateTime.after(now)) {
                // Si no hay próxima reserva o esta es más cercana
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

            todasReservas.remove(reservaToDelete);

            String userId = reservaToDelete.getUsuario();
            List<Reserva> userReservations = reservasPorUsuario.get(userId);
            if (userReservations != null) {
                userReservations.remove(reservaToDelete);
            }

            callback.onSuccess(true);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void updateReservation(String reservationId, String date, long startTime, long endTime,
                                  String reservationType, String plazaId, DataCallback<Boolean> callback) {
        try {
            // Buscar la reserva a actualizar
            Reserva reservaToUpdate = null;
            for (Reserva reserva : todasReservas) {
                if (reserva.getId().equals(reservationId)) {
                    reservaToUpdate = reserva;
                    break;
                }
            }

            if (reservaToUpdate == null) {
                callback.onFailure(new Exception("Reserva no encontrada"));
                return;
            }

            // Actualizar fecha
            reservaToUpdate.setFecha(date);

            // Crear y actualizar el objeto Hora
            Hora nuevaHora = new Hora(startTime, endTime);
            reservaToUpdate.setHora(nuevaHora);

            // Si se especificó una plaza, actualizarla
            if (plazaId != null && !plazaId.isEmpty()) {
                Plaza plazaActualizada = null;

                // Buscar por ID de plaza específico
                for (Plaza plaza : plazas) {
                    if (plaza.getId().equals(plazaId)) {
                        plazaActualizada = plaza;
                        break;
                    }
                }

                if (plazaActualizada != null) {
                    reservaToUpdate.setPlaza(plazaActualizada);
                } else {
                    callback.onFailure(new Exception("Plaza no encontrada"));
                    return;
                }
            } else if (reservationType != null) {
                // Si solo se especificó el tipo pero no una plaza específica
                // buscar una plaza disponible del tipo especificado
                for (Plaza plaza : plazas) {
                    if (plaza.getTipo().equals(reservationType)) {
                        reservaToUpdate.setPlaza(plaza);
                        break;
                    }
                }
            }

            callback.onSuccess(true);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void createReservation(Reserva reserva, DataCallback<Boolean> callback) {
        try {
            // Verificar si la plaza existe, si se especificó una
            if (reserva.getPlaza() != null && reserva.getPlaza().getId() != null) {
                boolean plazaExists = false;
                for (Plaza plaza : plazas) {
                    if (plaza.getId().equals(reserva.getPlaza().getId())) {
                        plazaExists = true;
                        break;
                    }
                }
                if (!plazaExists) {
                    callback.onFailure(new Exception("Plaza no encontrada"));
                    return;
                }
            } else {
                // Buscar una plaza disponible del tipo solicitado
                Plaza plazaDisponible = null;
                for (Plaza plaza : plazas) {
                    if (plaza.getTipo().equals(reserva.getPlaza().getTipo())) {
                        plazaDisponible = plaza;
                        break;
                    }
                }

                if (plazaDisponible == null) {
                    callback.onFailure(new Exception("No hay plazas disponibles del tipo solicitado"));
                    return;
                }

                reserva.setPlaza(plazaDisponible);
            }

            // Asignar ID si no tiene uno
            if (reserva.getId() == null || reserva.getId().isEmpty()) {
                reserva.setId(UUID.randomUUID().toString());
            }

            // Agregar la reserva a las colecciones correspondientes
            todasReservas.add(reserva);

            // Agregar a las reservas del usuario
            String userId = reserva.getUsuario();
            List<Reserva> userReservations = reservasPorUsuario.get(userId);
            if (userReservations == null) {
                userReservations = new ArrayList<>();
                reservasPorUsuario.put(userId, userReservations);
            }
            userReservations.add(reserva);

            callback.onSuccess(true);
        } catch (Exception e) {
            System.out.println("Error al crear reserva: " + e.getMessage());
            callback.onFailure(e);
        }
    }

    @Override
    public void checkAvailability(String date, long startTimeMs, long endTimeMs,
                                  String type, String plazaId, DataCallback<Boolean> callback) {
        try {
            // Verificar si hay alguna reserva que se solapa con la fecha, hora y plaza solicitada
            boolean isAvailable = true;

            for (Reserva reserva : todasReservas) {
                // Verificar si es la misma fecha
                if (reserva.getFecha().equals(date)) {
                    // Verificar si hay solapamiento de horarios
                    long reservaStart = reserva.getHora().getHoraInicio();
                    long reservaEnd = reserva.getHora().getHoraFin();

                    boolean timeOverlap = (startTimeMs < reservaEnd && endTimeMs > reservaStart);

                    // Si es la misma plaza o no se especificó plaza (verificando solo tipo)
                    boolean sameSpot = false;
                    if (plazaId != null && !plazaId.isEmpty()) {
                        sameSpot = reserva.getPlaza().getId().equals(plazaId);
                    } else {
                        // Si no se especificó plaza, verificar por tipo
                        sameSpot = reserva.getPlaza().getTipo().equals(type);
                    }

                    // Si hay solapamiento de tiempo y es la misma plaza, no está disponible
                    if (timeOverlap && sameSpot) {
                        isAvailable = false;
                        break;
                    }
                }
            }

            callback.onSuccess(isAvailable);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }
}