package com.example.heronhealth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.heronhealth.model.PersonalInfo;
import com.example.heronhealth.model.StepEntry;
import com.example.heronhealth.model.WeightEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1, mParam2;

    private View view;

    // Calories
    private ProgressBar progressCaloriesBar;
    private TextView tvRemainingValue, tvBaseGoal, tvFoodEaten, tvProteinLabel;
    private MaterialCardView caloriesCardView;

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
    private ImageView imgBackgroundWorkout;

    // Weight + Steps charts
    private LineChart weightLineChart, stepLineChart;
    private MaterialButton btnAddWeight ,btnAddWater;

    // Weekly habits
    private TextView tvHabitSubtitle;
    private MaterialButton btnStartHabit;

    private FrameLayout[] dayFrames    = new FrameLayout[7];
    private TextView[]    dayDateTexts = new TextView[7];
    private ImageView[]   dayChecks    = new ImageView[7];

    private MyDatabaseHelper myDb;
    private String currentUserEmail;
    private String todayDate;

    // ── Goal fields — single source of truth ────────────────────────────────
    private int calorieGoalField = 0; // 0 means "not loaded yet"
    private int proteinGoalField = 0;
    private int waterGoalMl      = 0;
    private int stepGoal         = 0;
    private int currentWaterValue = 0;

    private final List<String> weekDates = new ArrayList<>();

    private static final String PREF_STEPS_OFFSET  = "steps_at_start_of_day";
    private static final String PREF_LAST_LOG_DATE = "last_log_date";
    private static final String PREF_WEIGHT_SEEDED = "weight_chart_seeded";

    private final BroadcastReceiver stepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isAdded() || getView() == null) return;
            int stepsToday = intent.getIntExtra(StepCounterService.EXTRA_STEPS_TODAY, 0);
            tvStepCountValue.setText(String.valueOf(stepsToday));
            pbStepsProgress.setProgress(Math.min(stepsToday, pbStepsProgress.getMax()));
            autoCheckTodayHabit();
        }
    };

    public DashboardFragment() { }

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

        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }

        Intent serviceIntent = new Intent(getContext(), StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
        } else {
            requireContext().startService(serviceIntent);
        }

        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        SharedPreferences sharedPref = requireActivity()
                .getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        String lastSavedDate = sharedPref.getString(PREF_LAST_LOG_DATE, "");
        if (!lastSavedDate.equals(todayDate)) {
            sharedPref.edit()
                    .putString(PREF_LAST_LOG_DATE, todayDate)
                    .putInt(PREF_STEPS_OFFSET, -1)
                    .apply();
        }

        initialize();

        currentUserEmail = sharedPref.getString("userEmail", "");
        myDb.checkAndInitDailyLog(currentUserEmail, todayDate);
        seedInitialWeightIfNeeded(sharedPref);

        // Load goals first — populates calorieGoalField, proteinGoalField, etc.
        checkAndLoadData();

        buildWeekDates();

        // Click listeners
       /* caloriesCardView.setOnClickListener(v ->
                startActivity(new Intent(getContext(), NutritionActivity.class)));*/

        btnUpdateWater.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), WaterAddActivity.class);
            intent.putExtra("CURRENT_WATER", currentWaterValue);
            intent.putExtra("TARGET_WATER", waterGoalMl);
            startActivity(intent);
        });


        btnLogWorkout.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddExerciseActivity.class)));

        btnAddWeight.setOnClickListener(v ->
                startActivity(new Intent(getContext(), UpdateWeightActivity.class)));

        btnEditStepGoal.setOnClickListener(v -> showEditStepGoalDialog());

        btnStartHabit.setOnClickListener(v ->
                startActivity(new Intent(getContext(), HabitPickerActivity.class)));

        for (int i = 0; i < 7; i++) {
            final int idx = i;
            dayFrames[i].setOnClickListener(v -> toggleHabitDay(idx));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(StepCounterService.ACTION_STEPS_UPDATED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(stepReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(stepReceiver, filter);
        }

        // Re-load goals in case they changed (e.g. user updated profile)
        checkAndLoadData();

        // Then refresh stats using the freshly loaded goal fields
        refreshDashboardStats();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(stepReceiver);
        } catch (IllegalArgumentException ignored) { }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myDb != null) {
            myDb.close();
            myDb = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(getContext(), StepCounterService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(serviceIntent);
            } else {
                requireContext().startService(serviceIntent);
            }
        } else if (requestCode == 1) {
            Toast.makeText(getContext(),
                    "Permission denied. Steps cannot be tracked.", Toast.LENGTH_SHORT).show();
        }
    }

    // ── WEEKLY HABITS ────────────────────────────────────────────────────────

    private void buildWeekDates() {
        weekDates.clear();
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int daysBack = (dow == Calendar.SUNDAY) ? 6 : dow - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_YEAR, -daysBack);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            weekDates.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void refreshHabitsCard() {
        if (!isAdded() || getContext() == null) return;

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        String habitName  = prefs.getString(HabitPickerActivity.PREF_HABIT_NAME,  "");
        String habitEmoji = prefs.getString(HabitPickerActivity.PREF_HABIT_EMOJI, "");
        String weekStart  = prefs.getString(HabitPickerActivity.PREF_HABIT_WEEK_START, "");

        if (habitName.isEmpty()) {
            tvHabitSubtitle.setText("Choose a habit to track this week.");
            btnStartHabit.setText("Start a habit");
            for (int i = 0; i < 7; i++) {
                dayFrames[i].setAlpha(0.35f);
                dayChecks[i].setVisibility(View.GONE);
            }
            return;
        }

        tvHabitSubtitle.setText(habitEmoji + " " + habitName);
        btnStartHabit.setText("Change habit");

        Calendar cal = Calendar.getInstance();
        try {
            Date start = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(weekStart);
            cal.setTime(start);
        } catch (Exception ignored) { }

        for (int i = 0; i < 7; i++) {
            dayDateTexts[i].setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            dayFrames[i].setAlpha(1f);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        HashSet<String> doneDates = myDb.getCompletedHabitDates(currentUserEmail, weekDates);

        for (String date : weekDates) {
            if (!doneDates.contains(date)) {
                if (myDb.doesDailyDataSatisfyHabit(currentUserEmail, date, habitName)) {
                    myDb.setHabitDone(currentUserEmail, date, true);
                    doneDates.add(date);
                }
            }
        }

        for (int i = 0; i < 7; i++) {
            boolean done = doneDates.contains(weekDates.get(i));
            dayFrames[i].setBackgroundResource(done
                    ? R.drawable.circle_done : R.drawable.box_uncheck);
            dayChecks[i].setVisibility(done ? View.VISIBLE : View.GONE);
            dayDateTexts[i].setVisibility(done ? View.GONE : View.VISIBLE);
        }
    }

    private void toggleHabitDay(int index) {
        if (!isAdded() || getContext() == null) return;
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);
        String habitName = prefs.getString(HabitPickerActivity.PREF_HABIT_NAME, "");
        if (habitName.isEmpty()) {
            Toast.makeText(getContext(), "Pick a habit first!", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = weekDates.get(index);
        if (date.compareTo(todayDate) > 0) {
            Toast.makeText(getContext(), "Can't check a future day.", Toast.LENGTH_SHORT).show();
            return;
        }
        HashSet<String> done = myDb.getCompletedHabitDates(currentUserEmail, weekDates);
        myDb.setHabitDone(currentUserEmail, date, !done.contains(date));
        refreshHabitsCard();
    }

    private void autoCheckTodayHabit() {
        if (!isAdded() || getContext() == null) return;
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);
        String habitName = prefs.getString(HabitPickerActivity.PREF_HABIT_NAME, "");
        if (habitName.equals("Hit my step goal") || habitName.equals("Get more exercise")) {
            refreshHabitsCard();
        }
    }

    // ── INIT ─────────────────────────────────────────────────────────────────

    private void initialize() {
        caloriesCardView    = view.findViewById(R.id.cardCalories);
        progressCaloriesBar = view.findViewById(R.id.progressCaloriesBar);
        tvRemainingValue    = view.findViewById(R.id.tvRemainingValue);
        tvBaseGoal          = view.findViewById(R.id.tvBaseGoal);
        tvFoodEaten         = view.findViewById(R.id.tvFoodEaten);
        tvProteinLabel      = view.findViewById(R.id.tvProteinLabel);

        pbStepsProgress  = view.findViewById(R.id.pbStepsProgress);
        tvStepCountValue = view.findViewById(R.id.tvStepCountValue);
        btnEditStepGoal  = view.findViewById(R.id.btnEditStepGoal);
        tvStepGoalLabel  = view.findViewById(R.id.tvStepGoalLabel);

        tvTargetWater  = view.findViewById(R.id.tvWaterTarget);
        tvWaterCount   = view.findViewById(R.id.tvWaterCount);
        pbWaterIntake  = view.findViewById(R.id.pbWaterProgress);
        btnUpdateWater = view.findViewById(R.id.btnUpdateWater);

        tvRecentWorkoutStatus = view.findViewById(R.id.tvRecentWorkoutStatus);
        imgBackgroundWorkout  = view.findViewById(R.id.bgWorkout);
        btnLogWorkout         = view.findViewById(R.id.btnLogWorkout);

        weightLineChart = view.findViewById(R.id.weightLineChart);
        stepLineChart   = view.findViewById(R.id.StepsLineChart);
        btnAddWeight    = view.findViewById(R.id.btnAddWeight);

        tvHabitSubtitle = view.findViewById(R.id.tvHabitSubtitle);
        btnStartHabit   = view.findViewById(R.id.btnStartHabit);

        btnAddWater = view.findViewById(R.id.btnAddWeight);

        int[] frameIds = {R.id.frameMonDay, R.id.frameTueDay, R.id.frameWedDay,
                R.id.frameThuDay, R.id.frameFriDay, R.id.frameSatDay, R.id.frameSunDay};
        int[] dateIds  = {R.id.tvMon, R.id.tvTue, R.id.tvWed,
                R.id.tvThu, R.id.tvFri, R.id.tvSat, R.id.tvSun};
        int[] checkIds = {R.id.imgMon, R.id.imgTue, R.id.imgWed,
                R.id.imgThu, R.id.imgFri, R.id.imgSat, R.id.imgSun};

        for (int i = 0; i < 7; i++) {
            dayFrames[i]    = view.findViewById(frameIds[i]);
            dayDateTexts[i] = view.findViewById(dateIds[i]);
            dayChecks[i]    = view.findViewById(checkIds[i]);
        }

        myDb = new MyDatabaseHelper(requireContext());
    }

    // ── GOALS ────────────────────────────────────────────────────────────────

    /**
     * Loads the user's goals from the DB into the goal fields AND updates the UI.
     * Must be called before refreshDashboardStats().
     */
    private void checkAndLoadData() {
            ArrayList<PersonalInfo> users = myDb.getUserList(currentUserEmail);
            if (!users.isEmpty()) {
                PersonalInfo user = users.get(0);
                calorieGoalField = user.getCalorieGoal();
                proteinGoalField = user.getProteinGoal();
                waterGoalMl      = user.getWaterGoal();
                stepGoal         = user.getStepGoal();
                applyGoalsToUI();
            }
    }

    private void calculateInitialGoals(PersonalInfo user) {
        double weightKg = Double.parseDouble(user.getWeight());
        double heightCm = Double.parseDouble(user.getHeight());
        int age          = calculateAge(user.getDateOfBirth());
        String gender    = user.getGender();
        String activity  = user.getActivityLevel();
        String goal      = user.getGoal();

        // ── Step 1: BMR (Mifflin-St Jeor) ───────────────────────────────────────
        double bmr = gender.equalsIgnoreCase("Male")
                ? (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
                : (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;

        // ── Step 2: TDEE ─────────────────────────────────────────────────────────
        double tdee;
        switch (activity) {
            case "Lightly Active": tdee = bmr * 1.375; break;
            case "Active":         tdee = bmr * 1.55;  break;
            case "Very Active":    tdee = bmr * 1.725; break;
            default:               tdee = bmr * 1.2;   break; // Sedentary
        }

        // ── Step 3: Calorie goal based on user goal ───────────────────────────────
        calorieGoalField = goal.equalsIgnoreCase("Lose Weight") ? (int)(tdee - 500)
                : goal.equalsIgnoreCase("Gain Muscle")          ? (int)(tdee + 300)
                : (int) tdee;

        // ── Step 4: Macros (NASM guidelines) ─────────────────────────────────────

        // Protein: 1.6g/kg for active users (midpoint of 1.4–2.2 range), 1.1g/kg sedentary
        boolean isActive = activity.equals("Active") || activity.equals("Very Active");
        proteinGoalField = isActive
                ? (int)(weightKg * 1.6)
                : (int)(weightKg * 1.1);

        // Fat: minimum 1g/kg (NASM recommendation)
        int fatGoal = (int)(weightKg * 1.0);

        // Carbs: 55% of total calories is the midpoint of the 45–65% NASM range
        // 4 calories per gram of carbohydrate
        int carbGoal = (int)((calorieGoalField * 0.55) / 4.0);

        // Water: 1 oz per 2 lbs body weight, converted to ml (1 oz = 29.5735 ml)
        // weightKg → lbs = weightKg * 2.20462
        double weightLbs = weightKg * 2.20462;
        waterGoalMl = (int)((weightLbs / 2.0) * 29.5735);

        // Steps: sensible default
        stepGoal = 7000;

        // ── Persist to DB ─────────────────────────────────────────────────────────
        myDb.updateGoals(currentUserEmail, calorieGoalField, stepGoal, waterGoalMl, proteinGoalField);
        myDb.updateMacroGoals(currentUserEmail, carbGoal, fatGoal, 30, 50, 20, 15);

        applyGoalsToUI();
    }

    /**
     * Pushes the goal fields to all UI elements.
     * Separated from the field-loading logic so it's always consistent.
     */
    private void applyGoalsToUI() {
        tvBaseGoal.setText("Base Goal: " + calorieGoalField);
        tvRemainingValue.setText(String.valueOf(calorieGoalField));
        progressCaloriesBar.setMax(calorieGoalField);

        tvProteinLabel.setText("Protein: 0g / " + proteinGoalField + "g");

        tvStepGoalLabel.setText("/ " + stepGoal + " steps");
        pbStepsProgress.setMax(stepGoal);

        tvTargetWater.setText("Target: " + waterGoalMl + " ml");
        pbWaterIntake.setMax(waterGoalMl);
    }

    private int calculateAge(String dobString) {
        try {
            String[] parts = dobString.split("/");
            Calendar dob = Calendar.getInstance();
            dob.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--;
            return age;
        } catch (Exception e) { return 25; }
    }

    // ── STATS ────────────────────────────────────────────────────────────────

    private void refreshDashboardStats() {
        if (calorieGoalField == 0) return; // goals not loaded yet, skip

        ArrayList<Integer> dailyData = myDb.getDailyStats(currentUserEmail, todayDate);

        if (dailyData.size() >= 4) {
            int water    = dailyData.get(0);
            int steps    = dailyData.get(1);
            int calories = dailyData.get(2);
            int protein  = dailyData.get(3);

            currentWaterValue = water;

            // Water
            tvWaterCount.setText(water + " ml");
            pbWaterIntake.setProgress(Math.min(water, waterGoalMl));

            // Steps
            tvStepCountValue.setText(String.valueOf(steps));
            pbStepsProgress.setProgress(Math.min(steps, stepGoal));

            // Calories — always use calorieGoalField, never progressCaloriesBar.getMax()
            int remaining = Math.max(0, calorieGoalField - calories);
            tvRemainingValue.setText(String.valueOf(remaining));
            progressCaloriesBar.setProgress(Math.min(calories, calorieGoalField));
            tvFoodEaten.setText("Food: " + calories + " kcal");

            // Protein — always use proteinGoalField
            tvProteinLabel.setText("Protein: " + protein + "g / " + proteinGoalField + "g");
        }

        refreshWorkoutStatus();
        loadWeightChart();
        loadStepsChart();
        refreshHabitsCard();
    }

    private void refreshWorkoutStatus() {
        ArrayList<String> todayWorkouts = myDb.getWorkoutsForDate(currentUserEmail, todayDate);
        if (todayWorkouts.isEmpty()) {
            tvRecentWorkoutStatus.setText("No workouts logged today.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String w : todayWorkouts) sb.append("• ").append(w).append("\n");
            tvRecentWorkoutStatus.setText(sb.toString().trim());
            imgBackgroundWorkout.setImageResource(R.drawable.heronawake);
        }
    }

    private void loadWeightChart() {
        ArrayList<WeightEntry> history = myDb.getWeightHistoryList(currentUserEmail);
        if (history.isEmpty()) {
            weightLineChart.setNoDataText("No weight data yet. Tap Add to log your weight.");
            weightLineChart.invalidate();
            return;
        }
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            WeightEntry we = history.get(i);
            entries.add(new Entry(i, (float) we.getWeight()));
            labels.add(formatChartDate(we.getDate()));
        }
        LineDataSet ds = new LineDataSet(entries, "Weight (kg)");
        ds.setColor(0xFF2D9CDB);       ds.setCircleColor(0xFF2D9CDB);
        ds.setValueTextColor(0xFF000000); ds.setLineWidth(2f);
        ds.setCircleRadius(4f);        ds.setDrawValues(true);
        ds.setValueTextSize(10f);      ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        weightLineChart.setData(new LineData(ds));
        XAxis x = weightLineChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f); x.setLabelRotationAngle(-30f);
        weightLineChart.getAxisRight().setEnabled(false);
        weightLineChart.getDescription().setEnabled(false);
        weightLineChart.getLegend().setEnabled(false);
        weightLineChart.animateX(500);
        weightLineChart.invalidate();
    }

    private void loadStepsChart() {
        if (stepLineChart == null) return;
        ArrayList<StepEntry> history = myDb.getStepsHistoryList(currentUserEmail);
        if (history.isEmpty()) {
            stepLineChart.setNoDataText("No steps data yet. Start moving!");
            stepLineChart.invalidate();
            return;
        }
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            StepEntry se = history.get(i);
            entries.add(new Entry(i, (float) se.getSteps()));
            labels.add(formatChartDate(se.getDate()));
        }
        LineDataSet ds = new LineDataSet(entries, "Steps");
        ds.setColor(0xFF4CAF50);       ds.setCircleColor(0xFF4CAF50);
        ds.setValueTextColor(0xFF000000); ds.setLineWidth(2f);
        ds.setCircleRadius(4f);        ds.setDrawValues(true);
        ds.setValueTextSize(10f);      ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        stepLineChart.setData(new LineData(ds));
        XAxis x = stepLineChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f); x.setLabelRotationAngle(-30f);
        stepLineChart.getAxisRight().setEnabled(false);
        stepLineChart.getDescription().setEnabled(false);
        stepLineChart.getLegend().setEnabled(false);
        stepLineChart.animateX(500);
        stepLineChart.invalidate();
    }

    private String formatChartDate(String rawDate) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(rawDate);
            return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(d);
        } catch (Exception e) { return rawDate; }
    }

    // ── STEP GOAL DIALOG ─────────────────────────────────────────────────────

    private void showEditStepGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Set Step Goal");
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g. 10000");
        input.setText(String.valueOf(stepGoal));
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
                            myDb.updateGoals(currentUserEmail, user.getCalorieGoal(),
                                    stepGoal, user.getWaterGoal(), user.getProteinGoal());
                        }
                        pbStepsProgress.setMax(stepGoal);
                        tvStepGoalLabel.setText("/ " + stepGoal + " steps");
                        Toast.makeText(getContext(), "Step goal updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Please enter a positive step goal.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid number.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // ── SEED WEIGHT ──────────────────────────────────────────────────────────

    private void seedInitialWeightIfNeeded(SharedPreferences sharedPref) {
        if (sharedPref.getBoolean(PREF_WEIGHT_SEEDED, false)) return;
        ArrayList<WeightEntry> history = myDb.getWeightHistoryList(currentUserEmail);
        if (history.isEmpty()) {
            ArrayList<PersonalInfo> users = myDb.getUserList(currentUserEmail);
            if (!users.isEmpty()) {
                try {
                    double w = Double.parseDouble(users.get(0).getWeight());
                    myDb.addWeightEntry(currentUserEmail, w, todayDate);
                } catch (NumberFormatException ignored) { }
            }
        }
        sharedPref.edit().putBoolean(PREF_WEIGHT_SEEDED, true).apply();
    }
}