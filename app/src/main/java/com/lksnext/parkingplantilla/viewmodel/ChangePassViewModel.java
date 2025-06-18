package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.DataCallback;

public class ChangePassViewModel extends ViewModel {
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>("");
    private final DataRepository repository;

    public ChangePassViewModel() {
        repository = ParkingApplication.getRepository();
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void sendPasswordResetEmail(String email) {
        repository.checkUserExists(email, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (exists) {
                    repository.sendPasswordResetEmail(email, new DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean sent) {
                            statusMessage.postValue("Correo de recuperación enviado. Revisa tu bandeja de entrada.");
                        }
                        @Override
                        public void onFailure(Exception e) {
                            statusMessage.postValue("Error al enviar el correo: " + e.getMessage());
                        }
                    });
                } else {
                    statusMessage.postValue("El email no está registrado.");
                }
            }
            @Override
            public void onFailure(Exception e) {
                statusMessage.postValue("Error al comprobar el email: " + e.getMessage());
            }
        });
    }
}
