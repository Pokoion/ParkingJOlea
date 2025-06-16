package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.lksnext.parkingplantilla.adapters.ReservationsPagerAdapter;
import com.lksnext.parkingplantilla.databinding.FragmentReservationsBinding;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

public class ReservationsFragment extends Fragment {

    private FragmentReservationsBinding binding;
    private ReservationsViewModel viewModel;

    public ReservationsFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ReservationsViewModel.class);

        // Configurar ViewPager2 y TabLayout
        setupViewPager();

        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewPager() {
        ReservationsPagerAdapter adapter = new ReservationsPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // Configurar TabLayout con ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Actuales" : "Últimos 30 días")
        ).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}