package com.lksnext.parkingplantilla.view.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.adapters.ReservationTypeAdapter;
import com.lksnext.parkingplantilla.databinding.ActivityCreateReservationBinding;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.utils.DateUtils;
import com.lksnext.parkingplantilla.utils.Validators;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateReservationActivity extends AppCompatActivity
        implements ReservationTypeAdapter.OnTypeSelectedListener {

    private ActivityCreateReservationBinding binding;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private ReservationTypeAdapter adapter;
    private String selectedType;
    private boolean isEditMode = false;
    private String reservationId = null;

    private ReservationsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateReservationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReservationsViewModel.class);

        // Configurar la barra de herramientas
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Comprobar si estamos en modo edición
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        reservationId = getIntent().getStringExtra("RESERVATION_ID");

        if (isEditMode) {
            getSupportActionBar().setTitle("Editar Reserva");
        } else {
            getSupportActionBar().setTitle("Nueva Reserva");
        }

        // Inicializar componentes
        setupReservationTypes();
        setupParkingSpinners();
        setupListeners();

        // Si estamos en modo edición, rellenamos los campos con los datos de la reserva
        if (isEditMode) {
            fillFieldsWithReservationData();
        }
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.datePickerButton.setOnClickListener(v -> showDatePicker());
        binding.startTimeButton.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeButton.setOnClickListener(v -> showTimePicker(false));
        binding.btnSaveReservation.setOnClickListener(v -> validateAndSave());
    }

    private void setupReservationTypes() {
        // Crear lista de tipos de reserva con las constantes correctas
        List<String> reservationTypes = new ArrayList<>();
        reservationTypes.add(Plaza.TIPO_STANDARD);
        reservationTypes.add(Plaza.TIPO_MOTORCYCLE);
        reservationTypes.add(Plaza.TIPO_CV_CHARGER);
        reservationTypes.add(Plaza.TIPO_DISABLED);

        // Inicializar el adaptador con this como listener
        adapter = new ReservationTypeAdapter(reservationTypes, this);
        binding.reservationTypeRecyclerView.setAdapter(adapter);
        binding.reservationTypeRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Seleccionar tipo Standard por defecto (solo si no estamos en modo edición)
        if (!isEditMode) {
            selectedType = Plaza.TIPO_STANDARD;
            adapter.selectType(Plaza.TIPO_STANDARD);
            updateAvailableRows();
        }
    }

    private void setupParkingSpinners() {
        // Configurar spinner de filas inicialmente vacío
        List<String> rows = new ArrayList<>();
        ArrayAdapter<String> rowAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rows);
        rowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingRowSpinner.setAdapter(rowAdapter);

        // Listener para cuando cambia la fila
        binding.parkingRowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRow = (String) parent.getItemAtPosition(position);

                // Si tenemos fecha y hora válidas, verificar números disponibles
                if (isValidDateAndTime()) {
                    String apiDate = DateUtils.formatDateForApi(selectedDate);
                    long startTimeMs = DateUtils.timeToMs(
                            startTime.get(Calendar.HOUR_OF_DAY),
                            startTime.get(Calendar.MINUTE));
                    long endTimeMs = DateUtils.timeToMs(
                            endTime.get(Calendar.HOUR_OF_DAY),
                            endTime.get(Calendar.MINUTE));
                    checkAvailableNumbers(apiDate, startTimeMs, endTimeMs, selectedRow);
                } else {
                    // Si no hay fecha/hora válida, mostrar todos los números
                    updateParkingNumbers(selectedRow);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        // Mostrar/ocultar spinners según la opción seleccionada
        binding.parkingSelectionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            binding.parkingManualSelectionContainer.setVisibility(
                    checkedId == R.id.manualParkingRadioButton ? View.VISIBLE : View.GONE);

            // Si se selecciona manual y hay un tipo de reserva seleccionado, actualizar filas
            if (checkedId == R.id.manualParkingRadioButton && selectedType != null) {
                updateAvailableRows();

                // Si ya hay fecha y hora válida, cargar espacios disponibles
                if (isValidDateAndTime()) {
                    loadAvailableParkingSpaces();
                }
            }
        });
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 7);

        // Primero, cargar las fechas con reservas existentes
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        viewModel.getUserReservationDates().observe(this, reservedDates -> {
            binding.loadingIndicator.setVisibility(View.GONE);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        // Si la fecha es válida, actualizar la selección
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));

                        // Actualizar plazas disponibles
                        updateAvailableRows();
                        if (isValidDateAndTime() && binding.manualParkingRadioButton.isChecked()) {
                            loadAvailableParkingSpaces();
                        }
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );

            // Establecer límites de fechas
            datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

            // Verificar qué fechas no se deben seleccionar
            datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                Calendar checkDate = Calendar.getInstance();
                checkDate.set(year, month, dayOfMonth);
                String dateStr = DateUtils.formatDateForApi(checkDate);

                if (reservedDates.contains(dateStr) && !isEditMode) {
                    // Si ya tiene reserva y no es modo edición, mostrar mensaje
                    Toast.makeText(CreateReservationActivity.this,
                            "Ya tienes una reserva para esta fecha", Toast.LENGTH_SHORT).show();

                    // Reabrir el selector de fechas
                    new android.os.Handler().postDelayed(() -> showDatePicker(), 500);
                } else {
                    // Fecha válida, actualizar selección
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));

                    // Actualizar plazas disponibles
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

        // Verificar si la fecha seleccionada es hoy
        boolean isToday = DateUtils.formatDateForApi(selectedDate)
                .equals(DateUtils.getCurrentDateForApi());

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // Crear un Calendar temporal para validar
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);

                    // Si es hoy y la hora seleccionada es anterior a la actual
                    if (isToday && isStartTime && Calendar.getInstance().after(selectedTime)) {
                        Toast.makeText(this, "No puedes seleccionar una hora pasada", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isStartTime) {
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        binding.startTimeButton.setText(DateUtils.formatTimeFromMs(
                                DateUtils.timeToMs(hourOfDay, minute)));

                        // Si la hora fin es anterior a la hora inicio, actualizarla
                        if (endTime.before(startTime)) {
                            // Establecer hora fin 1 hora después de la inicio
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

                    // Actualizar plazas disponibles cuando cambian fecha u hora
                    if (isValidDateAndTime() && binding.manualParkingRadioButton.isChecked()) {
                        loadAvailableParkingSpaces();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        // Si es hoy, establecer hora mínima a la hora actual
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
        // Actualizar filas disponibles según el tipo seleccionado
        updateAvailableRows();

        // Si ya hay fecha y hora válida, actualizar plazas disponibles
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
                rows.add("A");
                rows.add("B");
                rows.add("C");
                rows.add("D");
                break;
            case Plaza.TIPO_MOTORCYCLE:
                rows.add("E");
                break;
            case Plaza.TIPO_DISABLED:
                rows.add("F");
                break;
            case Plaza.TIPO_CV_CHARGER:
                rows.add("G");
                break;
        }

        return rows;
    }

    private void updateRowSpinner(List<String> rows) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rows);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingRowSpinner.setAdapter(adapter);

        // Si hay filas disponibles, seleccionar la primera
        if (!rows.isEmpty() && binding.parkingRowSpinner.getCount() > 0) {
            binding.parkingRowSpinner.setSelection(0);
        }
    }

    private void updateParkingNumbers(String row) {
        List<String> numbers = new ArrayList<>();

        // Según la fila, determinamos cuántos números hay
        int maxNumber;
        switch (row) {
            case "F": maxNumber = 5; break;  // Menos plazas para discapacitados
            case "E": maxNumber = 8; break;  // Plazas para motos
            case "G": maxNumber = 6; break;  // Cargadores eléctricos
            default: maxNumber = 10; break;  // Número estándar para otras filas
        }

        // Generar lista de números
        for (int i = 1; i <= maxNumber; i++) {
            numbers.add(String.valueOf(i));
        }

        // Actualizar el spinner
        updateNumberSpinner(numbers);
    }

    private void updateNumberSpinner(List<String> numbers) {
        if (numbers.isEmpty()) {
            // Si no hay números disponibles, mostrar mensaje
            List<String> noAvailable = new ArrayList<>();
            noAvailable.add("No disponible");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, noAvailable);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.parkingNumberSpinner.setAdapter(adapter);
            binding.btnSaveReservation.setEnabled(false);
        } else {
            // Actualizar con números disponibles
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
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
        if (!isValidDateAndTime() || selectedType == null) {
            return;
        }

        binding.btnSaveReservation.setEnabled(false);
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // Crear reserva temporal para verificar disponibilidad
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE));

        // Si está en selección manual, actualizar las plazas disponibles
        if (binding.manualParkingRadioButton.isChecked()) {
            // Verificar primera la disponibilidad de filas según tipo seleccionado
            List<String> availableRows = getAvailableRowsForType(selectedType);
            updateRowSpinner(availableRows);

            // Si hay una fila seleccionada, verificar números disponibles
            if (binding.parkingRowSpinner.getSelectedItem() != null) {
                String selectedRow = binding.parkingRowSpinner.getSelectedItem().toString();
                checkAvailableNumbers(apiDate, startTimeMs, endTimeMs, selectedRow);
            }
        }
    }

    private void checkAvailableNumbers(String date, long startTimeMs, long endTimeMs, String row) {
        List<String> allNumbers = getNumbersForRow(row);
        List<String> availableNumbers = new ArrayList<>();

        // Mostrar un indicador de carga
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // Contador para seguimiento de verificaciones completadas
        final int[] completedChecks = {0};
        final int totalChecks = allNumbers.size();

        for (String number : allNumbers) {
            String plazaId = row + "-" + number;

            // Verificar disponibilidad
            viewModel.checkReservationAvailability(date, startTimeMs, endTimeMs, selectedType, plazaId)
                    .observe(this, isAvailable -> {
                        completedChecks[0]++;

                        if (Boolean.TRUE.equals(isAvailable)) {
                            availableNumbers.add(number);
                        }

                        // Cuando se completan todas las verificaciones
                        if (completedChecks[0] >= totalChecks) {
                            runOnUiThread(() -> {
                                updateNumberSpinner(availableNumbers);
                                binding.loadingIndicator.setVisibility(View.GONE);

                                if (availableNumbers.isEmpty()) {
                                    Toast.makeText(CreateReservationActivity.this,
                                            "No hay plazas disponibles para la selección actual",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        }
    }

    private List<String> getNumbersForRow(String row) {
        List<String> numbers = new ArrayList<>();
        int maxNumber;

        switch (row) {
            case "F": maxNumber = 5; break;  // Menos plazas para discapacitados
            case "E": maxNumber = 8; break;  // Plazas para motos
            case "G": maxNumber = 6; break;  // Cargadores eléctricos
            default: maxNumber = 10; break;  // Número estándar para otras filas
        }

        for (int i = 1; i <= maxNumber; i++) {
            numbers.add(String.valueOf(i));
        }

        return numbers;
    }

    private void validateAndSave() {
        if (!validateReservationData()) {
            return;
        }

        String apiDate = DateUtils.formatDateForApi(selectedDate);

        // Verificar si el usuario ya tiene una reserva para esta fecha (solo para nuevas reservas)
        if (!isEditMode) {
            viewModel.checkUserHasReservationOnDate(apiDate).observe(this, hasReservation -> {
                if (hasReservation) {
                    Toast.makeText(this, "Ya tienes una reserva para esta fecha",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Si no tiene reserva para esta fecha, continuar con la creación
                continueWithSaveProcess(apiDate);
            });
        } else {
            // En modo edición, permitimos actualizar la reserva existente
            continueWithSaveProcess(apiDate);
        }
    }

    private void continueWithSaveProcess(String apiDate) {
        String plazaId = getSelectedPlazaId();
        if (plazaId == null && binding.manualParkingRadioButton.isChecked()) {
            Toast.makeText(this, "Debes seleccionar una plaza disponible", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Debes seleccionar un tipo de reserva", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Validators.isValidDate(binding.datePickerButton.getText().toString())) {
            Toast.makeText(this, "Debes seleccionar una fecha", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Validators.isValidTimeSelection(
                binding.startTimeButton.getText().toString(),
                binding.endTimeButton.getText().toString())) {
            Toast.makeText(this, "Debes seleccionar el intervalo de horas", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!validateTimeInterval()) {
            Toast.makeText(this, "El intervalo de horas no es válido", Toast.LENGTH_SHORT).show();
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
            // Para selección aleatoria, el backend asignará la plaza
            return "RANDOM";
        }
        return null;
    }

    private void checkAvailabilityAndCreate(Reserva reserva) {
        // Para selección aleatoria, no necesitamos verificar disponibilidad
        if ("RANDOM".equals(reserva.getPlaza().getId())) {
            createReservation(reserva);
            return;
        }

        // Verificar disponibilidad usando el objeto Reserva directamente
        viewModel.checkReservationAvailability(reserva).observe(this, result -> {
            if (result != null) {
                if (result) {
                    // Disponible, crear la reserva
                    createReservation(reserva);
                } else {
                    // No disponible
                    Toast.makeText(this, "Ya existe una reserva para esa fecha, hora o plaza",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createReservation(Reserva reserva) {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.btnSaveReservation.setEnabled(false);

        viewModel.createReservation(reserva).observe(this, result -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.btnSaveReservation.setEnabled(true);

            if (result != null && result) {
                Toast.makeText(this, "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al crear la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExistingReservation(Reserva reserva) {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.btnSaveReservation.setEnabled(false);

        viewModel.updateReservation(reserva).observe(this, result -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.btnSaveReservation.setEnabled(true);

            if (result != null && result) {
                Toast.makeText(this, "Reserva actualizada correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillFieldsWithReservationData() {
        // Obtener datos de la reserva
        String type = getIntent().getStringExtra("RESERVATION_TYPE");
        String date = getIntent().getStringExtra("RESERVATION_DATE");
        long startTimeMs = getIntent().getLongExtra("RESERVATION_START_TIME", 0);
        long endTimeMs = getIntent().getLongExtra("RESERVATION_END_TIME", 0);
        String spot = getIntent().getStringExtra("RESERVATION_SPOT");

        // Seleccionar tipo
        adapter.selectType(type);
        selectedType = type;

        // Configurar fecha usando DateUtils
        try {
            selectedDate.setTime(DateUtils.getReservaDateTime(
                    new Reserva(date, "", "", null,
                            new Hora(startTimeMs, endTimeMs))));
            binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configurar horas usando DateUtils
        startTime.setTimeInMillis(startTime.getTimeInMillis() - startTime.getTimeInMillis() % (60 * 1000) + startTimeMs);
        endTime.setTimeInMillis(endTime.getTimeInMillis() - endTime.getTimeInMillis() % (60 * 1000) + endTimeMs);
        binding.startTimeButton.setText(DateUtils.formatTimeFromMs(startTimeMs));
        binding.endTimeButton.setText(DateUtils.formatTimeFromMs(endTimeMs));

        // Configurar plaza
        if (spot != null && !spot.isEmpty() && spot.contains("-")) {
            binding.manualParkingRadioButton.setChecked(true);
            binding.parkingManualSelectionContainer.setVisibility(View.VISIBLE);
            String[] spotParts = spot.split("-");
            if (spotParts.length == 2) {
                updateAvailableRows();

                // Seleccionar fila y número en los spinners
                int rowPosition = getPositionInAdapter(binding.parkingRowSpinner, spotParts[0]);
                if (rowPosition >= 0) {
                    binding.parkingRowSpinner.setSelection(rowPosition);

                    // Actualizar números después de seleccionar la fila
                    updateParkingNumbers(spotParts[0]);

                    int numberPosition = getPositionInAdapter(binding.parkingNumberSpinner, spotParts[1]);
                    if (numberPosition >= 0) {
                        binding.parkingNumberSpinner.setSelection(numberPosition);
                    }
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
}