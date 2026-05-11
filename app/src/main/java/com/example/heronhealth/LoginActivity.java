package com.example.heronhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin, btnSignup;
    AlertDialog.Builder builder;

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

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignUp);
        builder = new AlertDialog.Builder(this);
        createListeners();
    }
    public void createListeners(){
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validation
                if(etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()){
                    displayMessage("Input Error!", "Please fill all fields");
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
                    displayMessage("Invalid Email", "Please enter a valid email address.");
                    return;
                }
                //check if user exists
                MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(LoginActivity.this);
                boolean userMatch = myDatabaseHelper.searchUser(etEmail.getText().toString().trim(), etPassword.getText().toString().trim());

                //if usermatch it logs in and if not present error message
                if (!userMatch){
                    displayMessage("LogIn Error!", "Incorrect username or password");
                }else {
                    SharedPreferences sharedPref = getSharedPreferences("HeronHealthPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("userEmail", etEmail.getText().toString().trim());
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
    public void displayMessage(String title, String message){
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

}