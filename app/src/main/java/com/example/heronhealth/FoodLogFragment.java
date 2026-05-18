package com.example.heronhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.heronhealth.model.FoodEntry;
import com.example.heronhealth.model.PersonalInfo;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FoodLogFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View view;
    private TextView tvGoalCal, tvFoodCal,
            tvExerciseCal, tvRemainingCal;

    // Date nav
    private ImageButton btnPrevDay, btnNextDay;
    private TextView tvSelectedDate, tvWaterValue, tvExercises;

    // Meal add buttons
    private MaterialButton btnAddBreakfast, btnAddLunch, btnAddDinner, btnAddWorkOut, btnAddWater;

    // Meal food list containers (dynamically populated)
    private LinearLayout llBreakfastItems, llLunchItems, llDinnerItems;

    private MyDatabaseHelper myDb;
    private String currentUserEmail;

    // Calendar to track currently viewed date
    private Calendar selectedCalendar;

    public FoodLogFragment() {
        // Required empty public constructor
    }

    public static FoodLogFragment newInstance(String param1, String param2) {
        FoodLogFragment fragment = new FoodLogFragment();
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

        view = inflater.inflate(R.layout.fragment_food_log, container, false);

        initialize();

        // Start on today
        selectedCalendar = Calendar.getInstance();

        updateDateLabel();
        loadMealData();
        loadCalorieSummary();

        // Date navigation
        btnPrevDay.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_YEAR, -1);
            updateDateLabel();
            loadMealData();
            loadCalorieSummary();
        });

        btnNextDay.setOnClickListener(v -> {

            // Don't allow navigating into the future
            Calendar today = Calendar.getInstance();
            if (!isSameDay(selectedCalendar, today)) {
                selectedCalendar.add(Calendar.DAY_OF_YEAR, 1);
                updateDateLabel();
                loadMealData();
                loadCalorieSummary();
            }
        });

        // Meal add buttons — launch FoodSearchActivity with the meal type
        btnAddBreakfast.setOnClickListener(v -> openFoodSearch("Breakfast"));
        btnAddLunch.setOnClickListener(v    -> openFoodSearch("Lunch"));
        btnAddDinner.setOnClickListener(v   -> openFoodSearch("Dinner"));

        btnAddWorkOut.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), AddExerciseActivity.class);
            startActivity(intent);
        });
        btnAddWater.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), WaterAddActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh when returning from FoodSearchActivity
        loadMealData();
        loadCalorieSummary();
    }

    private void initialize() {

        btnPrevDay      = view.findViewById(R.id.btnPrevDay);
        btnNextDay      = view.findViewById(R.id.btnNextDay);
        tvSelectedDate  = view.findViewById(R.id.tvSelectedDate);
        tvWaterValue = view.findViewById(R.id.tvWaterValue);
        tvExercises = view.findViewById(R.id.tvExercises);

        btnAddBreakfast = view.findViewById(R.id.btnAddBreakfast);
        btnAddLunch     = view.findViewById(R.id.btnAddLunch);
        btnAddDinner    = view.findViewById(R.id.btnAddDinner);
        btnAddWorkOut = view.findViewById(R.id.btnAddExercise);
        btnAddWater = view.findViewById(R.id.btnAddWater);

        llBreakfastItems = view.findViewById(R.id.llBreakfastItems);
        llLunchItems     = view.findViewById(R.id.llLunchItems);
        llDinnerItems    = view.findViewById(R.id.llDinnerItems);
        tvGoalCal      = view.findViewById(R.id.tvGoalCal);
        tvFoodCal      = view.findViewById(R.id.tvFoodCal);
        tvExerciseCal  = view.findViewById(R.id.tvExerciseCal);
        tvRemainingCal = view.findViewById(R.id.tvRemainingCal);

        myDb = new MyDatabaseHelper(requireContext());

        SharedPreferences sharedPref = requireActivity()
                .getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);

        currentUserEmail = sharedPref.getString("userEmail", "");
    }

    private void updateDateLabel() {

        Calendar today = Calendar.getInstance();

        if (isSameDay(selectedCalendar, today)) {
            tvSelectedDate.setText("Today");
        } else {
            String label = new SimpleDateFormat(
                    "MMM dd, yyyy",
                    Locale.getDefault()
            ).format(selectedCalendar.getTime());
            tvSelectedDate.setText(label);
        }
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR)         == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private String getSelectedDateString() {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(selectedCalendar.getTime());
    }

    private void loadMealData() {

        String date = getSelectedDateString();

        populateMealItems(
                myDb.getFoodByMeal(currentUserEmail, date, "Breakfast"),
                llBreakfastItems
        );

        populateMealItems(
                myDb.getFoodByMeal(currentUserEmail, date, "Lunch"),
                llLunchItems
        );

        populateMealItems(
                myDb.getFoodByMeal(currentUserEmail, date, "Dinner"),
                llDinnerItems
        );

        ArrayList<Integer> dailyData =
                myDb.getDailyStats(currentUserEmail, date);

        int water    = dailyData.get(0);

        tvWaterValue.setText(water + "ml");

        ArrayList<String> todayWorkouts =
                myDb.getWorkoutsForDate(currentUserEmail, date);

        if (todayWorkouts.isEmpty()) {
            tvExercises.setText("No workouts logged today.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String w : todayWorkouts) {
                sb.append("• ").append(w).append("\n");
            }
            tvExercises.setText(sb.toString().trim());
        }

    }

    /**
     * Clears the container then adds one TextView row per food entry.
     */
    private void populateMealItems(ArrayList<FoodEntry> entries, LinearLayout container) {

        container.removeAllViews();

        if (entries.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Nothing logged yet.");
            empty.setTextColor(0xFF7A8A99);
            empty.setTextSize(13f);
            empty.setPadding(0, 8, 0, 4);
            container.addView(empty);
            return;
        }

        for (FoodEntry food : entries) {

            TextView tv = new TextView(requireContext());

            tv.setText(
                    "• " + food.getName()
                            + "  —  " + (int) food.getServingSize() + food.getUnit()
                            + "  |  " + food.getCalories() + " kcal"
                            + "  |  " + food.getProtein() + "g protein"
            );

            tv.setTextColor(0xFF2C3E50);
            tv.setTextSize(13f);
            tv.setPadding(0, 6, 0, 6);

            tv.setClickable(true);
            tv.setFocusable(true);

            // LONG CLICK TO DELETE
            tv.setOnLongClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Delete Entry")
                        .setMessage("Are you sure you want to remove \"" + food.getName() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {


                            boolean isDeleted = myDb.deleteFoodEntry(food.getId(), currentUserEmail, getSelectedDateString());

                            if (isDeleted) {
                                android.widget.Toast.makeText(getContext(),
                                        "Entry removed", android.widget.Toast.LENGTH_SHORT).show();

                                // Refresh layout instantly with fresh DB values
                                loadMealData();
                            } else {
                                android.widget.Toast.makeText(getContext(),
                                        "Failed to delete entry", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return true;
            });

            container.addView(tv);
        }
    }

    private void openFoodSearch(String mealType) {

        Intent intent = new Intent(getContext(), FoodSearchActivity.class);
        intent.putExtra("MEAL_TYPE", mealType);
        intent.putExtra("SELECTED_DATE", getSelectedDateString());
        startActivity(intent);
    }
    private void loadCalorieSummary() {

        String date = getSelectedDateString();

        // Get user info
        ArrayList<PersonalInfo> users =
                myDb.getUserList(currentUserEmail);

        int goalCalories = 0;

        if (!users.isEmpty()) {

            goalCalories =
                    users.get(0).getCalorieGoal();
        }

        // Daily stats already stores food calories
        ArrayList<Integer> stats =
                myDb.getDailyStats(currentUserEmail, date);

        int foodCalories = stats.get(2);

        // Exercise calories
        int exerciseCalories =
                myDb.getExerciseCalories(
                        currentUserEmail,
                        date
                );

        // Remaining
        int remaining =
                goalCalories
                        - foodCalories
                        + exerciseCalories;

        // Update UI
        tvGoalCal.setText(
                String.valueOf(goalCalories));

        tvFoodCal.setText(
                String.valueOf(foodCalories));

        tvExerciseCal.setText(
                String.valueOf(exerciseCalories));

        tvRemainingCal.setText(
                String.valueOf(remaining));
    }
}