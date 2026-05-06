package com.example.heronhealth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {
    EditText etUsername, etPassword, etEmail, etBirthday;

    Button btnRegister;
    AlertDialog.Builder builder;

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

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etBirthday = findViewById(R.id.etBirthday);

        btnRegister= findViewById(R.id.btnRegister);
        builder = new AlertDialog.Builder(this);
        setEtBirthday();
        userRegister();
    }
    public void userRegister(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String birthday = etBirthday.getText().toString().trim();

                if(username.isEmpty() || password.isEmpty() || email.isEmpty() || birthday.isEmpty()){
                    displayMessage("Input Error!", "Please fill all fields");
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    displayMessage("Invalid Email", "Please enter a valid email address.");
                    return;
                }
                if (userAge < 0 || userAge > 120) {
                    displayMessage("Invalid Age", "Please enter a realistic age (0-120).");
                    return;
                } else if (userAge < 14) {
                    displayMessage("Age Restriction", "You must be at least 14 years old to use HeronHealth.");
                    return;
                }
                displayMessage("Success", "Registration complete! Age: " + userAge);

                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);


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