package com.lksnext.parkingplantilla.view.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import com.lksnext.parkingplantilla.databinding.ActivityChangepassBinding;
import com.lksnext.parkingplantilla.viewmodel.ChangePassViewModel;

public class ChangePassActivity extends AppCompatActivity {
    private ActivityChangepassBinding binding;
    private ChangePassViewModel changePassViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangepassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        changePassViewModel = new ViewModelProvider(this).get(ChangePassViewModel.class);

        changePassViewModel.getStatusMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                binding.btnSend.setEnabled(true); // Reactivar botón tras respuesta
                if (msg.toLowerCase().contains("enviado")) {
                    // Redirigir a Login (o cerrar Activity)
                    finish();
                }
            }
        });

        // Observa el estado de carga para mostrar/ocultar el ProgressBar y desactivar el botón
        changePassViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSend.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);
            }
        });

        binding.btnSend.setOnClickListener(v -> {
            String email = binding.emailText.getText() != null ? binding.emailText.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Introduce un email válido.", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.btnSend.setEnabled(false); // Deshabilitar para evitar múltiples envíos
            changePassViewModel.sendPasswordResetEmail(email);
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }
}

