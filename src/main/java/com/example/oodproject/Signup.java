package com.example.oodproject;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.*;
import java.util.Objects;

import static javafx.scene.paint.Color.BLACK;

public class Signup extends Application {
    private TextField fullNameField, usernameField, ageField, weightField, heightField, contactField, addressField;
    private PasswordField passwordField, confirmPasswordField;
    private DatePicker dobPicker;
    private ComboBox<String> genderBox, bloodGroupBox;
    private Button signUpButton, signInButton;
    private Label titleLabel, passwordStrengthLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sign Up - DIABETO");

        StackPane root = new StackPane();

        // Background image with null check
        try {
            String bgImagePath = Objects.requireNonNull(getClass().getResource("/icons/n.jpg")).toExternalForm();
            root.setStyle("-fx-background-image: url('" + bgImagePath + "'); -fx-background-size: cover;");
        } catch (NullPointerException e) {
            System.out.println("Background image not found, using plain background.");
            root.setStyle("-fx-background-color: #FFFFFF;"); // fallback background
        }

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");

        // Logo image with null check
        ImageView logoImage;
        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/bv.png")));
            logoImage = new ImageView(logo);
            logoImage.setFitWidth(300);
            logoImage.setFitHeight(200);
            logoImage.setPreserveRatio(true);
            logoImage.setSmooth(true);
            logoImage.setOnScroll(event -> {
                double delta = event.getDeltaY();
                double scaleFactor = (delta > 0) ? 1.05 : 0.95;
                logoImage.setFitWidth(logoImage.getFitWidth() * scaleFactor);
                logoImage.setFitHeight(logoImage.getFitHeight() * scaleFactor);
            });
            layout.getChildren().add(logoImage);
        } catch (NullPointerException e) {
            System.out.println("Logo image not found, skipping logo display.");
        }

        // Welcome Message
        titleLabel = new Label("Welcome! Create Your Account");
        titleLabel.setTextFill(BLACK);
        titleLabel.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");
        layout.getChildren().add(titleLabel);

        // Input Fields
        fullNameField = createTextField("Full Name");
        usernameField = createTextField("Username");
        dobPicker = new DatePicker();
        dobPicker.setPromptText("Date of Birth");
        ageField = createTextField("Age");
        genderBox = createComboBox(new String[]{"Male", "Female", "Other"}, "Select Gender");
        weightField = createTextField("Weight (kg)");
        heightField = createTextField("Height (cm)");
        bloodGroupBox = createComboBox(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"}, "Blood Group");
        contactField = createTextField("Contact Number");
        addressField = createTextField("Address");
        passwordField = createPasswordField("Password");
        confirmPasswordField = createPasswordField("Confirm Password");

        passwordStrengthLabel = new Label("Password Strength: ");
        passwordStrengthLabel.setTextFill(BLACK);

        layout.getChildren().addAll(fullNameField, usernameField, dobPicker, ageField, genderBox, weightField, heightField,
                bloodGroupBox, contactField, addressField, passwordField, confirmPasswordField, passwordStrengthLabel);

        // Sign Up Button
        signUpButton = new Button("SIGN UP");
        signUpButton.setStyle("-fx-background-color: #006400; -fx-text-fill: white; -fx-font-size: 14px;");
        signUpButton.setOnAction(e -> registerUser());
        layout.getChildren().add(signUpButton);

        // Sign In Button (for redirection)
        signInButton = new Button("Already have an account? Sign In");
        signInButton.setStyle("-fx-background-color: #006400; -fx-text-fill: white; -fx-font-size: 12px;");
        signInButton.setOnAction(e -> redirectToLogin());
        layout.getChildren().add(signInButton);

        root.getChildren().add(layout);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Password Strength Check
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkPasswordStrength(newValue));
    }

    private TextField createTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setStyle("-fx-background-color: rgba(255, 255, 255, 0); -fx-text-fill: black; " +
                "-fx-border-radius: 30; -fx-border-color: black; -fx-border-width: 2; -fx-prompt-text-fill: black;");
        textField.setMaxWidth(250);
        return textField;
    }

    private PasswordField createPasswordField(String placeholder) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(placeholder);
        passwordField.setStyle("-fx-background-color: rgba(255, 255, 255, 0); -fx-text-fill: black; " +
                "-fx-border-radius: 30; -fx-border-color: black; -fx-border-width: 2; -fx-prompt-text-fill: black;");
        passwordField.setMaxWidth(250);
        return passwordField;
    }

    private ComboBox<String> createComboBox(String[] items, String prompt) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(items);
        comboBox.setPromptText(prompt);
        comboBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0); -fx-border-radius: 30; -fx-border-color: black; -fx-border-width: 2; -fx-prompt-text-fill: black;");
        comboBox.setMaxWidth(250);
        return comboBox;
    }

    private void registerUser() {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String dob = dobPicker.getValue() != null ? dobPicker.getValue().toString() : "";
        String age = ageField.getText();
        String gender = genderBox.getValue();
        String weight = weightField.getText();
        String height = heightField.getText();
        String bloodGroup = bloodGroupBox.getValue();
        String contact = contactField.getText();
        String address = addressField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || dob.isEmpty() || age.isEmpty() || gender == null || weight.isEmpty() || height.isEmpty() ||
                bloodGroup == null || contact.isEmpty() || address.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Ood_Project", "root", "");
             PreparedStatement stmt = con.prepareStatement("INSERT INTO users (full_name, username, date_of_birth, age, gender, weight, height, blood_group, contact_number, address, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");) {
            stmt.setString(1, fullName);
            stmt.setString(2, username);
            stmt.setString(3, dob);
            stmt.setInt(4, Integer.parseInt(age));
            stmt.setString(5, gender);
            stmt.setDouble(6, Double.parseDouble(weight));
            stmt.setDouble(7, Double.parseDouble(height));
            stmt.setString(8, bloodGroup);
            stmt.setString(9, contact);
            stmt.setString(10, address);
            stmt.setString(11, password);
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Registration Successful! Redirecting to Login...");
            new Login().start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register user.");
        }
    }

    private void checkPasswordStrength(String password) {
        String strength = "";
        if (password.length() < 6) {
            strength = "Weak";
        } else if (password.length() >= 6 && password.length() < 10) {
            strength = "Medium";
        } else {
            strength = "Strong";
        }
        passwordStrengthLabel.setText("Password Strength: " + strength);
    }

    private void redirectToLogin() {
        // Logic to redirect to Login page
        new Login().start(new Stage());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
