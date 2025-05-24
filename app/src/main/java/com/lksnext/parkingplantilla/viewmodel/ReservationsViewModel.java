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

import java.util.List;

public class ReservationsViewModel extends ViewModel {

    private final DataRepository repository;
    private final MutableLiveData<List<Reserva>> reservations = new MutableLiveData<>();
    private final MutableLiveData<List<Reserva>> historicReservations = new MutableLiveData<>();
    private final MutableLiveData<Reserva> currentReservation = new MutableLiveData<>();
    private final MutableLiveData<Reserva> nextReservation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ReservationsViewModel() {
        repository = ParkingApplication.getRepository();
    }

    public LiveData<List<Reserva>> getReservations() {
        return reservations;
    }
    public LiveData<List<Reserva>> getHistoricReservations() { return historicReservations;}
    public LiveData<Reserva> getCurrentReservation() { return currentReservation; }
    public LiveData<Reserva> getNextReservation() { return nextReservation; }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<String> getError() {
        return error;
    }

    /**
     * Carga la reserva actual del usuario
     */
    public void loadCurrentReservation() {
        User currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            isLoading.setValue(true);
            String userId = currentUser.getEmail();

            repository.getCurrentReservation(userId, new DataCallback<Reserva>() {
                @Override
                public void onSuccess(Reserva result) {
                    currentReservation.setValue(result);
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
     * Carga la próxima reserva del usuario
     */
    public void loadNextReservation() {
        User currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            isLoading.setValue(true);
            String userId = currentUser.getEmail();

            repository.getNextReservation(userId, new DataCallback<Reserva>() {
                @Override
                public void onSuccess(Reserva result) {
                    nextReservation.setValue(result);
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

    public void deleteReservation(String reservaId) {
        isLoading.setValue(true);
        repository.deleteReservation(reservaId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // Recargar las reservas después de eliminar
                loadUserReservations();
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
     * Verifica si una plaza está disponible para una fecha y hora específicas
     */
    public LiveData<Boolean> checkReservationAvailability(String date, long startTimeMs,
                                                          long endTimeMs, String type, String plazaId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

        // Implementación temporal - en un escenario real esto verificaría contra la base de datos
        // si hay alguna reserva que se solape con la fecha, hora y plaza solicitada

        // Simular verificación de disponibilidad (siempre disponible por ahora)
        // En una implementación real, esto consultaría al repositorio
        result.setValue(true);
        isLoading.setValue(false);

    repository.checkAvailability(date, startTimeMs, endTimeMs, type, plazaId,
        new DataCallback<Boolean>() {
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
     * Crea una nueva reserva con los datos proporcionados
     */
    public LiveData<Boolean> createReservation(String date, long startTimeMs,
                                               long endTimeMs, String type, String plazaId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

        User currentUser = repository.getCurrentUser();
        if (currentUser == null) {
            error.setValue("Usuario no conectado");
            result.setValue(false);
            isLoading.setValue(false);
            return result;
        }

        String userId = currentUser.getEmail();
        String reservationId = java.util.UUID.randomUUID().toString();

        // Crear objeto Hora
        Hora hora = new Hora(startTimeMs, endTimeMs);

        // Crear objeto Plaza con el tipo seleccionado
        Plaza plaza = new Plaza(plazaId, type);

        // Crear objeto Reserva
        Reserva reserva = new Reserva(date, userId, reservationId, plaza, hora);

        // Llamar al repositorio para guardar la reserva
        repository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                result.postValue(success);
                isLoading.postValue(false);
                // Recargar las reservas después de crear una nueva
                loadUserReservations();
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
     * Actualiza una reserva existente
     */
    public LiveData<Boolean> updateReservation(String reservationId, String date,
                                               long startTimeMs, long endTimeMs,
                                               String reservationType, String plazaId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);

        repository.updateReservation(reservationId, date, startTimeMs, endTimeMs,
                reservationType, plazaId, new DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean updateResult) {
                        result.setValue(updateResult);
                        // Recargar las reservas después de actualizar
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
}