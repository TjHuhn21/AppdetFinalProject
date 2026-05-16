package com.example.heronhealth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    // Other parts of the app listen for this broadcast to update the UI live
    public static final String ACTION_STEPS_UPDATED = "com.example.heronhealth.STEPS_UPDATED";
    public static final String EXTRA_STEPS_TODAY    = "steps_today";

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private MyDatabaseHelper myDb;
    private String currentUserEmail;
    private String todayDate;

    private static final String PREF_STEPS_OFFSET  = "steps_at_start_of_day";
    private static final String PREF_LAST_LOG_DATE = "last_log_date";

    @Override
    public void onCreate() {
        super.onCreate();

        myDb = new MyDatabaseHelper(this);

        SharedPreferences sharedPref =
                getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        currentUserEmail = sharedPref.getString("userEmail", "");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, "STEP_CHANNEL")
                .setContentTitle("HeronHealth is Active")
                .setContentText("Tracking your steps in the background...")
                .setSmallIcon(R.drawable.heronawake)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        if (stepSensor != null) {
            sensorManager.registerListener(
                    this,
                    stepSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;

        int totalStepsSinceReboot = (int) event.values[0];

        SharedPreferences sharedPref =
                getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        // Check for a new day — if so, reset the offset
        todayDate = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        String lastLogDate = sharedPref.getString(PREF_LAST_LOG_DATE, "");

        if (!lastLogDate.equals(todayDate)) {
            // New day: reset offset so steps restart from 0
            sharedPref.edit()
                    .putString(PREF_LAST_LOG_DATE, todayDate)
                    .putInt(PREF_STEPS_OFFSET, totalStepsSinceReboot)
                    .apply();
        }

        int savedOffset = sharedPref.getInt(PREF_STEPS_OFFSET, -1);

        if (savedOffset == -1) {
            // First ever reading — set baseline
            savedOffset = totalStepsSinceReboot;
            sharedPref.edit()
                    .putInt(PREF_STEPS_OFFSET, savedOffset)
                    .apply();
        }

        // Phone was rebooted — sensor reset to smaller number
        if (totalStepsSinceReboot < savedOffset) {
            savedOffset = totalStepsSinceReboot;
            sharedPref.edit()
                    .putInt(PREF_STEPS_OFFSET, savedOffset)
                    .apply();
        }

        int stepsToday = totalStepsSinceReboot - savedOffset;

        if (stepsToday < 0) stepsToday = 0;

        myDb.updateDailyValue(
                currentUserEmail,
                todayDate,
                MyDatabaseHelper.COL_CUR_STEPS,
                stepsToday
        );

        Intent broadcast = new Intent(ACTION_STEPS_UPDATED);

        broadcast.setPackage(getPackageName());

        broadcast.putExtra(EXTRA_STEPS_TODAY, stepsToday);
        sendBroadcast(broadcast);
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    "STEP_CHANNEL",
                    "Step Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}