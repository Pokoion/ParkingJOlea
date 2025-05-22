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
import com.lksnext.parkingplantilla.databinding.FragmentCurrentReservationsBinding;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

import java.util.List;

public class CurrentReservationsFragment extends Fragment {

    private FragmentCurrentReservationsBinding binding;
    private ReservationsViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCurrentReservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Compartir ViewModel con el fragmento padre
        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservationsViewModel.class);

        // Observar reservas actuales
        viewModel.getReservations().observe(getViewLifecycleOwner(), this::displayReservations);

        // Cargar reservas actuales
        viewModel.loadUserReservations();
    }

    private void displayReservations(List<Reserva> reservas) {
        LinearLayout container = binding.linearLayoutContainer;

        if (reservas == null || reservas.isEmpty()) {
            container.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.VISIBLE);
            container.removeAllViews();

            for (Reserva reserva : reservas) {
                CardReservationCurrentBinding cardBinding = CardReservationCurrentBinding.inflate(
                        getLayoutInflater(), container, false);
                cardBinding.setReserva(reserva);
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