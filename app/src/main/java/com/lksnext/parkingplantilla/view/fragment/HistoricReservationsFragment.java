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

import com.lksnext.parkingplantilla.databinding.FragmentHistoricReservationsBinding;
import com.lksnext.parkingplantilla.adapters.HistoricReservationsAdapter;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

public class HistoricReservationsFragment extends Fragment {

    private FragmentHistoricReservationsBinding binding;
    private ReservationsViewModel viewModel;

    private Handler refreshHandler = new Handler();
    private final int REFRESH_INTERVAL_MS = 10000; // 10 segundos
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isResumed()) {
                viewModel.loadHistoricReservations();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        }
    };

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
            if (reservas != null && !reservas.isEmpty()) {
                adapter.setReservas(reservas);
                binding.recyclerViewReservations.setVisibility(View.VISIBLE);
                binding.noReservationsText.setVisibility(View.GONE);
            } else {
                binding.recyclerViewReservations.setVisibility(View.GONE);
                binding.noReservationsText.setVisibility(View.VISIBLE);
            }
        });

        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.recyclerViewReservations.setVisibility(View.GONE);
                binding.noReservationsText.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                // Mostrar la lista o el texto según si hay reservas
                if (viewModel.getHistoricReservations().getValue() != null && !viewModel.getHistoricReservations().getValue().isEmpty()) {
                    binding.recyclerViewReservations.setVisibility(View.VISIBLE);
                    binding.noReservationsText.setVisibility(View.GONE);
                } else {
                    binding.recyclerViewReservations.setVisibility(View.GONE);
                    binding.noReservationsText.setVisibility(View.VISIBLE);
                }
            }
        });

        // Cargar reservas históricas
        viewModel.loadHistoricReservations();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

