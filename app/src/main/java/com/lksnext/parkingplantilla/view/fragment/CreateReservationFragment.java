package com.lksnext.parkingplantilla.view.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.adapters.ReservationTypeAdapter;
import com.lksnext.parkingplantilla.databinding.FragmentCreateReservationBinding;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.utils.DateUtils;
import com.lksnext.parkingplantilla.utils.Validators;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateReservationFragment extends Fragment implements ReservationTypeAdapter.OnTypeSelectedListener {
    private FragmentCreateReservationBinding binding;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private ReservationTypeAdapter adapter;
    private String selectedType;
    private boolean isEditMode = false;
    private String reservationId = null;
    private ReservationsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateReservationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ReservationsViewModel.class);

        // Obtener argumentos
        Bundle args = getArguments();
        if (args != null) {
            isEditMode = args.getBoolean("EDIT_MODE", false);
            reservationId = args.getString("RESERVATION_ID");
        }

        // Configurar la flecha de retroceso en la toolbar
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        binding.toolbar.setTitle(isEditMode ? "Editar Reserva" : "Nueva Reserva");

        setupReservationTypes();
        setupParkingSpinners();
        setupListeners();
        observeViewModel();

        if (isEditMode) {
            fillFieldsWithReservationData();
        }
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        binding.datePickerButton.setOnClickListener(v -> showDatePicker());
        binding.startTimeButton.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeButton.setOnClickListener(v -> showTimePicker(false));
        binding.btnSaveReservation.setOnClickListener(v -> validateAndSave());
    }

    private void setupReservationTypes() {
        List<String> reservationTypes = new ArrayList<>();
        reservationTypes.add(Plaza.TIPO_STANDARD);
        reservationTypes.add(Plaza.TIPO_MOTORCYCLE);
        reservationTypes.add(Plaza.TIPO_CV_CHARGER);
        reservationTypes.add(Plaza.TIPO_DISABLED);
        adapter = new ReservationTypeAdapter(reservationTypes, this);
        binding.reservationTypeRecyclerView.setAdapter(adapter);
        binding.reservationTypeRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        if (!isEditMode) {
            selectedType = Plaza.TIPO_STANDARD;
            adapter.selectType(Plaza.TIPO_STANDARD);
            updateAvailableRows();
        }
    }

    private void setupParkingSpinners() {
        List<String> rows = new ArrayList<>();
        ArrayAdapter<String> rowAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, rows);
        rowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingRowSpinner.setAdapter(rowAdapter);
        binding.parkingRowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRow = (String) parent.getItemAtPosition(position);
                if (isValidDateAndTime() && binding.manualParkingRadioButton.isChecked()) {
                    // Llama al ViewModel para obtener los números disponibles de la fila seleccionada
                    String apiDate = DateUtils.formatDateForApi(selectedDate);
                    long startTimeMs = DateUtils.timeToMs(
                            startTime.get(Calendar.HOUR_OF_DAY),
                            startTime.get(Calendar.MINUTE));
                    long endTimeMs = DateUtils.timeToMs(
                            endTime.get(Calendar.HOUR_OF_DAY),
                            endTime.get(Calendar.MINUTE));
                    viewModel.loadAvailableNumbers(selectedType, selectedRow, apiDate, startTimeMs, endTimeMs);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        binding.parkingSelectionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            binding.parkingManualSelectionContainer.setVisibility(
                    checkedId == R.id.manualParkingRadioButton ? View.VISIBLE : View.GONE);
            if (checkedId == R.id.manualParkingRadioButton && selectedType != null) {
                updateAvailableRows();
                if (isValidDateAndTime()) {
                    // Llama al ViewModel para la primera fila seleccionada
                    if (binding.parkingRowSpinner.getSelectedItem() != null) {
                        String selectedRow = binding.parkingRowSpinner.getSelectedItem().toString();
                        String apiDate = DateUtils.formatDateForApi(selectedDate);
                        long startTimeMs = DateUtils.timeToMs(
                                startTime.get(Calendar.HOUR_OF_DAY),
                                startTime.get(Calendar.MINUTE));
                        long endTimeMs = DateUtils.timeToMs(
                                endTime.get(Calendar.HOUR_OF_DAY),
                                endTime.get(Calendar.MINUTE));
                        viewModel.loadAvailableNumbers(selectedType, selectedRow, apiDate, startTimeMs, endTimeMs);
                    }
                }
            }
        });
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 7);
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        viewModel.getUserReservationDates().observe(getViewLifecycleOwner(), reservedDates -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));
                        updateAvailableRows();
                        if (isValidDateAndTime() && binding.manualParkingRadioButton.isChecked()) {
                            loadAvailableParkingSpaces();
                        }
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                Calendar checkDate = Calendar.getInstance();
                checkDate.set(year, month, dayOfMonth);
                String dateStr = DateUtils.formatDateForApi(checkDate);
                if (reservedDates.contains(dateStr) && !isEditMode) {
                    Toast.makeText(requireContext(),
                            "Ya tienes una reserva para esta fecha", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(this::showDatePicker, 500);
                } else {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));
                    updateAvailableRows();
                    if (isValidDateAndTime() && binding.manualParkingRadioButton.isChecked()) {
                        loadAvailableParkingSpaces();
                    }
                }
            });
            datePickerDialog.show();
        });
    }

    private void showTimePicker(final boolean isStartTime) {
        Calendar calendar = isStartTime ? startTime : endTime;
        boolean isToday = DateUtils.formatDateForApi(selectedDate)
                .equals(DateUtils.getCurrentDateForApi());
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    if (isToday && isStartTime && Calendar.getInstance().after(selectedTime)) {
                        Toast.makeText(requireContext(), "No puedes seleccionar una hora pasada", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isStartTime) {
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        binding.startTimeButton.setText(DateUtils.formatTimeFromMs(
                                DateUtils.timeToMs(hourOfDay, minute)));
                        if (endTime.before(startTime)) {
                            endTime.setTimeInMillis(startTime.getTimeInMillis() + 3600000);
                            binding.endTimeButton.setText(DateUtils.formatTimeFromMs(
                                    DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY),
                                            endTime.get(Calendar.MINUTE))));
                        }
                    } else {
                        endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endTime.set(Calendar.MINUTE, minute);
                        binding.endTimeButton.setText(DateUtils.formatTimeFromMs(
                                DateUtils.timeToMs(hourOfDay, minute)));
                    }
                    validateTimeInterval();
                    if (isValidDateAndTime() && binding.manualParkingRadioButton.isChecked()) {
                        loadAvailableParkingSpaces();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        if (isToday && isStartTime) {
            Calendar now = Calendar.getInstance();
            timePickerDialog.updateTime(now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE));
        }
        timePickerDialog.show();
    }

    @Override
    public void onTypeSelected(String type) {
        selectedType = type;
        updateAvailableRows();
        if (isValidDateAndTime() && binding.manualParkingRadioButton.isChecked()) {
            loadAvailableParkingSpaces();
        }
    }

    private void updateAvailableRows() {
        List<String> availableRows = getAvailableRowsForType(selectedType);
        updateRowSpinner(availableRows);
    }

    private List<String> getAvailableRowsForType(String type) {
        List<String> rows = new ArrayList<>();
        switch (type) {
            case Plaza.TIPO_STANDARD:
                rows.add("A"); rows.add("B"); rows.add("C"); rows.add("D"); break;
            case Plaza.TIPO_MOTORCYCLE: rows.add("E"); break;
            case Plaza.TIPO_DISABLED: rows.add("F"); break;
            case Plaza.TIPO_CV_CHARGER: rows.add("G"); break;
        }
        return rows;
    }

    private void updateRowSpinner(List<String> rows) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, rows);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingRowSpinner.setAdapter(adapter);
        if (!rows.isEmpty() && binding.parkingRowSpinner.getCount() > 0) {
            binding.parkingRowSpinner.setSelection(0);
        }
    }

    private void updateNumberSpinner(List<String> numbers) {
        if (numbers.isEmpty()) {
            List<String> noAvailable = new ArrayList<>();
            noAvailable.add("No disponible");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, noAvailable);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.parkingNumberSpinner.setAdapter(adapter);
            binding.btnSaveReservation.setEnabled(false);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, numbers);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.parkingNumberSpinner.setAdapter(adapter);
            binding.btnSaveReservation.setEnabled(true);
        }
    }

    private boolean isValidDateAndTime() {
        return !binding.datePickerButton.getText().toString().equals("Seleccionar fecha") &&
                !binding.startTimeButton.getText().toString().equals("Hora inicio") &&
                !binding.endTimeButton.getText().toString().equals("Hora fin") &&
                validateTimeInterval();
    }

    private boolean validateTimeInterval() {
        boolean isValid = Validators.isValidTimeInterval(startTime, endTime);
        binding.timeWarningText.setText(Validators.getTimeIntervalMessage(startTime, endTime));
        return isValid;
    }

    private void loadAvailableParkingSpaces() {
        if (!isValidDateAndTime() || selectedType == null) return;
        binding.btnSaveReservation.setEnabled(false);
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE));
        if (binding.manualParkingRadioButton.isChecked()) {
            List<String> availableRows = getAvailableRowsForType(selectedType);
            updateRowSpinner(availableRows);
            if (binding.parkingRowSpinner.getSelectedItem() != null) {
                String selectedRow = binding.parkingRowSpinner.getSelectedItem().toString();
                viewModel.loadAvailableNumbers(selectedType, selectedRow, apiDate, startTimeMs, endTimeMs);
            }
        }
    }

    private void validateAndSave() {
        if (!validateReservationData()) return;
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        if (!isEditMode) {
            viewModel.checkUserHasReservationOnDate(apiDate).observe(getViewLifecycleOwner(), hasReservation -> {
                if (hasReservation) {
                    Toast.makeText(requireContext(), "Ya tienes una reserva para esta fecha",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (binding.randomParkingRadioButton.isChecked()) {
                    solicitarPlazaRandom(apiDate);
                } else {
                    continueWithSaveProcess(apiDate);
                }
            });
        } else {
            if (binding.randomParkingRadioButton.isChecked()) {
                solicitarPlazaRandom(apiDate);
            } else {
                continueWithSaveProcess(apiDate);
            }
        }
    }

    private void solicitarPlazaRandom(String apiDate) {
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
                Toast.makeText(requireContext(), "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireContext(), "Error al crear la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void continueWithSaveProcess(String apiDate) {
        String plazaId = getSelectedPlazaId();
        if (plazaId == null && binding.manualParkingRadioButton.isChecked()) {
            Toast.makeText(requireContext(), "Debes seleccionar una plaza disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        long startTimeMs = DateUtils.timeToMs(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE));
        Hora hora = new Hora(startTimeMs, endTimeMs);
        Plaza plaza = new Plaza(plazaId, selectedType);
        Reserva reserva = new Reserva(apiDate, "", reservationId, plaza, hora);
        if (isEditMode && reservationId != null) {
            updateExistingReservation(reserva);
        } else {
            checkAvailabilityAndCreate(reserva);
        }
    }

    private boolean validateReservationData() {
        if (!Validators.isValidReservationType(selectedType)) {
            Toast.makeText(requireContext(), "Debes seleccionar un tipo de reserva", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Validators.isValidDate(binding.datePickerButton.getText().toString())) {
            Toast.makeText(requireContext(), "Debes seleccionar una fecha", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Validators.isValidTimeSelection(
                binding.startTimeButton.getText().toString(),
                binding.endTimeButton.getText().toString())) {
            Toast.makeText(requireContext(), "Debes seleccionar el intervalo de horas", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!validateTimeInterval()) {
            Toast.makeText(requireContext(), "El intervalo de horas no es válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
        } else if (binding.randomParkingRadioButton.isChecked()) {
            return "RANDOM";
        }
        return null;
    }

    private void checkAvailabilityAndCreate(Reserva reserva) {
        if ("RANDOM".equals(reserva.getPlaza().getId())) {
            createReservation(reserva);
            return;
        }
        viewModel.checkReservationAvailability(reserva).observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result) {
                    createReservation(reserva);
                } else {
                    Toast.makeText(requireContext(), "Ya existe una reserva para esa fecha, hora o plaza",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createReservation(Reserva reserva) {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.btnSaveReservation.setEnabled(false);
        viewModel.createReservation(reserva).observe(getViewLifecycleOwner(), result -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.btnSaveReservation.setEnabled(true);
            if (result != null && result) {
                Toast.makeText(requireContext(), "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireContext(), "Error al crear la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExistingReservation(Reserva reserva) {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.btnSaveReservation.setEnabled(false);
        viewModel.updateReservation(reserva).observe(getViewLifecycleOwner(), result -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.btnSaveReservation.setEnabled(true);
            if (result != null && result) {
                Toast.makeText(requireContext(), "Reserva actualizada correctamente", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireContext(), "Error al actualizar la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillFieldsWithReservationData() {
        Bundle args = getArguments();
        if (args == null) return;
        String type = args.getString("RESERVATION_TYPE");
        String date = args.getString("RESERVATION_DATE");
        long startTimeMs = args.getLong("RESERVATION_START_TIME", 0);
        long endTimeMs = args.getLong("RESERVATION_END_TIME", 0);
        String spot = args.getString("RESERVATION_SPOT");
        adapter.selectType(type);
        selectedType = type;
        try {
            selectedDate.setTime(DateUtils.getReservaDateTime(
                    new Reserva(date, "", "", null,
                            new Hora(startTimeMs, endTimeMs))));
            binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        startTime.setTimeInMillis(startTime.getTimeInMillis() - startTime.getTimeInMillis() % (60 * 1000) + startTimeMs);
        endTime.setTimeInMillis(endTime.getTimeInMillis() - endTime.getTimeInMillis() % (60 * 1000) + endTimeMs);
        binding.startTimeButton.setText(DateUtils.formatTimeFromMs(startTimeMs));
        binding.endTimeButton.setText(DateUtils.formatTimeFromMs(endTimeMs));
        if (spot != null && !spot.isEmpty() && spot.contains("-")) {
            binding.manualParkingRadioButton.setChecked(true);
            binding.parkingManualSelectionContainer.setVisibility(View.VISIBLE);
            String[] spotParts = spot.split("-");
            if (spotParts.length == 2) {
                updateAvailableRows();
                int rowPosition = getPositionInAdapter(binding.parkingRowSpinner, spotParts[0]);
                if (rowPosition >= 0) {
                    binding.parkingRowSpinner.setSelection(rowPosition);
                    // Esperar a que se carguen los números disponibles y seleccionar el correcto
                    binding.parkingRowSpinner.post(() -> {
                        viewModel.getAvailableNumbers().observe(getViewLifecycleOwner(), numbers -> {
                            if (numbers != null && !numbers.isEmpty()) {
                                int numberPosition = getPositionInAdapter(binding.parkingNumberSpinner, spotParts[1]);
                                if (numberPosition >= 0) {
                                    binding.parkingNumberSpinner.setSelection(numberPosition);
                                }
                            }
                        });
                        // Forzar la carga de números disponibles para la fila seleccionada
                        String apiDate = DateUtils.formatDateForApi(selectedDate);
                        long startMs = DateUtils.timeToMs(
                                startTime.get(Calendar.HOUR_OF_DAY),
                                startTime.get(Calendar.MINUTE));
                        long endMs = DateUtils.timeToMs(
                                endTime.get(Calendar.HOUR_OF_DAY),
                                endTime.get(Calendar.MINUTE));
                        viewModel.loadAvailableNumbers(selectedType, spotParts[0], apiDate, startMs, endMs);
                    });
                }
            }
        } else {
            binding.randomParkingRadioButton.setChecked(true);
            binding.parkingManualSelectionContainer.setVisibility(View.GONE);
        }
    }

    private int getPositionInAdapter(Spinner spinner, String value) {
        ArrayAdapter<?> arrayAdapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            if (arrayAdapter.getItem(i).toString().equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private void observeViewModel() {
        viewModel.getAvailableNumbers().observe(getViewLifecycleOwner(), numbers -> {
            if (binding.manualParkingRadioButton.isChecked()) {
                updateNumberSpinner(numbers);
                binding.loadingIndicator.setVisibility(View.GONE);
            }
        });
        viewModel.getRandomPlaza().observe(getViewLifecycleOwner(), plazaId -> {
            if (plazaId != null) {
                // Crear reserva con la plaza random asignada
                continueWithSaveProcessRandom(plazaId);
                // Limpiar el valor para evitar ejecuciones automáticas
                viewModel.clearRandomPlaza();
            }
            // No mostrar mensaje si plazaId es null y el loading está oculto (ya se consumió)
            else if (binding.loadingIndicator.getVisibility() == View.VISIBLE) {
                Toast.makeText(requireContext(), "No se pudo asignar una plaza aleatoria", Toast.LENGTH_SHORT).show();
                binding.loadingIndicator.setVisibility(View.GONE);
            }
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
