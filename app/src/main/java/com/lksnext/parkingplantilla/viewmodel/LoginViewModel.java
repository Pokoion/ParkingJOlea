package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.User;
import com.lksnext.parkingplantilla.utils.Validators;

public class LoginViewModel extends ViewModel {

    public enum LoginError {
        EMPTY_FIELDS,
        INVALID_CREDENTIALS,
        NETWORK_ERROR,
        APPLICATION_ERROR,
        ALREADY_LOGGED
    }

    private final DataRepository repository;
    private final MutableLiveData<Boolean> logged = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserEmail = new MutableLiveData<>();
    private final MutableLiveData<LoginError> loginError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        repository = ParkingApplication.getInstance().getRepository();
        // Check if user is already logged in
        if (repository.isUserLoggedIn()) {
            User currentUser = repository.getCurrentUser();
            if (currentUser != null) {
                logged.setValue(true);
                currentUserEmail.setValue(currentUser.getEmail());
            }
        }
    }

    public LoginViewModel(DataRepository repository) {
        this.repository = repository;
        // Check if user is already logged in
        if (repository != null && repository.isUserLoggedIn()) {
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

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loginUser(String email, String password) {
        isLoading.setValue(true);

        if (!validateLoginFields(email, password)) {
            isLoading.setValue(false);
            return;
        }

        repository.login(email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                logged.setValue(true);
                currentUserEmail.setValue(user.getEmail());
                isLoading.setValue(false);
                loginError.setValue(null);
            }
            @Override
            public void onFailure(Exception e) {
                logged.setValue(false);
                loginError.setValue(null);
                currentUserEmail.setValue(null);
                if (e instanceof IllegalStateException && e.getMessage() != null && e.getMessage().contains("sesión activa")) {
                    loginError.setValue(LoginError.ALREADY_LOGGED);
                } else {
                    loginError.setValue(LoginError.INVALID_CREDENTIALS);
                }
                isLoading.setValue(false);
            }
        });
    }

    public boolean validateLoginFields(String email, String password) {
        if (repository == null) {
            loginError.setValue(LoginError.APPLICATION_ERROR);
            return false;
        }
        if (!Validators.areLoginFieldsValid(email, password)) {
            loginError.setValue(LoginError.EMPTY_FIELDS);
            return false;
        }
        return true;
    }

    public void logout() {
        repository.logout();
        logged.setValue(false);
        currentUserEmail.setValue(null);
        loginError.setValue(null);
    }
}

