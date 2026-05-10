package com.example.heronhealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExerciseActivity extends AppCompatActivity {

    private ImageButton btnClose, btnSave;
    private EditText etWorkoutName, etSets, etReps, etWeight, etCalories;

    private MyDatabaseHelper myDb;
    private String currentUserEmail;
    private String todayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);

        initialize();

        btnClose.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v  -> saveWorkout());
    }

    private void initialize() {

        btnClose      = findViewById(R.id.btnClose);
        btnSave       = findViewById(R.id.btnSave);

        etWorkoutName = findViewById(R.id.etWorkoutName);
        etSets        = findViewById(R.id.etSets);
        etReps        = findViewById(R.id.etReps);
        etWeight      = findViewById(R.id.etWeight);
        etCalories    = findViewById(R.id.etCalories);

        myDb = new MyDatabaseHelper(this);

        SharedPreferences sharedPref = getSharedPreferences(
                "HeronHealthPrefs",
                Context.MODE_PRIVATE
        );

        currentUserEmail = sharedPref.getString("userEmail", "");

        todayDate = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());
    }

    private void saveWorkout() {

        String nameStr     = etWorkoutName.getText().toString().trim();
        String setsStr     = etSets.getText().toString().trim();
        String repsStr     = etReps.getText().toString().trim();
        String weightStr   = etWeight.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();

        // --- Validations ---
        if (nameStr.isEmpty()) {
            Toast.makeText(this, "Please enter an exercise name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (setsStr.isEmpty() || repsStr.isEmpty()) {
            Toast.makeText(this, "Please fill in sets and reps.", Toast.LENGTH_SHORT).show();
            return;
        }

        int sets, reps;
        double weightPerRep;
        int caloriesBurned = 0;

        try {
            sets         = Integer.parseInt(setsStr);
            reps         = Integer.parseInt(repsStr);
            weightPerRep = weightStr.isEmpty() ? 0 : Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number entered.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sets <= 0 || sets > 100) {
            Toast.makeText(this, "Sets must be between 1 and 100.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reps <= 0 || reps > 500) {
            Toast.makeText(this, "Reps must be between 1 and 500.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (weightPerRep < 0 || weightPerRep > 1000) {
            Toast.makeText(this, "Weight must be between 0 and 1000 kg.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!caloriesStr.isEmpty()) {
            try {
                caloriesBurned = Integer.parseInt(caloriesStr);
                if (caloriesBurned < 0 || caloriesBurned > 5000) {
                    Toast.makeText(this, "Calories burned must be between 0 and 5000.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                caloriesBurned = 0;
            }
        }

        // Auto-estimate calories if not provided
        if (caloriesBurned == 0) {
            caloriesBurned = estimateCalories(sets, reps, weightPerRep);
        }

        boolean success = myDb.addWorkoutEntry(
                currentUserEmail,
                todayDate,
                nameStr,
                sets,
                reps,
                weightPerRep,
                caloriesBurned
        );

        if (success) {
            Toast.makeText(this, nameStr + " logged!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to log workout. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Rough calorie estimate for resistance training.
     * Bodyweight: ~5 cal per set. Weighted: sets * reps * weight * constant.
     */
    private int estimateCalories(int sets, int reps, double weight) {

        if (weight <= 0) {
            return sets * 5;
        }

        return (int) (sets * reps * weight * 0.00035 * 60);
    }
}