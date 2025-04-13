package com.example.oodproject;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DailyRoutine extends Application {
    private static final String DARK_BLUE = "#34495E";
    private static final String TEAL = "#1ABC9C";
    private String userName;
    private int userId;

    // Add no-arg constructor
    public DailyRoutine() {
        this("Guest", 0); // Default values
    }

    public DailyRoutine(String userName, int userId) {
        this.userName = userName;
        this.userId = userId;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Your Daily Routine");

        // Main container
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: white;");

        // Top bar (similar to dashboard)
        HBox topBar = createTopBar();
        mainContainer.getChildren().add(topBar);

        // Scroll pane for content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: white;");

        // Content container
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Get user data from database
        UserData userData = getUserData();

        // Display user information
        VBox userInfoBox = createUserInfoBox(userData);
        content.getChildren().add(userInfoBox);

        // Calculate BMI
        double bmi = calculateBMI(userData.weight, userData.height);
        String bmiCategory = getBMICategory(bmi);

        // Display BMI information
        VBox bmiBox = createBMIBox(bmi, bmiCategory);
        content.getChildren().add(bmiBox);

        // Display diet plan based on BMI category
        VBox dietPlanBox = createDietPlanBox(bmiCategory);
        content.getChildren().add(dietPlanBox);

        // Create activity chart
        BarChart<String, Number> activityChart = createActivityChart(bmiCategory);
        content.getChildren().add(activityChart);

        scrollPane.setContent(content);
        mainContainer.getChildren().add(scrollPane);

        Scene scene = new Scene(mainContainer, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopBar() {
        Text welcomeText = new Text("Daily Routine for " + userName);
        welcomeText.setStyle("-fx-fill: white; -fx-font-size: 30px; -fx-font-family: 'Britannic Bold';");

        Text tagline = new Text("Stay Healthy, Stay Strong!");
        tagline.setStyle("-fx-fill: white; -fx-font-size: 20px; -fx-font-family: 'Segoe UI';");

        VBox welcomeBox = new VBox(welcomeText, tagline);
        welcomeBox.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView(new Image(getClass().getResourceAsStream("/Icon/ac.png")));
        logoImage.setFitWidth(200);
        logoImage.setFitHeight(100);

        HBox leftSide = new HBox(10);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        HBox centerSide = new HBox(welcomeBox);
        centerSide.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerSide, Priority.ALWAYS);

        HBox rightSide = new HBox(logoImage);
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        HBox topBar = new HBox(leftSide, centerSide, rightSide);
        topBar.setStyle("-fx-background-color: " + DARK_BLUE + "; -fx-padding: 20px;");
        topBar.setAlignment(Pos.CENTER);
        return topBar;
    }

    private UserData getUserData() {
        UserData userData = new UserData();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT age, gender, weight, height FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userData.age = rs.getInt("age");
                userData.gender = rs.getString("gender");
                userData.weight = rs.getDouble("weight");
                userData.height = rs.getDouble("height");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userData;
    }

    private VBox createUserInfoBox(UserData userData) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20px; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 5px;");

        Label title = new Label("YOUR PERSONAL INFORMATION");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_BLUE + ";");

        Label ageLabel = new Label("Age: " + userData.age + " years");
        Label genderLabel = new Label("Gender: " + userData.gender);
        Label weightLabel = new Label("Weight: " + userData.weight + " kg");
        Label heightLabel = new Label("Height: " + userData.height + " cm");

        // Style the labels
        String labelStyle = "-fx-font-size: 14px; -fx-text-fill: #495057;";
        ageLabel.setStyle(labelStyle);
        genderLabel.setStyle(labelStyle);
        weightLabel.setStyle(labelStyle);
        heightLabel.setStyle(labelStyle);

        box.getChildren().addAll(title, ageLabel, genderLabel, weightLabel, heightLabel);
        return box;
    }

    private double calculateBMI(double weight, double height) {
        // Convert height from cm to meters
        double heightInMeters = height / 100;
        return weight / (heightInMeters * heightInMeters);
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi < 25) {
            return "Normal";
        } else {
            return "Overweight";
        }
    }

    private VBox createBMIBox(double bmi, String category) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20px; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 5px;");

        Label title = new Label("YOUR BMI ANALYSIS");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_BLUE + ";");

        Label bmiLabel = new Label(String.format("BMI: %.1f", bmi));
        bmiLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label categoryLabel = new Label("Category: " + category);
        categoryLabel.setStyle("-fx-font-size: 14px;");

        // Set color based on category
        if (category.equals("Underweight")) {
            bmiLabel.setStyle("-fx-text-fill: #e67e22;");
        } else if (category.equals("Normal")) {
            bmiLabel.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            bmiLabel.setStyle("-fx-text-fill: #e74c3c;");
        }

        box.getChildren().addAll(title, bmiLabel, categoryLabel);
        return box;
    }

    private VBox createDietPlanBox(String bmiCategory) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20px; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 5px;");

        Label title = new Label("RECOMMENDED DAILY ROUTINE FOR " + bmiCategory.toUpperCase() + " BMI");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_BLUE + ";");

        // Create grid for the routine
        GridPane routineGrid = new GridPane();
        routineGrid.setHgap(10);
        routineGrid.setVgap(10);
        routineGrid.setPadding(new Insets(10));

        // Add column headers
        Label timeHeader = new Label("Time");
        timeHeader.setStyle("-fx-font-weight: bold;");
        Label activityHeader = new Label("Activity");
        activityHeader.setStyle("-fx-font-weight: bold;");
        Label detailsHeader = new Label("Details");
        detailsHeader.setStyle("-fx-font-weight: bold;");

        routineGrid.addRow(0, timeHeader, activityHeader, detailsHeader);

        // Add rows based on BMI category
        String[][] routineData = getRoutineData(bmiCategory);
        for (int i = 0; i < routineData.length; i++) {
            Label timeLabel = new Label(routineData[i][0]);
            Label activityLabel = new Label(routineData[i][1]);
            Label detailsLabel = new Label(routineData[i][2]);

            // Make details label wrap text
            detailsLabel.setWrapText(true);
            detailsLabel.setMaxWidth(400);

            routineGrid.addRow(i+1, timeLabel, activityLabel, detailsLabel);
        }

        box.getChildren().addAll(title, routineGrid);
        return box;
    }

    private String[][] getRoutineData(String bmiCategory) {
        if (bmiCategory.equals("Underweight")) {
            return new String[][] {
                    {"6:00 AM", "Wake Up & Hydrate", "Drink 1-2 glasses of water."},
                    {"6:30 AM", "Morning Exercise", "Light stretching or walking (10-15 minutes) to kickstart metabolism."},
                    {"7:00 AM", "Breakfast", "Whole wheat toast with scrambled eggs, low-fat milk, and a small serving of fruit (e.g., banana)."},
                    {"8:00 AM", "Hydrate & Rest", "Drink water or herbal tea."},
                    {"10:00 AM", "Morning Snack", "A small handful of nuts (e.g., almonds or walnuts) and a small apple."},
                    {"12:00 PM", "Lunch", "Grilled chicken breast, quinoa, steamed vegetables, and a side of avocado for healthy fats."},
                    {"1:30 PM", "Post-Lunch Hydration", "Drink water or a low-calorie drink (avoid sugary beverages)."},
                    {"2:30 PM", "Afternoon Exercise", "Light cardio or walking for 20 minutes."},
                    {"3:00 PM", "Afternoon Snack", "Greek yogurt with chia seeds and honey."},
                    {"5:00 PM", "Evening Exercise", "Light stretching or yoga (15 minutes)."},
                    {"6:00 PM", "Dinner", "Fish (e.g., salmon), brown rice, and a vegetable salad with olive oil."},
                    {"7:30 PM", "Hydrate & Rest", "Drink water or herbal tea."},
                    {"9:00 PM", "Night Snack", "A small bowl of cottage cheese with flaxseeds."},
                    {"10:00 PM", "Prepare for Sleep", "Relaxation techniques, such as deep breathing or meditation."},
                    {"10:30 PM", "Sleep", "Aim for 7-8 hours of sleep."}
            };
        } else if (bmiCategory.equals("Normal")) {
            return new String[][] {
                    {"6:00 AM", "Wake Up & Hydrate", "Drink 1-2 glasses of water."},
                    {"6:30 AM", "Morning Exercise", "20-30 minutes of moderate-intensity cardio (e.g., walking, cycling)."},
                    {"7:00 AM", "Breakfast", "Oatmeal with chia seeds, low-fat yogurt, and mixed berries."},
                    {"8:00 AM", "Hydrate & Rest", "Drink water or herbal tea."},
                    {"10:00 AM", "Morning Snack", "A small portion of mixed nuts or a boiled egg."},
                    {"12:00 PM", "Lunch", "Grilled turkey sandwich with whole wheat bread, spinach, and a side salad."},
                    {"1:30 PM", "Post-Lunch Hydration", "Drink water or a low-calorie drink (avoid sugary beverages)."},
                    {"2:30 PM", "Afternoon Exercise", "Moderate-intensity walking or cycling for 20-30 minutes."},
                    {"3:00 PM", "Afternoon Snack", "An apple with almond butter or a small handful of trail mix."},
                    {"5:00 PM", "Evening Exercise", "Light stretching or yoga for 15-20 minutes."},
                    {"6:00 PM", "Dinner", "Baked chicken breast with steamed broccoli and quinoa."},
                    {"7:30 PM", "Hydrate & Rest", "Drink water or herbal tea."},
                    {"9:00 PM", "Night Snack", "A small serving of cottage cheese or a protein smoothie."},
                    {"10:00 PM", "Prepare for Sleep", "Relaxation techniques, such as deep breathing or meditation."},
                    {"10:30 PM", "Sleep", "Aim for 7-8 hours of sleep."}
            };
        } else { // Overweight
            return new String[][] {
                    {"6:00 AM", "Wake Up & Hydrate", "Drink 1-2 glasses of water."},
                    {"6:30 AM", "Morning Exercise", "30-40 minutes of moderate-intensity exercise (e.g., brisk walking or cycling)."},
                    {"7:00 AM", "Breakfast", "Scrambled eggs with spinach, avocado, and a side of mixed berries."},
                    {"8:00 AM", "Hydrate & Rest", "Drink water or herbal tea."},
                    {"10:00 AM", "Morning Snack", "A small handful of almonds or a boiled egg."},
                    {"12:00 PM", "Lunch", "Grilled fish (e.g., tilapia or salmon) with a large vegetable salad (greens, tomatoes, cucumbers) and olive oil."},
                    {"1:30 PM", "Post-Lunch Hydration", "Drink water or a low-calorie drink (avoid sugary beverages)."},
                    {"2:30 PM", "Afternoon Exercise", "Moderate walking or swimming for 30 minutes."},
                    {"3:00 PM", "Afternoon Snack", "Carrot sticks with hummus or a small apple."},
                    {"5:00 PM", "Evening Exercise", "Light stretching or yoga for 15-20 minutes."},
                    {"6:00 PM", "Dinner", "Baked chicken with roasted vegetables (e.g., zucchini, bell peppers, and cauliflower)."},
                    {"7:30 PM", "Hydrate & Rest", "Drink water or herbal tea."},
                    {"9:00 PM", "Night Snack", "A small serving of low-fat Greek yogurt or a protein shake."},
                    {"10:00 PM", "Prepare for Sleep", "Relaxation techniques, such as deep breathing or meditation."},
                    {"10:30 PM", "Sleep", "Aim for 7-8 hours of sleep."}
            };
        }
    }

    private BarChart<String, Number> createActivityChart(String bmiCategory) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Activity Duration (minutes)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Daily Activity Timeline (" + bmiCategory + " BMI)");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Duration");

        // Assign durations to all activities
        String[][] routineData = getRoutineData(bmiCategory);
        for (String[] activity : routineData) {
            String time = activity[0];
            String activityName = activity[1];
            int duration = 0;

            // Assign durations based on activity type
            if (activityName.contains("Exercise")) {
                if (activityName.contains("Morning")) {
                    duration = bmiCategory.equals("Underweight") ? 15 :
                            bmiCategory.equals("Normal") ? 30 : 40;
                } else if (activityName.contains("Afternoon")) {
                    duration = bmiCategory.equals("Underweight") ? 20 :
                            bmiCategory.equals("Normal") ? 30 : 30;
                } else if (activityName.contains("Evening")) {
                    duration = bmiCategory.equals("Underweight") ? 15 :
                            bmiCategory.equals("Normal") ? 20 : 20;
                }
            } else if (activityName.contains("Sleep")) {
                duration = 480; // 8 hours in minutes
            } else if (activityName.contains("Meal") || activityName.contains("Snack")) {
                duration = 30; // 30 minutes for meals/snacks
            } else {
                duration = 15; // Default duration for other activities
            }

            XYChart.Data<String, Number> data = new XYChart.Data<>(time + "\n" + activityName, duration);

            // Store activity type in the extra value of the data
            data.setExtraValue(activityName);
            series.getData().add(data);
        }

        barChart.getData().add(series);

        // Style the bars after the chart is rendered
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
            });
            data.getNode().setOnMouseExited(e -> {
                // Apply color based on activity type when mouse exits
                String activity = (String) data.getExtraValue();
                if (activity.contains("Exercise")) {
                    data.getNode().setStyle("-fx-bar-fill: #e74c3c;");
                } else if (activity.contains("Meal") || activity.contains("Snack")) {
                    data.getNode().setStyle("-fx-bar-fill: #2ecc71;");
                } else if (activity.contains("Sleep")) {
                    data.getNode().setStyle("-fx-bar-fill: #3498db;");
                } else {
                    data.getNode().setStyle("-fx-bar-fill: #f39c12;");
                }
            });
        }

        // Apply initial styling after chart is shown
        barChart.widthProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    String activity = (String) data.getExtraValue();
                    if (activity.contains("Exercise")) {
                        data.getNode().setStyle("-fx-bar-fill: #e74c3c;");
                    } else if (activity.contains("Meal") || activity.contains("Snack")) {
                        data.getNode().setStyle("-fx-bar-fill: #2ecc71;");
                    } else if (activity.contains("Sleep")) {
                        data.getNode().setStyle("-fx-bar-fill: #3498db;");
                    } else {
                        data.getNode().setStyle("-fx-bar-fill: #f39c12;");
                    }
                }
            });
        });

        // Adjust chart appearance
        barChart.setCategoryGap(5);
        barChart.setBarGap(2);
        barChart.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1px;");

        return barChart;
    }
    // Helper class to hold user data
    private static class UserData {
        int age;
        String gender;
        double weight;
        double height;
    }
}