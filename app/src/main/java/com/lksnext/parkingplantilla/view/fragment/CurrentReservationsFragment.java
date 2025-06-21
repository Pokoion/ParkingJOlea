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

import com.lksnext.parkingplantilla.databinding.FragmentCurrentReservationsBinding;
import com.lksnext.parkingplantilla.adapters.ReservationsAdapter;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

public class CurrentReservationsFragment extends Fragment {

    private FragmentCurrentReservationsBinding binding;
    private ReservationsViewModel viewModel;

    private Handler refreshHandler = new Handler();
    private final int REFRESH_INTERVAL_MS = 10000; // 10 segundos
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isResumed()) {
                viewModel.loadUserReservations();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCurrentReservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ReservationsViewModel.class);

        ReservationsAdapter adapter = new ReservationsAdapter(viewModel);
        binding.recyclerViewReservations.setAdapter(adapter);

        // Observar reservas actuales
        viewModel.getReservations().observe(getViewLifecycleOwner(), reservas -> {
            if (reservas != null && !reservas.isEmpty()) {
                adapter.setReservas(reservas);
                binding.recyclerViewReservations.setVisibility(View.VISIBLE);
                binding.noReservationsText.setVisibility(View.GONE);
            } else {
                binding.recyclerViewReservations.setVisibility(View.GONE);
                binding.noReservationsText.setVisibility(View.VISIBLE);
            }
        });

        // Cargar reservas actuales
        viewModel.loadUserReservations();
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

