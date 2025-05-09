package com.lksnext.parkingplantilla.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.databinding.ActivityRegisterBinding;
import com.lksnext.parkingplantilla.utils.SnackbarUtils;
import com.lksnext.parkingplantilla.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Asignamos la vista/interfaz de registro
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Asignamos el viewModel de register
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Set up observers
        setupObservers();

        // Set up click listeners
        binding.btnRegister.setOnClickListener(v -> attemptRegistration());

        // Back button functionality
        binding.loginButton.setOnClickListener(v -> finish());
    }

    private void setupObservers() {
        // Observe registration process state
        registerViewModel.getIsRegistering().observe(this, isRegistering -> {
            binding.btnRegister.setEnabled(!isRegistering);
        });

        // Observe registration success
        registerViewModel.getRegistrationSuccess().observe(this, success -> {
            if (success) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerViewModel.getRegisterError().observe(this, error -> {
            if (error != null) {
                handleRegisterError(error);
            }
        });
    }

    private void handleRegisterError(RegisterViewModel.RegisterError error) {
        clearErrors();

        switch (error) {
            case INVALID_EMAIL:
                binding.email.setError(getString(R.string.invalid_email_format));
                break;

            case USERNAME_EMPTY:
                binding.user.setError(getString(R.string.username_required));
                break;

            case PASSWORD_TOO_SHORT:
                binding.password.setError(getString(R.string.password_too_short));
                break;

            case EMAIL_ALREADY_EXISTS:
                binding.email.setError(getString(R.string.email_already_exists));
                break;

            case NETWORK_ERROR:
                SnackbarUtils.showVisibleSnackbar(this, binding.getRoot(),
                        getString(R.string.network_error), Snackbar.LENGTH_LONG);
                break;

            case APPLICATION_ERROR:
                SnackbarUtils.showVisibleSnackbar(this, binding.getRoot(),
                        getString(R.string.application_error), Snackbar.LENGTH_LONG);
                break;
        }
    }

    private void attemptRegistration() {
        // Clear previous errors
        clearErrors();

        // Get input values
        String email = binding.emailText.getText().toString().trim();
        String username = binding.userText.getText().toString().trim();
        String password = binding.passwordText.getText().toString().trim();

        // Attempt registration through view model
        registerViewModel.register(email, username, password);
    }

    private void clearErrors() {
        binding.email.setError(null);
        binding.user.setError(null);
        binding.password.setError(null);
    }
}