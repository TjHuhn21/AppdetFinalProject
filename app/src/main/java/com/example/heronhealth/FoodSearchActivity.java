package com.example.heronhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.heronhealth.model.FoodEntry;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FoodSearchActivity extends AppCompatActivity {

    private ImageSlider imageSlider;
    private TextInputEditText etSearchFood;
    private RecyclerView rvFoodResults;

    private MyDatabaseHelper myDb;
    private FoodSearchAdapter adapter;
    private ArrayList<FoodEntry> searchResults;

    private String currentUserEmail;
    private String todayDate;
    String preselectedMealType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_search);

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
        setupImageSlider();
        setupRecyclerView();
        setupSearch();
    }

    private void initialize() {

        imageSlider   = findViewById(R.id.imageSlider);
        etSearchFood  = findViewById(R.id.etSearchFood);
        rvFoodResults = findViewById(R.id.rvFoodResults);

        myDb = new MyDatabaseHelper(this);

        SharedPreferences sharedPref = getSharedPreferences(
                "HeronHealthPrefs",
                Context.MODE_PRIVATE
        );

        currentUserEmail = sharedPref.getString("userEmail", "");

        searchResults = new ArrayList<>();
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String passedDate = getIntent().getStringExtra("SELECTED_DATE");
        todayDate = (passedDate != null && !passedDate.isEmpty())
                ? passedDate
                : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        preselectedMealType = getIntent().getStringExtra("MEAL_TYPE");
    }

    private void setupImageSlider() {

        ArrayList<SlideModel> slideModels = new ArrayList<>();

        slideModels.add(new SlideModel(R.drawable.protein, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.gofood, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.glow,   ScaleTypes.FIT));

        imageSlider.setImageList(slideModels, ScaleTypes.FIT);
    }

    private void setupRecyclerView() {

        adapter = new FoodSearchAdapter(searchResults, this::onFoodItemClicked);

        rvFoodResults.setLayoutManager(new LinearLayoutManager(this));
        rvFoodResults.setAdapter(adapter);

        // Load all foods on launch so the list isn't empty
        searchResults.addAll(myDb.searchFoodLibrary(""));
        adapter.notifyDataSetChanged();
    }

    private void setupSearch() {

        etSearchFood.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                searchResults.clear();
                searchResults.addAll(myDb.searchFoodLibrary(s.toString().trim()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }


     //Shows a dialog to pick meal type and serving size, then logs the food entry to the DB.

    private void onFoodItemClicked(FoodEntry food) {

        if (preselectedMealType != null) {
            showServingSizeDialog(food, preselectedMealType);
            return;
        }

        // Otherwise show the meal picker
        String[] mealOptions = {"Breakfast", "Lunch", "Dinner"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add \"" + food.getName() + "\" to...");
        builder.setItems(mealOptions, (dialog, which) -> {
            showServingSizeDialog(food, mealOptions[which]);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showServingSizeDialog(FoodEntry food, String mealType) {

        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        input.setHint("e.g. " + (int) food.getServingSize());
        input.setText(String.valueOf((int) food.getServingSize()));

        new AlertDialog.Builder(this)
                .setTitle("Serving size (" + food.getUnit() + ")")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {

                    String val = input.getText().toString().trim();

                    if (val.isEmpty()) {
                        Toast.makeText(this, "Enter a serving size.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double serving;

                    try {
                        serving = Double.parseDouble(val);
                        if (serving <= 0) {
                            Toast.makeText(this, "Serving size must be greater than 0.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Cap at 5000g / 5000ml — prevents absurd entries
                        if (serving > 5000) {
                            Toast.makeText(this, "Serving size cannot exceed 5000 " + food.getUnit() + ".", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Warn if it seems unrealistic (over 2000g but under cap)
                        if (serving > 2000) {
                            // Still allow, but show a warning dialog first
                            new AlertDialog.Builder(this)
                                    .setTitle("Large serving size")
                                    .setMessage("You entered " + (int) serving + food.getUnit() + ". Are you sure?")
                                    .setPositiveButton("Yes, add it", (d, w) -> logFoodEntry(food, mealType, serving))
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            return;
                        }

                        // Normal path — log it
                        logFoodEntry(food, mealType, serving);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logFoodEntry(FoodEntry food, String mealType, double serving) {

        // Scale calories and protein proportionally to serving
        double ratio    = serving / food.getServingSize();
        int scaledCals  = (int) (food.getCalories()  * ratio);
        int scaledProt  = (int) (food.getProtein()   * ratio);
        double scaledCarbs   = food.getCarbs() * ratio;
        double scaledFat     = food.getFat() * ratio;
        double scaledFiber   = food.getFiber() * ratio;
        double scaledSugar   = food.getSugar() * ratio;
        double scaledSatFat  = food.getSatFat() * ratio;
        double scaledPolyFat = food.getPolyFat() * ratio;

        // Insert food log record
        boolean foodSaved = myDb.addFoodLogEntry(
                currentUserEmail,
                mealType,
                food.getName(),
                serving,
                food.getUnit(),
                scaledCals,
                scaledProt,
                scaledCarbs,
                scaledFat,
                scaledFiber,
                scaledSugar,
                scaledSatFat,
                scaledPolyFat,
                todayDate
        );

        if (foodSaved) {

            // Update daily calorie and protein totals
            ArrayList<Integer> stats =
                    myDb.getDailyStats(currentUserEmail, todayDate);

            if (stats.size() >= 4) {

                int newCals  = stats.get(2) + scaledCals;
                int newProt  = stats.get(3) + scaledProt;

                myDb.updateDailyValue(
                        currentUserEmail,
                        todayDate,
                        MyDatabaseHelper.COL_CUR_CALORIES,
                        newCals
                );

                myDb.updateDailyValue(
                        currentUserEmail,
                        todayDate,
                        MyDatabaseHelper.COL_CUR_PROTEIN,
                        newProt
                );
            }

            Toast.makeText(
                    this,
                    food.getName() + " added to " + mealType + "!",
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            Toast.makeText(this, "Failed to log food. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}