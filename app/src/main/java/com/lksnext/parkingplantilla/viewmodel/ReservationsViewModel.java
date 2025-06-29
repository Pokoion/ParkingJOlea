package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.utils.DateUtils;
import com.lksnext.parkingplantilla.notifications.NotificationScheduler;
import com.lksnext.parkingplantilla.data.UserPreferencesManager;
import com.lksnext.parkingplantilla.utils.Validators;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Calendar;

public class ReservationsViewModel extends ViewModel {

    private final DataRepository repository;
    private final MutableLiveData<List<Reserva>> reservations = new MutableLiveData<>();
    private final MutableLiveData<List<Reserva>> historicReservations = new MutableLiveData<>();
    private final MutableLiveData<Reserva> currentReservation = new MutableLiveData<>();
    private final MutableLiveData<Reserva> nextReservation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> randomPlaza = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableNumbers = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableRows = new MutableLiveData<>();

    // Variables para guardar selección temporal
    private String tempSelectedType;
    private Calendar tempSelectedDate;
    private Calendar tempStartTime;
    private Calendar tempEndTime;
    private String tempReservationId;

    public ReservationsViewModel() {
        repository = ParkingApplication.getRepository();
    }

    public ReservationsViewModel(DataRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Reserva>> getReservations() {
        return reservations;
    }

    public LiveData<List<Reserva>> getHistoricReservations() {
        return historicReservations;
    }

    public LiveData<Reserva> getCurrentReservation() {
        return currentReservation;
    }

    public LiveData<Reserva> getNextReservation() {
        return nextReservation;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getRandomPlaza() {
        return randomPlaza;
    }

    public void clearRandomPlaza() {
        randomPlaza.setValue(null);
    }

    public LiveData<List<String>> getAvailableNumbers() {
        return availableNumbers;
    }

    public LiveData<List<String>> getAvailableRows() {
        return availableRows;
    }

    public void saveTempSelection(String type, Calendar date, Calendar start, Calendar end, String reservationId) {
        this.tempSelectedType = type;
        this.tempSelectedDate = (date != null) ? (Calendar) date.clone() : null;
        this.tempStartTime = (start != null) ? (Calendar) start.clone() : null;
        this.tempEndTime = (end != null) ? (Calendar) end.clone() : null;
        this.tempReservationId = reservationId;
    }

    public String getTempSelectedType() {
        return tempSelectedType;
    }

    public Calendar getTempSelectedDate() {
        return tempSelectedDate;
    }

    public Calendar getTempStartTime() {
        return tempStartTime;
    }

    public Calendar getTempEndTime() {
        return tempEndTime;
    }

    public String getTempReservationId() {
        return tempReservationId;
    }

    public void clearTempSelection() {
        tempSelectedType = null;
        tempSelectedDate = null;
        tempStartTime = null;
        tempEndTime = null;
        tempReservationId = null;
    }

    public void clearError() {
        error.setValue("");
    }

    /**
     * Carga la reserva actual del usuario (solo la que está en curso)
     */
    public void loadCurrentReservation() {
        User currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            isLoading.setValue(true);
            String userId = currentUser.getEmail();
            repository.getReservations(userId, new DataCallback<List<Reserva>>() {
                @Override
                public void onSuccess(List<Reserva> result) {
                    Reserva current = null;
                    for (Reserva r : result) {
                        if (DateUtils.isOngoingReservation(r)) {
                            current = r;
                            break;
                        }
                    }
                    currentReservation.setValue(current);
                    isLoading.setValue(false);
                }
                @Override
                public void onFailure(Exception e) {
                    error.setValue(e.getMessage());
                    isLoading.setValue(false);
                    currentReservation.setValue(null);
                }
            });
        } else {
            error.setValue("Usuario no conectado");
            currentReservation.setValue(null);
        }
    }

    public void loadNextReservation() {
        User currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            isLoading.setValue(true);
            String userId = currentUser.getEmail();
            repository.getReservations(userId, new DataCallback<List<Reserva>>() {
                @Override
                public void onSuccess(List<Reserva> result) {
                    Reserva next = null;
                    for (Reserva r : result) {
                        if (DateUtils.isFutureReservation(r)) {
                            if (next == null || DateUtils.getReservaDateTime(r).before(DateUtils.getReservaDateTime(next))) {
                                next = r;
                            }
                        }
                    }
                    nextReservation.setValue(next);
                    isLoading.setValue(false);
                }
                @Override
                public void onFailure(Exception e) {
                    error.setValue(e.getMessage());
                    isLoading.setValue(false);
                    nextReservation.setValue(null);
                }
            });
        } else {
            error.setValue("Usuario no conectado");
            nextReservation.setValue(null);
        }
    }

    /**
     * Carga tanto la reserva actual como la próxima
     */
    public void loadCurrentAndNextReservations() {
        loadCurrentReservation();
        loadNextReservation();
    }

    /**
     * Carga las reservas históricas del usuario
     */
    public void loadHistoricReservations() {
        User currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            isLoading.setValue(true);
            String userId = currentUser.getEmail();
            repository.getHistoricReservations(userId, new DataCallback<List<Reserva>>() {
                @Override
                public void onSuccess(List<Reserva> result) {
                    Log.d("ReservationsViewModel", "Historic reservations loaded: " + result);
                    if (result == null) {
                        historicReservations.setValue(new ArrayList<>());
                    } else {
                        historicReservations.setValue(result);
                    }
                    isLoading.setValue(false);
                }
                @Override
                public void onFailure(Exception e) {
                    error.setValue(e.getMessage());
                    isLoading.setValue(false);
                    historicReservations.setValue(new ArrayList<>());
                }
            });
        } else {
            error.setValue("Usuario no conectado");
            historicReservations.setValue(new ArrayList<>());
        }
    }

    /**
     * Carga todas las reservas del usuario actual
     */
    public void loadUserReservations() {
        User currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getEmail();
            loadUserReservations(userId);
        } else {
            error.setValue("Usuario no conectado");
            reservations.setValue(new ArrayList<>());
        }
    }

    /**
     * Carga todas las reservas de un usuario específico
     */
    public void loadUserReservations(String userId) {
        isLoading.setValue(true);
        repository.getReservations(userId, new DataCallback<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> result) {
                if (result == null) {
                    reservations.setValue(new ArrayList<>());
                } else {
                    reservations.setValue(result);
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                error.setValue(e.getMessage());
                isLoading.setValue(false);
                reservations.setValue(new ArrayList<>());
            }
        });
    }

    private void scheduleReservationNotifications(Reserva reserva) {
        Context context = ParkingApplication.getAppContext();
        UserPreferencesManager prefs = new UserPreferencesManager(context);
        long now = System.currentTimeMillis();
        long startReminderTime = DateUtils.getStartReminderTime(reserva);
        long endReminderTime = DateUtils.getEndReminderTime(reserva);
        String reservaId = reserva.getId();
        // Notificación de inicio (30 min antes)
        if (prefs.isStartReminderEnabled() && (startReminderTime - now) > 0) {
            NotificationScheduler.scheduleReservationNotification(
                context,
                startReminderTime,
                "Tu reserva está por comenzar",
                "Tu reserva de parking comienza en 30 minutos.",
                reservaId + "_start"
            );
        }
        // Notificación de fin (15 min antes de terminar)
        if (prefs.isEndReminderEnabled() && (endReminderTime - now) > 0) {
            NotificationScheduler.scheduleReservationNotification(
                context,
                endReminderTime,
                "Tu reserva está por terminar",
                "Tu reserva de parking termina en 15 minutos.",
                reservaId + "_end"
            );
        }
    }

    private void cancelReservationNotifications(Reserva reserva) {
        Context context = ParkingApplication.getAppContext();
        String reservaId = reserva.getId();
        NotificationScheduler.cancelReservationNotification(context, reservaId + "_start");
        NotificationScheduler.cancelReservationNotification(context, reservaId + "_end");
    }

    private void cancelReservationNotifications(String reservaId) {
        Context context = ParkingApplication.getAppContext();
        NotificationScheduler.cancelReservationNotification(context, reservaId + "_start");
        NotificationScheduler.cancelReservationNotification(context, reservaId + "_end");
    }

    /**
     * Elimina una reserva por su ID
     */
    public void deleteReservation(String reservaId) {
        isLoading.setValue(true);
        cancelReservationNotifications(reservaId);
        repository.deleteReservation(reservaId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                loadUserReservations();
                loadHistoricReservations();
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                error.setValue("Error al eliminar la reserva: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Verifica la disponibilidad de una reserva utilizando el objeto Reserva directamente
     */
    public LiveData<Boolean> checkReservationAvailability(Reserva reserva, String excludeReservationId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

        repository.checkAvailability(reserva, excludeReservationId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean available) {
                result.setValue(available);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                error.setValue("Error al verificar disponibilidad: " + e.getMessage());
                result.setValue(false);
                isLoading.setValue(false);
            }
        });

        return result;
    }

    // Sobrecarga para compatibilidad
    public LiveData<Boolean> checkReservationAvailability(Reserva reserva) {
        return checkReservationAvailability(reserva, null);
    }

    /**
     * Metodo de compatibilidad para verificar disponibilidad usando parámetros individuales
     */
    public LiveData<Boolean> checkReservationAvailability(String date, long startTimeMs,
                                                          long endTimeMs, String type, String plazaId) {
        Hora hora = new Hora(startTimeMs, endTimeMs);
        Plaza plaza = new Plaza(plazaId, type);
        Reserva reserva = new Reserva(date, "", "", plaza, hora);

        return checkReservationAvailability(reserva);
    }

    /**
     * Crea una nueva reserva usando el objeto Reserva directamente
     */
    public LiveData<Boolean> createReservation(Reserva reserva) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

        User currentUser = repository.getCurrentUser();
        if (currentUser == null) {
            error.setValue("Usuario no conectado");
            result.setValue(false);
            isLoading.setValue(false);
            return result;
        }

        // Validación: hora de fin debe ser posterior a la de inicio
        if (reserva.getHora().getHoraFin() <= reserva.getHora().getHoraInicio()) {
            error.setValue("La hora de fin debe ser posterior a la de inicio");
            result.setValue(false);
            isLoading.setValue(false);
            return result;
        }

        // Validación: duración máxima de 9 horas
        if (!Validators.isValidReservationDuration9h(
                reserva.getHora().getHoraInicio(), reserva.getHora().getHoraFin())) {
            error.setValue("La duración máxima de una reserva es de 9 horas");
            result.setValue(false);
            isLoading.setValue(false);
            return result;
        }

        // Asignar el usuario actual a la reserva
        reserva.setUsuario(currentUser.getEmail());

        // Generar ID si no tiene
        if (reserva.getId() == null || reserva.getId().isEmpty()) {
            reserva.setId(UUID.randomUUID().toString());
        }

        scheduleReservationNotifications(reserva);

        repository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                result.postValue(success);
                loadUserReservations();
                loadCurrentAndNextReservations();
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                // Si el mensaje de error ya es específico, solo setear error y NO poner result false
                if (e.getMessage() != null && e.getMessage().contains("2 minutos")) {
                    error.postValue(e.getMessage());
                } else {
                    error.postValue("Error al crear la reserva: " + e.getMessage());
                }
                result.postValue(false);
                isLoading.postValue(false);
            }
        });
        Log.d("ReservationsViewModel", "Creating reservation: " + reserva.toString());
        Log.d("ReservationsViewModel", "Devuelve: " + result.getValue());
        return result;
    }

    /**
     * Actualiza una reserva existente usando el objeto Reserva directamente
     */
    public LiveData<Boolean> updateReservation(Reserva reserva) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

        User currentUser = repository.getCurrentUser();
        if (currentUser == null) {
            error.setValue("Usuario no conectado");
            result.setValue(false);
            isLoading.setValue(false);
            return result;
        }
        // Asegurarse de que el usuario está correctamente asignado
        reserva.setUsuario(currentUser.getEmail());

        if (reserva.getId() == null || reserva.getId().isEmpty()) {
            error.setValue("ID de reserva no válido");
            result.setValue(false);
            isLoading.setValue(false);
            return result;
        }

        cancelReservationNotifications(reserva);
        scheduleReservationNotifications(reserva);

        repository.updateReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean updateResult) {
                result.setValue(updateResult);
                loadUserReservations();
                loadCurrentReservation();
                loadNextReservation();
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                error.setValue("Error al actualizar la reserva: " + e.getMessage());
                result.setValue(false);
                isLoading.setValue(false);
            }
        });

        return result;
    }

    public LiveData<Boolean> checkUserHasReservationOnDate(String date) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        User currentUser = repository.getCurrentUser();

        if (currentUser == null) {
            result.setValue(false);
            return result;
        }

        repository.hasReservationOnDate(currentUser.getEmail(), date, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean hasReservation) {
                result.postValue(hasReservation);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Error al verificar reservas: " + e.getMessage());
                result.postValue(false);
            }
        });

        return result;
    }

    public LiveData<List<String>> getUserReservationDates() {
        MutableLiveData<List<String>> reservedDates = new MutableLiveData<>();
        List<String> dates = new ArrayList<>();

        User currentUser = repository.getCurrentUser();
        if (currentUser == null) {
            reservedDates.setValue(dates);
            return reservedDates;
        }

        repository.getReservations(currentUser.getEmail(), new DataCallback<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> result) {
                for (Reserva reserva : result) {
                    // Solo considerar reservas futuras o actuales que NO estén canceladas
                    if (!DateUtils.isHistoricReservation(reserva)
                        && reserva.getEstado() != Reserva.Estado.CANCELADA) {
                        dates.add(reserva.getFecha());
                    }
                }
                reservedDates.postValue(dates);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Error al cargar fechas de reservas: " + e.getMessage());
                reservedDates.postValue(new ArrayList<>());
            }
        });

        return reservedDates;
    }

    public void assignRandomPlaza(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId) {
        isLoading.setValue(true);
        repository.assignRandomPlaza(tipo, fecha, horaInicio, horaFin, excludeReservationId, new DataCallback<String>() {
            @Override
            public void onSuccess(String result) {
                randomPlaza.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Error al asignar plaza aleatoria: " + e.getMessage());
                randomPlaza.postValue(null);
                isLoading.postValue(false);
            }
        });
    }

    // Sobrecarga para compatibilidad (sin excludeReservationId)
    public void assignRandomPlaza(String tipo, String fecha, long horaInicio, long horaFin) {
        assignRandomPlaza(tipo, fecha, horaInicio, horaFin, null);
    }

    public LiveData<List<String>> getAvailablePlazas(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId) {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        isLoading.setValue(true);
        repository.getAvailablePlazas(tipo, fecha, horaInicio, horaFin, excludeReservationId, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> plazas) {
                result.postValue(plazas);
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(Exception e) {
                error.postValue("Error al consultar plazas disponibles: " + e.getMessage());
                result.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }
        });
        return result;
    }

    public LiveData<List<String>> getAvailablePlazas(String tipo, String fecha, long horaInicio, long horaFin) {
        return getAvailablePlazas(tipo, fecha, horaInicio, horaFin, null);
    }

    public void loadAvailablePlazasAndExtractRowsNumbers(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId) {
        isLoading.setValue(true);
        repository.getAvailablePlazas(tipo, fecha, horaInicio, horaFin, excludeReservationId, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> plazas) {
                // Extraer filas (letras) y números únicos
                List<String> rows = new ArrayList<>();
                List<String> numbers = new ArrayList<>();
                for (String id : plazas) {
                    String[] parts = id.split("-");
                    if (parts.length == 2) {
                        String row = parts[0];
                        String number = parts[1];
                        if (!rows.contains(row)) rows.add(row);
                        if (!numbers.contains(number)) numbers.add(number);
                    }
                }
                availableRows.postValue(rows);
                availableNumbers.postValue(numbers);
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(Exception e) {
                error.postValue("Error al consultar plazas disponibles: " + e.getMessage());
                availableRows.postValue(new ArrayList<>());
                availableNumbers.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }
        });
    }

    public void loadAvailablePlazasAndExtractRowsNumbers(String tipo, String fecha, long horaInicio, long horaFin) {
        loadAvailablePlazasAndExtractRowsNumbers(tipo, fecha, horaInicio, horaFin, null);
    }

}
