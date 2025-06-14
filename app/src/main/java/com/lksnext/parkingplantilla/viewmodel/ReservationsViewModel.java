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

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class ReservationsViewModel extends ViewModel {

    private final DataRepository repository;
    private final MutableLiveData<List<Reserva>> reservations = new MutableLiveData<>();
    private final MutableLiveData<List<Reserva>> historicReservations = new MutableLiveData<>();
    private final MutableLiveData<Reserva> currentReservation = new MutableLiveData<>();
    private final MutableLiveData<Reserva> nextReservation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // NUEVO: LiveData para plazas disponibles y plaza random asignada
    private final MutableLiveData<List<String>> availablePlazas = new MutableLiveData<>();
    private final MutableLiveData<String> randomPlaza = new MutableLiveData<>();

    // NUEVO: LiveData para números de plaza disponibles en una fila concreta
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

    public LiveData<List<String>> getAvailablePlazas() {
        return availablePlazas;
    }

    public LiveData<String> getRandomPlaza() {
        return randomPlaza;
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

    /**
     * Elimina una reserva por su ID
     */
    public void deleteReservation(String reservaId) {
        isLoading.setValue(true);
        repository.deleteReservation(reservaId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // Recargar ambas listas para reflejar el cambio en ambos fragmentos
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
        repository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                result.postValue(success);
                loadUserReservations();
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

        repository.updateReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean updateResult) {
                result.setValue(updateResult);
                loadUserReservations();
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

    // NUEVO: Solicitar plazas disponibles
    public void loadAvailablePlazas(String tipo, String fecha, long horaInicio, long horaFin) {
        isLoading.setValue(true);
        repository.getAvailablePlazas(tipo, fecha, horaInicio, horaFin, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                availablePlazas.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Error al cargar plazas disponibles: " + e.getMessage());
                availablePlazas.postValue(new ArrayList<>());
                isLoading.postValue(false);
            }
        });
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

