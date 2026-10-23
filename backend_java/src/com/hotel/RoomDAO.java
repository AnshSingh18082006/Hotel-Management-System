package com.hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomDAO - Data Access Object for ROOM & ROOM_TYPE
 * Invokes Stored Procedure sp_CheckRoomAvailability via CallableStatement
 */
public class RoomDAO {

    public static class RoomRecord {
        public int roomId;
        public String roomNo;
        public int floor;
        public String typeName;
        public double price;
        public int capacity;

        public RoomRecord(int roomId, String roomNo, int floor, String typeName, double price, int capacity) {
            this.roomId = roomId;
            this.roomNo = roomNo;
            this.floor = floor;
            this.typeName = typeName;
            this.price = price;
            this.capacity = capacity;
        }
    }

    /**
     * Calls Stored Procedure: sp_CheckRoomAvailability(room_type_id, check_in, check_out)
     */
    public List<RoomRecord> checkAvailableRooms(int roomTypeId, Date checkIn, Date checkOut) throws SQLException {
        List<RoomRecord> list = new ArrayList<>();
        String sql = "{CALL sp_CheckRoomAvailability(?, ?, ?)}";

        String typeDesc;
        switch (roomTypeId) {
            case 1: typeDesc = "Single Room"; break;
            case 2: typeDesc = "Double Room"; break;
            case 3: typeDesc = "Executive Room"; break;
            case 4: typeDesc = "Luxury Suite"; break;
            default: typeDesc = "Standard Room"; break;
        }

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setInt(1, roomTypeId);
            cstmt.setDate(2, checkIn);
            cstmt.setDate(3, checkOut);

            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new RoomRecord(
                        rs.getInt("room_id"),
                        rs.getString("room_no"),
                        rs.getInt("floor"),
                        typeDesc,
                        rs.getDouble("price_per_night"),
                        rs.getInt("capacity")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Lists all physical rooms in the hotel catalog.
     */
    public List<RoomRecord> getAllRooms() throws SQLException {
        List<RoomRecord> list = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_no, r.floor, rt.price, rt.capacity, " +
                     "CASE rt.room_type_id " +
                     "    WHEN 1 THEN 'Single Room' " +
                     "    WHEN 2 THEN 'Double Room' " +
                     "    WHEN 3 THEN 'Executive Room' " +
                     "    WHEN 4 THEN 'Luxury Suite' " +
                     "    ELSE 'Standard Room' " +
                     "END AS type_name " +
                     "FROM Room r " +
                     "JOIN Room_Type rt ON r.room_type_id = rt.room_type_id " +
                     "ORDER BY r.floor ASC, r.room_no ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new RoomRecord(
                    rs.getInt("room_id"),
                    rs.getString("room_no"),
                    rs.getInt("floor"),
                    rs.getString("type_name"),
                    rs.getDouble("price"),
                    rs.getInt("capacity")
                ));
            }
        }
        return list;
    }
}
