package com.lksnext.parkingplantilla.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.databinding.ActivityMainBinding;
import com.lksnext.parkingplantilla.viewmodel.LoginViewModel;
import com.lksnext.parkingplantilla.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializamos el binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configuramos la navegación
        setupNavigation();

        // Configuramos el ViewModel y observadores
        setupViewModel();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.flFragment);
        navController = navHostFragment.getNavController();

        bottomNavigationView = binding.bottomNavigationView;
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                navController.navigate(R.id.mainFragment);
                return true;
            } else if (itemId == R.id.reservations) {
                navController.navigate(R.id.reservationsFragment);
                return true;
            } else if (itemId == R.id.user) {
                navController.navigate(R.id.userFragment);
                return true;
            }
            return false;
        });
    }

    private void setupViewModel() {
        // Inicializamos el ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Comprobamos si el usuario existe en la base de datos
        mainViewModel.checkCurrentUserExists();
        mainViewModel.getUserExists().observe(this, exists -> {
            if (exists != null && !exists) {
                mainViewModel.logoutAndRedirectToLogin(this);
            }
        });

        // Observamos el usuario actual desde el ViewModel
        mainViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                binding.textView2.setText("Bienvenido, " + user.getName() + "!");
            } else {
                binding.textView2.setText("Bienvenido!");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}

