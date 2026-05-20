package com.example.heronhealth;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class HabitReminderActivity extends AppCompatActivity {

    public static final String PREF_HABIT_REMINDER_ON   = "habit_reminder_on";
    public static final String PREF_HABIT_REMINDER_HOUR = "habit_reminder_hour";
    public static final String PREF_HABIT_REMINDER_MIN  = "habit_reminder_min";
    public static final String NOTIF_CHANNEL_ID         = "HABIT_REMINDER_CHANNEL";

    private Switch switchPushNotif;
    private TextView tvReminderTime, tvNotifSubtitle;
    private MaterialButton btnCreateHabit;

    private ImageButton btnBack;

    private int selectedHour   = 20;  // default 8:00 PM
    private int selectedMinute = 0;

    private String habitName  = "";
    private String habitEmoji = "";
    private String weekStart  = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_reminder);

        habitName  = getIntent().getStringExtra("HABIT_NAME");
        habitEmoji = getIntent().getStringExtra("HABIT_EMOJI");
        weekStart  = getIntent().getStringExtra("WEEK_START");

        if (habitName  == null) habitName  = "";
        if (habitEmoji == null) habitEmoji = "";
        if (weekStart  == null) weekStart  = "";

        initialize();
        createNotificationChannel();

        // Restore saved prefs
        SharedPreferences prefs =
                getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);
        boolean reminderOn = prefs.getBoolean(PREF_HABIT_REMINDER_ON, true);
        selectedHour   = prefs.getInt(PREF_HABIT_REMINDER_HOUR, 20);
        selectedMinute = prefs.getInt(PREF_HABIT_REMINDER_MIN,   0);

        switchPushNotif.setChecked(reminderOn);
        updateTimeLabel();

        switchPushNotif.setOnCheckedChangeListener((btn, isChecked) ->
                tvNotifSubtitle.setText(isChecked
                        ? "Habit reminders turned on"
                        : "Habit reminders turned off"));

        // Tap time row → open clock picker
        tvReminderTime.setOnClickListener(v -> showTimePicker());
        findViewById(R.id.tvReminderLabel).setOnClickListener(v -> showTimePicker());

        btnBack.setOnClickListener(v -> finish());

        btnCreateHabit.setOnClickListener(v -> createHabit());

        TextView btnClose = findViewById(R.id.btnCloseReminder);
        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void initialize() {
        switchPushNotif  = findViewById(R.id.switchPushNotif);
        tvReminderTime   = findViewById(R.id.tvReminderTime);
        tvNotifSubtitle  = findViewById(R.id.tvNotifSubtitle);
        btnCreateHabit   = findViewById(R.id.btnCreateHabit);
        btnBack          = findViewById(R.id.btnBackReminder);
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                android.R.style.Theme_DeviceDefault_Dialog_Alert,
                (view, hourOfDay, minute) -> {
                    selectedHour   = hourOfDay;
                    selectedMinute = minute;
                    updateTimeLabel();
                },
                selectedHour,
                selectedMinute,
                false
        );
        dialog.setTitle("SELECT TIME");
        dialog.show();
    }

    private void updateTimeLabel() {
        String ampm = selectedHour >= 12 ? "PM" : "AM";
        int hour12  = selectedHour % 12;
        if (hour12 == 0) hour12 = 12;
        tvReminderTime.setText(
                String.format("%d:%02d %s", hour12, selectedMinute, ampm)
        );
    }

    private void createHabit() {

        SharedPreferences prefs =
                getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        prefs.edit()
                .putString(HabitPickerActivity.PREF_HABIT_NAME,       habitName)
                .putString(HabitPickerActivity.PREF_HABIT_EMOJI,      habitEmoji)
                .putString(HabitPickerActivity.PREF_HABIT_WEEK_START, weekStart)
                .putBoolean(PREF_HABIT_REMINDER_ON,   switchPushNotif.isChecked())
                .putInt(PREF_HABIT_REMINDER_HOUR,     selectedHour)
                .putInt(PREF_HABIT_REMINDER_MIN,      selectedMinute)
                .apply();

        if (switchPushNotif.isChecked()) {
            scheduleHabitReminder(
                    selectedHour,
                    selectedMinute,
                    habitEmoji + " " + habitName
            );
        } else {
            cancelHabitReminder();
        }

        Toast.makeText(this,
                switchPushNotif.isChecked()
                        ? "Habit created! Reminder set for " + tvReminderTime.getText()
                        : "Habit created!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ── SCHEDULING ───────────────────────────────────────────────────────────

    private void scheduleHabitReminder(int hour, int minute, String habitLabel) {

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        Intent notifIntent = new Intent(this, HabitReminderReceiver.class);
        notifIntent.putExtra("habit_label", habitLabel);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1001,
                notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar trigger = Calendar.getInstance();
        trigger.set(Calendar.HOUR_OF_DAY, hour);
        trigger.set(Calendar.MINUTE,      minute);
        trigger.set(Calendar.SECOND,      0);
        trigger.set(Calendar.MILLISECOND, 0);

        // If the chosen time has already passed today, schedule for tomorrow
        if (trigger.before(Calendar.getInstance())) {
            trigger.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ — must check permission before calling setExactAndAllowWhileIdle
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            trigger.getTimeInMillis(),
                            pendingIntent
                    );
                } catch (SecurityException e) {
                    // Permission was revoked between the check and the call — fall back
                    setInexactAlarm(alarmManager, trigger, pendingIntent);
                }
            } else {
                // Permission not granted — send user to the exact alarm permission screen
                // and fall back to inexact in the meantime so the reminder still works
                setInexactAlarm(alarmManager, trigger, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent permIntent = new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + getPackageName())
                    );
                    try {
                        startActivity(permIntent);
                    } catch (Exception ignored) {
                        Toast.makeText(this,
                                "Reminder set (approximate time — grant exact alarm permission for precision).",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6–11: setExactAndAllowWhileIdle is safe without extra permission
            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        trigger.getTimeInMillis(),
                        pendingIntent
                );
            } catch (SecurityException e) {
                setInexactAlarm(alarmManager, trigger, pendingIntent);
            }

        } else {
            // Android < 6: use setRepeating (exact, repeats daily)
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    trigger.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }
    private void setInexactAlarm(AlarmManager alarmManager,
                                 Calendar trigger,
                                 PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    trigger.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private void cancelHabitReminder() {

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1001,
                new Intent(this, HabitReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    // ── NOTIFICATION CHANNEL ─────────────────────────────────────────────────

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIF_CHANNEL_ID,
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Daily reminders for your weekly habit goal");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}