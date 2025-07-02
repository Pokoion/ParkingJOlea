package com.lksnext.parkingplantilla.viewmodel;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.User;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.view.activity.LoginActivity;

public class MainViewModel extends ViewModel {
    private final DataRepository repository;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userExists = new MutableLiveData<>();

    public MainViewModel() {
        repository = ParkingApplication.getInstance().getRepository();
        loadCurrentUser();
    }

    public MainViewModel(DataRepository repository) {
        this.repository = repository;
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        User user = repository.getCurrentUser();
        currentUser.setValue(user);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getUserExists() {
        return userExists;
    }

    public void checkCurrentUserExists() {
        User user = repository.getCurrentUser();
        if (user == null) {
            userExists.setValue(false);
            return;
        }
        repository.checkUserExists(user.getEmail(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                userExists.setValue(exists);
            }
            @Override
            public void onFailure(Exception e) {
                userExists.setValue(false);
            }
        });
    }

    public void logoutAndRedirectToLogin(Activity activity) {
        LoginViewModel loginViewModel = new ViewModelProvider((FragmentActivity) activity).get(LoginViewModel.class);
        loginViewModel.logout();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
