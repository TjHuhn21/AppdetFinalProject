package com.example.heronhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.heronhealth.model.PersonalInfo;
import com.example.heronhealth.model.WeightEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment implements SensorEventListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View view;

    // Calories
    private ProgressBar progressCaloriesBar;
    private TextView tvRemainingValue, tvBaseGoal, tvFoodEaten, tvProteinLabel;
    private ImageView imgBackgroundWorkout;

    // Steps
    private ProgressBar pbStepsProgress;
    private TextView tvStepCountValue, tvStepGoalLabel;
    private MaterialButton btnEditStepGoal;

    // Water
    private ProgressBar pbWaterIntake;
    private TextView tvWaterCount, tvTargetWater;
    private MaterialButton btnUpdateWater;

    // Workout
    private TextView tvRecentWorkoutStatus;
    private MaterialButton btnLogWorkout;

    // Weight
    private LineChart weightLineChart;
    private MaterialButton btnAddWeight;

    private MyDatabaseHelper myDb;

    private String currentUserEmail;

    private int waterGoalMl;
    private int stepGoal;
    private int currentWaterValue = 0;

    // Sensors
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    // Step tracking
    // FIX: use -1 as "not yet set" sentinel stored in prefs
    private String todayDate;

    private static final String PREF_STEPS_OFFSET    = "steps_at_start_of_day";
    private static final String PREF_LAST_LOG_DATE   = "last_log_date";
    private static final String PREF_WEIGHT_SEEDED   = "weight_chart_seeded";

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        //ask permision four physcial actibede
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }

        //get date today
        todayDate = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        //get the last log date for sharedpref
        SharedPreferences sharedPref = requireActivity()
                .getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        String lastSavedDate = sharedPref.getString(PREF_LAST_LOG_DATE, "");

        // Reset daily step offset when day changes
        if (!lastSavedDate.equals(todayDate)) {
            sharedPref.edit()
                    .putString(PREF_LAST_LOG_DATE, todayDate)
                    .putInt(PREF_STEPS_OFFSET, -1)   // -1 = "not yet captured today"
                    .apply();
        }

        initialize();

        //get the current user email
        currentUserEmail = sharedPref.getString("userEmail", "");

        //check if log exists
        myDb.checkAndInitDailyLog(currentUserEmail, todayDate);

        // Seed the weight chart with the registration weight on first install
        seedInitialWeightIfNeeded(sharedPref);

        checkAndLoadData();

        // --- Water ---
        btnUpdateWater.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), WaterAddActivity.class);
            intent.putExtra("CURRENT_WATER", currentWaterValue);
            intent.putExtra("TARGET_WATER", waterGoalMl);
            startActivity(intent);
        });

        // --- Workout ---
        btnLogWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddExerciseActivity.class);
            startActivity(intent);
        });

        // --- Weight ---
        btnAddWeight.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UpdateWeightActivity.class);
            startActivity(intent);
        });

        // --- Step Goal Edit ---
        btnEditStepGoal.setOnClickListener(v -> showEditStepGoalDialog());

        if (stepCounterSensor == null) {
            tvStepCountValue.setText("N/A");
        }

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted! Start the sensor immediately
                registerStepSensor();
            } else {
                Toast.makeText(getContext(), "Permission denied. Steps cannot be tracked.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerStepSensor() {
        if (stepCounterSensor != null && sensorManager != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


     //On first install, the weight_progress table is empty.
     //We seed it with the weight the user entered at registration
     //so the chart always has a starting point.

    private void seedInitialWeightIfNeeded(SharedPreferences sharedPref) {

        boolean alreadySeeded = sharedPref.getBoolean(PREF_WEIGHT_SEEDED, false);

        if (alreadySeeded) return;

        // Only seed if weight_progress is truly empty for this user
        ArrayList<WeightEntry> history = myDb.getWeightHistoryList(currentUserEmail);

        if (history.isEmpty()) {

            ArrayList<PersonalInfo> users = myDb.getUserList(currentUserEmail);

            if (!users.isEmpty()) {

                String rawWeight = users.get(0).getWeight();

                try {
                    double registrationWeight = Double.parseDouble(rawWeight);

                    // Use today as the seed date (registration day approximation)
                    myDb.addWeightEntry(currentUserEmail, registrationWeight, todayDate);

                } catch (NumberFormatException ignored) { }
            }
        }

        // Mark as seeded so we never do this again
        sharedPref.edit().putBoolean(PREF_WEIGHT_SEEDED, true).apply();
    }

    private void initialize() {

        // Calories
        progressCaloriesBar = view.findViewById(R.id.progressCaloriesBar);
        tvRemainingValue    = view.findViewById(R.id.tvRemainingValue);
        tvBaseGoal          = view.findViewById(R.id.tvBaseGoal);
        tvFoodEaten         = view.findViewById(R.id.tvFoodEaten);
        tvProteinLabel      = view.findViewById(R.id.tvProteinLabel);

        // Steps
        pbStepsProgress  = view.findViewById(R.id.pbStepsProgress);
        tvStepCountValue = view.findViewById(R.id.tvStepCountValue);
        btnEditStepGoal  = view.findViewById(R.id.btnEditStepGoal);
        tvStepGoalLabel  = view.findViewById(R.id.tvStepGoalLabel);

        // Water
        tvTargetWater  = view.findViewById(R.id.tvWaterTarget);
        tvWaterCount   = view.findViewById(R.id.tvWaterCount);
        pbWaterIntake  = view.findViewById(R.id.pbWaterProgress);
        btnUpdateWater = view.findViewById(R.id.btnUpdateWater);

        // Workout
        tvRecentWorkoutStatus = view.findViewById(R.id.tvRecentWorkoutStatus);
        imgBackgroundWorkout = view.findViewById(R.id.bgWorkout);
        btnLogWorkout = view.findViewById(R.id.btnLogWorkout);

        // Weight
        weightLineChart = view.findViewById(R.id.weightLineChart);
        btnAddWeight    = view.findViewById(R.id.btnAddWeight);

        myDb = new MyDatabaseHelper(requireContext());

        sensorManager = (SensorManager)
                requireContext().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            stepCounterSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }

    private void refreshDashboardStats() {

        ArrayList<Integer> dailyData =
                myDb.getDailyStats(currentUserEmail, todayDate);

        if (dailyData.size() >= 4) {

            int water    = dailyData.get(0);
            int steps    = dailyData.get(1);
            int calories = dailyData.get(2);
            int protein  = dailyData.get(3);

            // Water
            currentWaterValue = water;
            tvWaterCount.setText(water + " ml");
            pbWaterIntake.setProgress(Math.min(water, pbWaterIntake.getMax()));

            // Steps — only update from DB if sensor hasn't fired yet this session
            // The sensor callback keeps the live count; DB is the floor on resume
            String displayed = tvStepCountValue.getText().toString();
            if (displayed.equals("0") || displayed.equals("N/A")) {
                tvStepCountValue.setText(String.valueOf(steps));
                pbStepsProgress.setProgress(Math.min(steps, pbStepsProgress.getMax()));
            }

            // Calories remaining
            int calorieGoal = progressCaloriesBar.getMax() > 0
                    ? progressCaloriesBar.getMax()
                    : 2000;

            int remaining = Math.max(0, calorieGoal - calories);
            tvRemainingValue.setText(String.valueOf(remaining));
            progressCaloriesBar.setProgress(Math.min(calories, progressCaloriesBar.getMax()));
            tvFoodEaten.setText("Food: " + calories + " kcal");

            // Protein
            tvProteinLabel.setText("Protein: " + protein + "g");
        }

        refreshWorkoutStatus();
        loadWeightChart();
    }

    private void refreshWorkoutStatus() {

        ArrayList<String> todayWorkouts =
                myDb.getWorkoutsForDate(currentUserEmail, todayDate);

        if (todayWorkouts.isEmpty()) {
            tvRecentWorkoutStatus.setText("No workouts logged today.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String w : todayWorkouts) {
                sb.append("• ").append(w).append("\n");
            }
            tvRecentWorkoutStatus.setText(sb.toString().trim());
            imgBackgroundWorkout.setImageResource(R.drawable.heronawake);

        }
    }

    private void loadWeightChart() {

        ArrayList<WeightEntry> history = myDb.getWeightHistoryList(currentUserEmail);

        if (history.isEmpty()) {
            weightLineChart.setNoDataText("No weight data yet. Tap Add to log your weight.");
            weightLineChart.setNoDataTextColor(
                    requireContext().getResources().getColor(android.R.color.darker_gray, null)
            );
            weightLineChart.invalidate();
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels  = new ArrayList<>();

        for (int i = 0; i < history.size(); i++) {
            WeightEntry we = history.get(i);
            entries.add(new Entry(i, (float) we.getWeight()));
            labels.add(formatChartDate(we.getDate()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weight (kg)");
        dataSet.setColor(0xFF2D9CDB);
        dataSet.setCircleColor(0xFF2D9CDB);
        dataSet.setValueTextColor(0xFF000000);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        weightLineChart.setData(lineData);

        XAxis xAxis = weightLineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-30f);

        weightLineChart.getAxisRight().setEnabled(false);
        weightLineChart.getDescription().setEnabled(false);
        weightLineChart.getLegend().setEnabled(false);
        weightLineChart.animateX(500);
        weightLineChart.invalidate();
    }

    private String formatChartDate(String rawDate) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(rawDate);
            return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(d);
        } catch (Exception e) {
            return rawDate;
        }
    }

    private void showEditStepGoalDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Set Step Goal");

        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g. 10000");
        input.setText(String.valueOf(pbStepsProgress.getMax()));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {

            String val = input.getText().toString().trim();

            if (!val.isEmpty()) {
                try {
                    int newGoal = Integer.parseInt(val);

                    if (newGoal > 0) {

                        stepGoal = newGoal;

                        ArrayList<PersonalInfo> users = myDb.getUserList(currentUserEmail);

                        if (!users.isEmpty()) {
                            PersonalInfo user = users.get(0);
                            myDb.updateGoals(
                                    currentUserEmail,
                                    user.getCalorieGoal(),
                                    stepGoal,
                                    user.getWaterGoal(),
                                    user.getProteinGoal()
                            );
                        }

                        pbStepsProgress.setMax(stepGoal);
                        tvStepGoalLabel.setText("/ " + stepGoal + " steps");

                        Toast.makeText(getContext(), "Step goal updated!", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getContext(), "Please enter a positive step goal.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid number.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        // FIX: re-register sensor every onResume — this is what makes steps update live
        if (stepCounterSensor != null && sensorManager != null) {
            sensorManager.registerListener(
                    this,
                    stepCounterSensor,
                    SensorManager.SENSOR_DELAY_NORMAL   // SENSOR_DELAY_NORMAL is better than UI for steps
            );
        }

        refreshDashboardStats();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;

        int totalStepsSinceReboot = (int) event.values[0];
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        int savedOffset = sharedPref.getInt(PREF_STEPS_OFFSET, -1);

        if (savedOffset == -1 || totalStepsSinceReboot < savedOffset) {
            savedOffset = totalStepsSinceReboot;
            sharedPref.edit().putInt(PREF_STEPS_OFFSET, savedOffset).apply();
        }

        int stepsToday = totalStepsSinceReboot - savedOffset;

        // --- FIX STARTS HERE ---
        // Use a post() or ensure you are on the Main Thread for UI
        if (isAdded() && getView() != null) {
            requireActivity().runOnUiThread(() -> {
                tvStepCountValue.setText(String.valueOf(stepsToday));

                // Ensure the progress bar is actually using the latest Max
                pbStepsProgress.setProgress(stepsToday);

                // Debug: Check if the Max is what you expect
                // Log.d("STEP_DEBUG", "Steps: " + stepsToday + " Max: " + pbStepsProgress.getMax());
            });
        }

        // Optimization: Only update DB every 5 steps to prevent lag
        if (stepsToday % 5 == 0) {
            myDb.updateDailyValue(currentUserEmail, todayDate, MyDatabaseHelper.COL_CUR_STEPS, stepsToday);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    private void calculateInitialGoals(PersonalInfo user) {

        double weight   = Double.parseDouble(user.getWeight());
        double height   = Double.parseDouble(user.getHeight());
        int age         = calculateAge(user.getDateOfBirth());
        String gender   = user.getGender();
        String activity = user.getActivityLevel();
        String goal     = user.getGoal();

        // BMR (Mifflin-St Jeor)
        double bmr;

        if (gender.equalsIgnoreCase("Male")) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        // TDEE
        double tdee;

        switch (activity) {
            case "Lightly Active": tdee = bmr * 1.375; break;
            case "Active":         tdee = bmr * 1.55;  break;
            case "Very Active":    tdee = bmr * 1.725; break;
            default:               tdee = bmr * 1.2;   break;
        }

        // Goal adjustment
        int finalCalorieGoal;

        if (goal.equalsIgnoreCase("Lose Weight")) {
            finalCalorieGoal = (int) (tdee - 500);
        } else if (goal.equalsIgnoreCase("Gain Muscle")) {
            finalCalorieGoal = (int) (tdee + 300);
        } else {
            finalCalorieGoal = (int) tdee;
        }

        // Water goal
        double weightLbs = weight * 2.20462;
        double waterOz   = weightLbs / 2.0;
        waterGoalMl      = (int) (waterOz * 29.57);

        int proteinGoal = (int) (weight * 2.0);
        stepGoal = 7000;

        myDb.updateGoals(
                currentUserEmail,
                finalCalorieGoal,
                stepGoal,
                waterGoalMl,
                proteinGoal
        );

        displayGoals(finalCalorieGoal, proteinGoal, waterGoalMl, stepGoal);
    }

    private int calculateAge(String dobString) {

        try {
            String[] parts = dobString.split("/");

            int day   = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year  = Integer.parseInt(parts[2]);

            Calendar dob   = Calendar.getInstance();
            dob.set(year, month, day);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;

        } catch (Exception e) {
            return 25;
        }
    }

    private void checkAndLoadData() {

        ArrayList<PersonalInfo> users = myDb.getUserList(currentUserEmail);

        if (!users.isEmpty()) {

            PersonalInfo user = users.get(0);

            if (user.getCalorieGoal() == 0) {
                calculateInitialGoals(user);
            } else {
                displayGoals(
                        user.getCalorieGoal(),
                        user.getProteinGoal(),
                        user.getWaterGoal(),
                        user.getStepGoal()
                );
            }
        }
    }

    private void displayGoals(int calories, int protein, int water, int steps) {

        waterGoalMl = water;
        stepGoal    = steps;

        // Calories
        tvBaseGoal.setText("Base Goal: " + calories);
        tvRemainingValue.setText(String.valueOf(calories));
        progressCaloriesBar.setMax(calories);
        tvProteinLabel.setText("Protein: 0g / " + protein + "g");

        // Steps
        tvStepGoalLabel.setText("/ " + steps + " steps");
        pbStepsProgress.setMax(steps);

        // Water
        tvTargetWater.setText("Target: " + water + " ml");
        pbWaterIntake.setMax(water);
    }
}