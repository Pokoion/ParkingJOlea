package com.lksnext.parkingplantilla.utils;

import java.util.Calendar;

public class Validators {

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }

    public static boolean isValidPassword(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }

    public static boolean areLoginFieldsValid(String email, String password) {
        return !email.trim().isEmpty() && !password.trim().isEmpty();
    }

    // En Validators.java - añadir estos métodos
    public static boolean isValidReservationType(String type) {
        return type != null && !type.isEmpty();
    }

    public static boolean isValidDate(String date) {
        return date != null && !date.equals("Seleccionar fecha");
    }

    public static boolean isValidTimeSelection(String startTime, String endTime) {
        return startTime != null && !startTime.equals("Hora inicio") &&
                endTime != null && !endTime.equals("Hora fin");
    }

    public static boolean isValidTimeInterval(Calendar startTime, Calendar endTime) {
        long diffMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        int diffHours = (int) (diffMillis / (60 * 60 * 1000));

        return diffMillis > 0 && diffHours <= 9;
    }

    public static String getTimeIntervalMessage(Calendar startTime, Calendar endTime) {
        long diffMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        int diffMinutes = (int) (diffMillis / (60 * 1000));
        int diffHours = diffMinutes / 60;
        int minutos = diffMinutes % 60;

        if (diffMillis <= 0) {
            return "La hora de fin debe ser posterior a la de inicio";
        } else if (diffHours > 9) {
            return "El intervalo no puede ser mayor a 9 horas";
        } else if (minutos == 0) {
            return "Intervalo de " + diffHours + " horas seleccionado";
        } else {
            return "Intervalo de " + diffHours + " horas y " + minutos + " minutos seleccionado";
        }
    }

    public static boolean isValidReservationStart(Calendar start) {
        Calendar now = Calendar.getInstance();
        // Redondear a minutos para evitar falsos negativos por milisegundos
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return !start.before(now);
    }

    public static boolean isValidReservationDuration(Calendar start, Calendar end, int maxHours) {
        long diffMillis = end.getTimeInMillis() - start.getTimeInMillis();
        double diffHours = diffMillis / (1000.0 * 60 * 60);
        return diffMillis > 0 && diffHours <= maxHours;
    }

    // Nueva validación: duración máxima en milisegundos
    public static boolean isValidReservationDurationMs(long startMs, long endMs, int maxHours) {
        long diffMillis = endMs - startMs;
        double diffHours = diffMillis / (1000.0 * 60 * 60);
        return diffMillis > 0 && diffHours <= maxHours;
    }

    // Validación específica para máximo 9 horas
    public static boolean isValidReservationDuration9h(long startMs, long endMs) {
        return isValidReservationDurationMs(startMs, endMs, 9);
    }
}
