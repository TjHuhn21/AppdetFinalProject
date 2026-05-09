package com.example.heronhealth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "HeronHealth.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "heron_User";
    private static final String COLUMN_ID = "user_ID";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "user_password";
    private static final String COLUMN_EMAIL = "user_email";
    private static final String COLUMN_AGE = "user_age";
    private static final String COLUMN_BIRTHDAY = "user_birthday";
    private static final String COLUMN_GENDER = "user_gender";
    private static final String COLUMN_WEIGHT = "user_weight";
    private static final String COLUMN_HEIGHT = "user_height";
    private static final String COLUMN_GOAL = "user_goal";


    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_BIRTHDAY + " TEXT, "
                + COLUMN_GENDER + " TEXT, "
                + COLUMN_WEIGHT + " REAL, "
                + COLUMN_HEIGHT + " REAL, "
                + COLUMN_GOAL + " TEXT)";
        db.execSQL(CREATE_USER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }
    public boolean addUser(String user, String pass, String email,String birthday,
                           String gender, double weight, double height, String goal) {
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

        long result = db.insert(TABLE_NAME, null, cv);

        db.close();


        return result != -1;
    }
    boolean searchUser(String username, String password){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = { COLUMN_ID };

        String selection = COLUMN_USERNAME + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";

        String[] selectionArgs = { username, password };

        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }
}
