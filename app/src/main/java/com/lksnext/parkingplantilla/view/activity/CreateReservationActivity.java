package com.lksnext.parkingplantilla.view.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.adapters.ReservationTypeAdapter;
import com.lksnext.parkingplantilla.databinding.ActivityCreateReservationBinding;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.utils.Validators;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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


    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateReservationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar la barra de herramientas
        setSupportActionBar(binding.toolbar);
        // Habilitar el botón de retroceso
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        reservationId = getIntent().getStringExtra("RESERVATION_ID");

        if (isEditMode) {
            getSupportActionBar().setTitle("Editar Reserva");
        } else {
            getSupportActionBar().setTitle("Nueva Reserva");
        }

        // Primero inicializamos todos los componentes
        setupReservationTypes();
        setupParkingSpinners();
        setupListeners();

        // Después de inicializar los componentes, si estamos en modo edición
        // rellenamos los campos con los datos de la reserva
        if (isEditMode) {
            fillFieldsWithReservationData();
        }
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

        // Configurar fecha
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate.setTime(apiFormat.parse(date));
            binding.datePickerButton.setText(dateFormat.format(selectedDate.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configurar horas
        startTime.setTimeInMillis(startTime.getTimeInMillis() - startTime.getTimeInMillis() % (60 * 1000) + startTimeMs);
        endTime.setTimeInMillis(endTime.getTimeInMillis() - endTime.getTimeInMillis() % (60 * 1000) + endTimeMs);
        binding.startTimeButton.setText(timeFormat.format(startTime.getTime()));
        binding.endTimeButton.setText(timeFormat.format(endTime.getTime()));

        // Configurar plaza
        if (spot != null && !spot.isEmpty() && spot.contains("-")) {
            binding.manualParkingRadioButton.setChecked(true);
            String[] spotParts = spot.split("-");
            if (spotParts.length == 2) {
                // Seleccionar fila y número en los spinners
                int rowPosition = getPositionInAdapter(binding.parkingRowSpinner, spotParts[0]);
                int numberPosition = getPositionInAdapter(binding.parkingNumberSpinner, spotParts[1]);

                if (rowPosition >= 0) binding.parkingRowSpinner.setSelection(rowPosition);
                if (numberPosition >= 0) binding.parkingNumberSpinner.setSelection(numberPosition);
            }
        } else {
            binding.randomParkingRadioButton.setChecked(true);
        }
    }

    private int getPositionInAdapter(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                return i;
            }
        }
        return -1;
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

    @Override
    public void onTypeSelected(String type) {
        selectedType = type;
        // Actualizar filas disponibles según el tipo seleccionado
        updateAvailableRows();
    }

    private void updateAvailableRows() {
        List<String> availableRows = new ArrayList<>();

        switch (selectedType) {
            case Plaza.TIPO_STANDARD:
                // Filas A-D para coches
                availableRows.add("A");
                availableRows.add("B");
                availableRows.add("C");
                availableRows.add("D");
                break;
            case Plaza.TIPO_MOTORCYCLE:
                // Fila E para motos
                availableRows.add("E");
                break;
            case Plaza.TIPO_DISABLED:
                // Fila F para plazas de discapacitados
                availableRows.add("F");
                break;
            case Plaza.TIPO_CV_CHARGER:
                // Para cargadores eléctricos
                availableRows.add("G");
                break;
        }

        // Actualizar el adapter del spinner de filas
        ArrayAdapter<String> rowAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availableRows);
        rowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingRowSpinner.setAdapter(rowAdapter);

        // Si hay filas disponibles, actualizamos el spinner de números
        if (!availableRows.isEmpty()) {
            binding.parkingRowSpinner.setSelection(0);
            updateParkingNumbers(availableRows.get(0));
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
                updateParkingNumbers(selectedRow);
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
            }
        });
    }

    private void updateParkingNumbers(String row) {
        List<String> numbers = new ArrayList<>();

        // Según la fila, determinamos cuántos números hay
        int maxNumber;
        if (row.equals("F")) {
            maxNumber = 5;  // Menos plazas para discapacitados
        } else {
            maxNumber = 10; // Número estándar para otras filas
        }

        // Generar lista de números
        for (int i = 1; i <= maxNumber; i++) {
            numbers.add(String.valueOf(i));
        }

        // Actualizar el spinner
        ArrayAdapter<String> numberAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, numbers);
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingNumberSpinner.setAdapter(numberAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.datePickerButton.setOnClickListener(v -> showDatePicker());
        binding.startTimeButton.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeButton.setOnClickListener(v -> showTimePicker(false));
        binding.btnSaveReservation.setOnClickListener(v -> validateAndSave());
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 7);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    binding.datePickerButton.setText(dateFormat.format(selectedDate.getTime()));
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

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    if (isStartTime) {
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        binding.startTimeButton.setText(timeFormat.format(startTime.getTime()));
                    } else {
                        endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endTime.set(Calendar.MINUTE, minute);
                        binding.endTimeButton.setText(timeFormat.format(endTime.getTime()));
                    }
                    validateTimeInterval();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private boolean validateTimeInterval() {
        boolean isValid = Validators.isValidTimeInterval(startTime, endTime);
        binding.timeWarningText.setText(Validators.getTimeIntervalMessage(startTime, endTime));
        return isValid;
    }

    private void validateAndSave() {
        if (!validateReservationData()) {
            return;
        }

        // Preparar los datos de la reserva
        String plazaId = getSelectedPlazaId();

        // Crear el ViewModel si no existe
        ReservationsViewModel viewModel = new ViewModelProvider(this).get(ReservationsViewModel.class);

        if (isEditMode && reservationId != null) {
            // Actualizar reserva existente
            updateExistingReservation(viewModel, plazaId);
        } else {
            // Verificar disponibilidad y crear nueva reserva
            checkAvailabilityAndCreate(viewModel, plazaId);
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
            String row = (String) binding.parkingRowSpinner.getSelectedItem();
            String number = (String) binding.parkingNumberSpinner.getSelectedItem();
            return row + "-" + number;
        }
        return null; // Para selección aleatoria
    }

    private void checkAvailabilityAndCreate(ReservationsViewModel viewModel, String plazaId) {
        // Formatear fecha para la API (yyyy-MM-dd)
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = apiDateFormat.format(selectedDate.getTime());

        // Calcular tiempos en milisegundos (desde medianoche)
        long startTimeMs = startTime.get(Calendar.HOUR_OF_DAY) * 3600000L +
                startTime.get(Calendar.MINUTE) * 60000L;
        long endTimeMs = endTime.get(Calendar.HOUR_OF_DAY) * 3600000L +
                endTime.get(Calendar.MINUTE) * 60000L;

        // Verificar disponibilidad
        viewModel.checkReservationAvailability(formattedDate, startTimeMs, endTimeMs, selectedType, plazaId)
                .observe(this, result -> {
                    if (result != null) {
                        if (result) {
                            // Disponible, crear la reserva
                            createReservation(viewModel, formattedDate, startTimeMs, endTimeMs, plazaId);
                        } else {
                            // No disponible
                            Toast.makeText(this, "Ya existe una reserva para esa fecha, hora o plaza",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createReservation(ReservationsViewModel viewModel, String date, long startTimeMs,
                                   long endTimeMs, String plazaId) {
        viewModel.createReservation(date, startTimeMs, endTimeMs, selectedType, plazaId)
                .observe(this, success -> {
                    if (success != null && success) {
                        Toast.makeText(this, "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al crear la reserva", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateExistingReservation(ReservationsViewModel viewModel, String plazaId) {
        // Formatear fecha para la API
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = apiDateFormat.format(selectedDate.getTime());

        // Calcular tiempos en milisegundos
        long startTimeMs = startTime.get(Calendar.HOUR_OF_DAY) * 3600000L +
                startTime.get(Calendar.MINUTE) * 60000L;
        long endTimeMs = endTime.get(Calendar.HOUR_OF_DAY) * 3600000L +
                endTime.get(Calendar.MINUTE) * 60000L;

        // Actualizar la reserva
        viewModel.updateReservation(reservationId, formattedDate, startTimeMs, endTimeMs, selectedType, plazaId)
                .observe(this, success -> {
                    if (success != null && success) {
                        Toast.makeText(this, "Reserva actualizada correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al actualizar la reserva", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}