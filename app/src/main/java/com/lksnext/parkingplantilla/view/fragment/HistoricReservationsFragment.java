package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.databinding.CardReservationHistoricBinding;
import com.lksnext.parkingplantilla.databinding.FragmentHistoricReservationsBinding;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

import java.util.List;

public class HistoricReservationsFragment extends Fragment {

    private FragmentHistoricReservationsBinding binding;
    private ReservationsViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoricReservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Compartir ViewModel con el fragmento padre
        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservationsViewModel.class);

        // Observar reservas históricas
        viewModel.getHistoricReservations().observe(getViewLifecycleOwner(), this::displayHistoricReservations);

        // Cargar reservas históricas
        viewModel.loadHistoricReservations();
    }

    private void displayHistoricReservations(List<Reserva> reservas) {
        LinearLayout container = binding.linearLayoutContainer;

        if (reservas == null || reservas.isEmpty()) {
            container.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.VISIBLE);
            container.removeAllViews();

            for (Reserva reserva : reservas) {
                CardReservationHistoricBinding cardBinding = CardReservationHistoricBinding.inflate(
                        getLayoutInflater(), container, false);

                // Usar data binding para establecer la reserva
                cardBinding.setReserva(reserva);

                // Aquí se podría establecer el estado dinámicamente si existe esa propiedad
                // Por ejemplo: cardBinding.statusText.setText(reserva.getEstado());

                container.addView(cardBinding.getRoot());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}