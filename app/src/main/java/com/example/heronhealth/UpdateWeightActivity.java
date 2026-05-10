package com.example.heronhealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UpdateWeightActivity extends AppCompatActivity {

    private ImageButton btnCancel, btnSave;
    private EditText etWeightValue;
    private TextView tvDateValue;

    private MyDatabaseHelper myDb;
    private String currentUserEmail;
    private String todayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_weight);

        initialize();
        loadData();

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v   -> saveWeight());
    }

    private void initialize() {

        btnCancel     = findViewById(R.id.btnCancel);
        btnSave       = findViewById(R.id.btnSave);
        etWeightValue = findViewById(R.id.etWeightValue);
        tvDateValue   = findViewById(R.id.tvDateValue);

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

        String displayDate = new SimpleDateFormat(
                "MMM dd, yyyy",
                Locale.getDefault()
        ).format(new Date());

        tvDateValue.setText(displayDate);
    }

    private void loadData() {

        // Pre-fill with current weight from profile
        ArrayList<com.example.heronhealth.model.PersonalInfo> users =
                myDb.getUserList(currentUserEmail);

        if (!users.isEmpty()) {
            etWeightValue.setText(users.get(0).getWeight());
        }

        // If already logged today, disable save and warn
        if (myDb.hasWeightEntryForDate(currentUserEmail, todayDate)) {
            etWeightValue.setEnabled(false);
            btnSave.setEnabled(false);
            Toast.makeText(
                    this,
                    "You've already logged your weight today.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void saveWeight() {

        String input = etWeightValue.getText() != null
                ? etWeightValue.getText().toString().trim()
                : "";

        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter a weight value.", Toast.LENGTH_SHORT).show();
            return;
        }

        double weightValue;

        try {
            weightValue = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weight value.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (weightValue <= 0 || weightValue > 500) {
            Toast.makeText(this, "Please enter a realistic weight (0–500 kg).", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = myDb.addWeightEntry(currentUserEmail, weightValue, todayDate);

        if (success) {
            Toast.makeText(this, "Weight saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save weight. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}