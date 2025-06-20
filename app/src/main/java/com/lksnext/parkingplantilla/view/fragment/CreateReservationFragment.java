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
import com.lksnext.parkingplantilla.domain.Plaza;
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

        // Restaurar selección temporal si existe
        if (viewModel.getTempSelectedType() != null) {
            selectedType = viewModel.getTempSelectedType();
            Calendar tempDate = viewModel.getTempSelectedDate();
            Calendar tempStart = viewModel.getTempStartTime();
            Calendar tempEnd = viewModel.getTempEndTime();
            if (tempDate != null) selectedDate = (Calendar) tempDate.clone();
            if (tempStart != null) startTime = (Calendar) tempStart.clone();
            if (tempEnd != null) endTime = (Calendar) tempEnd.clone();
            reservationId = viewModel.getTempReservationId();
            // Actualizar UI
            binding.datePickerButton.setText(DateUtils.formatDateForUi(selectedDate));
            binding.startTimeButton.setText(DateUtils.formatTimeFromMs(DateUtils.timeToMs(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE))));
            binding.endTimeButton.setText(DateUtils.formatTimeFromMs(DateUtils.timeToMs(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE))));
            adapter.selectType(selectedType);
            checkAvailableSpots();
        }

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

        binding.btnNextStep.setEnabled(false); // Desactivar por defecto

        setupReservationTypes();
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        binding.datePickerButton.setOnClickListener(v -> showDatePicker());
        binding.startTimeButton.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeButton.setOnClickListener(v -> showTimePicker(false));
        binding.btnNextStep.setOnClickListener(v -> {
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
            binding.availableSpotsText.setText("");
            binding.btnNextStep.setEnabled(false);
            binding.btnNextStep.setAlpha(0.5f); // Botón transparente
            return;
        }
        String apiDate = DateUtils.formatDateForApi(selectedDate);
        long startTimeMs = DateUtils.timeToMs(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE));
        long endTimeMs = DateUtils.timeToMs(
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE));
        viewModel.getAvailablePlazas(selectedType, apiDate, startTimeMs, endTimeMs).observe(getViewLifecycleOwner(), plazas -> {
            if (plazas != null && !plazas.isEmpty()) {
                binding.availableSpotsText.setText("Plazas disponibles: " + plazas.size());
                binding.btnNextStep.setEnabled(true);
                binding.btnNextStep.setAlpha(1.0f); // Botón opaco
            } else {
                binding.availableSpotsText.setText("No hay plazas disponibles para esta fecha y hora");
                binding.btnNextStep.setEnabled(false);
                binding.btnNextStep.setAlpha(0.5f); // Botón transparente
            }
        });
    }

    private void onNextStep() {
        // Guardar selección temporal antes de navegar
        viewModel.saveTempSelection(selectedType, selectedDate, startTime, endTime, reservationId);
        Bundle args = new Bundle();
        args.putString("SELECTED_TYPE", selectedType);
        args.putSerializable("SELECTED_DATE", selectedDate);
        args.putSerializable("START_TIME", startTime);
        args.putSerializable("END_TIME", endTime);
        args.putString("RESERVATION_ID", reservationId);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.flFragment);
        navController.navigate(R.id.action_createReservationFragment_to_selectParkingSpotFragment, args);
    }

    private void observeViewModel() {
        // Aquí puedes observar errores o loading si lo deseas
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
