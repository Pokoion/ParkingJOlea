package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.User;
import com.lksnext.parkingplantilla.utils.Validators;

public class LoginViewModel extends ViewModel {

    public enum LoginError {
        NONE,
        EMPTY_FIELDS,
        INVALID_CREDENTIALS,
        NETWORK_ERROR,
        APPLICATION_ERROR
    }

    private final DataRepository repository;
    private final MutableLiveData<Boolean> logged = new MutableLiveData<>(null);
    private final MutableLiveData<String> currentUserEmail = new MutableLiveData<>(null);
    private final MutableLiveData<LoginError> loginError = new MutableLiveData<>(LoginError.NONE);

    public LoginViewModel() {
        this.repository = ParkingApplication.getRepository();
        // Check if user is already logged in
        if (repository.isUserLoggedIn()) {
            User currentUser = repository.getCurrentUser();
            if (currentUser != null) {
                logged.setValue(true);
                currentUserEmail.setValue(currentUser.getEmail());
            }
        }
    }

    public LiveData<Boolean> isLogged() {
        return logged;
    }

    public LiveData<String> getCurrentUserEmail() {
        return currentUserEmail;
    }

    public LiveData<LoginError> getLoginError() {
        return loginError;
    }

    public void loginUser(String email, String password) {
        loginError.setValue(LoginError.NONE);

        if (!validateLoginFields(email, password)) return;

        repository.login(email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                logged.setValue(true);
                currentUserEmail.setValue(user.getEmail());
            }
            @Override
            public void onFailure(Exception e) {
                logged.setValue(false);
                currentUserEmail.setValue(null);
                loginError.setValue(LoginError.INVALID_CREDENTIALS);
            }
        });
    }

    public boolean validateLoginFields(String email, String password) {
        if (!Validators.areLoginFieldsValid(email, password)) {
            loginError.setValue(LoginError.EMPTY_FIELDS);
            return false;
        }

        if (repository == null) {
            loginError.setValue(LoginError.APPLICATION_ERROR);
            return false;
        }
        return true;
    }

    public void logout() {
        repository.logout();
        logged.setValue(false);
        currentUserEmail.setValue(null);
        loginError.setValue(LoginError.NONE);
    }
}

