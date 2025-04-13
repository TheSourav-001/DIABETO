package com.example.oodproject;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdditionalInfoForm extends Application {

    private int userId;

    public void start(Stage primaryStage, int userId) {
        this.userId = userId;
        start(primaryStage);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Update Additional Info");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(30));
        vbox.setStyle("-fx-background-color: linear-gradient(to bottom right, #f7fafc, #ebf8ff);");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        Font labelFont = Font.font("Segoe UI", FontWeight.BOLD, 14);
        Font inputFont = Font.font("Segoe UI", 14);

        // Blood Pressure Row
        ImageView bpIcon = createIcon("/Icon/blood-pressure.png");
        HBox bpHBox = createLabelBox(bpIcon, "Blood Pressure:");
        TextField bpField = createStyledTextField("e.g. 120/80");
        grid.add(bpHBox, 0, 0);
        grid.add(bpField, 1, 0);

        // Blood Sugar Row
        ImageView sugarIcon = createIcon("/Icon/high-blood-sugar.png");
        HBox sugarHBox = createLabelBox(sugarIcon, "Blood Sugar Level:");
        TextField sugarField = createStyledTextField("e.g. 100 mg/dL");
        grid.add(sugarHBox, 0, 1);
        grid.add(sugarField, 1, 1);

        // Insulin Week Row
        ImageView insulinWeekIcon = createIcon("/Icon/insulin.png");
        HBox insulinWeekHBox = createLabelBox(insulinWeekIcon, "Insulin Per Week:");
        TextField insulinWeekField = createStyledTextField("e.g. 15 units");
        grid.add(insulinWeekHBox, 0, 2);
        grid.add(insulinWeekField, 1, 2);

        // Insulin Month Row
        ImageView insulinMonthIcon = createIcon("/Icon/insulin.png");
        HBox insulinMonthHBox = createLabelBox(insulinMonthIcon, "Insulin Per Month:");
        TextField insulinMonthField = createStyledTextField("e.g. 60 units");
        grid.add(insulinMonthHBox, 0, 3);
        grid.add(insulinMonthField, 1, 3);

        // Recommendations Row
        ImageView recommendationIcon = createIcon("/Icon/suggestion.png");
        HBox recommendationHBox = createLabelBox(recommendationIcon, "Recommendations:");
        TextField recommendationField = createStyledTextField("e.g. Exercise daily");
        grid.add(recommendationHBox, 0, 4);
        grid.add(recommendationField, 1, 4);

        Button saveButton = new Button("Save Changes");
        styleSaveButton(saveButton);
        saveButton.setOnAction(e -> {
            saveAdditionalInfo(userId, bpField.getText(), sugarField.getText(),
                    insulinWeekField.getText(), insulinMonthField.getText(),
                    recommendationField.getText());
            primaryStage.close();
        });

        grid.add(saveButton, 1, 5);

        vbox.getChildren().add(grid);

        Scene scene = new Scene(vbox, 600, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ImageView createIcon(String path) {
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(24);
            iv.setFitHeight(24);
            return iv;
        } catch (Exception e) {
            System.out.println("Error loading icon: " + path);
            return new ImageView();
        }
    }

    private HBox createLabelBox(ImageView icon, String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#2d3748"));
        return new HBox(10, icon, label);
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setFont(Font.font("Segoe UI", 14));
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #ffffff; " +
                "-fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 6px; " +
                "-fx-padding: 8px 12px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 3, 0, 0, 1);");
        return tf;
    }

    private void styleSaveButton(Button btn) {
        btn.setStyle("-fx-background-color: #4299e1; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10px 25px; " +
                "-fx-background-radius: 6px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(66,153,225,0.3), 0, 4, 0, 0);");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3182ce;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #4299e1;"));
    }

    private void saveAdditionalInfo(int userId, String bp, String sugar, String insulinWeek,
                                    String insulinMonth, String recommendations) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO additional_info (user_id, blood_pressure, blood_sugar_level, " +
                             "insulin_per_week, insulin_per_month, recommendations, updated_at) " +
                             "VALUES (?, ?, ?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE " +
                             "blood_pressure = VALUES(blood_pressure), " +
                             "blood_sugar_level = VALUES(blood_sugar_level), " +
                             "insulin_per_week = VALUES(insulin_per_week), " +
                             "insulin_per_month = VALUES(insulin_per_month), " +
                             "recommendations = VALUES(recommendations), " +
                             "updated_at = NOW()")) {

            stmt.setInt(1, userId);
            stmt.setString(2, bp);
            stmt.setFloat(3, Float.parseFloat(sugar.replaceAll("[^\\d.]", "")));
            stmt.setFloat(4, Float.parseFloat(insulinWeek.replaceAll("[^\\d.]", "")));
            stmt.setFloat(5, Float.parseFloat(insulinMonth.replaceAll("[^\\d.]", "")));
            stmt.setString(6, recommendations);
            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}