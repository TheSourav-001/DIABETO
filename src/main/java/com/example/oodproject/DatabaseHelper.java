package com.example.oodproject;

import java.sql.*;
import java.time.LocalDate;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/Ood_Project";
    private static final String USER = "root";  // আপনার ইউজারনেম
    private static final String PASSWORD = "";  // আপনার পাসওয়ার্ড

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static ResultSet getUsersData() {
        try {
            Connection conn = getConnection();
            String query = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 🔹 নতুন মেথড: Additional Info ডাটাবেজে সেভ করার জন্য
    public static void saveAdditionalInfo(int userId, String bp, float sugar, float insulinWeek, float insulinMonth, String recommendations) {
        String query = "INSERT INTO additional_info (user_id, blood_pressure, blood_sugar_level, insulin_per_week, insulin_per_month, recommendations, entry_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE " +
                "blood_pressure = VALUES(blood_pressure), " +
                "blood_sugar_level = VALUES(blood_sugar_level), " +
                "insulin_per_week = VALUES(insulin_per_week), " +
                "insulin_per_month = VALUES(insulin_per_month), " +
                "recommendations = VALUES(recommendations), " +
                "entry_date = NOW()";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, bp);
            stmt.setFloat(3, sugar);
            stmt.setFloat(4, insulinWeek);
            stmt.setFloat(5, insulinMonth);
            stmt.setString(6, recommendations);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 নতুন মেথড: নির্দিষ্ট User-এর Additional Info রিটার্ন করার জন্য
    public static ResultSet getAdditionalInfo(int userId) {
        String query = "SELECT * FROM additional_info WHERE user_id = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ নতুন মেথড: নির্দিষ্ট User-এর সর্বশেষ Additional Info রিটার্ন করার জন্য
    public static ResultSet getLatestAdditionalInfo(int userId) {
        String query = "SELECT * FROM additional_info WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 🔹 নতুন মেথড: BloodSugarTracking ডাটাবেজে সেভ করার জন্য
    public static void saveBloodSugarEntry(int userId, LocalDate entryDate, String entryTime, double beforeMeal, double afterMeal) {
        String query = "INSERT INTO blood_sugar_entries (user_id, entry_date, entry_time, before_meal, after_meal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(entryDate));
            stmt.setString(3, entryTime);
            stmt.setDouble(4, beforeMeal);
            stmt.setDouble(5, afterMeal);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getBloodSugarEntries(int userId) {
        String query = "SELECT * FROM blood_sugar_entries WHERE user_id = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double calculateAverageDaily(int userId, LocalDate date) {
        String query = "SELECT AVG((before_meal + after_meal) / 2.0) FROM blood_sugar_entries WHERE user_id = ? AND entry_date = ?";
        return calculateAggregate(userId, date, query);
    }

    public static double calculateMaxDaily(int userId, LocalDate date) {
        String query = "SELECT MAX(GREATEST(before_meal, after_meal)) FROM blood_sugar_entries WHERE user_id = ? AND entry_date = ?";
        return calculateAggregate(userId, date, query);
    }

    public static double calculateMinDaily(int userId, LocalDate date) {
        String query = "SELECT MIN(LEAST(before_meal, after_meal)) FROM blood_sugar_entries WHERE user_id = ? AND entry_date = ?";
        return calculateAggregate(userId, date, query);
    }

    public static double calculateAverageWeekly(int userId, LocalDate startDate, LocalDate endDate) {
        String query = "SELECT AVG((before_meal + after_meal) / 2.0) FROM blood_sugar_entries WHERE user_id = ? AND entry_date BETWEEN ? AND ?";
        return calculateAggregate(userId, startDate, endDate, query);
    }

    public static double calculateMaxWeekly(int userId, LocalDate startDate, LocalDate endDate) {
        String query = "SELECT MAX(GREATEST(before_meal, after_meal)) FROM blood_sugar_entries WHERE user_id = ? AND entry_date BETWEEN ? AND ?";
        return calculateAggregate(userId, startDate, endDate, query);
    }

    public static double calculateMinWeekly(int userId, LocalDate startDate, LocalDate endDate) {
        String query = "SELECT MIN(LEAST(before_meal, after_meal)) FROM blood_sugar_entries WHERE user_id = ? AND entry_date BETWEEN ? AND ?";
        return calculateAggregate(userId, startDate, endDate, query);
    }

    public static double calculateAverageMonthly(int userId, LocalDate startDate, LocalDate endDate) {
        String query = "SELECT AVG((before_meal + after_meal) / 2.0) FROM blood_sugar_entries WHERE user_id = ? AND entry_date BETWEEN ? AND ?";
        return calculateAggregate(userId, startDate, endDate, query);
    }

    public static double calculateMaxMonthly(int userId, LocalDate startDate, LocalDate endDate) {
        String query = "SELECT MAX(GREATEST(before_meal, after_meal)) FROM blood_sugar_entries WHERE user_id = ? AND entry_date BETWEEN ? AND ?";
        return calculateAggregate(userId, startDate, endDate, query);
    }

    public static double calculateMinMonthly(int userId, LocalDate startDate, LocalDate endDate) {
        String query = "SELECT MIN(LEAST(before_meal, after_meal)) FROM blood_sugar_entries WHERE user_id = ? AND entry_date BETWEEN ? AND ?";
        return calculateAggregate(userId, startDate, endDate, query);
    }

    private static double calculateAggregate(int userId, LocalDate date, String query) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private static double calculateAggregate(int userId, LocalDate startDate, LocalDate endDate, String query) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    //  ✅ নতুন মেথড:  ব্যবহারকারীর নাম পাওয়ার জন্য
    public static String getUserName(int userId) {
        String userName = "Guest"; // ডিফল্ট নাম
        String query = "SELECT full_name FROM users WHERE id = ?";  // full_name ব্যবহার করুন
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userName = rs.getString("full_name");  // full_name ব্যবহার করুন
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userName;
    }

    //  ✅ নতুন মেথড: ব্যবহারকারীর সমস্ত ডেটা পাওয়ার জন্য
    public static ResultSet getUserDetails(int userId) {
        String query = "SELECT * FROM users WHERE id = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}