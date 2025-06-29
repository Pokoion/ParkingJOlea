package com.lksnext.parkingplantilla.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.lksnext.parkingplantilla.databinding.ActivityLoginBinding;
import com.lksnext.parkingplantilla.viewmodel.LoginViewModel;
import com.lksnext.parkingplantilla.R;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Set up observers
        setupObservers();

        // Set up click listeners
        setupClickListeners();
    }

    private void setupObservers() {
        loginViewModel.isLogged().observe(this, logged -> {
            if (logged != null && logged) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Observe login errors
        loginViewModel.getLoginError().observe(this, error -> {
            if (error != null) {
                handleLoginError(error);
            }
        });

        // Observer para el loading
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(android.view.View.VISIBLE);
                binding.loginButton.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(android.view.View.GONE);
                binding.loginButton.setEnabled(true);
            }
        });
    }

    private void setupClickListeners() {
        binding.loginButton.setOnClickListener(v -> attemptLogin());

        binding.createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.forgotPassButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ChangePassActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        clearErrors();

        String email = binding.emailText.getText().toString();
        String password = binding.passwordText.getText().toString();

        // Client-side validation
        boolean isValid = validateLoginInputs(email, password);

        if (isValid) {
            loginViewModel.loginUser(email, password);
        }
    }

    private boolean validateLoginInputs(String email, String password) {
        boolean isValid = true;
        if (email.isEmpty()) {
            binding.email.setError("Hace falta el email");
            isValid = false;
        }
        if (password.isEmpty()) {
            binding.password.setError("Hace falta la contraseña");
            isValid = false;
        }
        return isValid;
    }

    private void handleLoginError(LoginViewModel.LoginError error) {
        switch (error) {
            case INVALID_CREDENTIALS:
                // Show error at password field
                binding.password.setError("Credenciales inválidas");
                break;

            case EMPTY_FIELDS:
                // This should be caught by client-side validation already
                if (binding.emailText.getText().toString().isEmpty()) {
                    binding.email.setError("Hace falta el email");
                }
                if (binding.passwordText.getText().toString().isEmpty()) {
                    binding.password.setError("Hace falta la contraseña");
                }
                break;

            case NETWORK_ERROR:
                Snackbar.make(binding.getRoot(),
                        "Error de red. Por favor, comprueba tu conexión.",
                        Snackbar.LENGTH_LONG).show();
                break;

            case APPLICATION_ERROR:
                Snackbar.make(binding.getRoot(),
                        "Error en la aplicación. Por favor, inténtalo más tarde.",
                        Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    private void clearErrors() {
        binding.email.setError(null);
        binding.password.setError(null);
    }
}

