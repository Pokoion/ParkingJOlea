package com.lksnext.parkingplantilla.notifications;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    public static void scheduleReservationNotification(Context context, long triggerAtMillis, String title, String message, String uniqueTag) {
        long delay = triggerAtMillis - System.currentTimeMillis();
        if (delay <= 0) return; // No programar si ya pasó

        Data data = new Data.Builder()
                .putString(ReservationNotificationWorker.KEY_TITLE, title)
                .putString(ReservationNotificationWorker.KEY_MESSAGE, message)
                .build();

        WorkRequest workRequest = new OneTimeWorkRequest.Builder(ReservationNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(uniqueTag)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }

    public static void cancelReservationNotification(Context context, String uniqueTag) {
        WorkManager.getInstance(context).cancelAllWorkByTag(uniqueTag);
    }
}

