package com.lksnext.parkingplantilla.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationScheduler {

    private NotificationScheduler() {

    }
    public static void scheduleReservationNotification(Context context, long triggerAtMillis, String title, String message, String uniqueTag) {
        long delay = triggerAtMillis - System.currentTimeMillis();
        if (delay <= 0) return; // No programar si ya pasó

        Intent intent = new Intent(context, ReservationNotificationReceiver.class);
        intent.putExtra(ReservationNotificationReceiver.EXTRA_TITLE, title);
        intent.putExtra(ReservationNotificationReceiver.EXTRA_MESSAGE, message);
        intent.setAction(uniqueTag); // Para identificar la alarma

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueTag.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                    } else {
                        android.widget.Toast.makeText(context, "Debes habilitar el permiso de alarmas exactas en Ajustes para recibir notificaciones.", android.widget.Toast.LENGTH_LONG).show();
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            } catch (SecurityException e) {
                android.widget.Toast.makeText(context, "No tienes permiso para programar alarmas exactas.", android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void cancelReservationNotification(Context context, String uniqueTag) {
        Intent intent = new Intent(context, ReservationNotificationReceiver.class);
        intent.setAction(uniqueTag);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueTag.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
