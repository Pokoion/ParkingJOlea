package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;

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
}