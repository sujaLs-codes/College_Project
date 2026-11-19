package dao;

import db.DBConnection;

import util.SeatMapFile;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.BookingDetail;

public class BookingDAO {

    // Seats already taken for a show
    public List<String> getBookedSeats(int showId) {
        List<String> seats = new ArrayList<>();
        String sql = "SELECT seat_no FROM bookings WHERE show_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(rs.getString("seat_no"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    // Returns true if booked, false if the seat was already taken
    public boolean bookSeat(int showId, String seatNo, String customerName, double price) {
        String sql = "INSERT INTO bookings (show_id, seat_no, customer_name, price) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, showId);
            ps.setString(2, seatNo);
            ps.setString(3, customerName);
            ps.setDouble(4, price);
            ps.executeUpdate();
            SeatMapFile.write(showId, getBookedSeats(showId));
            return true;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return false; // seat already booked
            }
            e.printStackTrace();
            return false;
        }
    }

    public List<BookingDetail> getBookingsByCustomer(String name) {
        List<BookingDetail> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, m.title, s.show_time, s.screen_no, " +
                "b.seat_no, b.customer_name, COALESCE(b.price, 0) AS price " +
                "FROM bookings b " +
                "JOIN shows s ON b.show_id = s.show_id " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "WHERE b.customer_name ILIKE ? " +
                "ORDER BY s.show_time, b.seat_no";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookingDetail(
                            rs.getInt("booking_id"),
                            rs.getString("title"),
                            rs.getTimestamp("show_time"),
                            rs.getInt("screen_no"),
                            rs.getString("seat_no"),
                            rs.getString("customer_name"),
                            rs.getDouble("price")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean cancelBooking(int bookingId) {
        String sql = "DELETE FROM bookings WHERE booking_id = ? RETURNING show_id";
        int showId = -1;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) showId = rs.getInt("show_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if (showId == -1) return false;
        SeatMapFile.write(showId, getBookedSeats(showId));   // update file after cancel
        return true;
    }
}