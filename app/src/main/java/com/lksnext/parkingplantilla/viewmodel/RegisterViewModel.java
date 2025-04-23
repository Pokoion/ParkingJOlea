package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.utils.Validators;

public class RegisterViewModel extends ViewModel {

    public enum RegisterError {
        NONE,
        INVALID_EMAIL,
        USERNAME_EMPTY,
        PASSWORD_TOO_SHORT,
        EMAIL_ALREADY_EXISTS,
        NETWORK_ERROR,
        APPLICATION_ERROR
    }

    private final DataRepository repository;
    private final MutableLiveData<Boolean> isRegistering = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<RegisterError> registerError = new MutableLiveData<>(RegisterError.NONE);

    public RegisterViewModel() {
        repository = ParkingApplication.getRepository();
    }

    public LiveData<Boolean> getIsRegistering() {
        return isRegistering;
    }

    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }

    public LiveData<RegisterError> getRegisterError() {
        return registerError;
    }

    public void register(String email, String username, String password) {
        registerError.setValue(RegisterError.NONE);
        if (!validateRegisterFields(email, username, password)) return;
        isRegistering.setValue(true);
        repository.register(username, email, password, new Callback() {
            @Override
            public void onSuccess() {
                isRegistering.postValue(false);
                registrationSuccess.postValue(true);
            }

            @Override
            public void onFailure() {
                isRegistering.postValue(false);
                registerError.postValue(RegisterError.EMAIL_ALREADY_EXISTS);
            }
        });
    }

    public boolean validateRegisterFields(String email, String username, String password) {
        if (!Validators.isValidEmail(email)) {
            registerError.setValue(RegisterError.INVALID_EMAIL);
            return false;
        }

        if (!Validators.isValidUsername(username)) {
            registerError.setValue(RegisterError.USERNAME_EMPTY);
            return false;
        }

        if (!Validators.isValidPassword(password, 6)) {
            registerError.setValue(RegisterError.PASSWORD_TOO_SHORT);
            return false;
        }

        return true;
    }
}