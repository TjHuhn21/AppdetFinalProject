package com.example.heronhealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WaterAddActivity extends AppCompatActivity {

    private Button add250, add500, add1000, customSize, btnSave;
    private ProgressBar waterIntakeProgress;
    private ImageButton exit;
    private MyDatabaseHelper myDb;
    private TextView tvTotalVolume, tvProgressBarTarget;

    private int currentVolume, targetVolume;
    private String currentUserEmail;
    private String todayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_add);

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

        initialize();
        loadData();
        updateUI();

        add250.setOnClickListener(v  -> addWater(250));
        add500.setOnClickListener(v  -> addWater(500));
        add1000.setOnClickListener(v -> addWater(1000));

        customSize.setOnClickListener(v -> showCustomDialog());

        exit.setOnClickListener(v    -> finish());

        btnSave.setOnClickListener(v -> saveWaterToDb());
    }

    private void initialize() {

        tvTotalVolume      = findViewById(R.id.tvTotalVolume);
        add250             = findViewById(R.id.btnAdd250);
        add500             = findViewById(R.id.btnAdd500);
        add1000            = findViewById(R.id.btnAdd1000);
        btnSave            = findViewById(R.id.btnSave);
        customSize         = findViewById(R.id.btnCustom);
        waterIntakeProgress = findViewById(R.id.progressBarWater);
        exit               = findViewById(R.id.btnClose);

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

    private void loadData() {

        currentVolume = getIntent().getIntExtra("CURRENT_WATER", 0);
        targetVolume  = getIntent().getIntExtra("TARGET_WATER", 2500);
    }

    private void updateUI() {

        tvTotalVolume.setText(currentVolume + " ml");

        waterIntakeProgress.setMax(targetVolume);

        waterIntakeProgress.setProgress(
                Math.min(currentVolume, targetVolume)
        );
    }

    private void addWater(int amount) {

        currentVolume += amount;

        // Cap at double the goal to prevent runaway values
        if (currentVolume > targetVolume * 2) {
            currentVolume = targetVolume * 2;
            Toast.makeText(
                    this,
                    "Maximum water limit reached.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        updateUI();
    }

    private void showCustomDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("Enter custom amount (ml)");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g. 350");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {

            String val = input.getText().toString().trim();

            if (!val.isEmpty()) {

                try {

                    int amount = Integer.parseInt(val);

                    if (amount > 0) {
                        addWater(amount);
                    } else {
                        Toast.makeText(
                                this,
                                "Please enter a positive amount.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                } catch (NumberFormatException e) {
                    Toast.makeText(
                            this,
                            "Invalid amount.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void saveWaterToDb() {

        myDb.updateDailyValue(
                currentUserEmail,
                todayDate,
                MyDatabaseHelper.COL_CUR_WATER,
                currentVolume
        );

        Toast.makeText(this, "Water intake saved!", Toast.LENGTH_SHORT).show();

        finish();
    }
}