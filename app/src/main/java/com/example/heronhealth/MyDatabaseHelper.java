package com.example.heronhealth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.heronhealth.model.FoodEntry;
import com.example.heronhealth.model.PersonalInfo;
import com.example.heronhealth.model.WeightEntry;

import java.util.ArrayList;

class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "HeronHealth.db";
    private static final int DATABASE_VERSION = 5;

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

    // Weight Progress Table
    public static final String TABLE_WEIGHT = "weight_progress";
    public static final String COL_WEIGHT_ID = "weight_id";
    public static final String COL_WEIGHT_VALUE = "weight_value";
    public static final String COL_WEIGHT_DATE = "weight_date";

    // Daily Logs Table
    public static final String TABLE_DAILY_LOGS = "daily_logs";
    public static final String COL_LOG_ID = "log_id";
    public static final String COL_LOG_DATE = "log_date";
    public static final String COL_CUR_WATER = "current_water";
    public static final String COL_CUR_STEPS = "current_steps";
    public static final String COL_CUR_PROTEIN = "current_protein";
    public static final String COL_CUR_CALORIES = "current_calories";

    //Food log table
    public static final String TABLE_FOOD_LOG = "food_log";
    public static final String COL_FOOD_ID = "food_id";
    public static final String COL_MEAL_TYPE = "meal_type";
    public static final String COL_FOOD_NAME = "food_name";
    public static final String COL_CALORIES = "calories";
    public static final String COL_PROTEIN = "protein";
    public static final String COL_SERVING_SIZE = "serving_size";
    public static final String COL_SERVING_UNIT = "serving_unit";

    // Master Food Library Table (NEW)
    public static final String TABLE_FOOD_LIBRARY = "food_library";
    public static final String COL_LIB_ID = "lib_id";
    public static final String COL_LIB_NAME = "food_name";
    public static final String COL_LIB_CAL = "calories";
    public static final String COL_LIB_PRO = "protein";
    public static final String COL_LIB_SIZE = "base_serving_size";
    public static final String COL_LIB_UNIT = "base_unit";

    //Exercise table
    public static final String TABLE_WORKOUT_LOG  = "workout_log";
    public static final String COL_WORKOUT_ID     = "workout_id";
    public static final String COL_WORKOUT_NAME   = "workout_name";
    public static final String COL_WORKOUT_SETS   = "sets";
    public static final String COL_WORKOUT_REPS   = "reps";
    public static final String COL_WORKOUT_WEIGHT = "weight_kg";
    public static final String COL_WORKOUT_CALS   = "calories_burned";
    public static final String COL_WORKOUT_DATE   = "workout_date";


    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_BIRTHDAY + " TEXT, "
                + COLUMN_GENDER + " TEXT, "
                + COLUMN_WEIGHT + " REAL, "
                + COLUMN_HEIGHT + " REAL, "
                + COLUMN_GOAL + " TEXT, "
                + COLUMN_ACTIVITY_LEVEL + " TEXT, "
                + COLUMN_CALORIE_GOAL + " INTEGER, "
                + COLUMN_PROTEIN_GOAL + " INTEGER, "
                + COLUMN_WATER_GOAL + " INTEGER, "
                + COLUMN_STEP_GOAL + " INTEGER, "
                + COLUMN_PROFILE_IMAGE + " TEXT, " // Add this line
                + COLUMN_IS_DELETED + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_USER_TABLE);
        //Food library
        String CREATE_LIB_TABLE = "CREATE TABLE " + TABLE_FOOD_LIBRARY + " ("
                + COL_LIB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LIB_NAME + " TEXT, "
                + COL_LIB_CAL + " INTEGER, "
                + COL_LIB_PRO + " REAL, "
                + COL_LIB_SIZE + " REAL, "
                + COL_LIB_UNIT + " TEXT)";
        db.execSQL(CREATE_LIB_TABLE);

        //Weight table
        String CREATE_WEIGHT_TABLE = "CREATE TABLE " + TABLE_WEIGHT + " ("
                + COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT, "
                + COL_WEIGHT_VALUE + " REAL, "
                + COL_WEIGHT_DATE + " TEXT)";
        db.execSQL(CREATE_WEIGHT_TABLE);

        // Daily Logs Table
        String CREATE_LOGS_TABLE = "CREATE TABLE " + TABLE_DAILY_LOGS + " ("
                + COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT, "
                + COL_LOG_DATE + " TEXT, "
                + COL_CUR_WATER + " INTEGER DEFAULT 0, "
                + COL_CUR_STEPS + " INTEGER DEFAULT 0, "
                + COL_CUR_PROTEIN + " INTEGER DEFAULT 0, "
                + COL_CUR_CALORIES + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_LOGS_TABLE);

        String CREATE_FOOD_TABLE = "CREATE TABLE " + TABLE_FOOD_LOG + " ("
                + COL_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT, "
                + COL_MEAL_TYPE + " TEXT, "
                + COL_FOOD_NAME + " TEXT, "
                + COL_SERVING_SIZE + " REAL, "
                + COL_SERVING_UNIT + " TEXT, "
                + COL_CALORIES + " INTEGER, "
                + COL_PROTEIN + " INTEGER, "
                + COL_LOG_DATE + " TEXT)";
        db.execSQL(CREATE_FOOD_TABLE);
        String CREATE_WORKOUT_TABLE = "CREATE TABLE " + TABLE_WORKOUT_LOG + " ("
                + COL_WORKOUT_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL       + " TEXT, "
                + COL_WORKOUT_DATE   + " TEXT, "
                + COL_WORKOUT_NAME   + " TEXT, "
                + COL_WORKOUT_SETS   + " INTEGER, "
                + COL_WORKOUT_REPS   + " INTEGER, "
                + COL_WORKOUT_WEIGHT + " REAL, "
                + COL_WORKOUT_CALS   + " INTEGER)";
        db.execSQL(CREATE_WORKOUT_TABLE);

        prefillFoodLibrary(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_LIBRARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_LOG);
        onCreate(db);

    }
    private void prefillFoodLibrary(SQLiteDatabase db) {
        // PROTEINS - MEAT (Per 100g)
        insertLibraryItem(db, "Lean Minced Beef", 125, 22.0, 100, "g");
        insertLibraryItem(db, "Beef Sirloin Steak", 134, 23.0, 100, "g");
        insertLibraryItem(db, "Chicken Light Meat", 106, 24.0, 100, "g");
        insertLibraryItem(db, "Chicken Dark Meat", 109, 20.9, 100, "g");
        insertLibraryItem(db, "Lean Minced Lamb", 156, 19.1, 100, "g");
        insertLibraryItem(db, "Turkey Light Meat", 105, 24.4, 100, "g");
        insertLibraryItem(db, "Pork Loin Steak", 225, 19.9, 100, "g");
        insertLibraryItem(db, "Bacon", 215, 16.5, 100, "g");
        insertLibraryItem(db, "Venison", 103, 22.2, 100, "g");

        // PROTEINS - FISH & SEAFOOD (Per 100g)
        insertLibraryItem(db, "Cod", 75, 17.5, 100, "g");
        insertLibraryItem(db, "Haddock", 75, 17.8, 100, "g");
        insertLibraryItem(db, "Wild Salmon", 179, 22.1, 100, "g");
        insertLibraryItem(db, "Prawns", 77, 17.6, 100, "g");
        insertLibraryItem(db, "Squid", 77, 15.4, 100, "g");
        insertLibraryItem(db, "Lobster", 77, 17.0, 100, "g");

        // PROTEINS - OTHER & POWDERS
        insertLibraryItem(db, "Egg (Large)", 131, 13.0, 100, "g");
        insertLibraryItem(db, "Whey Protein Isolate", 379, 90.0, 100, "g");
        insertLibraryItem(db, "Pea Protein", 366, 66.0, 100, "g");
        insertLibraryItem(db, "Beef Broth", 111, 7.8, 100, "ml");

        // STARCHY CARBS (Per 100g)
        insertLibraryItem(db, "Quinoa", 319, 14.0, 100, "g");
        insertLibraryItem(db, "Rice - Brown", 354, 9.3, 100, "g");
        insertLibraryItem(db, "Rice - White", 345, 8.5, 100, "g");
        insertLibraryItem(db, "Chickpeas", 338, 21.0, 100, "g");
        insertLibraryItem(db, "Lentils (Red)", 323, 24.0, 100, "g");
        insertLibraryItem(db, "Buckwheat", 362, 8.1, 100, "g");

        // STARCHY VEGETABLES (Per 100g)
        insertLibraryItem(db, "Potatoes", 84, 1.9, 100, "g");
        insertLibraryItem(db, "Sweet Potatoes", 91, 1.2, 100, "g");
        insertLibraryItem(db, "Carrots", 43, 0.0, 100, "g");
        insertLibraryItem(db, "Beetroot", 45, 1.7, 100, "g");

        // FRUIT (Per 100g)
        insertLibraryItem(db, "Apple", 27, 0.6, 100, "g");
        insertLibraryItem(db, "Banana", 90, 1.1, 100, "g");
        insertLibraryItem(db, "Blueberry", 45, 0.9, 100, "g");
        insertLibraryItem(db, "Mango", 67, 0.7, 100, "g");
        insertLibraryItem(db, "Orange", 43, 0.8, 100, "g");
        insertLibraryItem(db, "Strawberry", 42, 0.6, 100, "g");
        insertLibraryItem(db, "Avocado", 201, 1.9, 100, "g");

        // NON-STARCHY VEGETABLES (Per 100g)
        insertLibraryItem(db, "Broccoli", 47, 4.3, 100, "g");
        insertLibraryItem(db, "Spinach", 19, 2.6, 100, "g");
        insertLibraryItem(db, "Cucumber", 19, 1.0, 100, "g");
        insertLibraryItem(db, "Mushroom", 18, 2.5, 100, "g");
        insertLibraryItem(db, "Tomato", 21, 0.0, 100, "g");
        insertLibraryItem(db, "Asparagus", 32, 2.9, 100, "g");

        // FATS (Per 100g/100ml)
        insertLibraryItem(db, "Butter", 749, 0.6, 100, "g");
        insertLibraryItem(db, "Olive Oil", 903, 0.0, 100, "ml");
        insertLibraryItem(db, "Coconut Oil", 903, 0.0, 100, "g");
        insertLibraryItem(db, "Walnuts", 714, 17.0, 100, "g");
        insertLibraryItem(db, "Cashews", 597, 21.0, 100, "g");
        insertLibraryItem(db, "Chia Seed", 469, 18.0, 100, "g");

        // PROBIOTICS
        insertLibraryItem(db, "Kefir", 58, 6.1, 100, "g");
        insertLibraryItem(db, "Kimchee", 22, 1.1, 100, "g");
    }
    //insertFoodLibrary
    private void insertLibraryItem(SQLiteDatabase db, String name, int cal, double pro, double size, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LIB_NAME, name);
        cv.put(COL_LIB_CAL, cal);
        cv.put(COL_LIB_PRO, pro);
        cv.put(COL_LIB_SIZE, size);
        cv.put(COL_LIB_UNIT, unit);
        db.insert(TABLE_FOOD_LIBRARY, null, cv);
    }
    public boolean updateUserWeight(String email, double newWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WEIGHT, newWeight);
        int result = db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        return result > 0;
    }


     //Insert a workout entry into workout_log.
    public boolean addWorkoutEntry(String email, String date, String name,
                                   int sets, int reps, double weight, int calories) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EMAIL,       email);
        cv.put(COL_WORKOUT_DATE,   date);
        cv.put(COL_WORKOUT_NAME,   name);
        cv.put(COL_WORKOUT_SETS,   sets);
        cv.put(COL_WORKOUT_REPS,   reps);
        cv.put(COL_WORKOUT_WEIGHT, weight);
        cv.put(COL_WORKOUT_CALS,   calories);
        long result = db.insert(TABLE_WORKOUT_LOG, null, cv);
        db.close();
        return result != -1;
    }
    public boolean hasWeightEntryForDate(String email, String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_WEIGHT_ID + " FROM " + TABLE_WEIGHT
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_WEIGHT_DATE + " = ?",
                new String[]{email, date}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    //Returns a list of workout name + summary strings for a given date.
    public ArrayList<String> getWorkoutsForDate(String email, String date) {
        ArrayList<String> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_WORKOUT_NAME + ", " + COL_WORKOUT_SETS + ", "
                        + COL_WORKOUT_REPS + ", " + COL_WORKOUT_CALS
                        + " FROM " + TABLE_WORKOUT_LOG
                        + " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_WORKOUT_DATE + " = ?",
                new String[]{email, date}
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name     = cursor.getString(0);
                int sets        = cursor.getInt(1);
                int reps        = cursor.getInt(2);
                int cals        = cursor.getInt(3);

                results.add(name + " — " + sets + "x" + reps
                        + (cals > 0 ? " (~" + cals + " kcal)" : ""));
            }
            cursor.close();
        }

        db.close();
        return results;
    }
    public boolean addFoodLogEntry(String email, String mealType, String foodName,
                                   double servingSize, String servingUnit,
                                   int calories, int protein, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EMAIL,    email);
        cv.put(COL_MEAL_TYPE,   mealType);
        cv.put(COL_FOOD_NAME,   foodName);
        cv.put(COL_SERVING_SIZE, servingSize);
        cv.put(COL_SERVING_UNIT, servingUnit);
        cv.put(COL_CALORIES,    calories);
        cv.put(COL_PROTEIN,     protein);
        cv.put(COL_LOG_DATE,    date);
        long result = db.insert(TABLE_FOOD_LOG, null, cv);
        db.close();
        return result != -1;
    }

    //Insert a weight entry into weight_progress
    public boolean addWeightEntry(String email, double weightValue, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EMAIL,    email);
        cv.put(COL_WEIGHT_VALUE, weightValue);
        cv.put(COL_WEIGHT_DATE,  date);
        long result = db.insert(TABLE_WEIGHT, null, cv);
        db.close();
        return result != -1;
    }

    // Search Method
    public ArrayList<FoodEntry> searchFoodLibrary(String queryText) {
        ArrayList<FoodEntry> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Using LIKE with % symbols to match any part of the name
        String query = "SELECT * FROM " + TABLE_FOOD_LIBRARY +
                " WHERE " + COL_LIB_NAME + " LIKE ?";

        Cursor cursor = db.rawQuery(query, new String[]{"%" + queryText + "%"});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                results.add(new FoodEntry(
                        cursor.getString(1), // Name
                        cursor.getDouble(4), // Base Serving Size
                        cursor.getString(5), // Base Unit
                        cursor.getInt(2),    // Calories
                        (int) cursor.getDouble(3) // Protein (cast to int for your model)
                ));
            }
            cursor.close();
        }
        return results;
    }
    public boolean addUser(String user, String pass, String email,String birthday,
                           String gender, double weight, double height, String goal, String activityLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USERNAME, user);
        cv.put(COLUMN_PASSWORD, pass);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_BIRTHDAY, birthday);
        cv.put(COLUMN_GENDER, gender);
        cv.put(COLUMN_WEIGHT, weight);
        cv.put(COLUMN_HEIGHT, height);
        cv.put(COLUMN_GOAL, goal);
        cv.put(COLUMN_ACTIVITY_LEVEL, activityLevel);

        cv.put(COLUMN_IS_DELETED, 0);

        long result = db.insert(TABLE_NAME, null, cv);

        db.close();


        return result != -1;
    }
    boolean searchUser(String email, String password){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = { COLUMN_ID };

        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ? AND " + COLUMN_IS_DELETED + " = 0";

        String[] selectionArgs = { email, password };

        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }
    public boolean updateUserImage(String email, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PROFILE_IMAGE, imageUri);

        int result = db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        return result > 0;
    }
    public boolean softDeleteUser(String email){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_IS_DELETED, 1);

        int result = db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        return result > 0;
    }

    public ArrayList<PersonalInfo> getUserList(String email) {
        ArrayList<PersonalInfo> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " +
                COLUMN_USERNAME + ", " + COLUMN_EMAIL + ", " + COLUMN_HEIGHT + ", " +
                COLUMN_WEIGHT + ", " + COLUMN_BIRTHDAY + ", " + COLUMN_GENDER + ", " +
                COLUMN_GOAL + ", " + COLUMN_ACTIVITY_LEVEL + ", " +
                COLUMN_CALORIE_GOAL + ", " + COLUMN_PROTEIN_GOAL + ", " +
                COLUMN_WATER_GOAL + ", " + COLUMN_STEP_GOAL + ", " +
                COLUMN_PROFILE_IMAGE +
                " FROM " + TABLE_NAME + " WHERE " + COLUMN_EMAIL + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                // Pass all 13 fields directly into the constructor
                userList.add(new PersonalInfo(
                        cursor.getString(0),  // name
                        cursor.getString(1),  // email
                        cursor.getString(2),  // height
                        cursor.getString(3),  // weight
                        cursor.getString(4),  // birthday
                        cursor.getString(5),  // gender
                        cursor.getString(6),  // goal
                        cursor.getString(7),  // activity
                        cursor.getInt(8),     // calorie goal
                        cursor.getInt(9),     // protein goal
                        cursor.getInt(10),    // water goal
                        cursor.getInt(11),    // step goal
                        cursor.getString(12)  // imageUri (The 13th argument)
                ));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return userList;
    }

    public boolean updateFullProfile(String email, String name, String height, String dob) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USERNAME, name);
        cv.put(COLUMN_HEIGHT, height);
        cv.put(COLUMN_BIRTHDAY, dob);

        int result = db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        return result > 0;
    }
    public boolean updateGoals(String email, int calories, int steps, int water, int protein) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CALORIE_GOAL, calories);
        cv.put(COLUMN_STEP_GOAL, steps);
        cv.put(COLUMN_WATER_GOAL, water);
        cv.put(COLUMN_PROTEIN_GOAL, protein);

        int result = db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        return result > 0;
    }
    // Check if a log exists for today if not create one
    public void checkAndInitDailyLog(String email, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DAILY_LOGS +
                        " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?",
                new String[]{email, date});

        if (cursor.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_EMAIL, email);
            cv.put(COL_LOG_DATE, date);
            cv.put(COL_CUR_WATER, 0);
            cv.put(COL_CUR_STEPS, 0);
            cv.put(COL_CUR_PROTEIN, 0);
            cv.put(COL_CUR_CALORIES, 0);
            db.insert(TABLE_DAILY_LOGS, null, cv);
        }
        cursor.close();
    }

    //Update specific daily values
    public void updateDailyValue(String email, String date, String column, int newValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(column, newValue);

        db.update(TABLE_DAILY_LOGS, cv, COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?",
                new String[]{email, date});
    }
    public ArrayList<WeightEntry> getWeightHistoryList(String email) {
        ArrayList<WeightEntry> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Querying the weight table
        Cursor cursor = db.rawQuery("SELECT " + COL_WEIGHT_VALUE + ", " + COL_WEIGHT_DATE +
                        " FROM " + TABLE_WEIGHT +
                        " WHERE " + COLUMN_EMAIL + " = ? ORDER BY " + COL_WEIGHT_DATE + " ASC",
                new String[]{email});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                double weight = cursor.getDouble(0);
                String date = cursor.getString(1);

                history.add(new WeightEntry(weight, date));
            }
            cursor.close();
        }

        db.close();
        return history;
    }
    public ArrayList<FoodEntry> getFoodByMeal(String email, String date, String mealType) {
        ArrayList<FoodEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Use specific column names in your SELECT to be safe
        String query = "SELECT " + COL_FOOD_NAME + ", " + COL_SERVING_SIZE + ", " +
                COL_SERVING_UNIT + ", " + COL_CALORIES + ", " + COL_PROTEIN +
                " FROM " + TABLE_FOOD_LOG +
                " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ? AND " + COL_MEAL_TYPE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{email, date, mealType});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Indexing starts at 0 based on the SELECT order above
                list.add(new FoodEntry(
                        cursor.getString(0), // Name
                        cursor.getDouble(1), // Serving Size
                        cursor.getString(2), // Unit
                        cursor.getInt(3),    // Calories
                        cursor.getInt(4)     // Protein
                ));
            }
            cursor.close();
        }
        db.close();
        return list;
    }
    public ArrayList<Integer> getDailyStats(String email, String date) {
        ArrayList<Integer> stats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get water, steps, calories, and protein
        String query = "SELECT " + COL_CUR_WATER + ", " + COL_CUR_STEPS + ", " +
                COL_CUR_CALORIES + ", " + COL_CUR_PROTEIN +
                " FROM " + TABLE_DAILY_LOGS +
                " WHERE " + COLUMN_EMAIL + " = ? AND " + COL_LOG_DATE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{email, date});

        if (cursor.moveToFirst()) {
            stats.add(cursor.getInt(0)); // Water
            stats.add(cursor.getInt(1)); // Steps
            stats.add(cursor.getInt(2)); // Calories
            stats.add(cursor.getInt(3)); // Protein
        } else {
            // If no record exists yet, return zeros
            stats.add(0);
            stats.add(0);
            stats.add(0);
            stats.add(0);
        }
        cursor.close();
        db.close();
        return stats;
    }

}
