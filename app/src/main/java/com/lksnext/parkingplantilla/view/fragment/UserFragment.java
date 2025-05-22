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

        if (userViewModel.getCurrentUser() != null) {
            binding.textView4.setText(userViewModel.getCurrentUser().getName());
            binding.textView5.setText(userViewModel.getCurrentUser().getEmail());
        }

        // Configurar el RadioGroup para el tema
        setupThemeSelection();

        // Configurar los switches de notificaciones
        setupNotificationSwitches();
    }

    private void setupNotificationSwitches() {
        // Establecer el estado inicial de los switches según las preferencias guardadas
        binding.switchCompat.setChecked(userViewModel.isStartReminderEnabled());
        binding.switchCompat3.setChecked(userViewModel.isEndReminderEnabled());

        // Configurar listeners para los switches
        binding.switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.setStartReminderEnabled(isChecked);
        });

        binding.switchCompat3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.setEndReminderEnabled(isChecked);
        });
    }

    private void setupThemeSelection() {
        // Establecer el tema actual según las preferencias guardadas
        int currentTheme = userViewModel.getThemeMode();
        switch (currentTheme) {
            case UserViewModel.THEME_LIGHT:
                binding.radioButtonLight.setChecked(true);
                break;
            case UserViewModel.THEME_DARK:
                binding.radioButtonDark.setChecked(true);
                break;
            case UserViewModel.THEME_SYSTEM:
            default:
                binding.radioButtonSystem.setChecked(true);
                break;
        }

        // Configurar listeners para los RadioButtons
        binding.radioButtonLight.setOnClickListener(v -> {
            if (userViewModel.getThemeMode() != UserViewModel.THEME_LIGHT) {
                userViewModel.setThemeMode(UserViewModel.THEME_LIGHT);
                requireActivity().recreate();
            }
        });

        binding.radioButtonDark.setOnClickListener(v -> {
            if (userViewModel.getThemeMode() != UserViewModel.THEME_DARK) {
                userViewModel.setThemeMode(UserViewModel.THEME_DARK);
                requireActivity().recreate();
            }
        });

        binding.radioButtonSystem.setOnClickListener(v -> {
            if (userViewModel.getThemeMode() != UserViewModel.THEME_SYSTEM) {
                userViewModel.setThemeMode(UserViewModel.THEME_SYSTEM);
                requireActivity().recreate();
            }
        });
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