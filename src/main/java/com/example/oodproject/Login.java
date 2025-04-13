package com.example.oodproject;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;
import java.io.InputStream;
import java.sql.*;

public class Login extends Application {

    private TextField tf1;
    private PasswordField pf2;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Diabetes Tracking System");

        // Root Pane with Background Image
        StackPane root = new StackPane();
        InputStream bgImageStream = getClass().getResourceAsStream("/icons/n.jpg");
        if (bgImageStream != null) {
            root.setStyle("-fx-background-image: url('" + getClass().getResource("/icons/n.jpg") + "'); -fx-background-size: cover;");
        } else {
            System.out.println("Background image not found!");
        }

        // Main VBox for UI Components
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: rgba(0, 0, 0, 0); -fx-padding: 30; -fx-border-radius: 15;");

        // Logo Image with Safe Loading
        InputStream logoStream = getClass().getResourceAsStream("/icons/bv.png");

        if (logoStream != null) {
            ImageView logoImage = new ImageView(new Image(logoStream));
            logoImage.setFitWidth(400);
            logoImage.setFitHeight(300);
            logoImage.setPreserveRatio(true);
            logoImage.setSmooth(true);
            logoImage.setTranslateY(0);
            mainLayout.getChildren().add(logoImage);
        } else {
            System.out.println("Logo image not found! Skipping logo display.");
        }

        // Title
        Label titleLabel = new Label("Welcome to DIABETO");
        titleLabel.setFont(new Font("Britannic Bold", 60));
        titleLabel.setTextFill(Color.BLACK);
        mainLayout.getChildren().add(titleLabel);

        Label subTitleLabel = new Label("- Smart Tracking, Healthier Living!");
        subTitleLabel.setFont(new Font("Segoe UI", 30));
        subTitleLabel.setTextFill(Color.BLACK);
        mainLayout.getChildren().add(subTitleLabel);

        // Username Input
        tf1 = new TextField();
        tf1.setPromptText("Enter your username");
        tf1.setStyle("-fx-background-color: rgba(0, 0, 0, 0); -fx-text-fill: black; " +
                "-fx-padding: 10; -fx-border-radius: 50; -fx-border-color: black; -fx-border-width: 2;");
        tf1.setMaxWidth(250);

        // Password Input
        pf2 = new PasswordField();
        pf2.setPromptText("Enter your password");
        pf2.setStyle("-fx-background-color: rgba(0, 0, 0, 0); -fx-text-fill: black; " +
                "-fx-padding: 10; -fx-border-radius: 30; -fx-border-color: black; -fx-border-width: 2;");
        pf2.setMaxWidth(250);

        mainLayout.getChildren().addAll(tf1, pf2);

        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button signInButton = createStyledButton("SIGN IN", "#007bff");
        Button clearButton = createStyledButton("CLEAR", "#28a745");
        Button signUpButton = createStyledButton("SIGN UP", "#6c757d");

        buttonBox.getChildren().addAll(signInButton, clearButton, signUpButton);
        mainLayout.getChildren().add(buttonBox);
        root.getChildren().add(mainLayout);

        // Fade-in Animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), mainLayout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Login functionality
        signInButton.setOnAction(event -> {
            String username = tf1.getText();  // User's entered username
            String password = pf2.getText();  // User's entered password

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(AlertType.ERROR, "Error", "Please enter both username and password.");
            } else {
                int userId = validateLogin(username, password);
                if (userId != -1) {
                    showAlert(AlertType.INFORMATION, "Login Successful", "Welcome, " + username + "!");
                    new Dashboard(username, userId).start(new Stage()); // Pass userId here
                    primaryStage.close();
                } else {
                    showAlert(AlertType.ERROR, "Login Failed", "Incorrect Username or Password.");
                }
            }
        });

        // Clear Button Action
        clearButton.setOnAction(e -> {
            tf1.clear();
            pf2.clear();
        });

        // Sign Up Button Action
        signUpButton.setOnAction(e -> {
            new Signup().start(new Stage());
            primaryStage.close();
        });

        // Setup Scene
        Scene scene = new Scene(root, 800, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Function to create styled buttons
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-background-radius: 30; -fx-border-radius: 30; -fx-padding: 10 20; -fx-border-color: white; -fx-border-width: 2;");
        button.setPrefWidth(140);
        return button;
    }

    // Updated validateLogin function to return userId
    private int validateLogin(String username, String password) {
        int userId = -1; // Default to -1 indicating login failure
        try (Connection con = conn.getConnection()) {
            String query = "SELECT id FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id"); // Retrieve user ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    // Function to show alerts
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
