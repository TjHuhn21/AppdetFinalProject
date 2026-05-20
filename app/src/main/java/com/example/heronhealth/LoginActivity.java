package com.example.heronhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.mindrot.jbcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin, btnSignup;
    AlertDialog.Builder builder;

    private static final long SESSION_DURATION_MS = 7L * 24 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("HeronHealthPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            long loginTime = prefs.getLong("login_time", 0);
            long elapsed = System.currentTimeMillis() - loginTime;
            if (elapsed < SESSION_DURATION_MS) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            } else {
                prefs.edit().clear().apply();
            }
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignUp);
        builder = new AlertDialog.Builder(this);
        createListeners();
    }

    public void createListeners() {
        btnSignup.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                displayMessage("Input Error!", "Please fill all fields");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                displayMessage("Invalid Email", "Please enter a valid email address.");
                return;
            }
            MyDatabaseHelper myDb = new MyDatabaseHelper(LoginActivity.this);

            String storedHash = myDb.getHashedPassword(email);

            if (storedHash != null && BCrypt.checkpw(password, storedHash)) {
                SharedPreferences sharedPref = getSharedPreferences("HeronHealthPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("userEmail", email);
                editor.putLong("login_time", System.currentTimeMillis());
                editor.apply();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                displayMessage("Login Error!", "Incorrect email or password.");

            }
        });
    }

    public void displayMessage(String title, String message) {
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}