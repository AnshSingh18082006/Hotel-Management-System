package com.hotel;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection - Database Connection Factory
 * Manages JDBC connection pooling and credentials for hotel_management_db.
 */
public class DBConnection {

    private static String url = "jdbc:mysql://localhost:3306/hotel_management_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "root"; // Default empty, can be set via setPassword() or db.properties

    static {
        // Attempt to load from db.properties if present
        try {
            File propFile = new File("db.properties");
            if (propFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(propFile)) {
                    props.load(fis);
                    if (props.containsKey("db.url")) url = props.getProperty("db.url");
                    if (props.containsKey("db.user")) user = props.getProperty("db.user");
                    if (props.containsKey("db.password")) password = props.getProperty("db.password");
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void setCredentials(String username, String pass) {
        user = username;
        password = pass;
    }

    public static void saveCredentialsToFile(String username, String pass) {
        setCredentials(username, pass);
        try {
            File propFile = new File("db.properties");
            Properties props = new Properties();
            if (propFile.exists()) {
                try (FileInputStream fis = new FileInputStream(propFile)) {
                    props.load(fis);
                }
            }
            props.setProperty("db.url", url);
            props.setProperty("db.user", username);
            props.setProperty("db.password", pass);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(propFile)) {
                props.store(fos, "Saved Hotel Database Configuration");
            }
        } catch (Exception e) {
            System.out.println("[WARN] Could not persist password to db.properties: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found! Ensure mysql-connector-j.jar is in classpath.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
