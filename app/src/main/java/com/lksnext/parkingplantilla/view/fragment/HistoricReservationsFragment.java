package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.databinding.FragmentHistoricReservationsBinding;
import com.lksnext.parkingplantilla.adapters.HistoricReservationsAdapter;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

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

        // Inicializar adaptador
        HistoricReservationsAdapter adapter = new HistoricReservationsAdapter();
        binding.recyclerViewReservations.setAdapter(adapter);

        // Compartir ViewModel con el fragmento padre
        viewModel = new ViewModelProvider(requireActivity()).get(ReservationsViewModel.class);

        // Observar reservas históricas
        viewModel.getHistoricReservations().observe(getViewLifecycleOwner(), reservas -> {
            if (reservas != null) {
                adapter.setReservas(reservas);
                binding.recyclerViewReservations.setVisibility(reservas.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        // Cargar reservas históricas
        viewModel.loadHistoricReservations();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}