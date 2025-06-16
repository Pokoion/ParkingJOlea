package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.User;
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
        repository.register(username, email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                isRegistering.postValue(false);
                registrationSuccess.postValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                isRegistering.postValue(false);
                RegisterError error = RegisterError.APPLICATION_ERROR;
                if (e != null && e.getMessage() != null) {
                    String msg = e.getMessage().toLowerCase();
                    if (msg.contains("already") || msg.contains("existe") || msg.contains("in use")) {
                        error = RegisterError.EMAIL_ALREADY_EXISTS;
                    } else if (msg.contains("network")) {
                        error = RegisterError.NETWORK_ERROR;
                    }
                }
                registerError.postValue(error);
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
