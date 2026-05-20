package com.example.heronhealth;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {
    EditText etUsername, etPassword, etEmail, etBirthday ,etWeight, etHeight;

    Button btnRegister;
    AlertDialog.Builder builder, successMessage;

    Spinner spnrGender, spnrGoal, spnrActivityLevel;

    MyDatabaseHelper myDb;

    int userAge = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //set the options for spinner gender
        spnrGender = findViewById(R.id.spGender);
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spnrGender.setAdapter(adapter);

        //same here
        spnrGoal = findViewById(R.id.spGoal);
        String[] goals = {"Lose Weight", "Maintain", "Gain Muscle"};
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        spnrGoal.setAdapter(goalAdapter);


        //same here
        spnrActivityLevel = findViewById(R.id.spActivityLevel);
        String[] activityLevel = {"Not Very Active", "Lightly Active", "Active", "Very Active"};
        ArrayAdapter<String> activityLevelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityLevel);
        spnrActivityLevel.setAdapter(activityLevelAdapter);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etBirthday = findViewById(R.id.etBirthday);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        myDb = new MyDatabaseHelper(this);

        btnRegister= findViewById(R.id.btnRegister);
        builder = new AlertDialog.Builder(this);
        successMessage = new AlertDialog.Builder(this);
        setEtBirthday();
        userRegister();
    }
    public void userRegister() {
        btnRegister.setOnClickListener(view -> {
            String username     = etUsername.getText().toString().trim();
            String password     = etPassword.getText().toString().trim();
            String email        = etEmail.getText().toString().trim();
            String birthday     = etBirthday.getText().toString().trim();
            String gender       = spnrGender.getSelectedItem().toString();
            String goal         = spnrGoal.getSelectedItem().toString();
            String activityLevel= spnrActivityLevel.getSelectedItem().toString();

            // ── Validations ───────────────────────────────────────────────────────
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() ||
                    birthday.isEmpty() || etWeight.getText().toString().isEmpty() ||
                    etHeight.getText().toString().isEmpty()) {
                displayMessage("Input Error!", "Please fill all fields");
                return;
            }

            String PASSWORD_PATTERN =
                    "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()-+=])(?=\\S+$).{8,20}$";
            if (!password.matches(PASSWORD_PATTERN)) {
                displayMessage("Weak Password",
                        "Password must be 8–20 characters with uppercase, lowercase, " +
                                "a number, and a special character.");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                displayMessage("Invalid Email", "Please enter a valid email address.");
                return;
            }
            if (userAge < 14 || userAge > 120) {
                displayMessage("Age Restriction", "You must be at least 14 years old.");
                return;
            }

            double weightKg, heightCm;
            try {
                weightKg = Double.parseDouble(etWeight.getText().toString());
                heightCm = Double.parseDouble(etHeight.getText().toString());
            } catch (NumberFormatException e) {
                displayMessage("Invalid Input", "Please enter valid numbers for weight and height.");
                return;
            }

            if (weightKg < 20 || weightKg > 500) {
                displayMessage("Invalid Weight", "Please enter a realistic weight in kg.");
                return;
            }
            if (heightCm < 50 || heightCm > 250) {
                displayMessage("Invalid Height", "Please enter a realistic height in cm.");
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // ── Step 1: Register user ─────────────────────────────────────────────
            boolean success = myDb.addUser(username, hashedPassword, email, birthday,
                    gender, weightKg, heightCm, goal, activityLevel);

            if (!success) {
                displayMessage("Error", "Username or Email already exists.");
                return;
            }

            // ── Step 2: Calculate BMR (Mifflin-St Jeor) ──────────────────────────
            double bmr = gender.equalsIgnoreCase("Male")
                    ? (10 * weightKg) + (6.25 * heightCm) - (5 * userAge) + 5
                    : (10 * weightKg) + (6.25 * heightCm) - (5 * userAge) - 161;

            // ── Step 3: TDEE ──────────────────────────────────────────────────────
            double tdee;
            switch (activityLevel) {
                case "Lightly Active": tdee = bmr * 1.375; break;
                case "Active":         tdee = bmr * 1.55;  break;
                case "Very Active":    tdee = bmr * 1.725; break;
                default:               tdee = bmr * 1.2;   break; // Not Very Active / Sedentary
            }

            // ── Step 4: Calorie goal ──────────────────────────────────────────────
            int calorieGoal = goal.equalsIgnoreCase("Lose Weight") ? (int)(tdee - 500)
                    : goal.equalsIgnoreCase("Gain Muscle")         ? (int)(tdee + 300)
                    : (int) tdee;

            // ── Step 5: Macros (NASM guidelines) ─────────────────────────────────
            // Protein: 1.6g/kg active, 1.1g/kg sedentary/lightly active
            boolean isActive = activityLevel.equals("Active") || activityLevel.equals("Very Active");
            int proteinGoal = isActive ? (int)(weightKg * 1.6) : (int)(weightKg * 1.1);

            // Fat: minimum 1g/kg body weight
            int fatGoal  = (int)(weightKg * 1.0);

            // Carbs: 55% of calories ÷ 4 kcal/g (midpoint of NASM's 45–65% range)
            int carbGoal = (int)((calorieGoal * 0.55) / 4.0);

            // Fiber goal
            int fiberGoal = (int)((calorieGoal / 1000.0) * 14);

            // Sugar goal
            int sugarGoal = (int)((calorieGoal * 0.10) / 4);

            // Saturated fat goal
            int satFatGoal = (int)((calorieGoal * 0.10) / 9);

            // Polyunsaturated goal
            int polyGoal = (int)((calorieGoal * 0.07) / 9);

            // ── Step 6: Water (Kinetico: 1 oz per 2 lbs → ml) ───────────────────
            double weightLbs = weightKg * 2.20462;
            int waterGoalMl  = (int)((weightLbs / 2.0) * 29.5735);

            // ── Step 7: Steps default ─────────────────────────────────────────────
            int stepGoal = 7000;
            myDb.updateGoals(email, calorieGoal, stepGoal, waterGoalMl, proteinGoal);
            myDb.updateMacroGoals(email, carbGoal, fatGoal, fiberGoal, sugarGoal, satFatGoal, polyGoal);

            successMessage.setCancelable(false);
            successMessage.setTitle("Success");
            successMessage.setMessage("Welcome to HeronHealth!");
            successMessage.setPositiveButton("OK", (dialog, i) -> {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            });
            successMessage.show();
        });
    }
    public void setEtBirthday(){
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get date today
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                //creates the date picker
                DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                // Formatting the month adding 1 because months are 0-indexed
                                String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                etBirthday.setText(formattedDate);
                                userAge = calculateAge(selectedYear, selectedMonth, selectedDay);
                            }
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });
    }
    public void displayMessage(String title, String message){
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
    private int calculateAge(int year, int month, int day) {
        Calendar birthDate = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        birthDate.set(year, month, day);

         int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

}