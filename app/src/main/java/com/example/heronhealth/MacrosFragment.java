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
    private LinearLayout llFoodList;

    private MyDatabaseHelper db;

    // ── Factory ─────────────────────────────────────────────────────────────
    public static MacrosFragment newInstance(String email, String date) {
        MacrosFragment f = new MacrosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_DATE,  date);
        f.setArguments(args);
        return f;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userEmail    = getArguments().getString(ARG_EMAIL);
            selectedDate = getArguments().getString(ARG_DATE);
        }
        db = new MyDatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your first XML (rename the file to fragment_macros.xml)
        return inflater.inflate(R.layout.fragment_macros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        loadData();
    }

    // ── View binding ─────────────────────────────────────────────────────────
    private void bindViews(View view) {
        pieChart = view.findViewById(R.id.pieChart);

        tvCarbsTotal   = view.findViewById(R.id.tvCarbsTotal);
        tvCarbsGoal    = view.findViewById(R.id.tvCarbsGoal);
        tvFatTotal     = view.findViewById(R.id.tvFatTotal);
        tvFatGoal      = view.findViewById(R.id.tvFatGoal);
        tvProteinTotal = view.findViewById(R.id.tvProteinTotal);
        tvProteinGoal  = view.findViewById(R.id.tvProteinGoal);

        llFoodList = view.findViewById(R.id.llFoodList); // wrap the dynamic food rows
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

        setMacroRow(tvCarbsTotal,   tvCarbsGoal,
                totals.carbs, goals.carbs, totalMacroG, 0.50);
        setMacroRow(tvFatTotal,     tvFatGoal,
                totals.fat,   goals.fat,   totalMacroG, 0.30);
        setMacroRow(tvProteinTotal, tvProteinGoal,
                totals.protein, goals.protein, totalMacroG, 0.20);

        // ── Top-carb foods list ──────────────────────────────────────────────
        populateFoodList();
    }

    private void setMacroRow(TextView tvTotal, TextView tvGoal,
                             double actual, double goal,
                             double totalMacroG, double defaultGoalPct) {
        // "X%" actual share of total macros logged
        String pctText = totalMacroG > 0
                ? String.format("%.0f%%", (actual / totalMacroG) * 100)
                : "0%";
        if (tvTotal != null) tvTotal.setText(pctText);

        // Goal column: use DB goal if set, else fall back to default %
        double goalPct = goal > 0 && (goals(goal, totalMacroG) > 0)
                ? (goal / (goals(goal, totalMacroG)) * 100)
                : defaultGoalPct * 100;
        if (tvGoal != null) tvGoal.setText(String.format("%.0f%%", goalPct));
    }

    private double goals(double goal, double total) { return goal; }

    private void setupPieChart(MacroTotals t) {
        List<PieEntry> entries = new ArrayList<>();

        float carbs   = (float) t.carbs;
        float fat     = (float) t.fat;
        float protein = (float) t.protein;
        float total   = carbs + fat + protein;

        if (total == 0) {
            // Empty state — show a single grey slice
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
                Color.parseColor("#FFE831"),   // Carbs  – yellow
                Color.parseColor("#16A61D"),   // Fat    – green
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

    private void populateFoodList() {
        if (llFoodList == null) return;
        llFoodList.removeAllViews();

        ArrayList<String[]> foods = db.getTopFoodsByCarbs(userEmail, selectedDate, 5);

        if (foods.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No foods logged yet");
            empty.setTextColor(Color.parseColor("#8A8D93"));
            empty.setTextSize(13f);
            llFoodList.addView(empty);
            return;
        }

        for (String[] food : foods) {
            RelativeLayout row = buildFoodRow(food[0], food[1]);
            llFoodList.addView(row);

            // Divider
            View divider = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            lp.topMargin = dpToPx(8);
            divider.setLayoutParams(lp);
            divider.setBackgroundColor(Color.parseColor("#2D3139"));
            llFoodList.addView(divider);
        }
    }

    private RelativeLayout buildFoodRow(String name, String value) {
        RelativeLayout row = new RelativeLayout(requireContext());
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dpToPx(10);
        row.setLayoutParams(rowLp);

        TextView tvName = new TextView(requireContext());
        tvName.setText(name);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(14f);
        RelativeLayout.LayoutParams nameLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        nameLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        tvName.setLayoutParams(nameLp);

        TextView tvVal = new TextView(requireContext());
        tvVal.setText(value);
        tvVal.setTextColor(Color.WHITE);
        tvVal.setTextSize(14f);
        RelativeLayout.LayoutParams valLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        valLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        tvVal.setLayoutParams(valLp);

        row.addView(tvName);
        row.addView(tvVal);
        return row;
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // ── Public refresh (called by host when date changes) ───────────────────
    public void refreshData(String newDate) {
        this.selectedDate = newDate;
        loadData();
    }
}