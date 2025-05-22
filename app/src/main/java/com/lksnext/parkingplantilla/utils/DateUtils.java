package com.lksnext.parkingplantilla.utils;

import com.lksnext.parkingplantilla.domain.Reserva;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     * Obtiene un objeto Date con la fecha y hora de inicio de la reserva
     */
    public static Date getReservaDateTime(Reserva reserva) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date horaInicio = new Date(reserva.getHora().getHoraInicio());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String timeStr = timeFormat.format(horaInicio);
            return sdf.parse(reserva.getFecha() + " " + timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // Fallback a fecha actual
        }
    }

    /**
     * Obtiene un objeto Date con la fecha y hora de fin de la reserva
     */
    public static Date getReservaEndTime(Reserva reserva) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date horaFin = new Date(reserva.getHora().getHoraFin());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String timeStr = timeFormat.format(horaFin);
            return sdf.parse(reserva.getFecha() + " " + timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            // Fallback: 2 horas después de la hora actual
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 2);
            return calendar.getTime();
        }
    }
}