package com.hotel;

import java.sql.*;

/**
 * BillingDAO - Invoice Generation & Payment Settlement
 * Invokes Stored Procedure sp_CalculateFinalBill and enriches with Guest & Room details.
 */
public class BillingDAO {

    public static class InvoiceSummary {
        public int bookingId;
        public String customerName;
        public String contactNo;
        public String email;
        public String roomNo;
        public int floor;
        public String roomType;
        public Date checkIn;
        public Date checkOut;
        public int nights;

        public double roomSubtotal;
        public double servicesSubtotal;
        public double finalTotal;
        public double advancePaid;
        public double balancePayable;
        public String status;

        public InvoiceSummary(int bookingId, String customerName, String contactNo, String email,
                              String roomNo, int floor, String roomType, Date checkIn, Date checkOut, int nights,
                              double roomSubtotal, double servicesSubtotal, double finalTotal, 
                              double advancePaid, double balancePayable, String status) {
            this.bookingId = bookingId;
            this.customerName = customerName;
            this.contactNo = contactNo;
            this.email = email;
            this.roomNo = roomNo;
            this.floor = floor;
            this.roomType = roomType;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.nights = nights;

            this.roomSubtotal = roomSubtotal;
            this.servicesSubtotal = servicesSubtotal;
            this.finalTotal = finalTotal;
            this.advancePaid = advancePaid;
            this.balancePayable = balancePayable;
            this.status = status;
        }
    }

    /**
     * Executes Stored Procedure: sp_CalculateFinalBill(booking_id) and attaches Guest & Room metadata.
     */
    public InvoiceSummary generateFinalBill(int bookingId) throws SQLException {
        String guestSql = "SELECT c.name, c.contact_no, c.email, " +
                          "COALESCE(r.room_no, 'N/A') AS room_no, " +
                          "COALESCE(r.floor, 0) AS floor, " +
                          "CASE rt.room_type_id " +
                          "    WHEN 1 THEN 'Single Room' " +
                          "    WHEN 2 THEN 'Double Room' " +
                          "    WHEN 3 THEN 'Executive Room' " +
                          "    WHEN 4 THEN 'Luxury Suite' " +
                          "    ELSE 'Standard Room' " +
                          "END AS room_type, " +
                          "b.check_in_date, b.check_out_date, " +
                          "DATEDIFF(b.check_out_date, b.check_in_date) AS nights " +
                          "FROM Booking b " +
                          "JOIN Customer c ON b.customer_id = c.customer_id " +
                          "LEFT JOIN Booking_Room br ON b.booking_id = br.booking_id " +
                          "LEFT JOIN Room r ON br.room_id = r.room_id " +
                          "LEFT JOIN Room_Type rt ON r.room_type_id = rt.room_type_id " +
                          "WHERE b.booking_id = ?";

        String procSql = "{CALL sp_CalculateFinalBill(?)}";

        try (Connection conn = DBConnection.getConnection()) {
            String custName = "Guest";
            String contact = "N/A";
            String email = "N/A";
            String roomNo = "N/A";
            int floor = 0;
            String roomType = "Standard Room";
            Date inDate = null;
            Date outDate = null;
            int nights = 1;

            try (PreparedStatement pstmt = conn.prepareStatement(guestSql)) {
                pstmt.setInt(1, bookingId);
                try (ResultSet rsGuest = pstmt.executeQuery()) {
                    if (rsGuest.next()) {
                        custName = rsGuest.getString("name");
                        contact = rsGuest.getString("contact_no");
                        email = rsGuest.getString("email");
                        roomNo = rsGuest.getString("room_no");
                        floor = rsGuest.getInt("floor");
                        roomType = rsGuest.getString("room_type");
                        inDate = rsGuest.getDate("check_in_date");
                        outDate = rsGuest.getDate("check_out_date");
                        nights = rsGuest.getInt("nights");
                    } else {
                        return null; // Booking does not exist
                    }
                }
            }

            try (CallableStatement cstmt = conn.prepareCall(procSql)) {
                cstmt.setInt(1, bookingId);

                try (ResultSet rs = cstmt.executeQuery()) {
                    if (rs.next()) {
                        return new InvoiceSummary(
                            rs.getInt("booking_id"),
                            custName,
                            contact,
                            email,
                            roomNo,
                            floor,
                            roomType,
                            inDate,
                            outDate,
                            nights,
                            rs.getDouble("room_subtotal"),
                            rs.getDouble("services_subtotal"),
                            rs.getDouble("final_total_amount"),
                            rs.getDouble("advance_payment_received"),
                            rs.getDouble("net_balance_payable"),
                            rs.getString("settlement_status")
                        );
                    }
                }
            }
        }
        return null;
    }
}
