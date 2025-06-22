package com.lksnext.parkingplantilla.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationScheduler {
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
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
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
