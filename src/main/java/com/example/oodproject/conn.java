package com.example.oodproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conn {
    private static Connection connection = null;

    static {
        // Load the JDBC driver when the class is loaded
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load MySQL JDBC driver", e);
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String url = "jdbc:mysql://localhost:3306/Ood_Project";
                String username = "root";  // your database username
                String password = "";    // your database password

                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Database connection established successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to establish database connection", e);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to close database connection", e);
        } finally {
            connection = null; // Ensure connection is marked as null after closing
        }
    }
}