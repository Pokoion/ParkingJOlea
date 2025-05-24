package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.databinding.FragmentCurrentReservationsBinding;
import com.lksnext.parkingplantilla.adapters.ReservationsAdapter;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

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

        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservationsViewModel.class);

        ReservationsAdapter adapter = new ReservationsAdapter(viewModel);
        binding.recyclerViewReservations.setAdapter(adapter);

        // Observar reservas actuales
        viewModel.getReservations().observe(getViewLifecycleOwner(), reservas -> {
            if (reservas != null) {
                adapter.setReservas(reservas);
                binding.recyclerViewReservations.setVisibility(reservas.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        // Cargar reservas actuales
        viewModel.loadUserReservations();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}