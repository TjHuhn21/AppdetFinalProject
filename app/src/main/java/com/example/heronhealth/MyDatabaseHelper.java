
package com.example.heronhealth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.heronhealth.model.FoodEntry;
import com.example.heronhealth.model.MacroTotals;
import com.example.heronhealth.model.PersonalInfo;
import com.example.heronhealth.model.StepEntry;
import com.example.heronhealth.model.WeightEntry;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;

class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "HeronHealth.db";
    private static final int DATABASE_VERSION = 13;

    private static final String TABLE_NAME = "heron_User";
    private static final String COLUMN_ID = "user_ID";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "user_password";
    private static final String COLUMN_EMAIL = "user_email";
    private static final String COLUMN_BIRTHDAY = "user_birthday";
    private static final String COLUMN_GENDER = "user_gender";
    private static final String COLUMN_WEIGHT = "user_weight";
    private static final String COLUMN_HEIGHT = "user_height";
    private static final String COLUMN_GOAL = "user_goal";
    private static final String COLUMN_ACTIVITY_LEVEL = "user_activity_level";
    private static final String COLUMN_PROFILE_IMAGE = "user_profile_image";
    private static final String COLUMN_IS_DELETED = "is_deleted";
    private static final String COLUMN_CALORIE_GOAL = "user_calorie_goal";
    private static final String COLUMN_PROTEIN_GOAL = "user_protein_goal";
    private static final String COLUMN_WATER_GOAL = "user_water_goal";
    private static final String COLUMN_STEP_GOAL = "user_step_goal";
    private static final String COLUMN_CARB_GOAL    = "user_carb_goal";
    private static final String COLUMN_FAT_GOAL     = "user_fat_goal";
    private static final String COLUMN_FIBER_GOAL   = "user_fiber_goal";
    private static final String COLUMN_SUGAR_GOAL   = "user_sugar_goal";
    private static final String COLUMN_SAT_FAT_GOAL = "user_sat_fat_goal";
    private static final String COLUMN_POLY_GOAL    = "user_poly_goal";

    public static final String TABLE_WEIGHT     = "weight_progress";
    public static final String COL_WEIGHT_ID    = "weight_id";
    public static final String COL_WEIGHT_VALUE = "weight_value";
    public static final String COL_WEIGHT_DATE  = "weight_date";

    public static final String TABLE_DAILY_LOGS  = "daily_logs";
    public static final String COL_LOG_ID        = "log_id";
    public static final String COL_LOG_DATE      = "log_date";
    public static final String COL_CUR_WATER     = "current_water";
    public static final String COL_CUR_STEPS     = "current_steps";
    public static final String COL_CUR_PROTEIN   = "current_protein";
    public static final String COL_CUR_CALORIES  = "current_calories";

    public static final String TABLE_FOOD_LOG    = "food_log";
    public static final String COL_FOOD_ID       = "food_id";
    public static final String COL_MEAL_TYPE     = "meal_type";
    public static final String COL_FOOD_NAME     = "food_name";
    public static final String COL_CALORIES      = "calories";
    public static final String COL_PROTEIN       = "protein";
    public static final String COL_SERVING_SIZE  = "serving_size";
    public static final String COL_SERVING_UNIT  = "serving_unit";
    public static final String COL_CARBS         = "carbs";
    public static final String COL_FAT           = "fat";
    public static final String COL_FIBER         = "fiber";
    public static final String COL_SUGAR         = "sugar";
    public static final String COL_SAT_FAT       = "sat_fat";
    public static final String COL_POLY_FAT      = "poly_fat";

    public static final String TABLE_FOOD_LIBRARY = "food_library";
    public static final String COL_LIB_ID         = "lib_id";
    public static final String COL_LIB_NAME       = "food_name";
    public static final String COL_LIB_CAL        = "calories";
    public static final String COL_LIB_PRO        = "protein";
    public static final String COL_LIB_SIZE       = "base_serving_size";
    public static final String COL_LIB_UNIT       = "base_unit";
    public static final String COL_LIB_CARBS      = "carbs";
    public static final String COL_LIB_FAT        = "fat";
    public static final String COL_LIB_FIBER      = "fiber";
    public static final String COL_LIB_SUGAR      = "sugar";
    public static final String COL_LIB_SAT_FAT    = "sat_fat";
    public static final String COL_LIB_POLY       = "poly_fat";

    public static final String TABLE_WORKOUT_LOG  = "workout_log";
    public static final String COL_WORKOUT_ID     = "workout_id";
    public static final String COL_WORKOUT_NAME   = "workout_name";
    public static final String COL_WORKOUT_SETS   = "sets";
    public static final String COL_WORKOUT_REPS   = "reps";
    public static final String COL_WORKOUT_WEIGHT = "weight_kg";
    public static final String COL_WORKOUT_CALS   = "calories_burned";
    public static final String COL_WORKOUT_DATE   = "workout_date";

    public static final String TABLE_HABIT_LOG = "habit_log";
    public static final String COL_HABIT_DATE  = "habit_date";
    public static final String COL_HABIT_DONE  = "habit_done";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME       + " TEXT, "
                + COLUMN_PASSWORD       + " TEXT, "
                + COLUMN_EMAIL          + " TEXT UNIQUE, "
                + COLUMN_BIRTHDAY       + " TEXT, "
                + COLUMN_GENDER         + " TEXT, "
                + COLUMN_WEIGHT         + " REAL, "
                + COLUMN_HEIGHT         + " REAL, "
                + COLUMN_GOAL           + " TEXT, "
                + COLUMN_ACTIVITY_LEVEL + " TEXT, "
                + COLUMN_CALORIE_GOAL   + " INTEGER DEFAULT 0, "
                + COLUMN_PROTEIN_GOAL   + " INTEGER DEFAULT 0, "
                + COLUMN_WATER_GOAL     + " INTEGER DEFAULT 0, "
                + COLUMN_STEP_GOAL      + " INTEGER DEFAULT 0, "
                + COLUMN_CARB_GOAL      + " INTEGER DEFAULT 0, "
                + COLUMN_FAT_GOAL       + " INTEGER DEFAULT 0, "
                + COLUMN_FIBER_GOAL     + " INTEGER DEFAULT 0, "
                + COLUMN_SUGAR_GOAL     + " INTEGER DEFAULT 0, "
                + COLUMN_SAT_FAT_GOAL   + " INTEGER DEFAULT 0, "
                + COLUMN_POLY_GOAL      + " INTEGER DEFAULT 0, "
                + COLUMN_PROFILE_IMAGE  + " TEXT, "
                + COLUMN_IS_DELETED     + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_FOOD_LIBRARY + " ("
                + COL_LIB_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LIB_NAME    + " TEXT, "
                + COL_LIB_CAL     + " REAL, "
                + COL_LIB_CARBS   + " REAL, "
                + COL_LIB_FAT     + " REAL, "
                + COL_LIB_PRO     + " REAL, "
                + COL_LIB_FIBER   + " REAL, "
                + COL_LIB_SUGAR   + " REAL, "
                + COL_LIB_SAT_FAT + " REAL, "
                + COL_LIB_POLY    + " REAL DEFAULT 0, "
                + COL_LIB_SIZE    + " REAL, "
                + COL_LIB_UNIT    + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_WEIGHT + " ("
                + COL_WEIGHT_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL     + " TEXT, "
                + COL_WEIGHT_VALUE + " REAL, "
                + COL_WEIGHT_DATE  + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_DAILY_LOGS + " ("
                + COL_LOG_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL     + " TEXT, "
                + COL_LOG_DATE     + " TEXT, "
                + COL_CUR_WATER    + " INTEGER DEFAULT 0, "
                + COL_CUR_STEPS    + " INTEGER DEFAULT 0, "
                + COL_CUR_PROTEIN  + " INTEGER DEFAULT 0, "
                + COL_CUR_CALORIES + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_FOOD_LOG + " ("
                + COL_FOOD_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL     + " TEXT, "
                + COL_MEAL_TYPE    + " TEXT, "
                + COL_FOOD_NAME    + " TEXT, "
                + COL_SERVING_SIZE + " REAL, "
                + COL_SERVING_UNIT + " TEXT, "
                + COL_CALORIES     + " REAL DEFAULT 0, "
                + COL_PROTEIN      + " REAL DEFAULT 0, "
                + COL_CARBS        + " REAL DEFAULT 0, "
                + COL_FAT          + " REAL DEFAULT 0, "
                + COL_FIBER        + " REAL DEFAULT 0, "
                + COL_SUGAR        + " REAL DEFAULT 0, "
                + COL_SAT_FAT      + " REAL DEFAULT 0, "
                + COL_POLY_FAT     + " REAL DEFAULT 0, "
                + COL_LOG_DATE     + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_WORKOUT_LOG + " ("
                + COL_WORKOUT_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL       + " TEXT, "
                + COL_WORKOUT_DATE   + " TEXT, "
                + COL_WORKOUT_NAME   + " TEXT, "
                + COL_WORKOUT_SETS   + " INTEGER, "
                + COL_WORKOUT_REPS   + " INTEGER, "
                + COL_WORKOUT_WEIGHT + " REAL, "
                + COL_WORKOUT_CALS   + " INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_HABIT_LOG + " ("
                + COL_LOG_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL   + " TEXT, "
                + COL_HABIT_DATE + " TEXT, "
                + COL_HABIT_DONE + " INTEGER DEFAULT 0)");

        prefillFoodLibrary(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_LIBRARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABIT_LOG);
        onCreate(db);
    }

    private void prefillFoodLibrary(SQLiteDatabase db) {
        insertLibraryItem(db,"Lean Minced Beef",125,0.0,4.8,22.0,0.0,0.0,1.8,0.5,100,"g");
        insertLibraryItem(db,"Beef Sirloin Steak",134,0.0,5.0,23.0,0.0,0.0,2.1,0.3,100,"g");
        insertLibraryItem(db,"Chicken Light Meat",106,0.0,1.7,24.0,0.0,0.0,0.5,0.4,100,"g");
        insertLibraryItem(db,"Chicken Dark Meat",109,0.0,3.2,20.9,0.0,0.0,0.9,0.7,100,"g");
        insertLibraryItem(db,"Lean Minced Lamb",156,0.0,7.0,19.1,0.0,0.0,3.0,0.5,100,"g");
        insertLibraryItem(db,"Turkey Light Meat",105,0.0,1.2,24.4,0.0,0.0,0.3,0.3,100,"g");
        insertLibraryItem(db,"Pork Loin Steak",225,0.0,14.0,19.9,0.0,0.0,5.0,1.6,100,"g");
        insertLibraryItem(db,"Bacon",215,1.4,18.0,16.5,0.0,1.0,6.2,2.1,100,"g");
        insertLibraryItem(db,"Venison",103,0.0,1.5,22.2,0.0,0.0,0.7,0.3,100,"g");
        insertLibraryItem(db,"Cod",75,0.0,0.7,17.5,0.0,0.0,0.1,0.2,100,"g");
        insertLibraryItem(db,"Haddock",75,0.0,0.8,17.8,0.0,0.0,0.2,0.3,100,"g");
        insertLibraryItem(db,"Wild Salmon",179,0.0,10.0,22.1,0.0,0.0,2.3,3.5,100,"g");
        insertLibraryItem(db,"Prawns",77,0.2,0.9,17.6,0.0,0.0,0.2,0.3,100,"g");
        insertLibraryItem(db,"Squid",77,3.1,1.2,15.4,0.0,0.0,0.4,0.5,100,"g");
        insertLibraryItem(db,"Lobster",77,1.2,0.6,17.0,0.0,0.0,0.1,0.2,100,"g");
        insertLibraryItem(db,"Egg (Large)",131,1.1,9.5,13.0,0.0,1.1,3.1,1.4,100,"g");
        insertLibraryItem(db,"Whey Protein Isolate",379,4.0,1.0,90.0,0.0,1.0,0.5,0.2,100,"g");
        insertLibraryItem(db,"Pea Protein",366,8.0,6.0,66.0,4.0,0.5,1.0,2.5,100,"g");
        insertLibraryItem(db,"Beef Broth",111,3.0,7.0,7.8,0.0,1.0,2.0,0.3,100,"ml");
        insertLibraryItem(db,"Quinoa",319,64.0,6.0,14.0,7.0,4.0,0.7,3.3,100,"g");
        insertLibraryItem(db,"Rice - Brown",354,76.0,2.8,9.3,3.5,0.7,0.6,1.0,100,"g");
        insertLibraryItem(db,"Rice - White",345,78.0,0.4,8.5,0.4,0.1,0.1,0.1,100,"g");
        insertLibraryItem(db,"Chickpeas",338,61.0,6.0,21.0,17.0,11.0,0.6,2.7,100,"g");
        insertLibraryItem(db,"Lentils (Red)",323,56.0,1.0,24.0,11.0,2.0,0.2,0.5,100,"g");
        insertLibraryItem(db,"Buckwheat",362,75.0,3.4,8.1,10.0,0.9,0.7,1.0,100,"g");
        insertLibraryItem(db,"Potatoes",84,20.0,0.1,1.9,2.2,1.2,0.0,0.0,100,"g");
        insertLibraryItem(db,"Sweet Potatoes",91,21.0,0.2,1.2,3.3,6.5,0.0,0.1,100,"g");
        insertLibraryItem(db,"Carrots",43,10.0,0.2,0.9,2.8,4.7,0.0,0.1,100,"g");
        insertLibraryItem(db,"Beetroot",45,10.0,0.2,1.7,2.8,7.0,0.0,0.1,100,"g");
        insertLibraryItem(db,"Apple",52,14.0,0.2,0.3,2.4,10.0,0.0,0.1,100,"g");
        insertLibraryItem(db,"Banana",89,23.0,0.3,1.1,2.6,12.0,0.1,0.1,100,"g");
        insertLibraryItem(db,"Blueberry",57,14.0,0.3,0.7,2.4,10.0,0.0,0.1,100,"g");
        insertLibraryItem(db,"Mango",60,15.0,0.4,0.8,1.6,14.0,0.1,0.1,100,"g");
        insertLibraryItem(db,"Orange",47,12.0,0.1,0.9,2.4,9.0,0.0,0.0,100,"g");
        insertLibraryItem(db,"Strawberry",32,8.0,0.3,0.7,2.0,4.9,0.0,0.2,100,"g");
        insertLibraryItem(db,"Avocado",160,9.0,15.0,2.0,7.0,0.7,2.1,1.8,100,"g");
        insertLibraryItem(db,"Broccoli",34,7.0,0.4,2.8,2.6,1.7,0.0,0.2,100,"g");
        insertLibraryItem(db,"Spinach",23,3.6,0.4,2.9,2.2,0.4,0.1,0.2,100,"g");
        insertLibraryItem(db,"Cucumber",15,3.6,0.1,0.7,0.5,1.7,0.0,0.0,100,"g");
        insertLibraryItem(db,"Mushroom",22,3.3,0.3,3.1,1.0,2.0,0.0,0.1,100,"g");
        insertLibraryItem(db,"Tomato",18,3.9,0.2,0.9,1.2,2.6,0.0,0.1,100,"g");
        insertLibraryItem(db,"Asparagus",20,3.9,0.1,2.2,2.1,1.9,0.0,0.0,100,"g");
        insertLibraryItem(db,"Butter",717,0.1,81.0,0.9,0.0,0.1,51.0,3.0,100,"g");
        insertLibraryItem(db,"Olive Oil",884,0.0,100.0,0.0,0.0,0.0,14.0,10.5,100,"ml");
        insertLibraryItem(db,"Coconut Oil",892,0.0,100.0,0.0,0.0,0.0,82.0,0.2,100,"g");
        insertLibraryItem(db,"Walnuts",654,14.0,65.0,15.0,7.0,2.6,6.1,47.2,100,"g");
        insertLibraryItem(db,"Cashews",553,30.0,44.0,18.0,3.3,5.9,7.8,7.8,100,"g");
        insertLibraryItem(db,"Chia Seed",486,42.0,31.0,17.0,34.0,0.0,3.3,23.7,100,"g");
        insertLibraryItem(db,"Kefir",55,4.0,2.0,3.5,0.0,4.0,1.3,0.1,100,"g");
        insertLibraryItem(db,"Kimchee",15,2.4,0.5,1.1,1.6,1.1,0.1,0.3,100,"g");
        insertLibraryItem(db,"Oatmeal",389,66.0,7.0,17.0,11.0,1.0,1.2,2.5,100,"g");
        insertLibraryItem(db,"Greek Yogurt",59,3.6,0.4,10.0,0.0,3.2,0.1,0.0,100,"g");
        insertLibraryItem(db,"Milk",42,5.0,1.0,3.4,0.0,5.0,0.6,0.0,100,"ml");
        insertLibraryItem(db,"Cheddar Cheese",403,1.3,33.0,25.0,0.0,0.5,19.0,1.0,100,"g");
        insertLibraryItem(db,"Peanut Butter",588,20.0,50.0,25.0,6.0,9.0,10.0,15.7,100,"g");
        insertLibraryItem(db,"Tofu",76,1.9,4.8,8.0,0.3,0.6,0.7,2.7,100,"g");
        insertLibraryItem(db,"Tuna",132,0.0,1.0,29.0,0.0,0.0,0.3,0.4,100,"g");
        insertLibraryItem(db,"Sardines",208,0.0,11.0,25.0,0.0,0.0,1.5,3.9,100,"g");
        insertLibraryItem(db,"Bangus",148,0.0,7.0,20.0,0.0,0.0,2.0,1.5,100,"g");
        insertLibraryItem(db,"Tilapia",96,0.0,1.7,20.0,0.0,0.0,0.6,0.6,100,"g");
    }

    private void insertLibraryItem(SQLiteDatabase db, String name, double calories,
                                   double carbs, double fat, double protein,
                                   double fiber, double sugar, double satFat,
                                   double polyFat, double size, String unit) {
        ContentValues v = new ContentValues();
        v.put(COL_LIB_NAME, name);    v.put(COL_LIB_CAL, calories);
        v.put(COL_LIB_CARBS, carbs);  v.put(COL_LIB_FAT, fat);
        v.put(COL_LIB_PRO, protein);  v.put(COL_LIB_FIBER, fiber);
        v.put(COL_LIB_SUGAR, sugar);  v.put(COL_LIB_SAT_FAT, satFat);
        v.put(COL_LIB_POLY, polyFat); v.put(COL_LIB_SIZE, size);
        v.put(COL_LIB_UNIT, unit);
        db.insert(TABLE_FOOD_LIBRARY, null, v);
    }

    public boolean addFoodLogEntry(String email, String mealType, String foodName,
                                   double servingSize, String servingUnit,
                                   double calories, double protein,
                                   double carbs, double fat,
                                   double fiber, double sugar,
                                   double satFat, double polyFat,
                                   String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EMAIL, email);       cv.put(COL_MEAL_TYPE, mealType);
        cv.put(COL_FOOD_NAME, foodName);   cv.put(COL_SERVING_SIZE, servingSize);
        cv.put(COL_SERVING_UNIT, servingUnit); cv.put(COL_CALORIES, calories);
        cv.put(COL_PROTEIN, protein);      cv.put(COL_CARBS, carbs);
        cv.put(COL_FAT, fat);              cv.put(COL_FIBER, fiber);
        cv.put(COL_SUGAR, sugar);          cv.put(COL_SAT_FAT, satFat);
        cv.put(COL_POLY_FAT, polyFat);     cv.put(COL_LOG_DATE, date);
        long result = db.insert(TABLE_FOOD_LOG, null, cv);
        return result != -1;
    }

    public ArrayList<FoodEntry> getFoodByMeal(String email, String date, String mealType) {
        ArrayList<FoodEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_FOOD_ID + ", " + COL_FOOD_NAME + ", "
                        + COL_SERVING_SIZE + ", " + COL_SERVING_UNIT + ", "
                        + COL_CALORIES + ", " + COL_PROTEIN
                        + " FROM " + TABLE_FOOD_LOG
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ? AND "
                        + COL_MEAL_TYPE + " = ?",
                new String[]{email, date, mealType});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FoodEntry food = new FoodEntry(
                        cursor.getString(1), cursor.getDouble(2), cursor.getString(3),
                        (int) cursor.getDouble(4), (int) cursor.getDouble(5));
                food.setId(cursor.getInt(0));
                list.add(food);
            }
            cursor.close();
        }
        return list;
    }

    public MacroTotals getMacroTotalsForDate(String email, String date) {
        if (email == null || date == null) return new MacroTotals();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_CARBS + "), SUM(" + COL_FAT + "), SUM(" + COL_PROTEIN + "), "
                        + "SUM(" + COL_FIBER + "), SUM(" + COL_SUGAR + "), SUM(" + COL_SAT_FAT + "), "
                        + "SUM(" + COL_POLY_FAT + ") FROM " + TABLE_FOOD_LOG
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?",
                new String[]{email, date});
        MacroTotals totals = new MacroTotals();
        if (cursor != null && cursor.moveToFirst()) {
            totals.carbs   = cursor.isNull(0) ? 0 : cursor.getDouble(0);
            totals.fat     = cursor.isNull(1) ? 0 : cursor.getDouble(1);
            totals.protein = cursor.isNull(2) ? 0 : cursor.getDouble(2);
            totals.fiber   = cursor.isNull(3) ? 0 : cursor.getDouble(3);
            totals.sugar   = cursor.isNull(4) ? 0 : cursor.getDouble(4);
            totals.satFat  = cursor.isNull(5) ? 0 : cursor.getDouble(5);
            totals.polyFat = cursor.isNull(6) ? 0 : cursor.getDouble(6);
            cursor.close();
        }
        return totals;
    }

    public double[] getCaloriesPerMeal(String email, String date) {
        double[] result = new double[4];
        if (email == null || date == null) return result;
        String[] meals = {"Breakfast", "Lunch", "Dinner", "Snacks"};
        SQLiteDatabase db = this.getReadableDatabase();
        for (int i = 0; i < meals.length; i++) {
            Cursor cursor = db.rawQuery(
                    "SELECT SUM(" + COL_CALORIES + ") FROM " + TABLE_FOOD_LOG
                            + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ? AND "
                            + COL_MEAL_TYPE + " = ?",
                    new String[]{email, date, meals[i]});
            if (cursor != null && cursor.moveToFirst()) {
                result[i] = cursor.isNull(0) ? 0 : cursor.getDouble(0);
                cursor.close();
            }
        }
        return result;
    }

    public ArrayList<String[]> getTopFoodsByCarbs(String email, String date, int limit) {
        ArrayList<String[]> list = new ArrayList<>();
        if (email == null || date == null) return list;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_FOOD_NAME + ", " + COL_CARBS + " FROM " + TABLE_FOOD_LOG
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?"
                        + " ORDER BY " + COL_CARBS + " DESC LIMIT ?",
                new String[]{email, date, String.valueOf(limit)});
        if (cursor != null) {
            while (cursor.moveToNext())
                list.add(new String[]{cursor.getString(0),
                        String.format("%.0f g", cursor.getDouble(1))});
            cursor.close();
        }
        return list;
    }

    public MacroTotals getMacroGoals(String email) {
        MacroTotals goals = new MacroTotals();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_CARB_GOAL + ", " + COLUMN_FAT_GOAL + ", "
                        + COLUMN_PROTEIN_GOAL + ", " + COLUMN_FIBER_GOAL + ", "
                        + COLUMN_SUGAR_GOAL + ", " + COLUMN_SAT_FAT_GOAL + ", "
                        + COLUMN_POLY_GOAL + ", " + COLUMN_CALORIE_GOAL
                        + " FROM " + TABLE_NAME + " WHERE " + COLUMN_EMAIL + " = ?",
                new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            goals.carbs    = cursor.getDouble(0); goals.fat      = cursor.getDouble(1);
            goals.protein  = cursor.getDouble(2); goals.fiber    = cursor.getDouble(3);
            goals.sugar    = cursor.getDouble(4); goals.satFat   = cursor.getDouble(5);
            goals.polyFat  = cursor.getDouble(6); goals.calories = cursor.getDouble(7);
            cursor.close();
        }
        return goals;
    }

    public boolean updateMacroGoals(String email, int carbs, int fat, int fiber,
                                    int sugar, int satFat, int poly) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CARB_GOAL, carbs);    cv.put(COLUMN_FAT_GOAL, fat);
        cv.put(COLUMN_FIBER_GOAL, fiber);   cv.put(COLUMN_SUGAR_GOAL, sugar);
        cv.put(COLUMN_SAT_FAT_GOAL, satFat); cv.put(COLUMN_POLY_GOAL, poly);
        return db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email}) > 0;
    }

    public boolean deleteFoodEntry(int foodId, String email, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        double caloriesToSubtract = 0, proteinToSubtract = 0;
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_CALORIES + ", " + COL_PROTEIN
                        + " FROM " + TABLE_FOOD_LOG + " WHERE " + COL_FOOD_ID + " = ?",
                new String[]{String.valueOf(foodId)});
        if (cursor.moveToFirst()) {
            caloriesToSubtract = cursor.getDouble(0);
            proteinToSubtract  = cursor.getDouble(1);
        }
        cursor.close();
        int deleted = db.delete(TABLE_FOOD_LOG, COL_FOOD_ID + " = ?",
                new String[]{String.valueOf(foodId)});
        if (deleted > 0) {
            db.execSQL("UPDATE " + TABLE_DAILY_LOGS
                            + " SET " + COL_CUR_CALORIES + " = MAX(0, " + COL_CUR_CALORIES + " - ?), "
                            + COL_CUR_PROTEIN + " = MAX(0, " + COL_CUR_PROTEIN + " - ?)"
                            + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?",
                    new Object[]{caloriesToSubtract, proteinToSubtract, email, date});
            return true;
        }
        return false;
    }

    public boolean updateUserWeight(String email, double newWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WEIGHT, newWeight);
        return db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email}) > 0;
    }

    public boolean addWorkoutEntry(String email, String date, String name,
                                   int sets, int reps, double weight, int calories) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EMAIL, email);         cv.put(COL_WORKOUT_DATE, date);
        cv.put(COL_WORKOUT_NAME, name);      cv.put(COL_WORKOUT_SETS, sets);
        cv.put(COL_WORKOUT_REPS, reps);      cv.put(COL_WORKOUT_WEIGHT, weight);
        cv.put(COL_WORKOUT_CALS, calories);
        return db.insert(TABLE_WORKOUT_LOG, null, cv) != -1;
    }

    public boolean hasWeightEntryForDate(String email, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_WEIGHT_ID + " FROM " + TABLE_WEIGHT
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_WEIGHT_DATE + " = ?",
                new String[]{email, date});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public ArrayList<String> getWorkoutsForDate(String email, String date) {
        ArrayList<String> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_WORKOUT_NAME + ", " + COL_WORKOUT_SETS + ", "
                        + COL_WORKOUT_REPS + ", " + COL_WORKOUT_CALS
                        + " FROM " + TABLE_WORKOUT_LOG
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_WORKOUT_DATE + " = ?",
                new String[]{email, date});
        if (cursor != null) {
            while (cursor.moveToNext())
                results.add(cursor.getString(0) + " — " + cursor.getInt(1) + "x" + cursor.getInt(2)
                        + (cursor.getInt(3) > 0 ? " (~" + cursor.getInt(3) + " kcal)" : ""));
            cursor.close();
        }
        return results;
    }

    public boolean addWeightEntry(String email, double weightValue, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EMAIL, email);
        cv.put(COL_WEIGHT_VALUE, weightValue);
        cv.put(COL_WEIGHT_DATE, date);
        return db.insert(TABLE_WEIGHT, null, cv) != -1;
    }

    public ArrayList<FoodEntry> searchFoodLibrary(String queryText) {
        ArrayList<FoodEntry> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_FOOD_LIBRARY + " WHERE " + COL_LIB_NAME + " LIKE ?",
                new String[]{"%" + queryText + "%"});
        if (cursor != null) {
            while (cursor.moveToNext())
                results.add(new FoodEntry(
                        cursor.getString(1), cursor.getDouble(10), cursor.getString(11),
                        cursor.getInt(2), (int) cursor.getDouble(5),
                        cursor.getDouble(3), cursor.getDouble(4), cursor.getDouble(6),
                        cursor.getDouble(7), cursor.getDouble(8), cursor.getDouble(9)));
            cursor.close();
        }
        return results;
    }

    public boolean addUser(String user, String pass, String email, String birthday,
                           String gender, double weight, double height,
                           String goal, String activityLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, user);         cv.put(COLUMN_PASSWORD, pass);
        cv.put(COLUMN_EMAIL, email);           cv.put(COLUMN_BIRTHDAY, birthday);
        cv.put(COLUMN_GENDER, gender);         cv.put(COLUMN_WEIGHT, weight);
        cv.put(COLUMN_HEIGHT, height);         cv.put(COLUMN_GOAL, goal);
        cv.put(COLUMN_ACTIVITY_LEVEL, activityLevel); cv.put(COLUMN_IS_DELETED, 0);
        return db.insert(TABLE_NAME, null, cv) != -1;
    }

    boolean searchUser(String email, String plainPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_PASSWORD},
                COLUMN_EMAIL + " = ? AND " + COLUMN_IS_DELETED + " = 0",
                new String[]{email}, null, null, null, "1");
        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) cursor.close();
            return false;
        }
        String storedHash = cursor.getString(0);
        cursor.close();
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateUserImage(String email, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PROFILE_IMAGE, imageUri);
        return db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email}) > 0;
    }

    public boolean softDeleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_DELETED, 1);
        return db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email}) > 0;
    }

    public ArrayList<PersonalInfo> getUserList(String email) {
        ArrayList<PersonalInfo> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_USERNAME + ", " + COLUMN_EMAIL + ", " + COLUMN_HEIGHT + ", "
                        + COLUMN_WEIGHT + ", " + COLUMN_BIRTHDAY + ", " + COLUMN_GENDER + ", "
                        + COLUMN_GOAL + ", " + COLUMN_ACTIVITY_LEVEL + ", "
                        + COLUMN_CALORIE_GOAL + ", " + COLUMN_PROTEIN_GOAL + ", "
                        + COLUMN_WATER_GOAL + ", " + COLUMN_STEP_GOAL + ", "
                        + COLUMN_PROFILE_IMAGE
                        + " FROM " + TABLE_NAME + " WHERE " + COLUMN_EMAIL + " = ?",
                new String[]{email});
        if (cursor.moveToFirst()) {
            do {
                userList.add(new PersonalInfo(
                        cursor.getString(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5),
                        cursor.getString(6), cursor.getString(7),
                        cursor.getInt(8),    cursor.getInt(9),
                        cursor.getInt(10),   cursor.getInt(11),
                        cursor.getString(12)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userList;
    }

    public boolean updateFullProfile(String email, String name, String height, String dob) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, name); cv.put(COLUMN_HEIGHT, height); cv.put(COLUMN_BIRTHDAY, dob);
        return db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email}) > 0;
    }

    public boolean updateGoals(String email, int calories, int steps, int water, int protein) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CALORIE_GOAL, calories); cv.put(COLUMN_STEP_GOAL, steps);
        cv.put(COLUMN_WATER_GOAL, water);      cv.put(COLUMN_PROTEIN_GOAL, protein);
        return db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email}) > 0;
    }

    public void checkAndInitDailyLog(String email, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_LOG_ID + " FROM " + TABLE_DAILY_LOGS
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?",
                new String[]{email, date});
        if (cursor.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_EMAIL, email); cv.put(COL_LOG_DATE, date);
            cv.put(COL_CUR_WATER, 0);    cv.put(COL_CUR_STEPS, 0);
            cv.put(COL_CUR_PROTEIN, 0);  cv.put(COL_CUR_CALORIES, 0);
            db.insert(TABLE_DAILY_LOGS, null, cv);
        }
        cursor.close();
    }

    public void updateDailyValue(String email, String date, String column, int newValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(column, newValue);
        db.update(TABLE_DAILY_LOGS, cv,
                COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?",
                new String[]{email, date});
    }

    public ArrayList<WeightEntry> getWeightHistoryList(String email) {
        ArrayList<WeightEntry> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_WEIGHT_VALUE + ", " + COL_WEIGHT_DATE
                        + " FROM " + TABLE_WEIGHT
                        + " WHERE " + COLUMN_EMAIL + " = ? ORDER BY " + COL_WEIGHT_DATE + " ASC",
                new String[]{email});
        if (cursor != null) {
            while (cursor.moveToNext())
                history.add(new WeightEntry(cursor.getDouble(0), cursor.getString(1)));
            cursor.close();
        }
        return history;
    }

    public ArrayList<Integer> getDailyStats(String email, String date) {
        ArrayList<Integer> stats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_CUR_WATER + ", " + COL_CUR_STEPS + ", "
                        + COL_CUR_CALORIES + ", " + COL_CUR_PROTEIN
                        + " FROM " + TABLE_DAILY_LOGS
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?",
                new String[]{email, date});
        if (cursor.moveToFirst()) {
            stats.add(cursor.getInt(0)); stats.add(cursor.getInt(1));
            stats.add(cursor.getInt(2)); stats.add(cursor.getInt(3));
        } else {
            stats.add(0); stats.add(0); stats.add(0); stats.add(0);
        }
        cursor.close();
        return stats;
    }

    public ArrayList<StepEntry> getStepsHistoryList(String email) {
        ArrayList<StepEntry> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_LOG_DATE + ", " + COL_CUR_STEPS
                        + " FROM " + TABLE_DAILY_LOGS
                        + " WHERE " + COLUMN_EMAIL + " = ? ORDER BY " + COL_LOG_DATE + " ASC",
                new String[]{email});
        if (cursor.moveToFirst()) {
            do {
                history.add(new StepEntry(cursor.getString(0), cursor.getInt(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return history;
    }

    public void setHabitDone(String email, String date, boolean done) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_LOG_ID + " FROM " + TABLE_HABIT_LOG
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_HABIT_DATE + " = ?",
                new String[]{email, date});
        ContentValues cv = new ContentValues();
        cv.put(COL_HABIT_DONE, done ? 1 : 0);
        if (cursor.getCount() > 0) {
            db.update(TABLE_HABIT_LOG, cv,
                    COLUMN_EMAIL + " = ? AND " + COL_HABIT_DATE + " = ?",
                    new String[]{email, date});
        } else {
            cv.put(COLUMN_EMAIL, email);
            cv.put(COL_HABIT_DATE, date);
            db.insert(TABLE_HABIT_LOG, null, cv);
        }
        cursor.close();
    }

    public java.util.HashSet<String> getCompletedHabitDates(String email,
                                                            java.util.List<String> dates) {
        java.util.HashSet<String> done = new java.util.HashSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        for (String date : dates) {
            Cursor cursor = db.rawQuery(
                    "SELECT " + COL_HABIT_DONE + " FROM " + TABLE_HABIT_LOG
                            + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_HABIT_DATE
                            + " = ? AND " + COL_HABIT_DONE + " = 1",
                    new String[]{email, date});
            if (cursor.getCount() > 0) done.add(date);
            cursor.close();
        }
        return done;
    }

    public boolean doesDailyDataSatisfyHabit(String email, String date, String habitName) {
        if (habitName == null || email == null || date == null) return false;
        ArrayList<Integer> stats = getDailyStats(email, date);
        if (stats == null || stats.size() < 4) return false;
        int water = stats.get(0), steps = stats.get(1),
                calories = stats.get(2), protein = stats.get(3);
        ArrayList<PersonalInfo> users = getUserList(email);
        PersonalInfo p = (!users.isEmpty()) ? users.get(0) : null;
        switch (habitName) {
            case "Eat more protein": return p != null && protein >= p.getProteinGoal();
            case "Drink more water": return p != null && water   >= p.getWaterGoal();
            case "Log a daily meal": return calories > 0;
            case "Hit my step goal": return p != null && steps   >= p.getStepGoal();
            case "Get more exercise":
                return getWorkoutsForDate(email, date) != null
                        && !getWorkoutsForDate(email, date).isEmpty();
            default: return false;
        }
    }

    public int getExerciseCalories(String email, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        int total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_WORKOUT_CALS + ") FROM " + TABLE_WORKOUT_LOG
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_WORKOUT_DATE + " = ?",
                new String[]{email, date});
        if (cursor.moveToFirst()) {
            total = cursor.isNull(0) ? 0 : cursor.getInt(0);
        }
        cursor.close();
        return total;
    }
    public String getHashedPassword(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = null;

        String query = "SELECT " + COLUMN_PASSWORD + " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_IS_DELETED + " = 0";

        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                hashedPassword = cursor.getString(0);
            }
            cursor.close();
        }
        return hashedPassword;
    }
}