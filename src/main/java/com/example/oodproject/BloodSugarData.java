package com.example.oodproject;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class BloodSugarData {
    private final ObjectProperty<LocalDate> date;
    private final StringProperty time;
    private final DoubleProperty beforeMeal;
    private final DoubleProperty afterMeal;
    private final DoubleProperty avgDaily;
    private final DoubleProperty maxDaily;
    private final DoubleProperty minDaily;
    private final DoubleProperty avgWeekly;
    private final DoubleProperty maxWeekly;
    private final DoubleProperty minWeekly;
    private final DoubleProperty avgMonthly;
    private final DoubleProperty maxMonthly;
    private final DoubleProperty minMonthly;

    public BloodSugarData(LocalDate date, String time, double beforeMeal, double afterMeal,
                          double avgDaily, double maxDaily, double minDaily,
                          double avgWeekly, double maxWeekly, double minWeekly,
                          double avgMonthly, double maxMonthly, double minMonthly) {
        this.date = new SimpleObjectProperty<>(date);
        this.time = new SimpleStringProperty(time);
        this.beforeMeal = new SimpleDoubleProperty(beforeMeal);
        this.afterMeal = new SimpleDoubleProperty(afterMeal);
        this.avgDaily = new SimpleDoubleProperty(avgDaily);
        this.maxDaily = new SimpleDoubleProperty(maxDaily);
        this.minDaily = new SimpleDoubleProperty(minDaily);
        this.avgWeekly = new SimpleDoubleProperty(avgWeekly);
        this.maxWeekly = new SimpleDoubleProperty(maxWeekly);
        this.minWeekly = new SimpleDoubleProperty(minWeekly);
        this.avgMonthly = new SimpleDoubleProperty(avgMonthly);
        this.maxMonthly = new SimpleDoubleProperty(maxMonthly);
        this.minMonthly = new SimpleDoubleProperty(minMonthly);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public StringProperty timeProperty() {
        return time;
    }

    public DoubleProperty beforeMealProperty() {
        return beforeMeal;
    }

    public DoubleProperty afterMealProperty() {
        return afterMeal;
    }

    public DoubleProperty avgDailyProperty() {
        return avgDaily;
    }

    public DoubleProperty maxDailyProperty() {
        return maxDaily;
    }

    public DoubleProperty minDailyProperty() {
        return minDaily;
    }

    public DoubleProperty avgWeeklyProperty() {
        return avgWeekly;
    }

    public DoubleProperty maxWeeklyProperty() {
        return maxWeekly;
    }

    public DoubleProperty minWeeklyProperty() {
        return minWeekly;
    }

    public DoubleProperty avgMonthlyProperty() {
        return avgMonthly;
    }

    public DoubleProperty maxMonthlyProperty() {
        return maxMonthly;
    }

    public DoubleProperty minMonthlyProperty() {
        return minMonthly;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public String getTime() {
        return time.get();
    }

    public double getBeforeMeal() {
        return beforeMeal.get();
    }

    public double getAfterMeal() {
        return afterMeal.get();
    }

    public double getAvgDaily() {
        return avgDaily.get();
    }

    public double getMaxDaily() {
        return maxDaily.get();
    }

    public double getMinDaily() {
        return minDaily.get();
    }

    public double getAvgWeekly() {
        return avgWeekly.get();
    }

    public double getMaxWeekly() {
        return maxWeekly.get();
    }

    public double getMinWeekly() {
        return minWeekly.get();
    }

    public double getAvgMonthly() {
        return avgMonthly.get();
    }

    public double getMaxMonthly() {
        return maxMonthly.get();
    }

    public double getMinMonthly() {
        return minMonthly.get();
    }
}