package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.databinding.FragmentSelectParkingSpotBinding;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.utils.DateUtils;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SelectParkingSpotFragment extends Fragment {
    private FragmentSelectParkingSpotBinding binding;
    private ReservationsViewModel viewModel;
    private String selectedType;
    private Calendar selectedDate;
    private Calendar startTime;
    private Calendar endTime;
    private String reservationId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSelectParkingSpotBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ReservationsViewModel.class);
        Bundle args = getArguments();
        if (args != null) {
            selectedType = args.getString("SELECTED_TYPE");
            selectedDate = (Calendar) args.getSerializable("SELECTED_DATE");
            startTime = (Calendar) args.getSerializable("START_TIME");
            endTime = (Calendar) args.getSerializable("END_TIME");
            reservationId = args.getString("RESERVATION_ID");
        }
        // Cargar filas y números disponibles al iniciar (una sola llamada)
        if (selectedType != null && selectedDate != null && startTime != null && endTime != null) {
            String apiDate = DateUtils.formatDateForApi(selectedDate);
            long startTimeMs = DateUtils.timeToMs(
                    startTime.get(Calendar.HOUR_OF_DAY),
                    startTime.get(Calendar.MINUTE));
            long endTimeMs = DateUtils.timeToMs(
                    endTime.get(Calendar.HOUR_OF_DAY),
                    endTime.get(Calendar.MINUTE));
            viewModel.loadAvailablePlazasAndExtractRowsNumbers(selectedType, apiDate, startTimeMs, endTimeMs);
        }
        // Configurar toolbar y botón de retroceso si existe
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationIcon(com.lksnext.parkingplantilla.R.drawable.ic_arrow_back);
            binding.toolbar.setNavigationOnClickListener(v -> {
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireView());
                navController.popBackStack();
            });
            binding.toolbar.setTitle("Seleccionar plaza");
        }
        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        binding.randomParkingRadioButton.setChecked(true);
        binding.parkingManualSelectionContainer.setVisibility(View.GONE);
        binding.parkingSelectionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.manualParkingRadioButton.getId()) {
                binding.parkingManualSelectionContainer.setVisibility(View.VISIBLE);
            } else {
                binding.parkingManualSelectionContainer.setVisibility(View.GONE);
            }
        });
        setupParkingSpinners();
        binding.btnSaveReservation.setOnClickListener(v -> validateAndSave());
    }

    private void setupParkingSpinners() {
        // No cargar ni actualizar desde el spinner, solo observar los LiveData
        // El filtrado de números se hará en updateRowSpinner
    }

    private void updateRowSpinner(List<String> rows) {
        List<String> displayRows = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            displayRows.add("No disponible");
            binding.parkingRowSpinner.setEnabled(false);
        } else {
            displayRows.addAll(rows);
            binding.parkingRowSpinner.setEnabled(true);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, displayRows);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingRowSpinner.setAdapter(adapter);
        if (!displayRows.isEmpty() && binding.parkingRowSpinner.getCount() > 0) {
            binding.parkingRowSpinner.setSelection(0);
        }
        // Al cambiar la fila, filtrar los números disponibles para esa fila
        binding.parkingRowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRow = (String) parent.getItemAtPosition(position);
                if (selectedRow != null && !selectedRow.equals("No disponible")) {
                    // Filtrar los números para la fila seleccionada
                    viewModel.getAvailableNumbers().observe(getViewLifecycleOwner(), numbers -> {
                        List<String> filtered = new ArrayList<>();
                        for (String num : numbers) {
                            // Solo mostrar números que correspondan a la fila seleccionada
                            // En este enfoque, numbers ya está filtrado, pero si necesitas filtrar, hazlo aquí
                            filtered.add(num);
                        }
                        updateNumberSpinner(filtered);
                    });
                } else {
                    updateNumberSpinner(new ArrayList<>());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateNumberSpinner(List<String> numbers) {
        List<String> displayNumbers = new ArrayList<>();
        if (numbers == null || numbers.isEmpty()) {
            if (binding.parkingRowSpinner.getSelectedItem() == null ||
                "No disponible".equals(binding.parkingRowSpinner.getSelectedItem().toString())) {
                displayNumbers.add("Selecciona fila para ver números");
            } else {
                displayNumbers.add("No disponible");
            }
            binding.parkingNumberSpinner.setEnabled(false);
            binding.btnSaveReservation.setEnabled(false);
        } else {
            displayNumbers.addAll(numbers);
            binding.parkingNumberSpinner.setEnabled(true);
            binding.btnSaveReservation.setEnabled(true);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, displayNumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingNumberSpinner.setAdapter(adapter);
        if (!displayNumbers.isEmpty() && binding.parkingNumberSpinner.getCount() > 0) {
            binding.parkingNumberSpinner.setSelection(0);
        }
    }

    private void observeViewModel() {
        viewModel.getAvailableRows().observe(getViewLifecycleOwner(), this::updateRowSpinner);
        viewModel.getRandomPlaza().observe(getViewLifecycleOwner(), plazaId -> {
            if (plazaId != null) {
                continueWithSaveProcessRandom(plazaId);
                viewModel.clearRandomPlaza();
            }
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }

    private void validateAndSave() {
        if (binding.randomParkingRadioButton.isChecked()) {
            solicitarPlazaRandom();
        } else {
            String plazaId = getSelectedPlazaId();
            if (plazaId == null) {
                Toast.makeText(requireContext(), "Debes seleccionar una plaza disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            continueWithSaveProcess(plazaId);
        }
    }

    private void solicitarPlazaRandom() {
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE));
        viewModel.assignRandomPlaza(selectedType, apiDate, startTimeMs, endTimeMs);
    }

    private void continueWithSaveProcessRandom(String plazaId) {
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE));
        Hora hora = new Hora(startTimeMs, endTimeMs);
        Plaza plaza = new Plaza(plazaId, selectedType);
        Reserva reserva = new Reserva(apiDate, "", reservationId, plaza, hora);
        viewModel.createReservation(reserva).observe(getViewLifecycleOwner(), result -> {
            binding.btnSaveReservation.setEnabled(true);
            if (result != null && result) {
                viewModel.clearTempSelection(); // Limpiar selección temporal al crear correctamente
                Toast.makeText(requireContext(), "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
                NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), com.lksnext.parkingplantilla.R.id.flFragment);
                navController.popBackStack(R.id.mainFragment, false);
            } else {
                Toast.makeText(requireContext(), "Error al crear la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void continueWithSaveProcess(String plazaId) {
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE));
        Hora hora = new Hora(startTimeMs, endTimeMs);
        Plaza plaza = new Plaza(plazaId, selectedType);
        Reserva reserva = new Reserva(apiDate, "", reservationId, plaza, hora);
        viewModel.createReservation(reserva).observe(getViewLifecycleOwner(), result -> {
            binding.btnSaveReservation.setEnabled(true);
            if (result != null && result) {
                viewModel.clearTempSelection(); // Limpiar selección temporal al crear correctamente
                Toast.makeText(requireContext(), "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), com.lksnext.parkingplantilla.R.id.flFragment);
                navController.popBackStack(com.lksnext.parkingplantilla.R.id.mainFragment, false);
            } else {
                Toast.makeText(requireContext(), "Error al crear la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedPlazaId() {
        if (binding.manualParkingRadioButton.isChecked() &&
                binding.parkingRowSpinner.getSelectedItem() != null &&
                binding.parkingNumberSpinner.getSelectedItem() != null) {
            String selectedNumber = binding.parkingNumberSpinner.getSelectedItem().toString();
            if ("No disponible".equals(selectedNumber)) {
                return null;
            }
            String row = binding.parkingRowSpinner.getSelectedItem().toString();
            return row + "-" + selectedNumber;
        }
        return null;
    }
}

