package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.databinding.CardReservationNowBinding;
import com.lksnext.parkingplantilla.databinding.CardReservationNextBinding;
import com.lksnext.parkingplantilla.databinding.FragmentHomeBinding;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;
import com.lksnext.parkingplantilla.view.activity.CreateReservationActivity;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ReservationsViewModel viewModel;

    public HomeFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ReservationsViewModel.class);

        // Observar la reserva actual
        viewModel.getCurrentReservation().observe(getViewLifecycleOwner(), this::updateCurrentReservation);

        // Observar la próxima reserva
        viewModel.getNextReservation().observe(getViewLifecycleOwner(), this::updateNextReservation);

        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Implementar indicador de carga si es necesario
        });

        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Mostrar mensaje de error
            }
        });

        // Configurar FAB para crear nueva reserva
        binding.createReservationButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateReservationActivity.class);
            startActivity(intent);
        });

        // Aumentar el espaciado vertical para los mensajes de "no hay reservas"
        binding.noCurrentReservationText.setPadding(0, 40, 0, 40);
        binding.noNextReservationText.setPadding(0, 40, 0, 40);

        // Cargar datos
        viewModel.loadCurrentAndNextReservations();

    }

    private void updateCurrentReservation(Reserva reserva) {
        if (reserva != null) {
            // Hay una reserva actual, mostrar la tarjeta
            binding.noCurrentReservationText.setVisibility(View.GONE);

            // Inflar la vista de la tarjeta usando card_reservation_now.xml
            CardReservationNowBinding cardBinding = CardReservationNowBinding.inflate(
                    getLayoutInflater(), binding.currentReservationContainer, false);

            // Asignar la reserva a la tarjeta
            cardBinding.setReserva(reserva);

            // Limpiar el contenedor y agregar la tarjeta
            binding.currentReservationContainer.removeAllViews();
            binding.currentReservationContainer.addView(cardBinding.getRoot());
        } else {
            // No hay reserva actual, mostrar mensaje
            binding.currentReservationContainer.removeAllViews();
            binding.noCurrentReservationText.setVisibility(View.VISIBLE);
        }
    }

    private void updateNextReservation(Reserva reserva) {
        if (reserva != null) {
            // Hay una próxima reserva, mostrar la tarjeta
            binding.noNextReservationText.setVisibility(View.GONE);

            // Inflar la vista de la tarjeta usando card_reservation_next.xml
            CardReservationNextBinding cardBinding = CardReservationNextBinding.inflate(
                    getLayoutInflater(), binding.nextReservationContainer, false);

            // Asignar la reserva a la tarjeta
            cardBinding.setReserva(reserva);

            // Limpiar el contenedor y agregar la tarjeta
            binding.nextReservationContainer.removeAllViews();
            binding.nextReservationContainer.addView(cardBinding.getRoot());
        } else {
            // No hay próxima reserva, mostrar mensaje
            binding.nextReservationContainer.removeAllViews();
            binding.noNextReservationText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}