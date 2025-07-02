package com.lksnext.parkingplantilla.view.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
    private static final String NO_DISPONIBLE = "No disponible";

    private FragmentCreateReservationBinding binding;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private ReservationTypeAdapter adapter;
    private String selectedType;
    private boolean isEditMode = false;
    private String reservationId = null;
    private ReservationsViewModel viewModel;
    private String selectedRowManual = null;
    private String selectedNumberManual = null;
    private List<String> availablePlazas = new ArrayList<>();
    private String returnFragmentTag = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateReservationBinding.inflate(inflater, container, false);
        binding.spotSelectionContainer.setVisibility(View.GONE); // Oculto por defecto
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ReservationsViewModel.class);
        setupToolbar();
        binding.btnNextStep.setEnabled(false);
        binding.btnNextStep.setAlpha(0.5f);
        binding.btnNextStep.setClickable(false);
        setupReservationTypes();
        restoreTempSelectionIfExists();
        handleArguments(getArguments());
        setupListeners();
        setupSpotSelectionUI();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> {
            // Limpiar selección temporal SOLO al salir del flujo de crear reserva
            viewModel.clearTempSelection();
            NavController navController = Navigation.findNavController(requireActivity(), R.id.flFragment);
            navController.popBackStack(R.id.mainFragment, false);
        });
        binding.toolbar.setTitle(isEditMode ? "Editar Reserva" : "Nueva Reserva");
    }

    private void restoreTempSelectionIfExists() {
        if (viewModel.getTempSelectedType() != null) {
            selectedType = viewModel.getTempSelectedType();
            Calendar tempDate = viewModel.getTempSelectedDate();
            Calendar tempStart = viewModel.getTempStartTime();
            Calendar tempEnd = viewModel.getTempEndTime();
            if (tempDate != null) selectedDate = (Calendar) tempDate.clone();
            if (tempStart != null) startTime = (Calendar) tempStart.clone();
            if (tempEnd != null) endTime = (Calendar) tempEnd.clone();
            reservationId = viewModel.getTempReservationId();
            updateDateTimeUI();
            if (adapter != null) adapter.selectType(selectedType);
            checkAvailableSpots();
        }
    }

    private void updateDateTimeUI() {
        binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));
        binding.startTimeButton.setText(DateUtils.formatTimeFromMs(DateUtils.timeToMs(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE))));
        binding.endTimeButton.setText(DateUtils.formatTimeFromMs(DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE))));
    }

    private void handleArguments(Bundle args) {
        if (args == null) return;
        isEditMode = args.getBoolean("EDIT_MODE", false);
        reservationId = args.getString("RESERVATION_ID");
        returnFragmentTag = args.getString("RETURN_FRAGMENT_TAG", "mainFragment");
        if (isEditMode) {
            selectedType = args.getString("RESERVATION_TYPE");
            String dateStr = args.getString("RESERVATION_DATE");
            long startMs = args.getLong("RESERVATION_START_TIME", -1);
            long endMs = args.getLong("RESERVATION_END_TIME", -1);
            if (selectedType != null && adapter != null) {
                adapter.selectType(selectedType);
            }
            if (dateStr != null) {
                selectedDate = DateUtils.parseDateForApiToCalendar(dateStr);
                binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));
            }
            if (startMs != -1) {
                startTime = DateUtils.combineDateAndTimeMs(selectedDate, startMs);
                binding.startTimeButton.setText(DateUtils.formatTimeFromMs(startMs));
            }
            if (endMs != -1) {
                endTime = DateUtils.combineDateAndTimeMs(selectedDate, endMs);
                binding.endTimeButton.setText(DateUtils.formatTimeFromMs(endMs));
            }
            checkAvailableSpots();
        }
    }

    private void setupListeners() {
        binding.datePickerButton.setOnClickListener(v -> showDatePicker());
        binding.startTimeButton.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeButton.setOnClickListener(v -> showTimePicker(false));
        binding.btnNextStep.setOnClickListener(v -> {
            // Solo permitir avanzar si el botón está habilitado
            if (binding.btnNextStep.isEnabled()) {
                onNextStep();
            }
        });
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
        }
    }

    @Override
    public void onTypeSelected(String type) {
        selectedType = type;
        checkAvailableSpots();
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 7);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));
                    checkAvailableSpots();
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(final boolean isStartTime) {
        Calendar calendar = isStartTime ? startTime : endTime;
        boolean isToday = DateUtils.formatDateForApi(selectedDate)
                .equals(DateUtils.getCurrentDateForApi());
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    Calendar now = Calendar.getInstance();
                    now.set(Calendar.SECOND, 0);
                    now.set(Calendar.MILLISECOND, 0);
                    if (isStartTime) {
                        Calendar tempStart = (Calendar) selectedDate.clone();
                        tempStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        tempStart.set(Calendar.MINUTE, minute);
                        tempStart.set(Calendar.SECOND, 0);
                        tempStart.set(Calendar.MILLISECOND, 0);
                        if (tempStart.before(now)) {
                            Toast.makeText(requireContext(), "La hora de inicio no puede ser anterior a la actual", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        startTime.set(Calendar.SECOND, 0);
                        startTime.set(Calendar.MILLISECOND, 0);
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
                        endTime.set(Calendar.SECOND, 0);
                        endTime.set(Calendar.MILLISECOND, 0);
                        binding.endTimeButton.setText(DateUtils.formatTimeFromMs(
                                DateUtils.timeToMs(hourOfDay, minute)));
                    }
                    validateTimeInterval();
                    checkAvailableSpots();
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

    private void checkAvailableSpots() {
        if (!isValidDateAndTime() || selectedType == null) {
            updateAvailableSpotsUI("", false);
            return;
        }
        updateAvailablePlazas(); // <-- Actualiza availablePlazas cada vez que cambian los filtros
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        viewModel.checkUserHasReservationOnDate(apiDate).observe(getViewLifecycleOwner(), hasReservation -> {
            if (hasReservation != null && hasReservation && !(isEditMode && reservationId != null)) {
                updateAvailableSpotsUI("Ya tienes una reserva para este día", false);
            } else {
                fetchAndDisplayAvailableSpots(apiDate);
            }
        });
    }

    private void updateAvailablePlazas() {
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
        if (isEditMode && reservationId != null) {
            viewModel.getAvailablePlazas(selectedType, apiDate, startTimeMs, endTimeMs, reservationId).observe(getViewLifecycleOwner(), plazas -> availablePlazas = plazas != null ? plazas : new ArrayList<>());
        } else {
            viewModel.getAvailablePlazas(selectedType, apiDate, startTimeMs, endTimeMs).observe(getViewLifecycleOwner(), plazas -> availablePlazas = plazas != null ? plazas : new ArrayList<>());
        }
    }

    private void fetchAndDisplayAvailableSpots(String apiDate) {
        long startTimeMs = DateUtils.timeToMs(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
        if (isEditMode && reservationId != null) {
            viewModel.getAvailablePlazas(selectedType, apiDate, startTimeMs, endTimeMs, reservationId)
                .observe(getViewLifecycleOwner(), this::handleAvailablePlazasResult);
        } else {
            viewModel.getAvailablePlazas(selectedType, apiDate, startTimeMs, endTimeMs)
                .observe(getViewLifecycleOwner(), this::handleAvailablePlazasResult);
        }
    }

    private void handleAvailablePlazasResult(List<String> plazas) {
        if (plazas != null && !plazas.isEmpty()) {
            updateAvailableSpotsUI("Plazas disponibles: " + plazas.size(), true);
        } else {
            updateAvailableSpotsUI("No hay plazas disponibles para esta fecha y hora", false);
        }
    }

    private void updateAvailableSpotsUI(String message, boolean enableNextStep) {
        binding.availableSpotsText.setText(message);
        binding.btnNextStep.setEnabled(enableNextStep);
        binding.btnNextStep.setAlpha(enableNextStep ? 1.0f : 0.5f);
        // Deshabilitar el click si no está habilitado
        binding.btnNextStep.setClickable(enableNextStep);
    }

    private void onNextStep() {
        // Validar margen de 2 minutos respecto a la hora actual
        Calendar now = Calendar.getInstance();
        Calendar minStart = (Calendar) now.clone();
        minStart.add(Calendar.MINUTE, -2);
        if (startTime.before(minStart)) {
            Toast.makeText(requireContext(), "La hora de inicio no puede ser más de 2 minutos anterior al momento actual", Toast.LENGTH_LONG).show();
            return;
        }
        // Guardar selección temporal antes de navegar
        viewModel.saveTempSelection(selectedType, selectedDate, startTime, endTime, reservationId);
        showStep2();
    }

    private void setupSpotSelectionUI() {
        // Inicializa la UI de selección de plaza (manual/aleatoria)
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
        binding.btnSaveReservation.setOnClickListener(v -> validateAndSaveSpotSelection());
        // Quitar botón atrás y usar la flecha del toolbar
        binding.btnBackToStep1.setVisibility(View.GONE);
    }

    private void setupParkingSpinners() {
        // Solo observar filas, los números se filtran localmente
        viewModel.getAvailableRows().observe(getViewLifecycleOwner(), this::updateRowSpinner);
        // Observar la lista completa de plazas disponibles para filtrar números
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
        if (isEditMode && reservationId != null) {
            viewModel.getAvailablePlazas(selectedType, apiDate, startTimeMs, endTimeMs, reservationId).observe(getViewLifecycleOwner(), plazas -> availablePlazas = plazas != null ? plazas : new ArrayList<>());
        } else {
            viewModel.getAvailablePlazas(selectedType, apiDate, startTimeMs, endTimeMs).observe(getViewLifecycleOwner(), plazas -> availablePlazas = plazas != null ? plazas : new ArrayList<>());
        }
    }

    private void updateRowSpinner(List<String> rows) {
        List<String> displayRows = getDisplayRows(rows);
        binding.parkingRowSpinner.setEnabled(!displayRows.isEmpty() && !NO_DISPONIBLE.equals(displayRows.get(0)));
        setRowSpinnerAdapter(displayRows);
        preselectRowSpinner(displayRows);
        binding.parkingRowSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedRowSpinner = (String) parent.getItemAtPosition(position);
                if (selectedRowSpinner != null && !selectedRowSpinner.equals(NO_DISPONIBLE)) {
                    updateNumberSpinner(getFilteredNumbers(selectedRowSpinner));
                } else {
                    updateNumberSpinner(new ArrayList<>());
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private List<String> getDisplayRows(List<String> rows) {
        List<String> displayRows = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            displayRows.add(NO_DISPONIBLE);
        } else {
            displayRows.addAll(rows);
        }
        return displayRows;
    }

    private void setRowSpinnerAdapter(List<String> displayRows) {
        android.widget.ArrayAdapter<String> rowSpinnerAdapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, displayRows);
        rowSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingRowSpinner.setAdapter(rowSpinnerAdapter);
    }

    private void preselectRowSpinner(List<String> displayRows) {
        if (isEditMode && selectedRowManual != null && displayRows.contains(selectedRowManual)) {
            binding.parkingRowSpinner.setSelection(displayRows.indexOf(selectedRowManual));
        } else if (!displayRows.isEmpty() && binding.parkingRowSpinner.getCount() > 0) {
            binding.parkingRowSpinner.setSelection(0);
        }
    }

    private List<String> getFilteredNumbers(String selectedRowSpinner) {
        List<String> filteredNumbers = new ArrayList<>();
        for (String plazaId : availablePlazas) {
            String[] parts = plazaId.split("-");
            if (parts.length == 2 && parts[0].equals(selectedRowSpinner)) {
                filteredNumbers.add(parts[1]);
            }
        }
        return filteredNumbers;
    }

    private void updateNumberSpinner(List<String> numbers) {
        List<String> displayNumbers = new ArrayList<>();
        if (numbers == null || numbers.isEmpty()) {
            if (binding.parkingRowSpinner.getSelectedItem() == null ||
                NO_DISPONIBLE.equals(binding.parkingRowSpinner.getSelectedItem().toString())) {
                displayNumbers.add("Selecciona fila para ver números");
            } else {
                displayNumbers.add(NO_DISPONIBLE);
            }
            binding.parkingNumberSpinner.setEnabled(false);
            binding.btnSaveReservation.setEnabled(false);
        } else {
            displayNumbers.addAll(numbers);
            binding.parkingNumberSpinner.setEnabled(true);
            binding.btnSaveReservation.setEnabled(true);
        }
        android.widget.ArrayAdapter<String> numberSpinnerAdapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, displayNumbers);
        numberSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingNumberSpinner.setAdapter(numberSpinnerAdapter);
        // Preseleccionar número si es edición
        if (isEditMode && selectedNumberManual != null && displayNumbers.contains(selectedNumberManual)) {
            binding.parkingNumberSpinner.setSelection(displayNumbers.indexOf(selectedNumberManual));
        } else if (!displayNumbers.isEmpty() && binding.parkingNumberSpinner.getCount() > 0) {
            binding.parkingNumberSpinner.setSelection(0);
        }
    }

    private void showStep1() {
        binding.step1Container.setVisibility(View.VISIBLE);
        binding.spotSelectionContainer.setVisibility(View.GONE);
        binding.btnSaveReservation.setVisibility(View.GONE);
        binding.btnNextStep.setVisibility(View.VISIBLE);
        binding.toolbar.setTitle(isEditMode ? "Editar Reserva" : "Nueva Reserva");
        // Cambiar icono de la toolbar para volver atrás según modo
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> {
            // Limpiar selección temporal SOLO al salir del flujo de crear reserva
            viewModel.clearTempSelection();
            NavController navController = Navigation.findNavController(requireActivity(), R.id.flFragment);
            navController.popBackStack(R.id.mainFragment, false);
        });
    }

    private void showStep2() {
        binding.step1Container.setVisibility(View.GONE);
        binding.spotSelectionContainer.setVisibility(View.VISIBLE);
        binding.btnSaveReservation.setVisibility(View.VISIBLE);
        binding.btnNextStep.setVisibility(View.GONE);
        binding.toolbar.setTitle("Seleccionar plaza");
        // Cambiar icono de la toolbar para volver al paso 1
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> showStep1());
        // Cargar filas y números disponibles
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
        if (isEditMode && reservationId != null) {
            preselectManualSpotIfEditing();
            viewModel.loadAvailablePlazasAndExtractRowsNumbers(selectedType, apiDate, startTimeMs, endTimeMs, reservationId);
        } else {
            setRandomSpotSelectionUI();
            viewModel.loadAvailablePlazasAndExtractRowsNumbers(selectedType, apiDate, startTimeMs, endTimeMs);
        }
        // Cambiar texto del botón según modo
        binding.btnSaveReservation.setText(isEditMode ? "Actualizar reserva" : "Crear reserva");
    }

    private void preselectManualSpotIfEditing() {
        Reserva reservaEdit = getReservaEdit();
        if (reservaEdit != null && reservaEdit.getPlaza() != null) {
            String plazaId = reservaEdit.getPlaza().getId();
            if (plazaId != null && plazaId.contains("-")) {
                String[] parts = plazaId.split("-");
                selectedRowManual = parts[0];
                selectedNumberManual = parts[1];
            }
        }
        binding.manualParkingRadioButton.setChecked(true);
        binding.parkingManualSelectionContainer.setVisibility(View.VISIBLE);
    }

    private Reserva getReservaEdit() {
        List<Reserva> reservas = viewModel.getReservations().getValue();
        if (reservas != null) {
            for (Reserva r : reservas) {
                if (reservationId.equals(r.getId())) {
                    return r;
                }
            }
        }
        return null;
    }

    private void setRandomSpotSelectionUI() {
        binding.randomParkingRadioButton.setChecked(true);
        binding.parkingManualSelectionContainer.setVisibility(View.GONE);
    }

    private void validateAndSaveSpotSelection() {
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
        long startTimeMs = DateUtils.timeToMs(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
        if (isEditMode && reservationId != null) {
            viewModel.assignRandomPlaza(selectedType, apiDate, startTimeMs, endTimeMs, reservationId);
        } else {
            viewModel.assignRandomPlaza(selectedType, apiDate, startTimeMs, endTimeMs);
        }
        viewModel.getRandomPlaza().observe(getViewLifecycleOwner(), plazaId -> {
            if (plazaId != null) {
                continueWithSaveProcess(plazaId);
                viewModel.clearRandomPlaza();
            }
        });
    }

    private void continueWithSaveProcess(String plazaId) {
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
        Hora hora = new Hora(startTimeMs, endTimeMs);
        Plaza plaza = new Plaza(plazaId, selectedType);
        Reserva reserva = new Reserva(apiDate, "", reservationId, plaza, hora);
        if (isEditMode) {
            viewModel.updateReservation(reserva).observe(getViewLifecycleOwner(), result -> {
                if (result != null && result) {
                    viewModel.clearTempSelection();
                    Toast.makeText(requireContext(), "Reserva actualizada correctamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireActivity(), R.id.flFragment).popBackStack(getReturnFragmentId(), false);
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar la reserva", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            viewModel.createReservation(reserva).observe(getViewLifecycleOwner(), result -> {
                if (result != null && result) {
                    viewModel.clearTempSelection();
                    Toast.makeText(requireContext(), "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireActivity(), R.id.flFragment).popBackStack(getReturnFragmentId(), false);
                } else {
                    Toast.makeText(requireContext(), "Error al crear la reserva", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private int getReturnFragmentId() {
        if ("reservationsFragment".equals(returnFragmentTag)) {
            return R.id.reservationsFragment;
        } else {
            return R.id.mainFragment;
        }
    }

    private String getSelectedPlazaId() {
        if (binding.manualParkingRadioButton.isChecked() &&
                binding.parkingRowSpinner.getSelectedItem() != null &&
                binding.parkingNumberSpinner.getSelectedItem() != null) {
            String selectedNumberStr = binding.parkingNumberSpinner.getSelectedItem().toString();
            if (NO_DISPONIBLE.equals(selectedNumberStr)) {
                return null;
            }
            String row = binding.parkingRowSpinner.getSelectedItem().toString();
            return row + "-" + selectedNumberStr;
        }
        return null;
    }

    private void observeViewModel() {
        // Aquí puedes observar errores o loading si lo deseas
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });

        // Observa el estado de loading para mostrar/ocultar el ProgressBar y desactivar botones
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSaveReservation.setEnabled(false);
                binding.btnNextStep.setEnabled(false);
                binding.btnNextStep.setAlpha(0.5f);
                binding.btnNextStep.setClickable(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveReservation.setEnabled(true);
            }
        });
    }
}
