package com.lksnext.parkingplantilla.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.lksnext.parkingplantilla.databinding.FragmentUserBinding;
import com.lksnext.parkingplantilla.view.activity.LoginActivity;
import com.lksnext.parkingplantilla.viewmodel.LoginViewModel;
import com.lksnext.parkingplantilla.viewmodel.UserViewModel;
import com.lksnext.parkingplantilla.data.UserPreferencesManager;

import android.app.AlarmManager;
import android.provider.Settings;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private UserViewModel userViewModel;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private android.widget.CompoundButton lastSwitchTriedToEnable = null;
    private UserPreferencesManager userPreferencesManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (Boolean.FALSE.equals(isGranted) && lastSwitchTriedToEnable != null) {
                        lastSwitchTriedToEnable.setChecked(false);
                    }
                    lastSwitchTriedToEnable = null;
                }
        );

        userPreferencesManager = new UserPreferencesManager(requireContext());
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        LoginViewModel loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        userViewModel.setLoginViewModel(loginViewModel);
        setupUI();
        observeLogout();
    }

    private void setupUI() {
        // Set logout button click listener
        binding.logoutButton.setOnClickListener(v -> userViewModel.logout());

        if (userViewModel.getCurrentUser() != null) {
            binding.userNameText.setText(userViewModel.getCurrentUser().getName());
            binding.userEmailText.setText(userViewModel.getCurrentUser().getEmail());
        }

        // Configurar el RadioGroup para el tema
        setupThemeSelection();

        // Configurar los switches de notificaciones
        setupNotificationSwitches();
    }

    private void setupNotificationSwitches() {
        if (userPreferencesManager.isFirstTimeUserFragment()) {
            resetNotificationSwitches();
        } else {
            setNotificationSwitchesFromPreferences();
        }

        binding.startReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> handleReminderSwitchChange(buttonView, isChecked, true));

        binding.endReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> handleReminderSwitchChange(buttonView, isChecked, false));
    }

    private void resetNotificationSwitches() {
        binding.startReminderSwitch.setChecked(false);
        binding.endReminderSwitch.setChecked(false);
        userPreferencesManager.setStartReminderEnabled(false);
        userPreferencesManager.setEndReminderEnabled(false);
        userPreferencesManager.setFirstTimeUserFragment(false);
    }

    private void setNotificationSwitchesFromPreferences() {
        binding.startReminderSwitch.setChecked(userPreferencesManager.isStartReminderEnabled());
        binding.endReminderSwitch.setChecked(userPreferencesManager.isEndReminderEnabled());
    }

    private void handleReminderSwitchChange(android.widget.CompoundButton buttonView, boolean isChecked, boolean isStartReminder) {
        if (isChecked) {
            requestNotificationPermissionIfNeeded(buttonView);
            requestExactAlarmPermissionIfNeeded();
        }
        if (isStartReminder) {
            userPreferencesManager.setStartReminderEnabled(isChecked);
        } else {
            userPreferencesManager.setEndReminderEnabled(isChecked);
        }
    }

    private void requestNotificationPermissionIfNeeded(android.widget.CompoundButton buttonView) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            lastSwitchTriedToEnable = buttonView;
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void requestExactAlarmPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(android.content.Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(requireContext(), "Activa el permiso de alarmas exactas para recibir recordatorios.", Toast.LENGTH_LONG).show();
            }
        }
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

