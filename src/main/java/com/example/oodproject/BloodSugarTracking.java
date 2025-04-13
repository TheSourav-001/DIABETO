package com.example.oodproject;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

public class BloodSugarTracking extends Application {

    private LineChart<String, Number> lineChart;
    private BarChart<String, Number> barChart;
    private ComboBox<String> filterBox;
    private TableView<BloodSugarData> dataTable;
    private List<BloodSugarData> dataList;
    private int userId;
    private ImageView logoImage;

    private static final double COLUMN_WIDTH = 220.0;

    public BloodSugarTracking() {
        this.userId = 0;
    }

    public BloodSugarTracking(int userId) {
        this.userId = userId;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Blood Sugar Tracker");

        GridPane dataEntryForm = createDataEntryForm();
        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All", "Last 7 Days", "Last 30 Days");
        filterBox.setValue("All");
        filterBox.setOnAction(e -> updateView());

        InputStream is = getClass().getResourceAsStream("/Icon/bv.png");
        if (is == null) {
            System.err.println("Error: Could not find logo image!");
        } else {
            logoImage = new ImageView(new Image(is));
            logoImage.setFitWidth(200);
            logoImage.setFitHeight(100);
        }
        lineChart = createLineChart();
        barChart = createBarChart();
        dataTable = createDataTable();

        VBox vbox = new VBox(dataEntryForm, filterBox, lineChart, barChart, dataTable);
        Scene scene = new Scene(vbox, 800, 600);

        stage.setScene(scene);
        stage.show();
        loadDataFromDatabase();
    }

    private GridPane createDataEntryForm() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label dateLabel = new Label("Date:");
        grid.add(dateLabel, 0, 0);
        DatePicker datePicker = new DatePicker();
        grid.add(datePicker, 1, 0);

        Label beforeMealLabel = new Label("Before Meal:");
        grid.add(beforeMealLabel, 0, 1);
        TextField beforeMealField = new TextField();
        grid.add(beforeMealField, 1, 1);

        Label afterMealLabel = new Label("After Meal:");
        grid.add(afterMealLabel, 0, 2);
        TextField afterMealField = new TextField();
        grid.add(afterMealField, 1, 2);

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addDataToDatabase(datePicker.getValue(), beforeMealField.getText(), afterMealField.getText()));
        grid.add(addButton, 1, 3);

        return grid;
    }

    private TableView<BloodSugarData> createDataTable() {
        TableView<BloodSugarData> table = new TableView<>();

        TableColumn<BloodSugarData, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty().asString());
        dateColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        timeColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> beforeMealColumn = new TableColumn<>("Before Meal");
        beforeMealColumn.setCellValueFactory(cellData -> cellData.getValue().beforeMealProperty().asObject());
        beforeMealColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> afterMealColumn = new TableColumn<>("After Meal");
        afterMealColumn.setCellValueFactory(cellData -> cellData.getValue().afterMealProperty().asObject());
        afterMealColumn.setPrefWidth(COLUMN_WIDTH);

        table.getColumns().addAll(dateColumn, timeColumn, beforeMealColumn, afterMealColumn);
        return table;
    }

    private void loadDataFromDatabase() {
        dataList = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM blood_sugar_entries WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate date = rs.getDate("entry_date").toLocalDate();
                String time = rs.getString("entry_time");
                double beforeMeal = rs.getDouble("before_meal");
                double afterMeal = rs.getDouble("after_meal");

                dataList.add(new BloodSugarData(date, time, beforeMeal, afterMeal));
            }
            updateView();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addDataToDatabase(LocalDate date, String beforeMealText, String afterMealText) {
        if (date == null || beforeMealText.isEmpty() || afterMealText.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        double beforeMeal, afterMeal;
        try {
            beforeMeal = Double.parseDouble(beforeMealText);
            afterMeal = Double.parseDouble(afterMealText);

            if (beforeMeal < 0 || afterMeal < 0) {
                showAlert("Error", "Sugar levels cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid input for sugar levels.");
            return;
        }

        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = time.format(formatter);

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO blood_sugar_entries (user_id, entry_date, entry_time, before_meal, after_meal) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, formattedTime);
            stmt.setDouble(4, beforeMeal);
            stmt.setDouble(5, afterMeal);
            stmt.executeUpdate();
            loadDataFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public LineChart<String, Number> createLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Blood Sugar Level");
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Blood Sugar Over Time");
        return chart;
    }

    public BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Blood Sugar Level");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Before & After Meal Blood Sugar Levels");
        return chart;
    }

    private void updateCharts(List<BloodSugarData> filteredData) {
        updateLineChart(lineChart, filteredData);
        updateBarChart(barChart, filteredData);
    }

    private void updateLineChart(LineChart<String, Number> chart, List<BloodSugarData> filteredData) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Before Meal");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (BloodSugarData data : filteredData) {
            series.getData().add(new XYChart.Data<>(data.getDate().format(formatter), data.getBeforeMeal()));
        }
        chart.getData().add(series);
    }

    private void updateBarChart(BarChart<String, Number> chart, List<BloodSugarData> filteredData) {
        chart.getData().clear();
        XYChart.Series<String, Number> beforeMealSeries = new XYChart.Series<>();
        beforeMealSeries.setName("Before Meal");
        XYChart.Series<String, Number> afterMealSeries = new XYChart.Series<>();
        afterMealSeries.setName("After Meal");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (BloodSugarData data : filteredData) {
            beforeMealSeries.getData().add(new XYChart.Data<>(data.getDate().format(formatter), data.getBeforeMeal()));
            afterMealSeries.getData().add(new XYChart.Data<>(data.getDate().format(formatter), data.getAfterMeal()));
        }
        chart.getData().addAll(beforeMealSeries, afterMealSeries);
    }

    private void updateTable(List<BloodSugarData> filteredData) {
        dataTable.getColumns().clear();
        dataTable.getItems().clear();

        // Common columns
        TableColumn<BloodSugarData, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty().asString());
        dateColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        timeColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> beforeMealColumn = new TableColumn<>("Before Meal");
        beforeMealColumn.setCellValueFactory(cellData -> cellData.getValue().beforeMealProperty().asObject());
        beforeMealColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> afterMealColumn = new TableColumn<>("After Meal");
        afterMealColumn.setCellValueFactory(cellData -> cellData.getValue().afterMealProperty().asObject());
        afterMealColumn.setPrefWidth(COLUMN_WIDTH);

        dataTable.getColumns().addAll(dateColumn, timeColumn, beforeMealColumn, afterMealColumn);
        dataTable.setItems(FXCollections.observableArrayList(filteredData));
    }

    private void updateView() {
        String filter = filterBox.getValue();
        List<BloodSugarData> filteredData = getFilteredData();

        updateCharts(filteredData);

        if ("All".equals(filter)) {
            showAllDataTable(filteredData);
        } else if ("Last 7 Days".equals(filter)) {
            showWeeklyDataTable(filteredData);
        } else if ("Last 30 Days".equals(filter)) {
            showMonthlyDataTable(filteredData);
        }
    }

    private void showAllDataTable(List<BloodSugarData> filteredData) {
        updateTable(filteredData);
        // Add daily columns
        TableColumn<BloodSugarData, Double> avgDailyColumn = new TableColumn<>("Avg Daily");
        avgDailyColumn.setCellValueFactory(cellData -> cellData.getValue().avgDailyProperty().asObject());
        avgDailyColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> maxDailyColumn = new TableColumn<>("Max Daily");
        maxDailyColumn.setCellValueFactory(cellData -> cellData.getValue().maxDailyProperty().asObject());
        maxDailyColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> minDailyColumn = new TableColumn<>("Min Daily");
        minDailyColumn.setCellValueFactory(cellData -> cellData.getValue().minDailyProperty().asObject());
        minDailyColumn.setPrefWidth(COLUMN_WIDTH);

        dataTable.getColumns().addAll(avgDailyColumn, maxDailyColumn, minDailyColumn);
        calculateAndSetDailyAverages(filteredData);

    }

    private void showWeeklyDataTable(List<BloodSugarData> filteredData) {
        updateTable(filteredData);
        // Add weekly columns
        TableColumn<BloodSugarData, Double> avgWeeklyColumn = new TableColumn<>("Avg Weekly");
        avgWeeklyColumn.setCellValueFactory(cellData -> cellData.getValue().avgWeeklyProperty().asObject());
        avgWeeklyColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> maxWeeklyColumn = new TableColumn<>("Max Weekly");
        maxWeeklyColumn.setCellValueFactory(cellData -> cellData.getValue().maxWeeklyProperty().asObject());
        maxWeeklyColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> minWeeklyColumn = new TableColumn<>("Min Weekly");
        minWeeklyColumn.setCellValueFactory(cellData -> cellData.getValue().minWeeklyProperty().asObject());
        minWeeklyColumn.setPrefWidth(COLUMN_WIDTH);

        dataTable.getColumns().addAll(avgWeeklyColumn, maxWeeklyColumn, minWeeklyColumn);
        calculateAndSetWeeklyAverages(filteredData);
    }

    private void showMonthlyDataTable(List<BloodSugarData> filteredData) {
        updateTable(filteredData);
        // Add monthly columns
        TableColumn<BloodSugarData, Double> avgMonthlyColumn = new TableColumn<>("Avg Monthly");
        avgMonthlyColumn.setCellValueFactory(cellData -> cellData.getValue().avgMonthlyProperty().asObject());
        avgMonthlyColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> maxMonthlyColumn = new TableColumn<>("Max Monthly");
        maxMonthlyColumn.setCellValueFactory(cellData -> cellData.getValue().maxMonthlyProperty().asObject());
        maxMonthlyColumn.setPrefWidth(COLUMN_WIDTH);

        TableColumn<BloodSugarData, Double> minMonthlyColumn = new TableColumn<>("Min Monthly");
        minMonthlyColumn.setCellValueFactory(cellData -> cellData.getValue().minMonthlyProperty().asObject());
        minMonthlyColumn.setPrefWidth(COLUMN_WIDTH);

        dataTable.getColumns().addAll(avgMonthlyColumn, maxMonthlyColumn, minMonthlyColumn);
        calculateAndSetMonthlyAverages(filteredData);
    }

    private List<BloodSugarData> getFilteredData() {
        LocalDate today = LocalDate.now();
        String filter = filterBox.getValue();
        if (dataList == null) {
            return new ArrayList<>();
        }
        return dataList.stream().filter(data -> {
            if ("Last 7 Days".equals(filter)) {
                return data.getDate().isAfter(today.minusDays(7));
            } else if ("Last 30 Days".equals(filter)) {
                return data.getDate().isAfter(today.minusDays(30));
            }
            return true;
        }).toList();
    }

    private void calculateAndSetDailyAverages(List<BloodSugarData> data) {
        for (BloodSugarData entry : data) {
            LocalDate date = entry.getDate();
            double avgDaily = DatabaseHelper.calculateAverageDaily(userId, date);
            double maxDaily = DatabaseHelper.calculateMaxDaily(userId, date);
            double minDaily = DatabaseHelper.calculateMinDaily(userId, date);

            entry.setAvgDaily(avgDaily);
            entry.setMaxDaily(maxDaily);
            entry.setMinDaily(minDaily);
        }
        dataTable.refresh();
    }

    private void calculateAndSetWeeklyAverages(List<BloodSugarData> data) {
        if (data.isEmpty()) return;

        LocalDate startDate = data.get(0).getDate();
        LocalDate endDate = data.get(data.size() - 1).getDate();

        double avgWeekly = DatabaseHelper.calculateAverageWeekly(userId, startDate, endDate);
        double maxWeekly = DatabaseHelper.calculateMaxWeekly(userId, startDate, endDate);
        double minWeekly = DatabaseHelper.calculateMinWeekly(userId, startDate, endDate);

        for (BloodSugarData entry : data) {
            entry.setAvgWeekly(avgWeekly);
            entry.setMaxWeekly(maxWeekly);
            entry.setMinWeekly(minWeekly);
        }
        dataTable.refresh();
    }

    private void calculateAndSetMonthlyAverages(List<BloodSugarData> data) {
        if (data.isEmpty()) return;

        LocalDate startDate = data.get(0).getDate();
        LocalDate endDate = data.get(data.size() - 1).getDate();

        double avgMonthly= DatabaseHelper.calculateAverageMonthly(userId, startDate, endDate);
        double maxMonthly = DatabaseHelper.calculateMaxMonthly(userId, startDate, endDate);
        double minMonthly = DatabaseHelper.calculateMinMonthly(userId, startDate, endDate);

        for (BloodSugarData entry : data) {
            entry.setAvgMonthly(avgMonthly);
            entry.setMaxMonthly(maxMonthly);
            entry.setMinMonthly(minMonthly);
        }
        dataTable.refresh();
    }

    public static class BloodSugarData {
        private final ObjectProperty<LocalDate> date;
        private final StringProperty time;
        private final DoubleProperty beforeMeal;
        private final DoubleProperty afterMeal;
        private DoubleProperty avgDaily;
        private DoubleProperty maxDaily;
        private DoubleProperty minDaily;
        private DoubleProperty avgWeekly;
        private DoubleProperty maxWeekly;
        private DoubleProperty minWeekly;
        private DoubleProperty avgMonthly;
        private DoubleProperty maxMonthly;
        private DoubleProperty minMonthly;

        public BloodSugarData(LocalDate date, String time, double beforeMeal, double afterMeal) {
            this.date = new SimpleObjectProperty<>(date);
            this.time= new SimpleStringProperty(time);
            this.beforeMeal = new SimpleDoubleProperty(beforeMeal);
            this.afterMeal = new SimpleDoubleProperty(afterMeal);
            this.avgDaily = new SimpleDoubleProperty(0.0);
            this.maxDaily = new SimpleDoubleProperty(0.0);
            this.minDaily = new SimpleDoubleProperty(0.0);
            this.avgWeekly = new SimpleDoubleProperty(0.0);
            this.maxWeekly = new SimpleDoubleProperty(0.0);
            this.minWeekly = new SimpleDoubleProperty(0.0);
            this.avgMonthly = new SimpleDoubleProperty(0.0);
            this.maxMonthly = new SimpleDoubleProperty(0.0);
            this.minMonthly = new SimpleDoubleProperty(0.0);
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

        public void setAvgDaily(double avgDaily) {
            this.avgDaily.set(avgDaily);
        }

        public void setMaxDaily(double maxDaily) {
            this.maxDaily.set(maxDaily);
        }

        public void setMinDaily(double minDaily) {
            this.minDaily.set(minDaily);
        }

        public void setAvgWeekly(double avgWeekly) {
            this.avgWeekly.set(avgWeekly);
        }

        public void setMaxWeekly(double maxWeekly) {
            this.maxWeekly.set(maxWeekly);
        }

        public void setMinWeekly(double minWeekly) {
            this.minWeekly.set(minWeekly);
        }

        public void setAvgMonthly(double avgMonthly) {
            this.avgMonthly.set(avgMonthly);
        }

        public void setMaxMonthly(double maxMonthly) {
            this.maxMonthly.set(maxMonthly);
        }

        public void setMinMonthly(double minMonthly) {
            this.minMonthly.set(minMonthly);
        }
    }
}