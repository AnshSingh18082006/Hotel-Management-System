package com.hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AnalyticsDAO - Powers Housekeeping, Cashier, General Manager & DBA Terminals
 * Executes departmental queries, trigger audit trails, and management KPIs.
 */
public class AnalyticsDAO {

    public static class RoomStatusRecord {
        public String roomNo;
        public int floor;
        public String typeName;
        public String status;

        public RoomStatusRecord(String roomNo, int floor, String typeName, String status) {
            this.roomNo = roomNo;
            this.floor = floor;
            this.typeName = typeName;
            this.status = status;
        }
    }

    public static class StaffRecord {
        public int staffId;
        public String name;
        public String deptName;
        public String role;
        public String phoneNo;

        public StaffRecord(int staffId, String name, String deptName, String role, String phoneNo) {
            this.staffId = staffId;
            this.name = name;
            this.deptName = deptName;
            this.role = role;
            this.phoneNo = phoneNo;
        }
    }

    public static class AuditLogRecord {
        public int logId;
        public int paymentId;
        public double amount;
        public String method;
        public Timestamp recordedAt;

        public AuditLogRecord(int logId, int paymentId, double amount, String method, Timestamp recordedAt) {
            this.logId = logId;
            this.paymentId = paymentId;
            this.amount = amount;
            this.method = method;
            this.recordedAt = recordedAt;
        }
    }

    public static class ManagerKpis {
        public int totalBookings;
        public int totalCustomers;
        public double totalAdvance;
        public double totalBilled;
    }

    public static class TriggerRecord {
        public String name;
        public String event;
        public String table;
        public String timing;

        public TriggerRecord(String name, String event, String table, String timing) {
            this.name = name;
            this.event = event;
            this.table = table;
            this.timing = timing;
        }
    }

    /**
     * Housekeeping: Get real-time status of all hotel rooms.
     */
    public List<RoomStatusRecord> getHousekeepingRoomList() throws SQLException {
        List<RoomStatusRecord> list = new ArrayList<>();
        String sql = "SELECT r.room_no, r.floor, " +
                     "CASE rt.room_type_id " +
                     "    WHEN 1 THEN 'Single Room' " +
                     "    WHEN 2 THEN 'Double Room' " +
                     "    WHEN 3 THEN 'Executive Room' " +
                     "    WHEN 4 THEN 'Luxury Suite' " +
                     "    ELSE 'Standard Room' " +
                     "END AS type_name, " +
                     "CASE " +
                     "    WHEN b.booking_id IS NOT NULL THEN 'OCCUPIED (In-Stay)' " +
                     "    ELSE 'CLEAN & VACANT' " +
                     "END AS status " +
                     "FROM Room r " +
                     "JOIN Room_Type rt ON r.room_type_id = rt.room_type_id " +
                     "LEFT JOIN Booking_Room br ON r.room_id = br.room_id " +
                     "LEFT JOIN Booking b ON br.booking_id = b.booking_id " +
                     "     AND CURRENT_DATE BETWEEN b.check_in_date AND b.check_out_date " +
                     "ORDER BY r.floor ASC, r.room_no ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new RoomStatusRecord(
                    rs.getString("room_no"),
                    rs.getInt("floor"),
                    rs.getString("type_name"),
                    rs.getString("status")
                ));
            }
        }
        return list;
    }

    /**
     * Housekeeping: Get staff assigned to Housekeeping department.
     */
    public List<StaffRecord> getHousekeepingStaff() throws SQLException {
        List<StaffRecord> list = new ArrayList<>();
        String sql = "SELECT s.staff_id, s.name, d.name AS dept_name, s.role, s.phone_no " +
                     "FROM Staff s " +
                     "JOIN Department d ON s.department_id = d.department_id " +
                     "WHERE d.name = 'Housekeeping' " +
                     "ORDER BY s.staff_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new StaffRecord(
                    rs.getInt("staff_id"),
                    rs.getString("name"),
                    rs.getString("dept_name"),
                    rs.getString("role"),
                    rs.getString("phone_no")
                ));
            }
        }
        return list;
    }

    /**
     * Cashier: Retrieve Audit Trail generated by trg_AuditPaymentLog.
     */
    public List<AuditLogRecord> getPaymentAuditLogs() throws SQLException {
        List<AuditLogRecord> list = new ArrayList<>();
        String sql = "SELECT log_id, payment_id, amount, payment_method, recorded_at " +
                     "FROM Payment_Audit_Log " +
                     "ORDER BY log_id DESC LIMIT 20";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new AuditLogRecord(
                    rs.getInt("log_id"),
                    rs.getInt("payment_id"),
                    rs.getDouble("amount"),
                    rs.getString("payment_method"),
                    rs.getTimestamp("recorded_at")
                ));
            }
        }
        return list;
    }

    /**
     * Cashier: Retrieve all settled final tax bills.
     */
    public void printSettledBills() throws SQLException {
        String sql = "SELECT bill_id, booking_id, total_amount, generation_date " +
                     "FROM Bill ORDER BY bill_id DESC LIMIT 15";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.printf("\n%-10s %-15s %-18s %-15s\n", 
                "Bill ID", "Booking ID", "Total Amount", "Bill Date");
            System.out.println("------------------------------------------------------------");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("%-10d #%-14d Rs. %-14.2f %-15s\n",
                    rs.getInt("bill_id"),
                    rs.getInt("booking_id"),
                    rs.getDouble("total_amount"),
                    rs.getDate("generation_date")
                );
            }
            if (!hasData) {
                System.out.println("[INFO] No checkout bills generated yet. Checkout a booking via Option [1] to generate.");
            }
        }
    }

    /**
     * General Manager: Executive Financial KPIs.
     */
    public ManagerKpis getExecutiveKPIs() throws SQLException {
        ManagerKpis kpi = new ManagerKpis();
        String sql = "SELECT " +
                     " (SELECT COUNT(*) FROM Booking) AS total_b, " +
                     " (SELECT COUNT(*) FROM Customer) AS total_c, " +
                     " (SELECT COALESCE(SUM(amount), 0.0) FROM Payment) AS total_adv, " +
                     " (SELECT COALESCE(SUM(total_amount), 0.0) FROM Bill) AS total_billed";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                kpi.totalBookings = rs.getInt("total_b");
                kpi.totalCustomers = rs.getInt("total_c");
                kpi.totalAdvance = rs.getDouble("total_adv");
                kpi.totalBilled = rs.getDouble("total_billed");
            }
        }
        return kpi;
    }

    /**
     * General Manager: Call sp_GetCustomerHistory(customer_id).
     */
    public void printCustomerHistory(int customerId) throws SQLException {
        String sql = "{CALL sp_GetCustomerHistory(?)}";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setInt(1, customerId);
            try (ResultSet rs = cstmt.executeQuery()) {
                System.out.printf("\n%-12s %-12s %-12s %-8s %-10s %-12s %-12s %-12s\n",
                    "Booking ID", "Check-In", "Check-Out", "Nights", "Room No", "Room Price", "Billed Amt", "Paid Amt");
                System.out.println("---------------------------------------------------------------------------------------------------------");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-12d %-12s %-12s %-8d %-10s Rs. %-8.2f Rs. %-8.2f Rs. %-8.2f\n",
                        rs.getInt("booking_id"),
                        rs.getDate("check_in_date"),
                        rs.getDate("check_out_date"),
                        rs.getInt("nights"),
                        rs.getString("room_no"),
                        rs.getDouble("room_price"),
                        rs.getDouble("billed_amount"),
                        rs.getDouble("amount_paid")
                    );
                }
                if (!found) {
                    System.out.println("[INFO] No past booking records found for Customer ID #" + customerId);
                }
            }
        }
    }

    /**
     * DBA: Show Active Triggers.
     */
    public List<TriggerRecord> getTriggers() throws SQLException {
        List<TriggerRecord> list = new ArrayList<>();
        String sql = "SELECT TRIGGER_NAME, EVENT_MANIPULATION, EVENT_OBJECT_TABLE, ACTION_TIMING " +
                     "FROM information_schema.TRIGGERS " +
                     "WHERE TRIGGER_SCHEMA = 'hotel_management_db' " +
                     "ORDER BY TRIGGER_NAME";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new TriggerRecord(
                    rs.getString("TRIGGER_NAME"),
                    rs.getString("EVENT_MANIPULATION"),
                    rs.getString("EVENT_OBJECT_TABLE"),
                    rs.getString("ACTION_TIMING")
                ));
            }
        }
        return list;
    }

    /**
     * DBA: Show Table Health & Record Counts.
     */
    public void printTableHealth() throws SQLException {
        String sql = "SELECT TABLE_NAME, TABLE_ROWS " +
                     "FROM information_schema.TABLES " +
                     "WHERE TABLE_SCHEMA = 'hotel_management_db' " +
                     "ORDER BY TABLE_NAME";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.printf("\n%-30s %-15s\n", "Table Name", "Row Count");
            System.out.println("----------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-30s %-15d\n", rs.getString("TABLE_NAME"), rs.getLong("TABLE_ROWS"));
            }
        }
    }
}
