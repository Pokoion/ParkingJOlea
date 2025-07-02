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
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

public class CurrentReservationsFragment extends Fragment {

    private FragmentCurrentReservationsBinding binding;
    private ReservationsViewModel viewModel;

    private static final int REFRESH_INTERVAL_MS = 10000; // 10 segundos
    private boolean isFirstLoad = true;
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isResumed()) {
                boolean prevFirstLoad = isFirstLoad;
                isFirstLoad = false;
                viewModel.loadUserReservations();
                isFirstLoad = prevFirstLoad;
                requireView().postDelayed(this, REFRESH_INTERVAL_MS);
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
        viewModel.getReservations().observe(getViewLifecycleOwner(), reservas -> updateReservationsUI(adapter, reservas));

        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateLoadingUI);

        // Cargar reservas actuales
        viewModel.loadUserReservations();
        isFirstLoad = false;
    }

    private void updateReservationsUI(ReservationsAdapter adapter, java.util.List<Reserva> reservas) {
        boolean hasReservas = reservas != null && !reservas.isEmpty();
        adapter.setReservas(reservas);
        binding.recyclerViewReservations.setVisibility(hasReservas ? View.VISIBLE : View.GONE);
        binding.noReservationsText.setVisibility(hasReservas ? View.GONE : View.VISIBLE);
    }

    private void updateLoadingUI(Boolean isLoading) {
        boolean hasReservas = viewModel.getReservations().getValue() != null && !viewModel.getReservations().getValue().isEmpty();
        if (Boolean.TRUE.equals(isLoading) && isFirstLoad) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.recyclerViewReservations.setVisibility(View.GONE);
            binding.noReservationsText.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.recyclerViewReservations.setVisibility(hasReservas ? View.VISIBLE : View.GONE);
            binding.noReservationsText.setVisibility(hasReservas ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireView().postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireView().removeCallbacks(refreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

