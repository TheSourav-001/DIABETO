package com.example.oodproject;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportGeneration extends Application {

    // Color scheme
    private static final BaseColor PRIMARY_COLOR = new BaseColor(52, 73, 94);
    private static final BaseColor SECONDARY_COLOR = new BaseColor(59, 89, 152);
    private static final BaseColor ACCENT_COLOR = new BaseColor(46, 204, 113);
    private static final BaseColor LIGHT_BG = new BaseColor(240, 248, 255);
    private static final BaseColor DARK_TEXT = new BaseColor(44, 62, 80);

    private int userId;
    private String userName;

    public ReportGeneration() {
        // Default constructor
    }

    public ReportGeneration(int userId) {
        this.userId = userId;
        this.userName = fetchUserName(userId);
    }

    private String fetchUserName(int userId) {
        String name = "User";
        try {
            ResultSet rs = DatabaseHelper.getUserDetails(userId);
            if (rs.next()) {
                name = rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Button downloadDashboardButton = createStyledButton("Download Dashboard PDF");
        Button downloadBloodSugarButton = createStyledButton("Download Blood Sugar PDF");
        Button downloadMealDietButton = createStyledButton("Download Meal Diet PDF");

        downloadDashboardButton.setOnAction(e -> generateDashboardPdf());
        downloadBloodSugarButton.setOnAction(e -> generateBloodSugarPdf());
        downloadMealDietButton.setOnAction(e -> generateMealDietPdf());

        root.getChildren().addAll(downloadDashboardButton, downloadBloodSugarButton, downloadMealDietButton);

        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Report Generation");
        primaryStage.show();
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #3B5998; -fx-text-fill: white; -fx-font-weight: bold;");
        button.setPrefWidth(250);
        return button;
    }

    private void addPdfHeader(Document document, String userName) throws DocumentException, IOException {
        // Load custom fonts
        String fontPath = "C:/Users/hp/IdeaProjects/OodProject/src/main/resources/Icon/";
        BaseFont titleFont = BaseFont.createFont(fontPath + "Britannic Bold Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bodyFont = BaseFont.createFont(fontPath + "Cascadia.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        // Font styles
        Font welcomeFont = new Font(titleFont, 22, Font.NORMAL, BaseColor.WHITE);
        Font subtitleFont = new Font(bodyFont, 16, Font.NORMAL, BaseColor.WHITE);
        Font dateFont = new Font(bodyFont, 12, Font.NORMAL, BaseColor.WHITE);

        // Header table
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingBefore(0);
        headerTable.setSpacingAfter(20);

        // Main header cell
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(20);
        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // Content table
        PdfPTable contentTable = new PdfPTable(2);
        contentTable.setWidthPercentage(100);
        contentTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        contentTable.setWidths(new float[]{60f, 40f});

        // Text content
        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        Paragraph textContent = new Paragraph();
        textContent.add(new Chunk("Diabetes Management Report\n", welcomeFont));
        textContent.add(new Chunk("for " + userName + "\n", subtitleFont));
        textContent.add(new Chunk("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")), dateFont));

        textCell.addElement(textContent);
        contentTable.addCell(textCell);

        // Logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        try {
            Image logo = Image.getInstance(getClass().getResource("/Icon/ac.png"));
            logo.scaleToFit(120, 120);
            logoCell.addElement(new Chunk(logo, 0, 0));
        } catch (Exception e) {
            // If logo fails to load, just leave it blank
        }

        contentTable.addCell(logoCell);
        headerCell.addElement(contentTable);
        headerTable.addCell(headerCell);
        document.add(headerTable);
    }

    private void generateDashboardPdf() {
        if (userId == 0) {
            System.out.println("Guest mode: Cannot generate report.");
            return;
        }
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Dashboard Report");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File directory = fileChooser.getSelectedFile();
                String filePath = directory.getAbsolutePath() + "/dashboard_report.pdf";

                Document document = new Document(PageSize.A4.rotate());
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();

                addPdfHeader(document, userName);
                addTitle(document, "Diabetes Management Dashboard");
                document.add(Chunk.NEWLINE);

                addSectionTitle(document, "User Profile");
                addUserDetailsTable(document);
                document.add(Chunk.NEWLINE);

                addSectionTitle(document, "Health Metrics");
                addLatestDashboardDataTable(document);
                document.add(Chunk.NEWLINE);

                addSectionTitle(document, "Nutrition Overview");
                addDashboardCharts(document);

                addFooter(writer, document);
                document.close();
                System.out.println("Dashboard report generated successfully at: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateBloodSugarPdf() {
        if (userId == 0) {
            System.out.println("Guest mode: Cannot generate report.");
            return;
        }
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Blood Sugar Report");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File directory = fileChooser.getSelectedFile();
                String filePath = directory.getAbsolutePath() + "/blood_sugar_report.pdf";

                Document document = new Document(PageSize.A4.rotate());
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();

                addPdfHeader(document, userName);
                addTitle(document, "Blood Sugar Tracking Report");
                document.add(Chunk.NEWLINE);

                addSectionTitle(document, "Blood Sugar Trends");
                addBloodSugarCharts(document);
                document.add(Chunk.NEWLINE);

                addSectionTitle(document, "Detailed Measurements");
                addBloodSugarDataTable(document);

                addFooter(writer, document);
                document.close();
                System.out.println("Blood sugar report generated successfully at: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateMealDietPdf() {
        if (userId == 0) {
            System.out.println("Guest mode: Cannot generate report.");
            return;
        }
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Meal Diet Report");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File directory = fileChooser.getSelectedFile();
                String filePath = directory.getAbsolutePath() + "/meal_diet_report.pdf";

                Document document = new Document(PageSize.A4.rotate());
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();

                addPdfHeader(document, userName);
                addTitle(document, "Meal & Nutrition Report");
                document.add(Chunk.NEWLINE);

                MealDietManagement mealDietManagement = new MealDietManagement(userName, userId);
                try {
                    ResultSet rs = DatabaseHelper.getLatestAdditionalInfo(userId);
                    if (rs.next()) {
                        int bloodSugarLevel = (int) rs.getFloat("blood_sugar_level");
                        String bloodPressure = rs.getString("blood_pressure");
                        ResultSet userDetailsRs = DatabaseHelper.getUserDetails(userId);
                        if (userDetailsRs.next()) {
                            double weight = userDetailsRs.getDouble("weight");
                            String gender = userDetailsRs.getString("gender");
                            double height = userDetailsRs.getDouble("height");
                            int id = userDetailsRs.getInt("id");
                            int age = userDetailsRs.getInt("age");

                            addSectionTitle(document, "Daily Nutritional Targets");
                            addDailyNeedsTable(document, mealDietManagement, weight, gender, bloodSugarLevel);

                            addSectionTitle(document, "Personalized Recommendations");
                            addDietRecommendationsTable(document, mealDietManagement, gender, weight, height,
                                    bloodPressure, bloodSugarLevel, id, age);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    addErrorMessage(document, "Error fetching data for meal diet report.");
                }

                addSectionTitle(document, "Nutrition Breakdown");
                addMealDietCharts(document);

                addFooter(writer, document);
                document.close();
                System.out.println("Meal diet report generated successfully at: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFooter(PdfWriter writer, Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Phrase footerPhrase = new Phrase("Confidential - Generated by Diabetes Management System", footerFont);

        // Create a footer table with explicit width
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
        footerTable.setLockedWidth(true);
        footerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        footerTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        footerTable.getDefaultCell().setPaddingTop(10);
        footerTable.getDefaultCell().setPaddingBottom(10);

        PdfPCell footerCell = new PdfPCell(footerPhrase);
        footerCell.setBorder(Rectangle.TOP);
        footerCell.setBorderColorTop(BaseColor.LIGHT_GRAY);
        footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        footerCell.setPaddingTop(5);
        footerCell.setBorderWidthTop(1f);
        footerTable.addCell(footerCell);

        // Position the footer at the bottom
        footerTable.writeSelectedRows(0, -1,
                document.leftMargin(),
                document.bottomMargin(),
                writer.getDirectContent()
        );
    }

    private PdfPTable createStyledTable(int columns, float widthPercentage) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(widthPercentage);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setPadding(8);
        return table;
    }

    private PdfPCell createHeaderCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(SECONDARY_COLOR);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell createDataCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, DARK_TEXT);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createLabelCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, DARK_TEXT);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBackgroundColor(LIGHT_BG);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createValueCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, DARK_TEXT);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private void addTitle(Document document, String title) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_COLOR);
        Paragraph p = new Paragraph(title, titleFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(15f);
        document.add(p);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, SECONDARY_COLOR);
        Paragraph p = new Paragraph(title, sectionFont);
        p.setSpacingBefore(20f);
        p.setSpacingAfter(10f);

        // Create a table with bottom border to simulate the paragraph border
        PdfPTable borderTable = new PdfPTable(1);
        borderTable.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(p);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColorBottom(SECONDARY_COLOR);
        cell.setBorderWidthBottom(2f);
        cell.setPaddingBottom(5f);
        cell.setBackgroundColor(BaseColor.WHITE);
        borderTable.addCell(cell);

        document.add(borderTable);
    }

    private void addErrorMessage(Document document, String message) throws DocumentException {
        Font errorFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.RED);
        Paragraph p = new Paragraph(message, errorFont);
        p.setSpacingAfter(10f);
        document.add(p);
    }

    private void addUserDetailsTable(Document document) throws DocumentException {
        try {
            ResultSet rs = DatabaseHelper.getUserDetails(userId);
            if (rs.next()) {
                PdfPTable table = createStyledTable(2, 80);

                // Only include columns that exist in your database
                addKeyValueRow(table, "Full Name:", rs.getString("full_name"));
                addKeyValueRow(table, "Date of Birth:", rs.getString("date_of_birth"));
                addKeyValueRow(table, "Gender:", rs.getString("gender"));
                addKeyValueRow(table, "Weight:", rs.getString("weight") + " kg");
                addKeyValueRow(table, "Height:", rs.getString("height") + " cm");
                addKeyValueRow(table, "Blood Group:", rs.getString("blood_group"));

                document.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            addErrorMessage(document, "Error fetching user details: " + e.getMessage());
        }
    }

    private void addLatestDashboardDataTable(Document document) throws DocumentException {
        PdfPTable table = createStyledTable(2, 80);

        table.addCell(createHeaderCell("Health Parameter"));
        table.addCell(createHeaderCell("Measurement"));

        try {
            ResultSet rs = DatabaseHelper.getLatestAdditionalInfo(userId);
            if (rs.next()) {
                addKeyValueRow(table, "Blood Pressure:", rs.getString("blood_pressure"));
                addKeyValueRow(table, "Blood Sugar Level:", String.format("%.1f mg/dL", rs.getFloat("blood_sugar_level")));
                addKeyValueRow(table, "Insulin Per Week:", String.format("%.1f units", rs.getFloat("insulin_per_week")));
                addKeyValueRow(table, "Insulin Per Month:", String.format("%.1f units", rs.getFloat("insulin_per_month")));
                addKeyValueRow(table, "Recommendations:", rs.getString("recommendations"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            addErrorMessage(document, "Error fetching latest dashboard data.");
        }
        document.add(table);
    }

    private void addKeyValueRow(PdfPTable table, String key, String value) {
        table.addCell(createLabelCell(key));
        table.addCell(createValueCell(value != null ? value : "N/A"));
    }

    private void addBloodSugarDataTable(Document document) throws DocumentException {
        PdfPTable table = createStyledTable(4, 100);

        table.addCell(createHeaderCell("Date"));
        table.addCell(createHeaderCell("Time"));
        table.addCell(createHeaderCell("Before Meal (mg/dL)"));
        table.addCell(createHeaderCell("After Meal (mg/dL)"));

        try {
            ResultSet rs = DatabaseHelper.getBloodSugarEntries(userId);
            while (rs.next()) {
                LocalDate date = rs.getDate("entry_date").toLocalDate();
                table.addCell(createDataCell(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
                table.addCell(createDataCell(rs.getString("entry_time")));
                table.addCell(createDataCell(String.format("%.1f", rs.getDouble("before_meal"))));
                table.addCell(createDataCell(String.format("%.1f", rs.getDouble("after_meal"))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            addErrorMessage(document, "Error fetching blood sugar data.");
        }
        document.add(table);
    }

    private void addDailyNeedsTable(Document document, MealDietManagement mealDietManagement,
                                    double weight, String gender, int bloodSugarLevel) throws DocumentException {
        VBox dailyNeedsVBox = mealDietManagement.getDailyNeeds(weight, gender, bloodSugarLevel);
        if (dailyNeedsVBox != null && !dailyNeedsVBox.getChildren().isEmpty()) {
            PdfPTable table = createStyledTable(2, 70);

            for (javafx.scene.Node node : dailyNeedsVBox.getChildren()) {
                if (node instanceof Label) {
                    String text = ((Label) node).getText();
                    String[] parts = text.split(":");
                    if (parts.length == 2) {
                        addKeyValueRow(table, parts[0].trim() + ":", parts[1].trim());
                    } else {
                        PdfPCell cell = new PdfPCell(new Phrase(text,
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SECONDARY_COLOR)));
                        cell.setColspan(2);
                        cell.setBorder(Rectangle.NO_BORDER);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cell);
                    }
                }
            }
            document.add(table);
            document.add(Chunk.NEWLINE);
        } else {
            addErrorMessage(document, "Could not retrieve daily nutritional needs.");
            document.add(Chunk.NEWLINE);
        }
    }

    private void addDietRecommendationsTable(Document document, MealDietManagement mealDietManagement,
                                             String gender, double weight, double height, String bloodPressure,
                                             int bloodSugarLevel, int blood_group, int age) throws DocumentException {
        String recommendations = mealDietManagement.calculateDietRecommendations(
                gender, weight, height, bloodPressure, bloodSugarLevel, blood_group, age);

        PdfPTable table = createStyledTable(1, 90);
        PdfPCell cell = new PdfPCell(new Phrase(recommendations,
                FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT)));
        cell.setPadding(15);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setBackgroundColor(LIGHT_BG);
        table.addCell(cell);
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addDashboardCharts(Document document) {
        try {
            MealDietManagement mealDietManagement = new MealDietManagement(userName, userId);
            VBox centerBox = mealDietManagement.createCenterBox(null);

            if (centerBox != null && !centerBox.getChildren().isEmpty()) {
                Parent firstChild = (Parent) centerBox.getChildren().get(0);

                // Add pie chart
                if (firstChild.getChildrenUnmodifiable().size() > 1) {
                    PieChart pieChart = (PieChart) firstChild.getChildrenUnmodifiable().get(1);
                    addChartToDocument(document, pieChart, "Nutrition Distribution", 500, 400);
                }

                // Add bar chart
                VBox barChartBox = mealDietManagement.createBarChartBox(null);
                if (barChartBox != null && !barChartBox.getChildren().isEmpty() &&
                        barChartBox.getChildren().get(0) instanceof BarChart) {
                    BarChart<String, Number> barChart = (BarChart<String, Number>) barChartBox.getChildren().get(0);
                    addChartToDocument(document, barChart, "Weekly Nutrition Intake", 600, 400);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                addErrorMessage(document, "Error generating charts: " + e.getMessage());
            } catch (DocumentException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addBloodSugarCharts(Document document) {
        try {
            BloodSugarTracking bloodSugarTracking = new BloodSugarTracking(userId);

            // Add line chart
            LineChart<String, Number> lineChart = bloodSugarTracking.createLineChart();
            addChartToDocument(document, lineChart, "Blood Sugar Trends", 600, 400);

            // Add bar chart
            BarChart<String, Number> barChart = bloodSugarTracking.createBarChart();
            addChartToDocument(document, barChart, "Blood Sugar Comparison", 600, 400);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                addErrorMessage(document, "Error generating blood sugar charts: " + e.getMessage());
            } catch (DocumentException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addMealDietCharts(Document document) {
        try {
            MealDietManagement mealDietManagement = new MealDietManagement(userName, userId);
            VBox centerBox = mealDietManagement.createCenterBox(null);

            if (centerBox != null && !centerBox.getChildren().isEmpty()) {
                Parent firstChild = (Parent) centerBox.getChildren().get(0);

                // Add pie chart
                if (firstChild.getChildrenUnmodifiable().size() > 1) {
                    PieChart pieChart = (PieChart) firstChild.getChildrenUnmodifiable().get(1);
                    addChartToDocument(document, pieChart, "Nutrition Distribution", 500, 400);
                }

                // Add bar chart
                VBox barChartBox = mealDietManagement.createBarChartBox(null);
                if (barChartBox != null && !barChartBox.getChildren().isEmpty() &&
                        barChartBox.getChildren().get(0) instanceof BarChart) {
                    BarChart<String, Number> barChart = (BarChart<String, Number>) barChartBox.getChildren().get(0);
                    addChartToDocument(document, barChart, "Weekly Nutrition Intake", 600, 400);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                addErrorMessage(document, "Error generating meal diet charts: " + e.getMessage());
            } catch (DocumentException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addChartToDocument(Document document, javafx.scene.chart.Chart chart,
                                    String title, int width, int height) throws DocumentException {
        try {
            // Add chart title
            Font chartTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, SECONDARY_COLOR);
            Paragraph chartTitle = new Paragraph(title, chartTitleFont);
            chartTitle.setAlignment(Element.ALIGN_CENTER);
            chartTitle.setSpacingAfter(10f);
            document.add(chartTitle);

            // Convert JavaFX chart to image
            BufferedImage chartImage = SwingFXUtils.fromFXImage(chart.snapshot(null, null), null);
            Image pdfImage = Image.getInstance(chartImage, null);

            // Scale image to fit page width while maintaining aspect ratio
            float scale = Math.min((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin()) / width,
                    (document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin()) / height);

            pdfImage.scaleToFit(width * scale, height * scale);
            pdfImage.setAlignment(Image.MIDDLE | Image.ALIGN_CENTER);

            document.add(pdfImage);
            document.add(Chunk.NEWLINE);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DocumentException(e);
        }
    }
}