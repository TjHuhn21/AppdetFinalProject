package com.example.heronhealth.model;

public class PersonalInfo {
    private String name;
    private String email;
    private String height;
    private String weight; // Added
    private String dateOfBirth;
    private String gender; // Added
    private String goal; // Added
    private String activityLevel; // Added

    // Goal-related fields (to store the results of your calculation)
    private int calorieGoal;
    private int proteinGoal;
    private int waterGoal;
    private int stepGoal;

    // Full Constructor
    public PersonalInfo(String name, String email, String height, String weight,
                        String dateOfBirth, String gender, String goal, String activityLevel,
                        int calorieGoal, int proteinGoal, int waterGoal, int stepGoal) {
        this.name = name;
        this.email = email;
        this.height = height;
        this.weight = weight;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.goal = goal;
        this.activityLevel = activityLevel;
        this.calorieGoal = calorieGoal;
        this.proteinGoal = proteinGoal;
        this.waterGoal = waterGoal;
        this.stepGoal = stepGoal;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getHeight() { return height; }
    public String getWeight() { return weight; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getGoal() { return goal; }
    public String getActivityLevel() { return activityLevel; }

    public int getCalorieGoal() { return calorieGoal; }
    public int getProteinGoal() { return proteinGoal; }
    public int getWaterGoal() { return waterGoal; }
    public int getStepGoal() { return stepGoal; }

    // --- SETTERS ---
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setHeight(String height) { this.height = height; }
    public void setWeight(String weight) { this.weight = weight; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setGender(String gender) { this.gender = gender; }
    public void setGoal(String goal) { this.goal = goal; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public void setCalorieGoal(int calorieGoal) { this.calorieGoal = calorieGoal; }
    public void setProteinGoal(int proteinGoal) { this.proteinGoal = proteinGoal; }
    public void setWaterGoal(int waterGoal) { this.waterGoal = waterGoal; }
    public void setStepGoal(int stepGoal) { this.stepGoal = stepGoal; }
}