package com.lksnext.parkingplantilla.view.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lksnext.parkingplantilla.adapter.ReservationTypeAdapter;
import com.lksnext.parkingplantilla.databinding.ActivityCreateReservationBinding;
import com.lksnext.parkingplantilla.domain.Plaza;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateReservationActivity extends AppCompatActivity {

    private ActivityCreateReservationBinding binding;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private ReservationTypeAdapter adapter;
    private String selectedType;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateReservationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Nueva Reserva");

        setupReservationTypes();
        setupParkingSpinners();
        setupListeners();
    }

    private void setupReservationTypes() {
        List<String> types = new ArrayList<>();
        types.add(Plaza.TIPO_STANDARD);
        types.add(Plaza.TIPO_CV_CHARGER);
        types.add(Plaza.TIPO_DISABLED);
        types.add(Plaza.TIPO_MOTORCYCLE);

        adapter = new ReservationTypeAdapter(types, type -> selectedType = type);
        binding.reservationTypeRecyclerView.setAdapter(adapter);
    }

    private void setupParkingSpinners() {
        // Configurar spinner de filas (letras)
        String[] rows = {"A", "B", "C", "D", "E", "F"};
        ArrayAdapter<String> rowAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, rows);
        rowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingRowSpinner.setAdapter(rowAdapter);

        // Configurar spinner de números
        String[] numbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter<String> numberAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, numbers);
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.parkingNumberSpinner.setAdapter(numberAdapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.datePickerButton.setOnClickListener(v -> showDatePicker());
        binding.startTimeButton.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeButton.setOnClickListener(v -> showTimePicker(false));

        binding.parkingSelectionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.manualParkingRadioButton.getId()) {
                binding.parkingManualSelectionContainer.setVisibility(View.VISIBLE);
            } else {
                binding.parkingManualSelectionContainer.setVisibility(View.GONE);
            }
        });

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
        long diffMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        int diffHours = (int) (diffMillis / (60 * 60 * 1000));

        if (diffMillis <= 0) {
            binding.timeWarningText.setText("La hora de fin debe ser posterior a la de inicio");
            return false;
        } else if (diffHours > 7) {
            binding.timeWarningText.setText("El intervalo no puede ser mayor a 7 horas");
            return false;
        } else {
            binding.timeWarningText.setText("Intervalo de " + diffHours + " horas seleccionado");
            return true;
        }
    }

    private void validateAndSave() {
        if (selectedType == null) {
            Toast.makeText(this, "Debes seleccionar un tipo de reserva", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.datePickerButton.getText().toString().equals("Seleccionar fecha")) {
            Toast.makeText(this, "Debes seleccionar una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.startTimeButton.getText().toString().equals("Hora inicio") ||
                binding.endTimeButton.getText().toString().equals("Hora fin")) {
            Toast.makeText(this, "Debes seleccionar el intervalo de horas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateTimeInterval()) {
            Toast.makeText(this, "El intervalo de horas no es válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String plazaId = null;
        if (binding.manualParkingRadioButton.isChecked()) {
            String row = (String) binding.parkingRowSpinner.getSelectedItem();
            String number = (String) binding.parkingNumberSpinner.getSelectedItem();
            plazaId = row + "-" + number;
        }

        // Aquí se llamaría al repositorio para crear la reserva
        Toast.makeText(this, "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
        finish();
    }
}