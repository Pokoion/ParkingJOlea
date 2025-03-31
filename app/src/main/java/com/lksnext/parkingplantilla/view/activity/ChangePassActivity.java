package com.lksnext.parkingplantilla.view.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.databinding.ActivityChangepassBinding;
import com.lksnext.parkingplantilla.viewmodel.ChangePassViewModel;

public class ChangePassActivity extends AppCompatActivity {

    private ActivityChangepassBinding binding;
    private ChangePassViewModel changePassViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Asignamos la vista/interfaz de cambiar contraseña
        binding = ActivityChangepassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Asignamos el viewModel de cambiar contraseña
        changePassViewModel = new ViewModelProvider(this).get(ChangePassViewModel.class);
    }
}