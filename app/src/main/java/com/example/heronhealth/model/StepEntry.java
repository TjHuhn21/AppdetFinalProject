package com.example.heronhealth.model;

public class StepEntry {

    private String date;
    private int steps;

    public StepEntry(String date, int steps) {
        this.date = date;
        this.steps = steps;
    }

    public String getDate() { return date; }
    public int getSteps() { return steps; }

    public void setDate(String date) {this.date = date;}

    public void setSteps(int steps) {this.steps = steps;}
}
