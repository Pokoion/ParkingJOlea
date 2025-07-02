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

    private static final int REFRESH_INTERVAL_MS = 10000; // 10 segundos
    private boolean isFirstLoad = true;
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isResumed()) {
                boolean prevFirstLoad = isFirstLoad;
                isFirstLoad = false;
                viewModel.loadHistoricReservations();
                isFirstLoad = prevFirstLoad;
                requireView().postDelayed(this, REFRESH_INTERVAL_MS);
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

        HistoricReservationsAdapter adapter = new HistoricReservationsAdapter();
        binding.recyclerViewReservations.setAdapter(adapter);
        viewModel = new ViewModelProvider(requireActivity()).get(ReservationsViewModel.class);

        viewModel.getHistoricReservations().observe(getViewLifecycleOwner(), reservas -> updateHistoricReservationsUI(adapter, reservas));
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateLoadingUI);

        viewModel.loadHistoricReservations();
        isFirstLoad = false;
    }

    private void updateHistoricReservationsUI(HistoricReservationsAdapter adapter, java.util.List<com.lksnext.parkingplantilla.domain.Reserva> reservas) {
        boolean hasReservas = reservas != null && !reservas.isEmpty();
        adapter.setReservas(reservas);
        binding.recyclerViewReservations.setVisibility(hasReservas ? View.VISIBLE : View.GONE);
        binding.noReservationsText.setVisibility(hasReservas ? View.GONE : View.VISIBLE);
    }

    private void updateLoadingUI(Boolean isLoading) {
        boolean hasReservas = viewModel.getHistoricReservations().getValue() != null && !viewModel.getHistoricReservations().getValue().isEmpty();
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

