package com.example.heronhealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
 * Tab 1 — Macros breakdown: Carbohydrates, Fat, Protein.
 * Layout: fragment_macros.xml  (your first XML file)
 *
 * This fragment receives the current date via newInstance() and refreshes
 * whenever refreshData() is called by the host Activity/Fragment.
 */
public class MacrosFragment extends Fragment {

    private static final String ARG_DATE  = "date";
    private static final String ARG_EMAIL = "email";

    private String selectedDate;
    private String userEmail;

    private PieChart pieChart;
    private TextView tvCarbsTotal, tvCarbsGoal;
    private TextView tvFatTotal,   tvFatGoal;
    private TextView tvProteinTotal, tvProteinGoal;
    private TextView tvCarbsLabel, tvFatLabel, tvProteinLabel;

    private MyDatabaseHelper db;

    // ── Factory ─────────────────────────────────────────────────────────────
    public static MacrosFragment newInstance(String date) {
        MacrosFragment f = new MacrosFragment();
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
        return inflater.inflate(R.layout.fragment_macros, container, false);
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

    // ── View binding ─────────────────────────────────────────────────────────
    private void bindViews(View view) {
        pieChart       = view.findViewById(R.id.pieChart);
        tvCarbsTotal   = view.findViewById(R.id.tvCarbsTotal);
        tvCarbsGoal    = view.findViewById(R.id.tvCarbsGoal);
        tvFatTotal     = view.findViewById(R.id.tvFatTotal);
        tvFatGoal      = view.findViewById(R.id.tvFatGoal);
        tvProteinTotal = view.findViewById(R.id.tvProteinTotal);
        tvProteinGoal  = view.findViewById(R.id.tvProteinGoal);
        tvCarbsLabel   = view.findViewById(R.id.tvCarbsLabel);
        tvFatLabel     = view.findViewById(R.id.tvFatLabel);
        tvProteinLabel = view.findViewById(R.id.tvProteinLabel);
    }

    // ── Data loading ─────────────────────────────────────────────────────────
    private void loadData() {
        if (userEmail == null || selectedDate == null) return;
        MacroTotals totals = db.getMacroTotalsForDate(userEmail, selectedDate);
        MacroTotals goals  = db.getMacroGoals(userEmail);

        // ── Pie chart ────────────────────────────────────────────────────────
        setupPieChart(totals);

        // ── Macro rows ───────────────────────────────────────────────────────
        double totalMacroG = totals.carbs + totals.fat + totals.protein;
        if (tvCarbsLabel != null)
            tvCarbsLabel.setText(String.format("Carbohydrates (%.0fg)", totals.carbs));
        if (tvFatLabel != null)
            tvFatLabel.setText(String.format("Fat (%.0fg)", totals.fat));
        if (tvProteinLabel != null)
            tvProteinLabel.setText(String.format("Protein (%.0fg)", totals.protein));

        setMacroRow(tvCarbsTotal,   tvCarbsGoal, totals.carbs,   goals.carbs,   totalMacroG, goals.calories, 0.50);
        setMacroRow(tvFatTotal,     tvFatGoal, totals.fat,     goals.fat,     totalMacroG, goals.calories, 0.30);
        setMacroRow(tvProteinTotal, tvProteinGoal, totals.protein, goals.protein, totalMacroG, goals.calories, 0.20);

    }

    private void setMacroRow(TextView tvTotal, TextView tvGoal, double actual, double goalG, double totalMacroG,
                             double calorieGoal, double defaultGoalPct) {
        // "X%" actual share of total macros logged
        String pctText = totalMacroG > 0
                ? String.format("%.0f%%", (actual / totalMacroG) * 100)
                : "0%";
        if (tvTotal != null) tvTotal.setText(pctText);

        // Goal column: use DB goal if set, else fall back to default %
        double goalPct;
        if (goalG > 0 && calorieGoal > 0) {
            goalPct = defaultGoalPct * 100;
        } else {
        goalPct = defaultGoalPct * 100;
        }
        if (tvGoal != null){
            tvGoal.setText(String.format("%.0fg", goalG));
        } else {
            tvGoal.setText(String.format("%.0f%%", goalPct));
        }
        tvGoal.setTextColor(Color.parseColor("#2D9CDB"));
    }

    private void setupPieChart(MacroTotals t) {
        List<PieEntry> entries = new ArrayList<>();

        float carbs   = (float) t.carbs;
        float fat     = (float) t.fat;
        float protein = (float) t.protein;
        float total   = carbs + fat + protein;

        if (total == 0) {
            entries.add(new PieEntry(1f, "No data"));
            PieDataSet set = new PieDataSet(entries, "");
            set.setColors(Color.DKGRAY);
            set.setDrawValues(false);
            PieData data = new PieData(set);
            styleChart(pieChart);
            pieChart.setData(data);
            pieChart.setCenterText("No food\nlogged");
            pieChart.invalidate();
            return;
        }

        entries.add(new PieEntry(carbs,   "Carbs"));
        entries.add(new PieEntry(fat,     "Fat"));
        entries.add(new PieEntry(protein, "Protein"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#FFE831"),   // Carbs   – yellow
                Color.parseColor("#16A61D"),   // Fat     – green
                Color.parseColor("#A13539")    // Protein – red
        );
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        styleChart(pieChart);
        pieChart.setData(data);
        pieChart.setCenterText(String.format("%.0f\nkcal", calculateKcal(t)));
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
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(10f);
        chart.setDrawEntryLabels(false);
        chart.animateY(800);
    }

    private double calculateKcal(MacroTotals t) {
        return (t.carbs * 4) + (t.protein * 4) + (t.fat * 9);
    }


    // ── Public refresh (called by host when date changes) ───────────────────
    public void refreshData(String newDate) {
        this.selectedDate = newDate;
        loadData();
    }
}