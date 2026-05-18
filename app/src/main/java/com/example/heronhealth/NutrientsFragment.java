package com.example.heronhealth;

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

/**
 * Tab 2 — Extended macros: Carbs, Fat, Protein, Fiber, Sugar, Sat Fat, Polyunsaturated.
 * Columns: Total (g) | Goal (g) | Left (g)
 * Layout: fragment_extended_macros.xml  (your second XML file)
 */
public class NutrientsFragment extends Fragment {

    private static final String ARG_DATE  = "date";
    private static final String ARG_EMAIL = "email";

    private String selectedDate;
    private String userEmail;
    private MyDatabaseHelper db;

    // ── Row TextViews — total / goal / left ──────────────────────────────────
    // Carbs
    private TextView tvCarbsTotal, tvCarbsGoal, tvCarbsLeft;
    // Fat
    private TextView tvFatTotal, tvFatGoal, tvFatLeft;
    // Protein
    private TextView tvProteinTotal, tvProteinGoal, tvProteinLeft;
    // Fiber
    private TextView tvFiberTotal, tvFiberGoal, tvFiberLeft;
    // Sugar
    private TextView tvSugarTotal, tvSugarGoal, tvSugarLeft;
    // Sat Fat
    private TextView tvSatFatTotal, tvSatFatGoal, tvSatFatLeft;
    // Poly
    private TextView tvPolyTotal, tvPolyGoal, tvPolyLeft;

    // Food list container
    private LinearLayout llFoodList;

    // ── Factory ─────────────────────────────────────────────────────────────
    public static NutrientsFragment newInstance(String email, String date) {
        NutrientsFragment f = new NutrientsFragment();
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
        // Rename your second XML to fragment_extended_macros.xml
        return inflater.inflate(R.layout.fragment_nutrients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        loadData();
    }

    // ── View binding ─────────────────────────────────────────────────────────
    // Add these android:id values to your second XML's TextViews.
    private void bindViews(View view) {
        tvCarbsTotal   = view.findViewById(R.id.tvCarbsTotal);
        tvCarbsGoal    = view.findViewById(R.id.tvCarbsGoal);
        tvCarbsLeft    = view.findViewById(R.id.tvCarbsLeft);

        tvFatTotal     = view.findViewById(R.id.tvFatTotal);
        tvFatGoal      = view.findViewById(R.id.tvFatGoal);
        tvFatLeft      = view.findViewById(R.id.tvFatLeft);

        tvProteinTotal = view.findViewById(R.id.tvProteinTotal);
        tvProteinGoal  = view.findViewById(R.id.tvProteinGoal);
        tvProteinLeft  = view.findViewById(R.id.tvProteinLeft);

        tvFiberTotal   = view.findViewById(R.id.tvFiberTotal);
        tvFiberGoal    = view.findViewById(R.id.tvFiberGoal);
        tvFiberLeft    = view.findViewById(R.id.tvFiberLeft);

        tvSugarTotal   = view.findViewById(R.id.tvSugarTotal);
        tvSugarGoal    = view.findViewById(R.id.tvSugarGoal);
        tvSugarLeft    = view.findViewById(R.id.tvSugarLeft);

        tvSatFatTotal  = view.findViewById(R.id.tvSatFatTotal);
        tvSatFatGoal   = view.findViewById(R.id.tvSatFatGoal);
        tvSatFatLeft   = view.findViewById(R.id.tvSatFatLeft);

        tvPolyTotal    = view.findViewById(R.id.tvPolyTotal);
        tvPolyGoal     = view.findViewById(R.id.tvPolyGoal);
        tvPolyLeft     = view.findViewById(R.id.tvPolyLeft);

        llFoodList     = view.findViewById(R.id.llFoodList);
    }

    // ── Data loading ─────────────────────────────────────────────────────────
    private void loadData() {
        if (userEmail == null || selectedDate == null) return;
        MacroTotals totals = db.getMacroTotalsForDate(userEmail, selectedDate);
        MacroTotals goals  = db.getMacroGoals(userEmail);

        setRow(tvCarbsTotal,   tvCarbsGoal,   tvCarbsLeft,   totals.carbs,   goals.carbs);
        setRow(tvFatTotal,     tvFatGoal,     tvFatLeft,     totals.fat,     goals.fat);
        setRow(tvProteinTotal, tvProteinGoal, tvProteinLeft, totals.protein, goals.protein);
        setRow(tvFiberTotal,   tvFiberGoal,   tvFiberLeft,   totals.fiber,   goals.fiber);
        setRow(tvSugarTotal,   tvSugarGoal,   tvSugarLeft,   totals.sugar,   goals.sugar);
        setRow(tvSatFatTotal,  tvSatFatGoal,  tvSatFatLeft,  totals.satFat,  goals.satFat);
        setRow(tvPolyTotal,    tvPolyGoal,    tvPolyLeft,    totals.polyFat, goals.polyFat);

        populateFoodList();
    }

    /**
     * Fills one macro row.
     * Total  → white  (actual grams consumed)
     * Goal   → blue   (#2D9CDB)
     * Left   → green if ≥ 0, red if over goal
     */
    private void setRow(TextView tvTotal, TextView tvGoal, TextView tvLeft,
                        double actual, double goal) {
        String totalStr = formatG(actual);
        String goalStr  = formatG(goal);
        double left     = goal - actual;
        String leftStr  = formatG(Math.abs(left));

        if (tvTotal != null) {
            tvTotal.setText(totalStr);
            tvTotal.setTextColor(Color.WHITE);
        }
        if (tvGoal != null) {
            tvGoal.setText(goalStr);
            tvGoal.setTextColor(Color.parseColor("#2D9CDB"));
        }
        if (tvLeft != null) {
            if (left >= 0) {
                tvLeft.setText(leftStr);
                tvLeft.setTextColor(Color.parseColor("#4CAF50")); // green – under goal
            } else {
                tvLeft.setText("-" + leftStr);                    // over goal
                tvLeft.setTextColor(Color.parseColor("#F44336")); // red
            }
        }
    }

    private String formatG(double val) {
        return String.format("%.1f", val);
    }

    // ── Top-carb food list (same as Fragment 1) ──────────────────────────────
    private void populateFoodList() {
        if (llFoodList == null) return;
        llFoodList.removeAllViews();

        java.util.ArrayList<String[]> foods =
                db.getTopFoodsByCarbs(userEmail, selectedDate, 5);

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
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    // ── Public refresh ───────────────────────────────────────────────────────
    public void refreshData(String newDate) {
        this.selectedDate = newDate;
        loadData();
    }
}