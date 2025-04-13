package com.example.oodproject;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSettings extends Application {
    private int userId;
    private String userName;

    // Color constants matching dashboard
    private static final String DARK_BLUE = "#34495E";
    private static final String TEAL = "#1ABC9C";
    private static final String WHITE = "#ffffff";
    private static final String LIGHT_GRAY = "#f5f5f5";

    public UserSettings(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("User Settings - " + userName);

        // Main container with background
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: " + LIGHT_GRAY + ";");

        // Top bar with welcome message and logo (matching dashboard style)
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: " + DARK_BLUE + "; -fx-padding: 15px;");
        topBar.setAlignment(Pos.CENTER); // প্রাথমিক প্রান্তিককরণ

        // Left side - Welcome text
        Text welcomeText = new Text("User Settings (DIABETO)");
        welcomeText.setStyle("-fx-fill: white; -fx-font-size: 24px; -fx-font-family: 'Britannic Bold';");

        Text subtitle = new Text("Update your personal information");
        subtitle.setStyle("-fx-fill: white; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");

        VBox welcomeBox = new VBox(5, welcomeText, subtitle);
        welcomeBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(welcomeBox, new Insets(0, 0, 0, 20)); // বাম দিকে ২০ পিক্সেল মার্জিন

        // Right side - Logo
        ImageView logoImage = new ImageView(new Image(getClass().getResourceAsStream("/Icon/ac.png")));
        logoImage.setFitWidth(150);
        logoImage.setFitHeight(60);
        logoImage.setPreserveRatio(true);
        HBox.setMargin(logoImage, new Insets(0, 20, 0, 0)); // ডান দিকে ২০ পিক্সেল মার্জিন

        topBar.getChildren().addAll(welcomeBox, new Region(), logoImage); // মাঝখানে একটি ফাঁকা Region যোগ করা হচ্ছে
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS); // ফাঁকা Region কে প্রসারিত করার অনুমতি দেওয়া হচ্ছে

        mainContainer.setTop(topBar);


        // Form container with shadow effect
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.TOP_CENTER);
        formContainer.setPadding(new Insets(30, 40, 40, 40));
        formContainer.setStyle("-fx-background-color: " + WHITE + "; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Form title
        Label formTitle = new Label("Personal Information");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_BLUE + ";");
        formTitle.setPadding(new Insets(0, 0, 10, 0));

        // Create form grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));

        // Form fields
        TextField fullNameField = createStyledTextField("Full Name");
        TextField usernameField = createStyledTextField("Username");
        PasswordField passwordField = createStyledPasswordField("Password");
        DatePicker dobPicker = createStyledDatePicker();
        TextField ageField = createStyledTextField("Age");
        ComboBox<String> genderCombo = createStyledComboBox("Gender", "Male", "Female", "Other");
        TextField heightField = createStyledTextField("Height (cm)");
        TextField weightField = createStyledTextField("Weight (kg)");
        ComboBox<String> bloodGroupCombo = createStyledComboBox("Blood Group",
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        TextField contactField = createStyledTextField("Contact Number");
        TextArea addressArea = createStyledTextArea("Address");

        // Add labels and fields to grid
        addFormRow(grid, 0, "Full Name:", fullNameField);
        addFormRow(grid, 1, "Username:", usernameField);
        addFormRow(grid, 2, "Password:", passwordField);
        addFormRow(grid, 3, "Date of Birth:", dobPicker);
        addFormRow(grid, 4, "Age:", ageField);
        addFormRow(grid, 5, "Gender:", genderCombo);
        addFormRow(grid, 6, "Height (cm):", heightField);
        addFormRow(grid, 7, "Weight (kg):", weightField);
        addFormRow(grid, 8, "Blood Group:", bloodGroupCombo);
        addFormRow(grid, 9, "Contact Number:", contactField);
        addFormRow(grid, 10, "Address:", addressArea);

        // Load current user data
        if (userId != 0) {
            loadUserData(userId, fullNameField, usernameField, passwordField, dobPicker,
                    ageField, genderCombo, heightField, weightField,
                    bloodGroupCombo, contactField, addressArea);
        }

        // Buttons with matching dashboard style
        Button saveButton = new Button("SAVE CHANGES");
        saveButton.setStyle("-fx-background-color: " + TEAL + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 25 10 25; " +
                "-fx-font-size: 14px;");
        saveButton.setOnAction(e -> saveUserData(userId, fullNameField, usernameField, passwordField, dobPicker,
                ageField, genderCombo, heightField, weightField,
                bloodGroupCombo, contactField, addressArea, primaryStage));

        Button cancelButton = new Button("CANCEL");
        cancelButton.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: red; " + // অথবা "red" ব্যবহার করতে পারেন
                "-fx-font-weight: bold; " +
                "-fx-border-color: red; " + // অথবা "red" ব্যবহার করতে পারেন
                "-fx-border-width: 2; " +
                "-fx-padding: 10 25 10 25; " +
                "-fx-font-size: 14px;");
        cancelButton.setOnAction(e -> primaryStage.close());

        HBox buttonBox = new HBox(20, cancelButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        formContainer.getChildren().addAll(formTitle, grid, buttonBox);

        // Center the form in the main container with ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(formContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        mainContainer.setCenter(scrollPane);

        // Set scene
        Scene scene = new Scene(mainContainer, 700, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setStyle("-fx-background-color: " + WHITE + "; " +
                "-fx-border-color: #ddd; " +
                "-fx-border-radius: 4; " +
                "-fx-padding: 8; " +
                "-fx-font-size: 14px;");
        field.setPromptText(prompt);
        field.setPrefWidth(250);
        return field;
    }

    private PasswordField createStyledPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setStyle("-fx-background-color: " + WHITE + "; " +
                "-fx-border-color: #ddd; " +
                "-fx-border-radius: 4; " +
                "-fx-padding: 8; " +
                "-fx-font-size: 14px;");
        field.setPromptText(prompt);
        field.setPrefWidth(250);
        return field;
    }

    private DatePicker createStyledDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setStyle("-fx-background-color: " + WHITE + "; " +
                "-fx-border-color: #ddd; " +
                "-fx-border-radius: 4; " +
                "-fx-font-size: 14px;");
        picker.setPrefWidth(250);
        return picker;
    }

    private ComboBox<String> createStyledComboBox(String prompt, String... items) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(items);
        combo.setStyle("-fx-background-color: " + WHITE + "; " +
                "-fx-border-color: #ddd; " +
                "-fx-border-radius: 4; " +
                "-fx-font-size: 14px;");
        combo.setPromptText(prompt);
        combo.setPrefWidth(250);
        return combo;
    }

    private TextArea createStyledTextArea(String prompt) {
        TextArea area = new TextArea();
        area.setStyle("-fx-background-color: " + WHITE + "; " +
                "-fx-border-color: #ddd; " +
                "-fx-border-radius: 4; " +
                "-fx-font-size: 14px;");
        area.setPromptText(prompt);
        area.setPrefWidth(250);
        area.setPrefRowCount(3);
        return area;
    }

    private void addFormRow(GridPane grid, int row, String labelText, Control field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_BLUE + "; -fx-font-size: 14px;");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private void loadUserData(int userId, TextField fullName, TextField username, PasswordField password,
                              DatePicker dob, TextField age, ComboBox<String> gender, TextField height,
                              TextField weight, ComboBox<String> bloodGroup, TextField contact,
                              TextArea address) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                fullName.setText(rs.getString("full_name"));
                username.setText(rs.getString("username"));
                password.setText(rs.getString("password"));
                if (rs.getDate("date_of_birth") != null) {
                    dob.setValue(rs.getDate("date_of_birth").toLocalDate());
                }
                age.setText(String.valueOf(rs.getInt("age")));
                gender.setValue(rs.getString("gender"));
                height.setText(String.valueOf(rs.getDouble("height")));
                weight.setText(String.valueOf(rs.getDouble("weight")));
                bloodGroup.setValue(rs.getString("blood_group"));
                contact.setText(rs.getString("contact_number"));
                address.setText(rs.getString("address"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load user data");
            alert.setContentText("An error occurred while loading your information. Please try again.");
            alert.showAndWait();
        }
    }

    private void saveUserData(int userId, TextField fullName, TextField username, PasswordField password,
                              DatePicker dob, TextField age, ComboBox<String> gender, TextField height,
                              TextField weight, ComboBox<String> bloodGroup, TextField contact,
                              TextArea address, Stage stage) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET full_name = ?, username = ?, password = ?, " +
                             "date_of_birth = ?, age = ?, gender = ?, height = ?, weight = ?, " +
                             "blood_group = ?, contact_number = ?, address = ? WHERE id = ?")) {

            stmt.setString(1, fullName.getText());
            stmt.setString(2, username.getText());
            stmt.setString(3, password.getText());
            stmt.setDate(4, dob.getValue() != null ? Date.valueOf(dob.getValue()) : null);
            stmt.setInt(5, Integer.parseInt(age.getText()));
            stmt.setString(6, gender.getValue());
            stmt.setDouble(7, Double.parseDouble(height.getText()));
            stmt.setDouble(8, Double.parseDouble(weight.getText()));
            stmt.setString(9, bloodGroup.getValue());
            stmt.setString(10, contact.getText());
            stmt.setString(11, address.getText());
            stmt.setInt(12, userId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Profile Updated");
                alert.setContentText("Your information has been successfully updated.");
                alert.showAndWait();
                stage.close();
            } else {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Update Failed");
            alert.setContentText("An error occurred while updating your information. Please try again.");
            alert.showAndWait();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Please enter valid numbers for age, height, and weight.");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}