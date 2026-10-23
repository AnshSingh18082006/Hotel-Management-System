package com.hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerDAO - Data Access Object for CUSTOMER & Customer_Alternate_No tables
 */
public class CustomerDAO {

    public static class CustomerRecord {
        public int id;
        public String name;
        public String contactNo;
        public String email;
        public String alternateNo;

        public CustomerRecord(int id, String name, String contactNo, String email, String alternateNo) {
            this.id = id;
            this.name = name;
            this.contactNo = contactNo;
            this.email = email;
            this.alternateNo = alternateNo;
        }
    }

    /**
     * Registers a new customer and returns the generated customer_id.
     */
    public int registerCustomer(String name, String contactNo, String email, String alternateNo) throws SQLException {
        String insertCustomer = "INSERT INTO Customer (name, contact_no, email) VALUES (?, ?, ?)";
        String insertAlt = "INSERT INTO Customer_Alternate_No (customer_id, alternate_no) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtCust = conn.prepareStatement(insertCustomer, Statement.RETURN_GENERATED_KEYS)) {
                stmtCust.setString(1, name);
                stmtCust.setString(2, contactNo);
                stmtCust.setString(3, email);
                stmtCust.executeUpdate();

                int customerId = -1;
                try (ResultSet rs = stmtCust.getGeneratedKeys()) {
                    if (rs.next()) {
                        customerId = rs.getInt(1);
                    }
                }

                if (customerId > 0 && alternateNo != null && !alternateNo.trim().isEmpty()) {
                    try (PreparedStatement stmtAlt = conn.prepareStatement(insertAlt)) {
                        stmtAlt.setInt(1, customerId);
                        stmtAlt.setString(2, alternateNo.trim());
                        stmtAlt.executeUpdate();
                    }
                }

                conn.commit();
                return customerId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves all registered customers with primary and alternate contact numbers.
     */
    public List<CustomerRecord> getAllCustomers() throws SQLException {
        List<CustomerRecord> list = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.name, c.contact_no, c.email, " +
                     "COALESCE(GROUP_CONCAT(a.alternate_no SEPARATOR ', '), 'None') AS alternate_numbers " +
                     "FROM Customer c " +
                     "LEFT JOIN Customer_Alternate_No a ON c.customer_id = a.customer_id " +
                     "GROUP BY c.customer_id, c.name, c.contact_no, c.email " +
                     "ORDER BY c.customer_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new CustomerRecord(
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("contact_no"),
                    rs.getString("email"),
                    rs.getString("alternate_numbers")
                ));
            }
        }
        return list;
    }
}
