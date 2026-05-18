package com.example.heronhealth.model;

public class FoodEntry {

    private int    id;
    private String name;
    private double servingSize;
    private String unit;
    private int    calories;
    private int    protein;

    // ── New macro fields ─────────────────────────────────────────────────────
    private double carbs;
    private double fat;
    private double fiber;
    private double sugar;
    private double satFat;
    private double polyFat;

    // ── Constructor used by searchFoodLibrary (full macros) ──────────────────
    public FoodEntry(String name, double servingSize, String unit,
                     int calories, int protein,
                     double carbs, double fat, double fiber,
                     double sugar, double satFat, double polyFat) {
        this.name        = name;
        this.servingSize = servingSize;
        this.unit        = unit;
        this.calories    = calories;
        this.protein     = protein;
        this.carbs       = carbs;
        this.fat         = fat;
        this.fiber       = fiber;
        this.sugar       = sugar;
        this.satFat      = satFat;
        this.polyFat     = polyFat;
    }

    // ── Legacy constructor (used by getFoodByMeal — no macros needed there) ──
    public FoodEntry(String name, double servingSize, String unit,
                     int calories, int protein) {
        this(name, servingSize, unit, calories, protein,
                0, 0, 0, 0, 0, 0);
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int    getId()          { return id; }
    public String getName()        { return name; }
    public double getServingSize() { return servingSize; }
    public String getUnit()        { return unit; }
    public int    getCalories()    { return calories; }
    public int    getProtein()     { return protein; }
    public double getCarbs()       { return carbs; }
    public double getFat()         { return fat; }
    public double getFiber()       { return fiber; }
    public double getSugar()       { return sugar; }
    public double getSatFat()      { return satFat; }
    public double getPolyFat()     { return polyFat; }

    // ── Setter (only id needs one — rest are set via constructor) ────────────
    public void setId(int id) { this.id = id; }
}