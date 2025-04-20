package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.databinding.CardReservationCurrentBinding;
import com.lksnext.parkingplantilla.databinding.FragmentReservationsBinding;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

import java.util.List;

public class ReservationsFragment extends Fragment {

    private FragmentReservationsBinding binding;
    private ReservationsViewModel viewModel;

    public ReservationsFragment() {
        // Es necesario un constructor vacio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ReservationsViewModel.class);

        // Setup observers
        setupObservers();

        // Load reservations
        viewModel.loadUserReservations();
    }

    private void setupObservers() {
        // Observe reservations
        viewModel.getReservations().observe(getViewLifecycleOwner(), this::displayReservations);

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayReservations(List<Reserva> reservas) {
        LinearLayout container = binding.linearLayoutContainer;

        if (reservas == null || reservas.isEmpty()) {
            container.setVisibility(View.GONE);

        } else {
            container.setVisibility(View.VISIBLE);

            // Clear any existing views
            container.removeAllViews();

            // Inflate and add cards for each reservation
            for (Reserva reserva : reservas) {
                CardReservationCurrentBinding cardBinding = CardReservationCurrentBinding.inflate(
                        getLayoutInflater(), container, false);

                // Set the reservation data using data binding
                cardBinding.setReserva(reserva);

                // Add the card to the container
                container.addView(cardBinding.getRoot());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}