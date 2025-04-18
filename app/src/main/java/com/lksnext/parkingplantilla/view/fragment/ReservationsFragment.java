package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lksnext.parkingplantilla.databinding.CardReservationCurrentBinding;
import com.lksnext.parkingplantilla.databinding.FragmentReservationsBinding;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;

import java.util.ArrayList;
import java.util.List;


public class ReservationsFragment extends Fragment {

    private FragmentReservationsBinding binding;

    public ReservationsFragment() {
        // Es necesario un constructor vacio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout container = binding.linearLayoutContainer;

        // Create sample reservations
        List<Reserva> reservas = createSampleReservations();

        if (reservas.isEmpty()) {
            container.setVisibility(View.GONE);
        } else {
            container.setVisibility(View.VISIBLE);

            // Clear any existing views
            container.removeAllViews();

            // Inflate and add cards for each reservation
            for (Reserva reserva : reservas) {
                CardReservationCurrentBinding binding = CardReservationCurrentBinding.inflate(
                        getLayoutInflater(), container, false);

                // Set the reservation data using data binding
                binding.setReserva(reserva);

                // Add the card to the container
                container.addView(binding.getRoot());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }

    private List<Reserva> createSampleReservations() {
        List<Reserva> reservas = new ArrayList<>();

        // Create sample plazas with different types and locations
        Plaza plazaStandard1 = new Plaza("A-13", Plaza.TIPO_STANDARD);
        Plaza plazaStandard2 = new Plaza("A-14", Plaza.TIPO_STANDARD);
        Plaza plazaStandard3 = new Plaza("A-22", Plaza.TIPO_STANDARD);
        Plaza plazaCharger1 = new Plaza("B-01", Plaza.TIPO_CV_CHARGER);
        Plaza plazaCharger2 = new Plaza("B-05", Plaza.TIPO_CV_CHARGER);
        Plaza plazaDisabled = new Plaza("C-03", Plaza.TIPO_DISABLED);
        Plaza plazaVIP = new Plaza("D-01", Plaza.TIPO_MOTORCYCLE);

        // Current time in milliseconds
        long now = System.currentTimeMillis();

        // Time intervals
        long fourHoursMillis = 4 * 60 * 60 * 1000;
        long sixHoursMillis = 6 * 60 * 60 * 1000;
        long dayMillis = 24 * 60 * 60 * 1000;

        // Create sample hours with different timeframes
        Hora hora1 = new Hora(now, now + fourHoursMillis);
        Hora hora2 = new Hora(now + dayMillis, now + dayMillis + sixHoursMillis);
        Hora hora3 = new Hora(now + (2 * dayMillis), now + (2 * dayMillis) + fourHoursMillis);
        Hora hora4 = new Hora(now + (3 * dayMillis), now + (3 * dayMillis) + sixHoursMillis);
        Hora hora5 = new Hora(now + (4 * dayMillis), now + (4 * dayMillis) + fourHoursMillis);

        // Morning hours
        Hora morning1 = new Hora(now + (8 * 60 * 60 * 1000), now + (12 * 60 * 60 * 1000));
        Hora morning2 = new Hora(now + dayMillis + (8 * 60 * 60 * 1000), now + dayMillis + (12 * 60 * 60 * 1000));

        // Afternoon hours
        Hora afternoon1 = new Hora(now + (14 * 60 * 60 * 1000), now + (18 * 60 * 60 * 1000));
        Hora afternoon2 = new Hora(now + dayMillis + (14 * 60 * 60 * 1000), now + dayMillis + (18 * 60 * 60 * 1000));

        // Create 10 sample reservations with different dates, plazas and hours
        reservas.add(new Reserva("09/05/2023", "user@example.com", "R001", plazaStandard1, hora1));
        reservas.add(new Reserva("10/05/2023", "user@example.com", "R002", plazaCharger1, hora2));
        reservas.add(new Reserva("11/05/2023", "john.doe@company.com", "R003", plazaStandard2, hora3));
        reservas.add(new Reserva("12/05/2023", "user@example.com", "R004", plazaDisabled, hora4));
        reservas.add(new Reserva("13/05/2023", "jane.smith@company.com", "R005", plazaStandard3, hora5));
        reservas.add(new Reserva("14/05/2023", "user@example.com", "R006", plazaCharger2, morning1));
        reservas.add(new Reserva("15/05/2023", "mark.johnson@company.com", "R007", plazaVIP, afternoon1));
        reservas.add(new Reserva("16/05/2023", "user@example.com", "R008", plazaStandard1, morning2));
        reservas.add(new Reserva("17/05/2023", "sarah.williams@company.com", "R009", plazaCharger1, afternoon2));
        reservas.add(new Reserva("18/05/2023", "user@example.com", "R010", plazaStandard2, hora1));

        return reservas;
    }
}