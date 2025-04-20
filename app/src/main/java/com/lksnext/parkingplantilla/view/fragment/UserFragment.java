package com.lksnext.parkingplantilla.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.databinding.FragmentUserBinding;
import com.lksnext.parkingplantilla.view.activity.LoginActivity;
import com.lksnext.parkingplantilla.viewmodel.LoginViewModel;
import com.lksnext.parkingplantilla.viewmodel.UserViewModel;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private UserViewModel userViewModel;
    private LoginViewModel loginViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        // Connect the ViewModels
        userViewModel.setLoginViewModel(loginViewModel);

        // Set up UI components
        setupUI();

        // Observe logout state
        observeLogout();
    }

    private void setupUI() {
        // Set logout button click listener
        binding.logoutButton.setOnClickListener(v -> userViewModel.logout());

        // Setup other UI elements as needed
    }

    private void observeLogout() {
        userViewModel.isLogoutSuccessful().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut != null && isLoggedOut) {
                // Navigate to login screen
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                // Clear activity stack so user can't go back
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Reset logout state
                userViewModel.resetLogoutState();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}