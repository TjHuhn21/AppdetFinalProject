package com.example.heronhealth.model;

public class FoodEntry {
    private String name;
    private double servingSize;
    private String unit;
    private int calories;
    private int protein;

    private int id;

    public FoodEntry(String name, double servingSize, String unit, int calories, int protein) {
        this.name = name;
        this.servingSize = servingSize;
        this.unit = unit;
        this.calories = calories;
        this.protein = protein;
    }




    // --- GETTERS ---
    public String getName() { return name; }
    public int getId() { return id; }
    public double getServingSize() { return servingSize; }
    public String getUnit() { return unit; }
    public int getCalories() { return calories; }
    public int getProtein() { return protein; }

    // --- SETTERS ---
    public void setName(String name) { this.name = name; }
    public void setId(int id) { this.id = id; }
    public void setServingSize(double servingSize) { this.servingSize = servingSize; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setProtein(int protein) { this.protein = protein; }

    // Helper for UI Display
    public String getDisplayText() {
        return name + " (" + servingSize + " " + unit + ")";
    }
}