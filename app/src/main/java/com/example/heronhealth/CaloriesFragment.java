package com.example.heronhealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.heronhealth.model.MacroTotals;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab 3 — Calories per meal (Breakfast / Lunch / Dinner / Snacks).
 * Shows a PieChart and summary rows for Total, Net, and Goal calories.
 * Layout: fragment_calories.xml  (your third XML file)
 */
public class CaloriesFragment extends Fragment {

    private static final String ARG_DATE  = "date";
    private static final String ARG_EMAIL = "email";

    private String selectedDate;
    private String userEmail;
    private MyDatabaseHelper db;

    private PieChart pieChart;
    private TextView tvBreakfastPct, tvLunchPct, tvDinnerPct, tvSnacksPct;
    private TextView tvTotalCalories, tvNetCalories, tvGoalCalories;

    // ── Factory ─────────────────────────────────────────────────────────────
    public static CaloriesFragment newInstance(String date) {
        CaloriesFragment f = new CaloriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        f.setArguments(args);
        return f;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = requireContext().getSharedPreferences("HeronHealthPrefs", Context.MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", null);
        if (getArguments() != null) {
            selectedDate = getArguments().getString(ARG_DATE);
        }
        db = new MyDatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }


    private void bindViews(View view) {
        pieChart         = view.findViewById(R.id.pieChart);
        tvBreakfastPct   = view.findViewById(R.id.tvBreakfastPct);
        tvLunchPct       = view.findViewById(R.id.tvLunchPct);
        tvDinnerPct      = view.findViewById(R.id.tvDinnerPct);
        tvSnacksPct      = view.findViewById(R.id.tvSnacksPct);
        tvTotalCalories  = view.findViewById(R.id.tvTotalCalories);
        tvNetCalories    = view.findViewById(R.id.tvNetCalories);
        tvGoalCalories   = view.findViewById(R.id.tvGoalCalories);
    }

    // ── Data loading ─────────────────────────────────────────────────────────
    private void loadData() {
        if (userEmail == null || selectedDate == null) {
            tvGoalCalories.setText("Nothing");
            return;
        }

        // [0]=Breakfast [1]=Lunch [2]=Dinner [3]=Snacks
        double[] mealCals = db.getCaloriesPerMeal(userEmail, selectedDate);
        MacroTotals goals = db.getMacroGoals(userEmail);

        double total = mealCals[0] + mealCals[1] + mealCals[2] + mealCals[3];

        // ── Pie chart ────────────────────────────────────────────────────────
        setupPieChart(mealCals, total);

        // ── Meal legend rows ─────────────────────────────────────────────────
        updateLegend(tvBreakfastPct, mealCals[0], total);
        updateLegend(tvLunchPct,     mealCals[1], total);
        updateLegend(tvDinnerPct,    mealCals[2], total);
        updateLegend(tvSnacksPct,    mealCals[3], total);

        // ── Summary rows ─────────────────────────────────────────────────────
        // Total calories eaten
        if (tvTotalCalories != null)
            tvTotalCalories.setText(String.format("%.0f", total));

        double exerciseBurn = db.getExerciseCalories(userEmail, selectedDate);
        double net = total - exerciseBurn;
        if (tvNetCalories != null)
            tvNetCalories.setText(String.format("%.0f", net));

        // Goal — blue
        if (tvGoalCalories != null) {
            if (goals.calories > 0) {
                tvGoalCalories.setText(String.format("%.0f", goals.calories));
            } else {
                tvGoalCalories.setText("—");
            }
            tvGoalCalories.setTextColor(Color.parseColor("#2D9CDB"));
        }
    }
    // ── Pie chart setup ──────────────────────────────────────────────────────
    private void setupPieChart(double[] cals, double total) {
        List<PieEntry> entries = new ArrayList<>();

        if (total == 0) {
            entries.add(new PieEntry(1f, "No data"));
            PieDataSet set = new PieDataSet(entries, "");
            set.setColor(Color.DKGRAY);
            set.setDrawValues(false);
            PieData data = new PieData(set);
            styleChart(pieChart);
            pieChart.setData(data);
            pieChart.setCenterText("No food\nlogged");
            pieChart.invalidate();
            return;
        }

        String[] labels = {"Breakfast", "Lunch", "Dinner", "Snacks"};
        int[] colors = {
                Color.parseColor("#3B82F6"),  // Breakfast – blue
                Color.parseColor("#60A5FA"),  // Lunch     – lighter blue
                Color.parseColor("#2563EB"),  // Dinner    – medium blue
                Color.parseColor("#1D4ED8")   // Snacks    – dark blue
        };


        List<Integer> usedColors = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (cals[i] > 0) {
                entries.add(new PieEntry((float) cals[i], labels[i]));
                usedColors.add(colors[i]);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(usedColors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        styleChart(pieChart);
        pieChart.setData(data);
        pieChart.setCenterText(String.format("%.0f\nkcal", total));
        pieChart.invalidate();
    }

    private void styleChart(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(60f);
        chart.setTransparentCircleColor(Color.parseColor("#22FFFFFF"));
        chart.setCenterTextColor(Color.WHITE);
        chart.setCenterTextSize(14f);
        chart.getLegend().setEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.animateY(800);
    }


     //Updates a meal legend TextView with the format:
     private void updateLegend(TextView tv, double mealCal, double total) {
         if (tv == null) return;
         String pct = total > 0
         ? String.format("%.0f%%", (mealCal / total) * 100)
         : "0%";

         String current  = tv.getText().toString();
         String mealName = current.contains("\n")
         ? current.substring(0, current.indexOf("\n"))
         : current;
         tv.setText(mealName + "\n" + pct + " (" + String.format("%.0f", mealCal) + " cal)");
         tv.setTextColor(Color.WHITE);
     }

    public void refreshData(String newDate) {
        this.selectedDate = newDate;
        loadData();
    }
}