package com.hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingDAO - Handles Reservations, Payment Creation & Room Allocation
 * Enforces ACID Transactions using setAutoCommit(false), commit(), and rollback()
 */
public class BookingDAO {

    public static class BookingResult {
        public boolean success;
        public int bookingId;
        public int paymentId;
        public String errorMessage;

        public BookingResult(boolean success, int bookingId, int paymentId, String errorMessage) {
            this.success = success;
            this.bookingId = bookingId;
            this.paymentId = paymentId;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Executes a Walk-In Room Reservation as an Atomic ACID Transaction.
     */
    public BookingResult allotRoomToGuest(int customerId, int roomId, Date checkIn, Date checkOut, 
                                          double advanceAmount, String paymentMethod) {
        String insertPayment = "INSERT INTO Payment (amount, payment_date, payment_method) VALUES (?, CURDATE(), ?)";
        String insertBooking = "INSERT INTO Booking (customer_id, payment_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?)";
        String insertBookingRoom = "INSERT INTO Booking_Room (booking_id, room_id) VALUES (?, ?)";
        String insertBill = "INSERT INTO Bill (booking_id, bill_id, payment_id, generation_date, total_amount) VALUES (?, ?, ?, CURDATE(), 0.00)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Insert Payment Record
            int paymentId = -1;
            try (PreparedStatement stmtPay = conn.prepareStatement(insertPayment, Statement.RETURN_GENERATED_KEYS)) {
                stmtPay.setDouble(1, advanceAmount);
                stmtPay.setString(2, paymentMethod);
                stmtPay.executeUpdate();
                try (ResultSet rs = stmtPay.getGeneratedKeys()) {
                    if (rs.next()) paymentId = rs.getInt(1);
                }
            }

            // 2. Insert Booking Record (Fires trg_ValidateBookingDates)
            int bookingId = -1;
            try (PreparedStatement stmtBook = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                stmtBook.setInt(1, customerId);
                stmtBook.setInt(2, paymentId);
                stmtBook.setDate(3, checkIn);
                stmtBook.setDate(4, checkOut);
                stmtBook.executeUpdate();
                try (ResultSet rs = stmtBook.getGeneratedKeys()) {
                    if (rs.next()) bookingId = rs.getInt(1);
                }
            }

            // 3. Link Room to Booking (Fires trg_PreventDoubleBooking & trg_ValidateAdvancePaymentCeiling)
            try (PreparedStatement stmtRoom = conn.prepareStatement(insertBookingRoom)) {
                stmtRoom.setInt(1, bookingId);
                stmtRoom.setInt(2, roomId);
                stmtRoom.executeUpdate();
            }

            // 4. Initialize Bill Record
            int billId = 9000 + bookingId; // Distinct bill identifier
            try (PreparedStatement stmtBill = conn.prepareStatement(insertBill)) {
                stmtBill.setInt(1, bookingId);
                stmtBill.setInt(2, billId);
                stmtBill.setInt(3, paymentId);
                stmtBill.executeUpdate();
            }

            // Commit all operations atomically
            conn.commit();
            return new BookingResult(true, bookingId, paymentId, null);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback prevents partial corrupted entries
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return new BookingResult(false, -1, -1, e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Orders an extra amenity for an active booking.
     * Database Trigger `trg_AutoUpdateBillOnService` will automatically increment the bill!
     */
    public boolean orderAmenityService(int bookingId, int serviceId) throws SQLException {
        String sql = "INSERT INTO Booking_Service (booking_id, service_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            stmt.setInt(2, serviceId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static class BookingSummary {
        public int bookingId;
        public String customerName;
        public String contactNo;
        public String roomNo;
        public Date checkIn;
        public Date checkOut;
        public double advancePaid;
        public String status;

        public BookingSummary(int bookingId, String customerName, String contactNo, String roomNo, 
                              Date checkIn, Date checkOut, double advancePaid, String status) {
            this.bookingId = bookingId;
            this.customerName = customerName;
            this.contactNo = contactNo;
            this.roomNo = roomNo;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.advancePaid = advancePaid;
            this.status = status;
        }
    }

    /**
     * Retrieves all bookings with customer details and allotted room.
     */
    public List<BookingSummary> getAllBookings() throws SQLException {
        ensureBookingStatusColumn();
        List<BookingSummary> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, c.name, c.contact_no, COALESCE(r.room_no, 'N/A') AS room_no, " +
                     "b.check_in_date, b.check_out_date, COALESCE(p.amount, 0.0) AS advance_paid, " +
                     "COALESCE(b.booking_status, 'CONFIRMED') AS booking_status " +
                     "FROM Booking b " +
                     "JOIN Customer c ON b.customer_id = c.customer_id " +
                     "LEFT JOIN Booking_Room br ON b.booking_id = br.booking_id " +
                     "LEFT JOIN Room r ON br.room_id = r.room_id " +
                     "LEFT JOIN Payment p ON b.payment_id = p.payment_id " +
                     "ORDER BY b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new BookingSummary(
                    rs.getInt("booking_id"),
                    rs.getString("name"),
                    rs.getString("contact_no"),
                    rs.getString("room_no"),
                    rs.getDate("check_in_date"),
                    rs.getDate("check_out_date"),
                    rs.getDouble("advance_paid"),
                    rs.getString("booking_status")
                ));
            }
        }
        return list;
    }

    public static class CancellationPreview {
        public int bookingId;
        public String guestName;
        public String contactNo;
        public String roomNo;
        public Date checkIn;
        public Date checkOut;
        public double advancePaid;
        public int daysUntilCheckIn;
        public double refundRate;
        public double refundAmount;
        public double cancellationFee;
        public String policyName;
        public String status;
    }

    private void ensureBookingStatusColumn() {
        String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                          "WHERE TABLE_SCHEMA = 'hotel_management_db' AND TABLE_NAME = 'Booking' AND COLUMN_NAME = 'booking_status'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (Statement alterStmt = conn.createStatement()) {
                    alterStmt.executeUpdate("ALTER TABLE Booking ADD COLUMN booking_status VARCHAR(20) DEFAULT 'CONFIRMED'");
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Previews cancellation terms, refund percentage, and fee breakdown based on check-in proximity.
     */
    public CancellationPreview getCancellationPreview(int bookingId) throws SQLException {
        ensureBookingStatusColumn();
        String sql = "SELECT b.booking_id, c.name, c.contact_no, COALESCE(r.room_no, 'N/A') AS room_no, " +
                     "b.check_in_date, b.check_out_date, COALESCE(p.amount, 0.0) AS advance_paid, " +
                     "DATEDIFF(b.check_in_date, CURRENT_DATE) AS days_to_in, " +
                     "COALESCE(b.booking_status, 'CONFIRMED') AS booking_status " +
                     "FROM Booking b " +
                     "JOIN Customer c ON b.customer_id = c.customer_id " +
                     "LEFT JOIN Booking_Room br ON b.booking_id = br.booking_id " +
                     "LEFT JOIN Room r ON br.room_id = r.room_id " +
                     "LEFT JOIN Payment p ON b.payment_id = p.payment_id " +
                     "WHERE b.booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CancellationPreview cp = new CancellationPreview();
                    cp.bookingId = rs.getInt("booking_id");
                    cp.guestName = rs.getString("name");
                    cp.contactNo = rs.getString("contact_no");
                    cp.roomNo = rs.getString("room_no");
                    cp.checkIn = rs.getDate("check_in_date");
                    cp.checkOut = rs.getDate("check_out_date");
                    cp.advancePaid = rs.getDouble("advance_paid");
                    cp.daysUntilCheckIn = rs.getInt("days_to_in");
                    cp.status = rs.getString("booking_status");

                    if ("CANCELLED".equalsIgnoreCase(cp.status)) {
                        cp.policyName = "ALREADY CANCELLED";
                        cp.refundRate = 0.0;
                        cp.refundAmount = 0.0;
                        cp.cancellationFee = 0.0;
                    } else if (cp.daysUntilCheckIn >= 2) {
                        cp.policyName = "Standard Advance Cancellation (> 48 hrs): 80% Refund";
                        cp.refundRate = 0.80;
                        cp.refundAmount = Math.round(cp.advancePaid * 0.80 * 100.0) / 100.0;
                        cp.cancellationFee = Math.round((cp.advancePaid - cp.refundAmount) * 100.0) / 100.0;
                    } else if (cp.daysUntilCheckIn >= 0) {
                        cp.policyName = "Late Cancellation (< 48 hrs): 50% Refund";
                        cp.refundRate = 0.50;
                        cp.refundAmount = Math.round(cp.advancePaid * 0.50 * 100.0) / 100.0;
                        cp.cancellationFee = Math.round((cp.advancePaid - cp.refundAmount) * 100.0) / 100.0;
                    } else {
                        cp.policyName = "Past Check-in / No-Show: No Refund (100% Fee)";
                        cp.refundRate = 0.0;
                        cp.refundAmount = 0.0;
                        cp.cancellationFee = cp.advancePaid;
                    }
                    return cp;
                }
            }
        }
        return null;
    }

    /**
     * Executes atomic cancellation transaction: marks status CANCELLED, frees room, and logs refund payment.
     */
    public boolean executeCancellation(int bookingId, double refundAmount, String refundMethod) {
        ensureBookingStatusColumn();
        String updateBooking = "UPDATE Booking SET booking_status = 'CANCELLED' WHERE booking_id = ?";
        String releaseRoom = "DELETE FROM Booking_Room WHERE booking_id = ?";
        String insertRefund = "INSERT INTO Payment (amount, payment_date, payment_method) VALUES (?, CURRENT_DATE, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(updateBooking)) {
                stmt.setInt(1, bookingId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(releaseRoom)) {
                stmt.setInt(1, bookingId);
                stmt.executeUpdate();
            }

            if (refundAmount > 0) {
                try (PreparedStatement stmt = conn.prepareStatement(insertRefund)) {
                    stmt.setDouble(1, refundAmount);
                    stmt.setString(2, "REFUND_" + refundMethod.toUpperCase());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            System.out.println("[DB ROLLBACK] " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Adds an occupant's government ID to Booking_Guest.
     * Database Trigger `trg_EnforceRoomCapacity` ensures the legal room limit is respected.
     */
    public boolean addCoOccupant(int bookingId, String fullName, String idType, String idNumber, boolean isPrimary) throws SQLException {
        String sql = "INSERT INTO Booking_Guest (booking_id, full_name, id_proof_type, id_number, is_primary) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            stmt.setString(2, fullName);
            stmt.setString(3, idType);
            stmt.setString(4, idNumber);
            stmt.setBoolean(5, isPrimary);
            return stmt.executeUpdate() > 0;
        }
    }
}
