package com.lksnext.parkingplantilla.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.lksnext.parkingplantilla.databinding.ActivityLoginBinding;
import com.lksnext.parkingplantilla.viewmodel.LoginViewModel;
import com.lksnext.parkingplantilla.R;

// In LoginActivity.java
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.loginButton.setOnClickListener(v -> {
            clearErrors();

            String email = binding.emailText.getText().toString();
            String password = binding.passwordText.getText().toString();

            // Client-side validation
            boolean isValid = true;
            if (email.isEmpty()) {
                binding.email.setError(getString(R.string.email_required));
                isValid = false;
            }
            if (password.isEmpty()) {
                binding.password.setError(getString(R.string.password_required));
                isValid = false;
            }

            if (isValid) {
                loginViewModel.loginUser(email, password);
            }
        });

        binding.createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.forgotPassButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ChangePassActivity.class);
            startActivity(intent);
        });

        loginViewModel.isLogged().observe(this, logged -> {
            if (logged != null && logged) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Observe login errors
        loginViewModel.getLoginError().observe(this, error -> {
            if (error != LoginViewModel.LoginError.NONE) {
                handleLoginError(error);
            }
        });
    }

    private void handleLoginError(LoginViewModel.LoginError error) {
        switch (error) {
            case INVALID_CREDENTIALS:
                // Show error at password field
                binding.password.setError(getString(R.string.invalid_credentials));
                break;

            case EMPTY_FIELDS:
                // This should be caught by client-side validation already
                if (binding.emailText.getText().toString().isEmpty()) {
                    binding.email.setError(getString(R.string.email_required));
                }
                if (binding.passwordText.getText().toString().isEmpty()) {
                    binding.password.setError(getString(R.string.password_required));
                }
                break;

            case NETWORK_ERROR:
                Snackbar.make(binding.getRoot(),
                        getString(R.string.network_error),
                        Snackbar.LENGTH_LONG).show();
                break;

            case APPLICATION_ERROR:
                Snackbar.make(binding.getRoot(),
                        getString(R.string.app_initialization_error),
                        Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    private void clearErrors() {
        binding.email.setError(null);
        binding.password.setError(null);
    }
}