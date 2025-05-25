package com.lksnext.parkingplantilla.utils;

import com.lksnext.parkingplantilla.domain.Reserva;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    // Constantes para formatos de fecha
    private static final String API_DATE_FORMAT = "yyyy-MM-dd";
    private static final String UI_DATE_FORMAT = "dd-MM-yyyy";
    private static final String TIME_FORMAT = "HH:mm";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * Obtiene un objeto Date con la fecha y hora de inicio de la reserva
     */
    public static Date getReservaDateTime(Reserva reserva) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
            Date horaInicio = new Date(reserva.getHora().getHoraInicio());
            SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
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
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
            Date horaFin = new Date(reserva.getHora().getHoraFin());
            SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
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
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_DATE_FORMAT);
            SimpleDateFormat outputFormat = new SimpleDateFormat(UI_DATE_FORMAT);
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
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        Date startTime = new Date(reserva.getHora().getHoraInicio());
        Date endTime = new Date(reserva.getHora().getHoraFin());
        return timeFormat.format(startTime) + " - " + timeFormat.format(endTime);
    }

    /**
     * Obtiene la fecha actual en formato para API (yyyy-MM-dd)
     */
    public static String getCurrentDateForApi() {
        SimpleDateFormat apiDateFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
        return apiDateFormat.format(new Date());
    }

    /**
     * Obtiene la fecha actual en formato para UI (dd-MM-yyyy)
     */
    public static String getCurrentDateForUi() {
        SimpleDateFormat uiDateFormat = new SimpleDateFormat(UI_DATE_FORMAT, Locale.getDefault());
        return uiDateFormat.format(new Date());
    }

    /**
     * Formatea una fecha Calendar para usar en API
     */
    public static String formatDateForApi(Calendar date) {
        SimpleDateFormat apiDateFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
        return apiDateFormat.format(date.getTime());
    }

    /**
     * Formatea una fecha Calendar para mostrar en UI
     */
    public static String formatDateForUi(Calendar date) {
        SimpleDateFormat uiDateFormat = new SimpleDateFormat(UI_DATE_FORMAT, Locale.getDefault());
        return uiDateFormat.format(date.getTime());
    }

    /**
     * Convierte milisegundos desde medianoche a string formato hora (HH:mm)
     */
    public static String formatTimeFromMs(long timeMs) {
        int hours = (int) (timeMs / 3600000);
        int minutes = (int) ((timeMs % 3600000) / 60000);
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    /**
     * Calcula milisegundos desde medianoche dado una hora y minuto
     */
    public static long timeToMs(int hours, int minutes) {
        return hours * 3600000L + minutes * 60000L;
    }

    /**
     * Obtiene milisegundos desde medianoche de la hora actual
     */
    public static long getCurrentTimeMs() {
        Calendar cal = Calendar.getInstance();
        int horaActual = cal.get(Calendar.HOUR_OF_DAY);
        int minutoActual = cal.get(Calendar.MINUTE);
        return timeToMs(horaActual, minutoActual);
    }

    /**
     * Convierte fecha de formato API a formato UI
     */
    public static String apiDateToUiDate(String apiDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_DATE_FORMAT);
            SimpleDateFormat outputFormat = new SimpleDateFormat(UI_DATE_FORMAT);
            Date date = inputFormat.parse(apiDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return apiDate;
        }
    }

    /**
     * Convierte fecha de formato UI a formato API
     */
    public static String uiDateToApiDate(String uiDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(UI_DATE_FORMAT);
            SimpleDateFormat outputFormat = new SimpleDateFormat(API_DATE_FORMAT);
            Date date = inputFormat.parse(uiDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return uiDate;
        }
    }
}