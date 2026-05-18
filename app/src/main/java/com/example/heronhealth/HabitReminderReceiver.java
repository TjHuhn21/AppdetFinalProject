package com.example.heronhealth;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class HabitReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();

        // Check if the system just finished booting up
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Re-schedule the alarm silently without pushing a notification
            rescheduleForTomorrow(context, null);
            return;
        }

        // Otherwise, it's our alarm firing!
        String habitLabel = intent.getStringExtra("habit_label");
        if (habitLabel == null || habitLabel.isEmpty()) {
            // Fallback: If pulled from boot or missing extra, pull fresh details from Prefs
            SharedPreferences prefs = context.getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);
            String name = prefs.getString(HabitPickerActivity.PREF_HABIT_NAME, "Your daily habit");
            String emoji = prefs.getString(HabitPickerActivity.PREF_HABIT_EMOJI, "🌟");
            habitLabel = emoji + " " + name;
        }

        // Build and fire the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, HabitReminderActivity.NOTIF_CHANNEL_ID)
                        .setSmallIcon(R.drawable.heronawake)
                        .setContentTitle("Habit Reminder 🌟")
                        .setContentText("Time to work on: " + habitLabel)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Don't break your streak! Time to work on: " + habitLabel))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent tapIntent = PendingIntent.getActivity(
                context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.setContentIntent(tapIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(1001, builder.build());
        }

        // Loop the alarm for tomorrow
        rescheduleForTomorrow(context, habitLabel);
    }

    private void rescheduleForTomorrow(Context context, String habitLabel) {
        SharedPreferences prefs =
                context.getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        boolean reminderOn = prefs.getBoolean(HabitReminderActivity.PREF_HABIT_REMINDER_ON, true);
        if (!reminderOn) return;

        // Fallback for label matching inside the boot loop sequence
        if (habitLabel == null) {
            String name = prefs.getString(HabitPickerActivity.PREF_HABIT_NAME, "Your daily habit");
            String emoji = prefs.getString(HabitPickerActivity.PREF_HABIT_EMOJI, "🌟");
            habitLabel = emoji + " " + name;
        }

        int hour   = prefs.getInt(HabitReminderActivity.PREF_HABIT_REMINDER_HOUR, 20);
        int minute = prefs.getInt(HabitReminderActivity.PREF_HABIT_REMINDER_MIN,   0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, hour);
        tomorrow.set(Calendar.MINUTE,      minute);
        tomorrow.set(Calendar.SECOND,      0);
        tomorrow.set(Calendar.MILLISECOND, 0);

        Intent nextIntent = new Intent(context, HabitReminderReceiver.class);
        nextIntent.putExtra("habit_label", habitLabel);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tomorrow.getTimeInMillis(), pendingIntent);
                } catch (SecurityException e) {
                    setInexactAlarm(alarmManager, tomorrow, pendingIntent);
                }
            } else {
                setInexactAlarm(alarmManager, tomorrow, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tomorrow.getTimeInMillis(), pendingIntent);
            } catch (SecurityException e) {
                setInexactAlarm(alarmManager, tomorrow, pendingIntent);
            }
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, tomorrow.getTimeInMillis(), pendingIntent);
        }
    }

    private void setInexactAlarm(AlarmManager alarmManager, Calendar trigger, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), pendingIntent);
        }
    }
}