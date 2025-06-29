package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.DataCallback;

public class ChangePassViewModel extends ViewModel {
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final DataRepository repository;

    public ChangePassViewModel() {
        repository = ParkingApplication.getRepository();
    }
    public ChangePassViewModel(DataRepository repository) {
        this.repository = repository;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void sendPasswordResetEmail(String email) {
        if (repository == null) {
            statusMessage.setValue(null);
            return;
        }
        isLoading.setValue(true);
        repository.checkUserExists(email, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (exists) {
                    repository.sendPasswordResetEmail(email, new DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean sent) {
                            statusMessage.setValue("Correo de recuperación enviado. Revisa tu bandeja de entrada.");
                            isLoading.setValue(false);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            statusMessage.setValue("Error al enviar el correo: " + e.getMessage());
                            isLoading.setValue(false);
                        }
                    });
                } else {
                    statusMessage.setValue("El email no está registrado.");
                    isLoading.setValue(false);
                }
            }
            @Override
            public void onFailure(Exception e) {
                statusMessage.setValue("Error al comprobar el email: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
}
