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
     * Obtiene un objeto Date con la fecha y hora de inicio de la reserva (formato API + ms desde medianoche)
     * Siempre usa la fecha en formato yyyy-MM-dd y la hora en ms desde medianoche (UTC).
     */
    public static Date getReservaDateTime(Reserva reserva) {
        try {
            // Parse fecha en formato API (yyyy-MM-dd)
            SimpleDateFormat apiDateFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
            apiDateFormat.setLenient(false);
            Date fecha = apiDateFormat.parse(reserva.getFecha());
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            // Añadir ms desde medianoche (hora de inicio)
            long msInicio = reserva.getHora().getHoraInicio();
            cal.set(Calendar.HOUR_OF_DAY, (int) (msInicio / 3600000));
            cal.set(Calendar.MINUTE, (int) ((msInicio % 3600000) / 60000));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return new Date(); // Fallback a fecha actual
        }
    }

    /**
     * Obtiene un objeto Date con la fecha y hora de fin de la reserva (formato API + ms desde medianoche)
     * Siempre usa la fecha en formato yyyy-MM-dd y la hora en ms desde medianoche (UTC).
     */
    public static Date getReservaEndTime(Reserva reserva) {
        try {
            SimpleDateFormat apiDateFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
            apiDateFormat.setLenient(false);
            Date fecha = apiDateFormat.parse(reserva.getFecha());
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            long msFin = reserva.getHora().getHoraFin();
            cal.set(Calendar.HOUR_OF_DAY, (int) (msFin / 3600000));
            cal.set(Calendar.MINUTE, (int) ((msFin % 3600000) / 60000));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        } catch (Exception e) {
            e.printStackTrace();
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
     * Verifica si una reserva está actualmente en curso (incluye el instante exacto de inicio)
     */
    public static boolean isOngoingReservation(Reserva reserva) {
        Date now = new Date();
        Date startTime = getReservaDateTime(reserva);
        Date endTime = getReservaEndTime(reserva);
        return !now.before(startTime) && now.before(endTime);
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
     * Formatea la fecha de una reserva para mostrar en la UI (dd-MM-yyyy)
     */
    public static String formatReservaDate(Reserva reserva) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(UI_DATE_FORMAT, Locale.getDefault());
            Date date = inputFormat.parse(reserva.getFecha());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return reserva.getFecha();
        }
    }

    /**
     * Formatea la hora de una reserva para mostrar en la UI (HH:mm - HH:mm)
     */
    public static String formatReservaTime(Reserva reserva) {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        // Usar Calendar para evitar problemas de zona horaria
        Calendar calInicio = Calendar.getInstance();
        calInicio.setTimeInMillis(0);
        calInicio.set(Calendar.HOUR_OF_DAY, (int) (reserva.getHora().getHoraInicio() / 3600000));
        calInicio.set(Calendar.MINUTE, (int) ((reserva.getHora().getHoraInicio() % 3600000) / 60000));
        Calendar calFin = Calendar.getInstance();
        calFin.setTimeInMillis(0);
        calFin.set(Calendar.HOUR_OF_DAY, (int) (reserva.getHora().getHoraFin() / 3600000));
        calFin.set(Calendar.MINUTE, (int) ((reserva.getHora().getHoraFin() % 3600000) / 60000));
        return timeFormat.format(calInicio.getTime()) + " - " + timeFormat.format(calFin.getTime());
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

    /**
     * Devuelve un texto amigable con el tiempo restante hasta la reserva ("En 2 días", "En 3 horas", etc.)
     */
    public static String getTimeUntilText(Reserva reserva) {
        Date now = new Date();
        Date reservaDate = getReservaDateTime(reserva);
        long diffMs = reservaDate.getTime() - now.getTime();
        if (diffMs <= 0) {
            return "Ya ha comenzado";
        }
        long diffSeconds = diffMs / 1000;
        long diffMinutes = diffSeconds / 60;
        long diffHours = diffMinutes / 60;
        long diffDays = diffHours / 24;
        if (diffDays > 0) {
            return "En " + diffDays + (diffDays == 1 ? " día" : " días");
        } else if (diffHours > 0) {
            return "En " + diffHours + (diffHours == 1 ? " hora" : " horas");
        } else if (diffMinutes > 0) {
            return "En " + diffMinutes + (diffMinutes == 1 ? " minuto" : " minutos");
        } else {
            return "En unos segundos";
        }
    }

    /**
     * Devuelve un texto amigable con el tiempo restante hasta que finalice la reserva en curso ("Quedan 1h 30min", etc.)
     */
    public static String getTimeRemainingText(Reserva reserva) {
        Date now = new Date();
        Date end = getReservaEndTime(reserva);
        long diffMs = end.getTime() - now.getTime();
        if (diffMs <= 0) {
            return "Finalizada";
        }
        long diffMinutes = diffMs / 60000;
        long hours = diffMinutes / 60;
        long minutes = diffMinutes % 60;
        if (hours > 0) {
            return "Quedan " + hours + "h " + minutes + "min";
        } else {
            return "Quedan " + minutes + "min";
        }
    }

    /**
     * Indica si una reserva está dentro de los últimos 30 días (para FINALIZADA) o es posterior/últimos 30 días (para CANCELADA)
     * @param reserva Reserva a comprobar
     * @param cancelled true si es para CANCELADA, false para FINALIZADA
     * @return true si cumple el criterio
     */
    public static boolean isWithinLast30Days(Reserva reserva, boolean cancelled) {
        Date now = new Date();
        Date hace30dias = new Date(now.getTime() - 30L * 24 * 60 * 60 * 1000);
        if (cancelled) {
            // CANCELADA: fecha de inicio posterior a hoy o dentro de los últimos 30 días
            Date start = getReservaDateTime(reserva);
            return start.after(now) || !start.before(hace30dias);
        } else {
            // FINALIZADA: fecha de fin dentro de los últimos 30 días
            Date end = getReservaEndTime(reserva);
            return !end.before(hace30dias) && end.before(now);
        }
    }

    /**
     * Devuelve el timestamp para la notificación de inicio (30 min antes de la reserva)
     */
    public static long getStartReminderTime(Reserva reserva) {
        return getReservaDateTime(reserva).getTime() - 30 * 60 * 1000;
    }

    /**
     * Devuelve el timestamp para la notificación de fin (15 min antes de que termine la reserva)
     */
    public static long getEndReminderTime(Reserva reserva) {
        return getReservaEndTime(reserva).getTime() - 15 * 60 * 1000;
    }
}
