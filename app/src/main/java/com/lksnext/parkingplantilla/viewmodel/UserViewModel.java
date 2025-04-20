package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;

public class UserViewModel extends ViewModel {

    private final DataRepository repository;
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(null);
    private LoginViewModel loginViewModel;

    public UserViewModel() {
        this.repository = ParkingApplication.getRepository();
    }

    public void setLoginViewModel(LoginViewModel loginViewModel) {
        this.loginViewModel = loginViewModel;
    }

    public LiveData<Boolean> isLogoutSuccessful() {
        return logoutSuccess;
    }

    public void logout() {
        // Clear user session in repository if needed
        if (repository != null) {
            repository.logout();
        }

        // Use LoginViewModel to clear credentials and update logged state
        if (loginViewModel != null) {
            loginViewModel.logout();
        }

        // Signal logout success
        logoutSuccess.setValue(true);
    }

    // Reset logout state (call this after navigation)
    public void resetLogoutState() {
        logoutSuccess.setValue(null);
    }
}