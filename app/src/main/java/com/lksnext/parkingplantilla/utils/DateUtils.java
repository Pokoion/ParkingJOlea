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

    /**
     * Verifica si una reserva es histórica (ya pasó)
     */
    public static boolean isHistoricReservation(Reserva reserva) {
        Date now = new Date();
        Date reservaEndTime = getReservaEndTime(reserva);
        return reservaEndTime.before(now);
    }

    /**
     * Verifica si una reserva está actualmente en curso
     */
    public static boolean isOngoingReservation(Reserva reserva) {
        Date now = new Date();
        Date startTime = getReservaDateTime(reserva);
        Date endTime = getReservaEndTime(reserva);
        return now.after(startTime) && now.before(endTime);
    }

    /**
     * Verifica si una reserva es futura (aún no ha comenzado)
     */
    public static boolean isFutureReservation(Reserva reserva) {
        Date now = new Date();
        Date startTime = getReservaDateTime(reserva);
        return startTime.after(now);
    }

    /**
     * Formatea la fecha de una reserva para mostrar en la UI
     */
    public static String formatReservaDate(Reserva reserva) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = inputFormat.parse(reserva.getFecha());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return reserva.getFecha();
        }
    }

    /**
     * Formatea la hora de una reserva para mostrar en la UI
     */
    public static String formatReservaTime(Reserva reserva) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date startTime = new Date(reserva.getHora().getHoraInicio());
        Date endTime = new Date(reserva.getHora().getHoraFin());
        return timeFormat.format(startTime) + " - " + timeFormat.format(endTime);
    }
}