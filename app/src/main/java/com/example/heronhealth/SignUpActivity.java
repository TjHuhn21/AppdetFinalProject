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
        spnrGender = findViewById(R.id.spGender);
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spnrGender.setAdapter(adapter);

        spnrGoal = findViewById(R.id.spGoal);
        String[] goals = {"Lose Weight", "Maintain", "Gain Muscle"};
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        spnrGoal.setAdapter(goalAdapter);

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
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String birthday = etBirthday.getText().toString().trim();


                String gender = spnrGender.getSelectedItem().toString();
                String goal = spnrGoal.getSelectedItem().toString();
                String activityLevel = spnrActivityLevel.getSelectedItem().toString();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty() ||
                        birthday.isEmpty() || etWeight.getText().toString().isEmpty() ||
                        etHeight.getText().toString().isEmpty()) {
                    displayMessage("Input Error!", "Please fill all fields");
                    return;
                }
                String PASSWORD_PATTERN =
                        "^(?=.*[0-9])" +         // at least one digit
                                "(?=.*[a-z])" +         // at least one lowercase letter
                                "(?=.*[A-Z])" +         // at least one uppercase letter
                                "(?=.*[!@#$%^&*()-+=])" + // at least one special character
                                "(?=\\S+$)" +           // no whitespace allowed
                                ".{8,20}$";

                if (!password.matches(PASSWORD_PATTERN)) {
                    displayMessage("Weak Password",
                            "Password must be at least 8 characters long, including " +
                                    "uppercase, lowercase, a number, and a special character (@#$%^&+=!).");
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

                double weight = Double.parseDouble(etWeight.getText().toString());
                double height = Double.parseDouble(etHeight.getText().toString());

                if (weight < 20 || weight > 500) {
                    displayMessage("Invalid Weight", "Please enter a realistic weight in kg.");
                    return;
                }
                if (height < 50 || height > 250) {
                    displayMessage("Invalid Height", "Please enter a realistic height in cm.");
                    return;
                }

                boolean success = myDb.addUser(username, password, email,birthday, gender, weight, height, goal, activityLevel);

                if (success) {
                    successMessage.setCancelable(false);
                    successMessage.setTitle("Success");
                    successMessage.setMessage("Welcome to HeronHealth!");

                    successMessage.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                    successMessage.show();

                } else {
                    successMessage.setCancelable(true);
                    displayMessage("Error", "Username or Email already exists.");
                }
            }
        });
    }
    public void setEtBirthday(){
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);


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