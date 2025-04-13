package com.example.oodproject;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class MealDietManagement extends Application {
    private String userName;
    private int userId;
    public MealDietManagement() {
// ডিফল্ট কনস্ট্রাক্টর যোগ করা হয়েছে
    }
    public MealDietManagement(String userName, int userId) {
        this.userName = userName;
        this.userId = userId;
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Daily Diet Recommendations");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F4F4;");
        HBox topBar = createTopBar();
        root.setTop(topBar);
// Create Left Box for Daily Needs
        VBox leftBox = createDailyNeedsBox(root);
        root.setLeft(leftBox);
// Create Right Box for Daily Diet Recommendations
        VBox rightBox = createDietRecommendationsBox(root);
        root.setRight(rightBox);
// Create Center Box for Pie Chart
        VBox centerBox = createCenterBox(root);
        root.setCenter(centerBox);
// Create Bottom Box for Bar Chart
        VBox bottomBox = createBarChartBox(root);
        root.setBottom(bottomBox);
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private HBox createTopBar() {
        Text welcomeText = new Text("Welcome, " + userName);
        welcomeText.setStyle("-fx-fill: white; -fx-font-size: 30px; -fx-font-family: 'Britannic Bold';");
        HBox topBar = new HBox(welcomeText);
        topBar.setStyle("-fx-background-color: #34495E; -fx-padding: 20px;");
        topBar.setAlignment(Pos.CENTER);
        return topBar;
    }
    private VBox createDailyNeedsBox(BorderPane root) {
        VBox leftBox = new VBox(20);
        leftBox.setPadding(new Insets(20, 20, 20, 40)); // বাম দিকে 40 প্যাডিং যোগ করা হয়েছে
        leftBox.setAlignment(Pos.TOP_LEFT);
        Label title = new Label("Daily Needs");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        leftBox.getChildren().add(title);
        try {
            Connection connection = DatabaseHelper.getConnection();
            String sql = "SELECT u.gender, u.weight, u.height, a.blood_pressure, a.blood_sugar_level, b.before_meal, b.after_meal " +
                    "FROM users u " +
                    "LEFT JOIN additional_info a ON u.id = a.user_id " +
                    "LEFT JOIN blood_sugar_entries b ON u.id = b.user_id " +
                    "WHERE u.id = ? " +
                    "ORDER BY a.updated_at DESC, b.entry_time DESC " +
                    "LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String gender = resultSet.getString("gender");
                double weight = resultSet.getDouble("weight");
                int bloodSugarLevel = resultSet.getInt("blood_sugar_level");
                VBox needs = getDailyNeeds(weight, gender, bloodSugarLevel);
                leftBox.getChildren().add(needs);
            } else {
                Label label = new Label("No data found for this user.");
                leftBox.getChildren().add(label);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Label label = new Label("Error fetching data: " + e.getMessage());
            leftBox.getChildren().add(label);
        }
        return leftBox;
    }
    public VBox getDailyNeeds(double weight, String gender, int bloodSugarLevel) {
        VBox needs = new VBox(10);
        needs.setAlignment(Pos.TOP_LEFT);
        double protein = weight * 1.2;
        double water = weight * 0.03;
        double fiber = 25;
        double carbohydrates = 300;
        double fats = 60;
        double vitamins = 100;
        double minerals = 50;
        if (gender.equalsIgnoreCase("female")) {
            carbohydrates -= 50;
            fats -= 10;
        }
        if (bloodSugarLevel > 180) {
            carbohydrates -= 100;
        }
        Label proteinLabel = new Label("Protein: " + String.format("%.2f", protein) + " grams");
        Label waterLabel = new Label("Water: " + String.format("%.2f", water) + " liters");
        Label fiberLabel = new Label("Fiber: " + fiber + " grams");
        Label carbohydratesLabel = new Label("Carbohydrates: " + carbohydrates + " grams");
        Label fatsLabel = new Label("Fats: " + fats + " grams");
        Label vitaminsLabel = new Label("Vitamins: " + vitamins + " mg");
        Label mineralsLabel = new Label("Minerals: " + minerals + " mg");
        needs.getChildren().addAll(
                proteinLabel,
                waterLabel,
                fiberLabel,
                carbohydratesLabel,
                fatsLabel,
                vitaminsLabel,
                mineralsLabel
        );
// ডেটা চেকিং
        System.out.println("getDailyNeeds() called with weight: " + weight + ", gender: " + gender + ", bloodSugarLevel: " + bloodSugarLevel);
        System.out.println("Water label text: " + waterLabel.getText());
        return needs;
    }
    private VBox createDietRecommendationsBox(BorderPane root) {
        VBox rightBox = new VBox(20);
        rightBox.setPadding(new Insets(20, 20, 20, 40)); // বাম দিকে 40 প্যাডিং যোগ করা হয়েছে
        rightBox.setAlignment(Pos.TOP_LEFT);
        Label title = new Label("Daily Diet Recommendations");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        rightBox.getChildren().add(title);
        try {
            Connection connection = DatabaseHelper.getConnection();
            String sql = "SELECT u.gender, u.weight, u.height, a.blood_pressure, a.blood_sugar_level, b.before_meal, b.after_meal " +
                    "FROM users u " +
                    "LEFT JOIN additional_info a ON u.id = a.user_id " +
                    "LEFT JOIN blood_sugar_entries b ON u.id = b.user_id " +
                    "WHERE u.id = ? " +
                    "ORDER BY a.updated_at DESC, b.entry_time DESC " +
                    "LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String gender = resultSet.getString("gender");
                double weight = resultSet.getDouble("weight");
                double height = resultSet.getDouble("height");
                String bloodPressure = resultSet.getString("blood_pressure");
                int bloodSugarLevel = resultSet.getInt("blood_sugar_level");
                int beforeMealSugar = resultSet.getInt("before_meal");
                int afterMealSugar = resultSet.getInt("after_meal");
                String recommendations = calculateDietRecommendations(gender, weight, height, bloodPressure, bloodSugarLevel, beforeMealSugar, afterMealSugar);
                Label label = new Label(recommendations);
                rightBox.getChildren().add(label);
            } else {
                Label label = new Label("No data found for this user.");
                rightBox.getChildren().add(label);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Label label = new Label("Error fetching data: " + e.getMessage());
            rightBox.getChildren().add(label);
        }
        return rightBox;
    }
    public String calculateDietRecommendations(String gender, double weight, double height, String bloodPressure, int bloodSugarLevel, int beforeMealSugar, int afterMealSugar) {
        StringBuilder recommendations = new StringBuilder("Daily Diet Recommendations:\n");
        recommendations.append("Gender: ").append(gender).append("\n");
        recommendations.append("Weight: ").append(weight).append(" kg\n");
        recommendations.append("Height: ").append(height).append(" cm\n");
        recommendations.append("Blood Pressure: ").append(bloodPressure).append("\n");
        recommendations.append("Blood Sugar Level: ").append(bloodSugarLevel).append(" mg/dL\n");
        recommendations.append("Before Meal Sugar: ").append(beforeMealSugar).append(" mg/dL\n");
        recommendations.append("After Meal Sugar: ").append(afterMealSugar).append(" mg/dL\n\n");
        double bmi = weight / ((height / 100.0) * (height / 100.0));
        recommendations.append("BMI: ").append(String.format("%.2f", bmi)).append("\n");
        if (bmi < 18.5) {
            recommendations.append("Recommendation: Increase calorie intake for healthy weight gain.\n");

        } else if (bmi >= 25) {
            recommendations.append("Recommendation: Reduce calorie intake and increase physical activity for weight loss.\n");
        } else {
            recommendations.append("Recommendation: Maintain a balanced diet and regular exercise.\n");
        }
        if (bloodPressure != null) {
            String[] bpValues = bloodPressure.split("/");
            if (bpValues.length == 2) {
                int systolic = Integer.parseInt(bpValues[0]);
                int diastolic = Integer.parseInt(bpValues[1]);
                if (systolic >= 140 || diastolic >= 90) {
                    recommendations.append("Recommendation: Consult a doctor for blood pressure management. Reduce salt intake and increase potassium intake.\n");
                }
            }
        }
        if (bloodSugarLevel > 180) {
            recommendations.append("Recommendation: Reduce sugar intake and increase fiber intake. Monitor blood sugar levels regularly.\n");
        } else if (bloodSugarLevel > 140) {
            recommendations.append("Recommendation: Moderate sugar intake and regular exercise.\n");
        } else {
            recommendations.append("Recommendation: Maintain a balanced diet with regular meals.\n");
        }
        if (afterMealSugar > 180) {
            recommendations.append("Recommendation: Avoid high-carbohydrate meals and increase physical activity after meals.\n");
        }
        return recommendations.toString();
    }
    public VBox createCenterBox(BorderPane root) {
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.CENTER);
        HBox pieChartsBox = createPieChartsBox(root);
        centerBox.getChildren().add(pieChartsBox);
        return centerBox;
    }
    private HBox createPieChartsBox(BorderPane root) {
        HBox pieChartsBox = new HBox(20);
        pieChartsBox.setAlignment(Pos.CENTER);
        Label title = new Label("");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        pieChartsBox.getChildren().add(title);
        try {
            Connection connection = DatabaseHelper.getConnection();
            String sql = "SELECT u.gender, u.weight, u.height, a.blood_pressure, a.blood_sugar_level, b.before_meal, b.after_meal " +
                    "FROM users u " +
                    "LEFT JOIN additional_info a ON u.id = a.user_id " +
                    "LEFT JOIN blood_sugar_entries b ON u.id = b.user_id " +
                    "WHERE u.id = ? " +
                    "ORDER BY a.updated_at DESC, b.entry_time DESC " +
                    "LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String gender = resultSet.getString("gender");
                double weight = resultSet.getDouble("weight");
                int bloodSugarLevel = resultSet.getInt("blood_sugar_level");
                ObservableList<PieChart.Data> pieChartData = getPieChartData(weight, gender, bloodSugarLevel);
                PieChart pieChart = new PieChart(pieChartData);
                pieChart.setTitle(""); // Remove default title
                pieChartsBox.getChildren().add(pieChart);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pieChartsBox;
    }
    private ObservableList<PieChart.Data> getPieChartData(double weight, String gender, int bloodSugarLevel) {
        double protein = weight * 1.2;
        double water = weight * 0.03;
        double fiber = 25;
        double carbohydrates = 300;
        double fats = 60;
        double vitamins = 100;
        double minerals = 50;
        if (gender.equalsIgnoreCase("female")) {
            carbohydrates -= 50;
            fats -= 10;
        }
        if (bloodSugarLevel > 180) {
            carbohydrates -= 100;
        }
        return FXCollections.observableArrayList(
                new PieChart.Data("Protein", protein),
                new PieChart.Data("Water", water),
                new PieChart.Data("Fiber", fiber),
                new PieChart.Data("Carbohydrates", carbohydrates),
                new PieChart.Data("Fats", fats),
                new PieChart.Data("Vitamins", vitamins),
                new PieChart.Data("Minerals", minerals)
        );
    }
    public VBox createBarChartBox(BorderPane root) {
        VBox bottomBox = new VBox(20);
        bottomBox.setPadding(new Insets(20));
        bottomBox.setAlignment(Pos.CENTER);
        BarChart<String, Number> barChart = createBarChart(root);
        bottomBox.getChildren().add(barChart);
        return bottomBox;
    }
    private BarChart<String, Number> createBarChart(BorderPane root) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Daily Needs Comparison");
        barChart.setPrefHeight(300); // Adjust height as needed
        barChart.setMaxWidth(Double.MAX_VALUE); // Make it full width
        try {
            Connection connection = DatabaseHelper.getConnection();
            String sql = "SELECT u.gender, u.weight, u.height, a.blood_pressure, a.blood_sugar_level, b.before_meal, b.after_meal " +
                    "FROM users u " +
                    "LEFT JOIN additional_info a ON u.id = a.user_id " +
                    "LEFT JOIN blood_sugar_entries b ON u.id = b.user_id " +
                    "WHERE u.id = ? " +
                    "ORDER BY a.updated_at DESC, b.entry_time DESC " +
                    "LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String gender = resultSet.getString("gender");
                double weight = resultSet.getDouble("weight");
                int bloodSugarLevel = resultSet.getInt("blood_sugar_level");
                XYChart.Series<String, Number> series = getBarChartSeries(weight, gender, bloodSugarLevel);
                barChart.getData().add(series);
// প্রতিটি ডেটা পয়েন্টের জন্য আলাদা রং যোগ করা হয়েছে
                String[] colors = {"#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2"};
                int colorIndex = 0;
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("-fx-bar-fill: " + colors[colorIndex % colors.length] + ";");
                    colorIndex++;
                }
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return barChart;
    }
    private XYChart.Series<String, Number> getBarChartSeries(double weight, String gender, int bloodSugarLevel) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        double protein = weight * 1.2;
        double water = weight * 0.03;
        double fiber = 25;
        double carbohydrates = 300;
        double fats = 60;
        double vitamins = 100;
        double minerals = 50;
        if (gender.equalsIgnoreCase("female")) {
            carbohydrates -= 50;
            fats -= 10;
        }
        if (bloodSugarLevel > 180) {
            carbohydrates -= 100;
        }
        series.getData().add(new XYChart.Data<>("Protein", protein));
        series.getData().add(new XYChart.Data<>("Water", water));
        series.getData().add(new XYChart.Data<>("Fiber", fiber));
        series.getData().add(new XYChart.Data<>("Carbohydrates", carbohydrates));
        series.getData().add(new XYChart.Data<>("Fats", fats));
        series.getData().add(new XYChart.Data<>("Vitamins", vitamins));
        series.getData().add(new XYChart.Data<>("Minerals", minerals));
        return series;
    }
}