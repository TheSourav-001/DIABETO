package com.example.oodproject;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import java.sql.*;
import java.io.InputStream;

public class Dashboard extends Application {

    // Color constants
    private static final String RED = "#e74c3c";
    private static final String GREEN = "#2ecc71";
    private static final String WHITE = "#ffffff";
    private static final String TEAL = "#1ABC9C";
    private static final String DARK_BLUE = "#34495E";
    private static final String LIGHT_GRAY = "#f8f9fa";
    private static final String CARD_SHADOW = "rgba(0,0,0,0.1)";
    private static final String TEXT_COLOR = "#2c3e50";
    private static final String CARD_BACKGROUND = "#ffffff";
    private static final String PRIMARY_COLOR = "#3498db";

    private VBox sidebar;
    private BorderPane root;
    private boolean isSidebarVisible = false;
    private String userName;
    private int userId;
    private ScrollPane scrollPane;

    public Dashboard() {
        this.userName = "Guest";
        this.userId = 0;
    }

    public Dashboard(String userName, int userId) {
        this.userName = userName;
        this.userId = userId;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Diabetes Management System - Dashboard");

        root = new BorderPane();
        root.setStyle("-fx-background-color: " + LIGHT_GRAY + ";");

        // Create scroll pane
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: " + LIGHT_GRAY + "; -fx-border-color: " + LIGHT_GRAY + ";");

        sidebar = new VBox(10);
        sidebar.setStyle("-fx-background-color: " + DARK_BLUE + ";");
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setMinWidth(250);
        sidebar.setTranslateX(-250);
        sidebar.setPadding(new Insets(20, 10, 20, 10));

        // Sidebar buttons
        Button updateInfoButton = createSidebarButton("Update Additional Info");
        updateInfoButton.setOnAction(e -> new AdditionalInfoForm().start(new Stage(), userId));

        Button sugarTracking = createSidebarButton("Blood Sugar Tracking");
        sugarTracking.setOnAction(e -> showBloodSugarTrackingPopup(primaryStage));

        Button dietManagement = createSidebarButton("Meal & Diet Management");
        dietManagement.setOnAction(e -> {
            MealDietManagement mealDietManagement = new MealDietManagement(userName, userId);
            mealDietManagement.start(new Stage());
        });

        Button reportGen = createSidebarButton("Report Generation");
        reportGen.setOnAction(e -> {
            ReportGeneration reportGeneration = new ReportGeneration(userId);
            Stage stage = new Stage();
            reportGeneration.start(stage);
        });

        Button bmiCalc = createSidebarButton("Your Daily Routine");
        bmiCalc.setOnAction(e -> {
            DailyRoutine dailyRoutine = new DailyRoutine(userName, userId);
            Stage stage = new Stage();
            dailyRoutine.start(stage);
        });

        Button settings = createSidebarButton("Settings");
        settings.setOnAction(e -> {
            UserSettings userSettings = new UserSettings(userId, userName);
            Stage stage = new Stage();
            userSettings.start(stage);
        });

        Button logout = createSidebarButton("Log Out");
        logout.setOnAction(e -> {
            new Login().start(new Stage());
            primaryStage.close();
        });

        sidebar.getChildren().addAll(updateInfoButton, sugarTracking, dietManagement, reportGen, bmiCalc, settings, logout);

        // Top bar (unchanged as requested)
        HBox topBar = createTopBar(primaryStage);
        root.setTop(topBar);

        // Main content container
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: " + LIGHT_GRAY + ";");

        // Alert cards section
        HBox alertCards = new HBox(20);
        alertCards.setAlignment(Pos.CENTER);

        VBox warningCard = createModernCard("WARNING", createWarningBox(userId), RED);
        VBox recommendationCard = createModernCard("RECOMMENDATION", createRecommendationBox(userId), GREEN);

        HBox.setHgrow(warningCard, Priority.ALWAYS);
        HBox.setHgrow(recommendationCard, Priority.ALWAYS);
        warningCard.setMaxWidth(Double.MAX_VALUE);
        recommendationCard.setMaxWidth(Double.MAX_VALUE);

        alertCards.getChildren().addAll(warningCard, recommendationCard);

        // Health metrics section
        VBox metricsSection = new VBox(15);
        metricsSection.setPadding(new Insets(20));
        metricsSection.setStyle("-fx-background-color: " + CARD_BACKGROUND + "; " +
                "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, " + CARD_SHADOW + ", 10, 0, 0, 2);");

        Label metricsTitle = new Label("Health Metrics");
        metricsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_COLOR + ";");

        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(20);
        metricsGrid.setVgap(20);
        metricsGrid.setAlignment(Pos.CENTER);

        // Blood Pressure Card
        VBox bpCard = createMetricCard("Blood Pressure", createBloodPressureChart(userId), getBloodPressure(userId), "#3498db");
        // Insulin Card
        VBox insulinCard = createMetricCard("Insulin Usage", createInsulinChart(userId),
                String.format("%.1f%%", getAverageInsulinPerDayValue(userId)), "#e74c3c");
        // Blood Sugar Card
        VBox bsCard = createMetricCard("Blood Sugar", createBloodSugarChart(userId),
                getBloodSugarLevel(userId) + " mg/dL", "#9b59b6");
        // Water Needs Card
        VBox waterCard = createMetricCard("Water Needs", createWaterChart(userId),
                String.format("%.1f L", calculateWaterNeedsFromMealDiet(userId)), "#3498db");

        metricsGrid.add(bpCard, 0, 0);
        metricsGrid.add(insulinCard, 1, 0);
        metricsGrid.add(bsCard, 0, 1);
        metricsGrid.add(waterCard, 1, 1);

        metricsSection.getChildren().addAll(metricsTitle, metricsGrid);

        // Trend chart section
        VBox trendSection = new VBox(15);
        trendSection.setPadding(new Insets(20));
        trendSection.setStyle("-fx-background-color: " + CARD_BACKGROUND + "; " +
                "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, " + CARD_SHADOW + ", 10, 0, 0, 2);");

        Label trendTitle = new Label("Blood Sugar Trends");
        trendTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_COLOR + ";");

        LineChart<Number, Number> sugarLevelChart = createTrendChart(userId);
        sugarLevelChart.setStyle("-fx-background-color: transparent;");
        sugarLevelChart.setPrefHeight(300);

        trendSection.getChildren().addAll(trendTitle, sugarLevelChart);

        // Add all sections to main content
        mainContent.getChildren().addAll(alertCards, metricsSection, trendSection);

        // Set up scrollable content
        scrollPane.setContent(mainContent);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createMetricCard(String title, Node chart, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + CARD_BACKGROUND + "; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, " + CARD_SHADOW + ", 5, 0, 0, 2);");
        card.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_COLOR + ";");

        // Value display
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(titleLabel, chart, valueLabel);
        return card;
    }

    private VBox createModernCard(String title, VBox content, String color) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: " + CARD_BACKGROUND + "; " +
                "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, " + CARD_SHADOW + ", 10, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        // Header
        StackPane header = new StackPane();
        header.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10 10 0 0;");
        header.setPrefHeight(40);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        StackPane.setAlignment(titleLabel, Pos.CENTER);

        header.getChildren().add(titleLabel);

        // Content
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-radius: 0 0 10 10;");

        card.getChildren().addAll(header, content);
        return card;
    }

    private Button createSidebarButton(String buttonText) {
        Button button = new Button();

        // Set different icons based on button text using Unicode characters
        String icon = "";
        switch(buttonText) {
            case "Update Additional Info":
                icon = "\uD83D\uDCDD"; // Pencil icon
                break;
            case "Blood Sugar Tracking":
                icon = "\u2665"; // Drop of blood icon
                break;
            case "Meal & Diet Management":
                icon = "\uD83C\uDF4E"; // Apple icon
                break;
            case "Report Generation":
                icon = "\uD83D\uDCCA"; // Bar chart icon
                break;
            case "Your Daily Routine":
                icon = "\uD83C\uDFCB"; // Weight lifter icon
                break;
            case "Settings":
                icon = "\u2699";       // Gear icon
                break;
            case "Log Out":
                icon = "\uD83D\uDEAA"; // Door icon
                break;
            default:
                icon = "\u25CF";       // Circle bullet
        }

        button.setText(icon + "  " + buttonText);
        button.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 15px; " +
                "-fx-alignment: center-left; " +
                "-fx-border-radius: 20px; " +
                "-fx-background-radius: 20px; " +
                "-fx-cursor: hand;");
        button.setMaxWidth(Double.MAX_VALUE);

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 15px; " +
                "-fx-alignment: center-left; " +
                "-fx-border-radius: 20px; " +
                "-fx-background-radius: 20px; " +
                "-fx-cursor: hand;"));

        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 15px; " +
                "-fx-alignment: center-left; " +
                "-fx-border-radius: 20px; " +
                "-fx-background-radius: 20px; " +
                "-fx-cursor: hand;"));

        return button;
    }
    private void toggleSidebar(Stage primaryStage) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidebar);
        if (isSidebarVisible) {
            transition.setToX(-250);
            root.setLeft(null);
        } else {
            transition.setToX(0);
            root.setLeft(sidebar);
        }
        isSidebarVisible = !isSidebarVisible;
        transition.play();
    }

    private HBox createTopBar(Stage primaryStage) {
        Text welcomeText = new Text("Welcome, " + userName);
        welcomeText.setStyle("-fx-fill: white; -fx-font-size: 30px; -fx-font-family: 'Britannic Bold';");

        Text tagline = new Text("Stay Healthy, Stay Strong!");
        tagline.setStyle("-fx-fill: white; -fx-font-size: 20px; -fx-font-family: 'Segoe UI';");

        VBox welcomeBox = new VBox(welcomeText, tagline);
        welcomeBox.setAlignment(Pos.CENTER);

        Button menuButton = new Button("☰");
        menuButton.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-text-fill: white;");
        menuButton.setOnAction(e -> toggleSidebar(primaryStage));

        ImageView logoImage = null;
        try {
            InputStream imageStream = getClass().getResourceAsStream("/icons/ac.png");
            if (imageStream != null) {
                Image image = new Image(imageStream);
                logoImage = new ImageView(image);
                logoImage.setFitWidth(200);
                logoImage.setFitHeight(100);
            } else {
                System.err.println("Image not found at /icons/ac.png");
                // Fallback to text if image not found
                logoImage = new ImageView();
                Label fallbackText = new Label("Diabetes Manager");
                fallbackText.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
                HBox fallbackBox = new HBox(fallbackText);
                fallbackBox.setAlignment(Pos.CENTER);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }

        HBox leftSide = new HBox(10, menuButton);
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

    private VBox createWarningBox(int userId) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        String warningMessage = "No Warning";
        String color = GREEN;
        String iconText = "✓";

        if (userId != 0) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT blood_sugar_level FROM additional_info WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int sugarLevel = rs.getInt("blood_sugar_level");

                    if (sugarLevel > 180) {
                        warningMessage = "High Sugar Level!\nImmediate Attention Needed";
                        color = RED;
                        iconText = "⚠";
                    } else if (sugarLevel > 140) {
                        warningMessage = "Elevated Sugar Level\nMonitor Closely";
                        color = "#e67e22";
                        iconText = "⚠";
                    } else if (sugarLevel < 70) {
                        warningMessage = "Low Sugar Level\nConsume Fast-Acting Carbs";
                        color = "#f1c40f";
                        iconText = "⚠";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: " + color + ";");

        Text warningText = new Text(warningMessage);
        warningText.setStyle("-fx-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-alignment: center;");
        warningText.setWrappingWidth(200);

        box.getChildren().addAll(iconLabel, warningText);
        return box;
    }

    private VBox createRecommendationBox(int userId) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        String recommendation = "No current recommendations";
        String iconText = "i";

        if (userId != 0) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT blood_pressure, blood_sugar_level FROM additional_info WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int sugarLevel = rs.getInt("blood_sugar_level");
                    String bp = rs.getString("blood_pressure");

                    if (sugarLevel > 180 || (bp != null && bp.equals("140/90"))) {
                        recommendation = "Consult your doctor immediately\nand adjust medication";
                        iconText = "🩺";
                    } else if (sugarLevel > 140) {
                        recommendation = "Reduce sugar intake\nand increase physical activity";
                        iconText = "🏃";
                    } else if (sugarLevel < 70) {
                        recommendation = "Consume 15g fast-acting carbs\nand recheck in 15 minutes";
                        iconText = "🧃";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: " + DARK_BLUE + ";");

        Text recText = new Text(recommendation);
        recText.setStyle("-fx-fill: " + DARK_BLUE + "; -fx-font-size: 14px; -fx-text-alignment: center;");
        recText.setWrappingWidth(200);

        box.getChildren().addAll(iconLabel, recText);
        return box;
    }

    private PieChart createBloodPressureChart(int userId) {
        String bloodPressure = getBloodPressure(userId);
        double systolic = 0;
        double diastolic = 0;
        if (!bloodPressure.equals("0/0")) {
            String[] parts = bloodPressure.split("/");
            systolic = Double.parseDouble(parts[0]);
            diastolic = Double.parseDouble(parts[1]);
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Systolic", systolic),
                new PieChart.Data("Diastolic", diastolic)
        );

        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(150, 150);
        pieChart.setStyle("-fx-background-color: transparent;");

        // Set colors
        pieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #3498db;");
        pieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #2ecc71;");

        return pieChart;
    }

    private PieChart createInsulinChart(int userId) {
        double avgInsulinPerDay = getAverageInsulinPerDayValue(userId);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Used", avgInsulinPerDay),
                new PieChart.Data("Remaining", 100 - avgInsulinPerDay)
        );

        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(150, 150);
        pieChart.setStyle("-fx-background-color: transparent;");

        // Apply gradient for segments
        pieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #FF0000;");  // Light Green (Top)
        pieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #FFD700;");  // Blue (Bottom)

        return pieChart;
    }

    private BarChart<String, Number> createBloodSugarChart(int userId) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("");
        yAxis.setLabel("mg/dL");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(150);
        barChart.setAnimated(false);
        barChart.setCategoryGap(50);
        barChart.setStyle("-fx-background-color: transparent;");

        // Customize axis
        xAxis.setTickLabelFill(Color.web(TEXT_COLOR));
        yAxis.setTickLabelFill(Color.web(TEXT_COLOR));

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        if (userId != 0) {
            int bloodSugarLevel = getBloodSugarLevel(userId);
            String level;
            if (bloodSugarLevel < 70) {
                level = "Low";
            } else if (bloodSugarLevel >= 70 && bloodSugarLevel < 140) {
                level = "Normal";
            } else if (bloodSugarLevel >= 140 && bloodSugarLevel < 200) {
                level = "Pre-diabetic";
            } else {
                level = "Diabetic";
            }
            series.getData().add(new XYChart.Data<>(level, bloodSugarLevel));
            barChart.getData().add(series);

            Platform.runLater(() -> {
                if (series.getData().size() > 0) {
                    Node node = series.getData().get(0).getNode();
                    node.setStyle("-fx-bar-fill: #9b59b6;");
                }
            });
        }

        return barChart;
    }

    private BarChart<String, Number> createWaterChart(int userId) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("");
        yAxis.setLabel("Liters");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(150);
        barChart.setAnimated(false);
        barChart.setCategoryGap(50);
        barChart.setStyle("-fx-background-color: transparent;");

        // Customize axis
        xAxis.setTickLabelFill(Color.web(TEXT_COLOR));
        yAxis.setTickLabelFill(Color.web(TEXT_COLOR));

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        if (userId != 0) {
            double waterNeeds = calculateWaterNeedsFromMealDiet(userId);
            series.getData().add(new XYChart.Data<>("Needs", waterNeeds));
            barChart.getData().add(series);

            Platform.runLater(() -> {
                if (series.getData().size() > 0) {
                    Node node = series.getData().get(0).getNode();
                    node.setStyle("-fx-bar-fill: #3498db;");
                }
            });
        }

        return barChart;
    }

    private LineChart<Number, Number> createTrendChart(int userId) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (Days)");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Blood Sugar (mg/dL)");

        // Customize axis
        xAxis.setTickLabelFill(Color.web(TEXT_COLOR));
        yAxis.setTickLabelFill(Color.web(TEXT_COLOR));

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Blood Sugar Level");

        if (userId == 0) {
            for (int i = 1; i <= 7; i++) {
                series.getData().add(new XYChart.Data<>(i, 0));
            }
        } else {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT blood_sugar_level, updated_at FROM additional_info WHERE user_id = ? ORDER BY updated_at DESC LIMIT 7")) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                int i = 1;
                while (rs.next()) {
                    int bloodSugarLevel = rs.getInt("blood_sugar_level");
                    series.getData().add(new XYChart.Data<>(i, bloodSugarLevel));
                    i++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        chart.getData().add(series);

        // Customize line and symbols
        series.getNode().setStyle("-fx-stroke: #9b59b6; -fx-stroke-width: 2px;");

        for (XYChart.Data<Number, Number> data : series.getData()) {
            Node symbol = data.getNode();
            if (symbol != null) {
                int value = data.getYValue().intValue();
                if (value > 180) {
                    symbol.setStyle("-fx-background-color: #e74c3c, white;");
                } else if (value > 140) {
                    symbol.setStyle("-fx-background-color: #f39c12, white;");
                } else if (value < 70) {
                    symbol.setStyle("-fx-background-color: #f1c40f, white;");
                } else {
                    symbol.setStyle("-fx-background-color: #2ecc71, white;");
                }
            }
        }

        return chart;
    }

    private String getBloodPressure(int userId) {
        if (userId == 0) return "0/0";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT blood_pressure FROM additional_info WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("blood_pressure");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0/0";
    }

    private double getAverageInsulinPerDayValue(int userId) {
        if (userId == 0) return 0.0;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmtWeek = conn.prepareStatement(
                     "SELECT insulin_per_week FROM additional_info WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1");
             PreparedStatement stmtMonth = conn.prepareStatement(
                     "SELECT insulin_per_month FROM additional_info WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1")) {
            stmtWeek.setInt(1, userId);
            stmtMonth.setInt(1, userId);
            ResultSet rsWeek = stmtWeek.executeQuery();
            ResultSet rsMonth = stmtMonth.executeQuery();
            double avgInsulinPerDay = 0;
            if (rsWeek.next() && rsWeek.getString("insulin_per_week") != null) {
                double insulinPerWeek = rsWeek.getDouble("insulin_per_week");
                avgInsulinPerDay = (insulinPerWeek / 7.0 / 100.0) * 100.0;
            } else if (rsMonth.next() && rsMonth.getString("insulin_per_month") != null) {
                double insulinPerMonth = rsMonth.getDouble("insulin_per_month");
                avgInsulinPerDay = (insulinPerMonth / 30.0 / 100.0) * 100.0;
            }
            return avgInsulinPerDay;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private int getBloodSugarLevel(int userId) {
        if (userId == 0) return 0;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT blood_sugar_level FROM additional_info WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("blood_sugar_level");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double calculateWaterNeedsFromMealDiet(int userId) {
        if (userId == 0) {
            return 0.0;
        }
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT weight, gender, blood_sugar_level FROM users u " +
                             "LEFT JOIN additional_info a ON u.id = a.user_id " +
                             "WHERE u.id = ? " +
                             "ORDER BY a.updated_at DESC LIMIT 1")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double weight = rs.getDouble("weight");
                String gender = rs.getString("gender");
                int bloodSugarLevel = rs.getInt("blood_sugar_level");

                MealDietManagement mealDietManagement = new MealDietManagement(userName, userId);
                VBox dailyNeeds = mealDietManagement.getDailyNeeds(weight, gender, bloodSugarLevel);

                if (dailyNeeds == null) {
                    return 0.0;
                }

                for (javafx.scene.Node node : dailyNeeds.getChildren()) {
                    if (node instanceof Label) {
                        String text = ((Label) node).getText();
                        if (text.startsWith("Water: ")) {
                            return Double.parseDouble(text.substring(7, text.indexOf(" liters")).trim());
                        }
                    }
                }
                return 0.0;
            } else {
                return 0.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private void showBloodSugarTrackingPopup(Stage ownerStage) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(ownerStage);
        popupStage.setTitle("Blood Sugar Tracking");
        BloodSugarTracking bloodSugarTracking = new BloodSugarTracking(userId);
        bloodSugarTracking.start(popupStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}