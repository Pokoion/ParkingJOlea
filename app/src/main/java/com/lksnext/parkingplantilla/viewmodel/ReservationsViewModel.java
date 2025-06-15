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
import com.lksnext.parkingplantilla.utils.Validators;
import com.lksnext.parkingplantilla.notifications.NotificationScheduler;
import com.lksnext.parkingplantilla.data.UserPreferencesManager;
import android.content.Context;

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
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> randomPlaza = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableNumbers = new MutableLiveData<>();

    public ReservationsViewModel() {
        repository = ParkingApplication.getRepository();
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

    // Método para limpiar el valor de randomPlaza
    public void clearRandomPlaza() {
        randomPlaza.setValue(null);
    }

    public LiveData<List<String>> getAvailableNumbers() {
        return availableNumbers;
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

    /**
     * Carga la próxima reserva del usuario (la primera futura, no en curso)
     */
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
                    historicReservations.setValue(result);
                    isLoading.setValue(false);
                }

                @Override
                public void onFailure(Exception e) {
                    error.setValue(e.getMessage());
                    isLoading.setValue(false);
                }
            });
        } else {
            error.setValue("Usuario no conectado");
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
                reservations.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                error.setValue(e.getMessage());
                isLoading.setValue(false);
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
    public LiveData<Boolean> checkReservationAvailability(Reserva reserva) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

        repository.checkAvailability(reserva, new DataCallback<Boolean>() {
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
                error.postValue("Error al crear la reserva: " + e.getMessage());
                result.postValue(false);
                isLoading.postValue(false);
            }
        });

        return result;
    }

    /**
     * Actualiza una reserva existente usando el objeto Reserva directamente
     */
    public LiveData<Boolean> updateReservation(Reserva reserva) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

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
                    // Solo considerar reservas futuras o actuales
                    if (!DateUtils.isHistoricReservation(reserva)) {
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

    // NUEVO: Solicitar plaza random (el backend la asigna y la devuelve)
    public void assignRandomPlaza(String tipo, String fecha, long horaInicio, long horaFin) {
        isLoading.setValue(true);
        repository.assignRandomPlaza(tipo, fecha, horaInicio, horaFin, new DataCallback<String>() {
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

    // NUEVO: Cargar números disponibles para una fila concreta
    public void loadAvailableNumbers(String tipo, String row, String fecha, long horaInicio, long horaFin) {
        isLoading.setValue(true);
        repository.getAvailableNumbers(tipo, row, fecha, horaInicio, horaFin, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                availableNumbers.postValue(result);
                isLoading.setValue(false);
            }
            @Override
            public void onFailure(Exception e) {
                error.postValue("Error al cargar números disponibles: " + e.getMessage());
                availableNumbers.postValue(new ArrayList<>());
                isLoading.setValue(false);
            }
        });
    }

}
