package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.User;

public class MainViewModel extends ViewModel {
    private final DataRepository repository;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public MainViewModel() {
        repository = ParkingApplication.getRepository();
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        User user = repository.getCurrentUser();
        currentUser.setValue(user);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
}