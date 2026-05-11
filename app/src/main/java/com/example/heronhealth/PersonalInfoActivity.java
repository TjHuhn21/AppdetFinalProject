package com.example.heronhealth;

import android.app.DatePickerDialog;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.heronhealth.model.PersonalInfo;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Calendar;

public class PersonalInfoActivity extends AppCompatActivity {

    private TextView tvUsernameValue, tvHeightValue, tvDOBValue,
            tvEmailValue, tvWeightValue;
    private Button btnSave;
    private MyDatabaseHelper myDb;
    private String currentUserEmail;

    private ImageView imgProfile;
    private Uri selectedImageUri;
    ActivityResultLauncher<Intent> imagePickLauncher;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgProfile.setImageURI(uri);
                    // Optional: Grant persistable permission if you want to load this URI later
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_info);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );


        myDb = new MyDatabaseHelper(this);

        SharedPreferences sharedPref =
                getSharedPreferences("HeronHealthPrefs", MODE_PRIVATE);

        currentUserEmail = sharedPref.getString("userEmail", "");

        tvUsernameValue = findViewById(R.id.tvUsernameValue);
        tvHeightValue   = findViewById(R.id.tvHeightValue);
        tvDOBValue      = findViewById(R.id.tvDOBValue);
        tvEmailValue    = findViewById(R.id.tvEmailValue);
        tvWeightValue   = findViewById(R.id.tvWeightValue);
        btnSave         = findViewById(R.id.btnSave);
        imgProfile = findViewById(R.id.imgProfile);

        loadData();

        // Editable fields
        tvUsernameValue.setOnClickListener(v ->
                showEditDialog("Edit Name", tvUsernameValue, false));

        tvHeightValue.setOnClickListener(v ->
                showEditDialog("Edit Height (cm)", tvHeightValue, true));

        tvWeightValue.setOnClickListener(v ->
                showEditDialog("Edit Weight (kg)", tvWeightValue, true));

        tvDOBValue.setOnClickListener(v -> showDatePickerDialog());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        imgProfile.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadData() {
        ArrayList<PersonalInfo> users = myDb.getUserList(currentUserEmail);

        if (!users.isEmpty()) {
            PersonalInfo info = users.get(0);

            tvUsernameValue.setText(info.getName());
            tvHeightValue.setText(info.getHeight());
            tvWeightValue.setText(info.getWeight());
            tvDOBValue.setText(info.getDateOfBirth());
            tvEmailValue.setText(info.getEmail());

            // LOAD THE IMAGE
            if (info.getImageUri() != null && !info.getImageUri().isEmpty()) {
                try {
                    imgProfile.setImageURI(android.net.Uri.parse(info.getImageUri()));
                } catch (Exception e) {
                    // Fallback if URI is invalid or permission is lost
                    imgProfile.setImageResource(R.drawable.baseline_account_circle_24);
                }
            }
        }
    }

    private void saveProfile() {

        String newName   = tvUsernameValue.getText().toString().trim();
        String newHeight = tvHeightValue.getText().toString().trim();
        String newWeight = tvWeightValue.getText().toString().trim();
        String newDob    = tvDOBValue.getText().toString().trim();

        // Basic validation for numeric fields
        if (!isValidDecimal(newHeight)) {
            Toast.makeText(this, "Please enter a valid height.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDecimal(newWeight)) {
            Toast.makeText(this, "Please enter a valid weight.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri != null) {
            myDb.updateUserImage(currentUserEmail, selectedImageUri.toString());
        }

        // Save name, height, DOB to user table
        boolean profileSaved = myDb.updateFullProfile(
                currentUserEmail,
                newName,
                newHeight,
                newDob
        );

        // Save weight separately (updates the user profile weight column)
        boolean weightSaved = myDb.updateUserWeight(
                currentUserEmail,
                Double.parseDouble(newWeight)
        );

        if (profileSaved && weightSaved) {

            // Recalculate goals because weight and height affect BMR/TDEE/water goal
            recalculateGoals(newWeight, newHeight, newDob);

            Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
            finish();

        } else {
            Toast.makeText(this, "Error saving profile.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Pulls the user's full profile and recalculates all goals
     * using the new weight/height. Saves updated goals to DB.
     */
    private void recalculateGoals(String newWeight, String newHeight, String newDob) {

        ArrayList<PersonalInfo> users = myDb.getUserList(currentUserEmail);

        if (users.isEmpty()) return;

        PersonalInfo user = users.get(0);

        double weight = Double.parseDouble(newWeight);
        double height = Double.parseDouble(newHeight);
        int age       = calculateAge(newDob);
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
        int waterGoalMl  = (int) (waterOz * 29.57);

        // Protein and step goal
        int proteinGoal = (int) (weight * 2.0);
        int stepGoal    = user.getStepGoal() > 0 ? user.getStepGoal() : 7000;

        myDb.updateGoals(
                currentUserEmail,
                finalCalorieGoal,
                stepGoal,
                waterGoalMl,
                proteinGoal
        );
    }

    private int calculateAge(String dobString) {

        try {
            String[] parts = dobString.split("/");

            int day   = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year  = Integer.parseInt(parts[2]);

            Calendar dob = Calendar.getInstance();
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

    private boolean isValidDecimal(String value) {
        try {
            double d = Double.parseDouble(value);
            return d > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showEditDialog(String title, TextView target, boolean numericInput) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setText(target.getText().toString());

        if (numericInput) {
            input.setInputType(
                    android.text.InputType.TYPE_CLASS_NUMBER
                            | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            );
        }

        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String val = input.getText().toString().trim();
            if (!val.isEmpty()) {
                target.setText(val);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDatePickerDialog() {

        final Calendar c = Calendar.getInstance();

        int year  = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day   = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Android months are 0-indexed so add 1
                    String formattedDate =
                            selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    tvDOBValue.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
}