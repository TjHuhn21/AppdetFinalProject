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

/**
 * Tab 2 — Extended macros: Carbs, Fat, Protein, Fiber, Sugar, Sat Fat, Polyunsaturated.
 * Columns: Total (g) | Goal (g) | Left (g)*/
public class NutrientsFragment extends Fragment {

    private static final String ARG_DATE  = "date";

    private String selectedDate;
    private String userEmail;
    private MyDatabaseHelper db;

    // ── Row TextViews — total / goal / left ──────────────────────────────────
    // Carbs
    private TextView tvCarbsTotal,   tvCarbsGoal,   tvCarbsLeft;
    private TextView tvFatTotal,     tvFatGoal,     tvFatLeft;
    private TextView tvProteinTotal, tvProteinGoal, tvProteinLeft;
    private TextView tvFiberTotal,   tvFiberGoal,   tvFiberLeft;
    private TextView tvSugarTotal,   tvSugarGoal,   tvSugarLeft;
    private TextView tvSatFatTotal,  tvSatFatGoal,  tvSatFatLeft;
    private TextView tvPolyTotal,    tvPolyGoal,    tvPolyLeft;


    // ── Factory ─────────────────────────────────────────────────────────────
    public static NutrientsFragment newInstance(String date) {
        NutrientsFragment f = new NutrientsFragment();
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
        return inflater.inflate(R.layout.fragment_nutrients, container, false);
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

    }

    /**
     * Fills one macro row.
     * Total  → white  (actual grams consumed)
     * Goal   → blue   (#2D9CDB)
     * Left   → green if ≥ 0, red if over goal
     */
    private void setRow(TextView tvTotal, TextView tvGoal, TextView tvLeft,
                        double actual, double goal) {
        if (tvTotal != null) {
            tvTotal.setText(formatG(actual));
            tvTotal.setTextColor(Color.WHITE);
        }
        if (tvGoal != null) {
            tvGoal.setText(goal > 0 ? formatG(goal) : "—");
            tvGoal.setTextColor(Color.parseColor("#2D9CDB"));
        }
        if (tvLeft != null) {
            if (goal <= 0) {
                tvLeft.setText("—");
                tvLeft.setTextColor(Color.parseColor("#8A8D93"));
            }else {
                double left = goal - actual;
                if (left >= 0) {
                    tvLeft.setText(formatG(left));
                    tvLeft.setTextColor(Color.parseColor("#4CAF50"));
                }else{
                    tvLeft.setText("-" + formatG(Math.abs(left)));
                    tvLeft.setTextColor(Color.parseColor("#F44336"));
                }
            }
        }
    }

    private String formatG(double val) {
        return String.format("%.1f", val);
    }

    // ── Public refresh ───────────────────────────────────────────────────────
    public void refreshData(String newDate) {
        this.selectedDate = newDate;
        loadData();
    }
}