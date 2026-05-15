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
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private MyDatabaseHelper myDb;
    private String currentUserEmail;

    private static final String PREF_STEPS_OFFSET = "steps_at_start_of_day";

    @Override
    public void onCreate() {
        super.onCreate();
        myDb = new MyDatabaseHelper(this);
        SharedPreferences sharedPref = getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);
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
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;

        int totalStepsSinceReboot = (int) event.values[0];
        SharedPreferences sharedPref = getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);
        int savedOffset = sharedPref.getInt(PREF_STEPS_OFFSET, -1);

        if (savedOffset == -1 || totalStepsSinceReboot < savedOffset) {
            savedOffset = totalStepsSinceReboot;
            sharedPref.edit().putInt(PREF_STEPS_OFFSET, savedOffset).apply();
        }

        int stepsToday = totalStepsSinceReboot - savedOffset;
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Update database directly. The Fragment will read this onResume.
        myDb.updateDailyValue(currentUserEmail, todayDate, "current_steps", stepsToday);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("STEP_CHANNEL",
                    "Step Tracking", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}