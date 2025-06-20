package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.databinding.CardReservationNowBinding;
import com.lksnext.parkingplantilla.databinding.CardReservationNextBinding;
import com.lksnext.parkingplantilla.databinding.FragmentHomeBinding;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ReservationsViewModel viewModel;

    private Handler refreshHandler = new Handler();
    private final int REFRESH_INTERVAL_MS = 10000; // 10 segundos
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isResumed()) {
                viewModel.loadCurrentAndNextReservations();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        }
    };

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
        viewModel = new ViewModelProvider(requireActivity()).get(ReservationsViewModel.class);

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
            // Navegación con Navigation Component
            NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.flFragment);
            navController.navigate(R.id.createReservationFragment);
        });

        // Cargar datos
        viewModel.loadCurrentAndNextReservations();

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
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

            // Calcular el texto de tiempo restante y asignarlo
            String timeRemaining = com.lksnext.parkingplantilla.utils.DateUtils.getTimeRemainingText(reserva);
            cardBinding.timeRemainingTextView.setText(timeRemaining);

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
            binding.noNextReservationText.setVisibility(View.GONE);
            CardReservationNextBinding cardBinding = CardReservationNextBinding.inflate(
                    getLayoutInflater(), binding.nextReservationContainer, false);
            cardBinding.setReserva(reserva);

            String timeText;
            if (com.lksnext.parkingplantilla.utils.DateUtils.isFutureReservation(reserva)) {
                timeText = com.lksnext.parkingplantilla.utils.DateUtils.getTimeUntilText(reserva);
            } else if (com.lksnext.parkingplantilla.utils.DateUtils.isOngoingReservation(reserva)) {
                timeText = com.lksnext.parkingplantilla.utils.DateUtils.getTimeRemainingText(reserva);
            } else {
                timeText = "Finalizada";
            }
            cardBinding.timeUntilTextView.setText(timeText);

            binding.nextReservationContainer.removeAllViews();
            binding.nextReservationContainer.addView(cardBinding.getRoot());
        } else {
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

