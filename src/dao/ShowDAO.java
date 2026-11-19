package dao;

import db.DBConnection;
import model.Show;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShowDAO {

    public List<Show> getShowsByMovie(int movieId) {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT show_id, movie_id, show_time, screen_no, price " +
                "FROM shows WHERE movie_id = ? ORDER BY show_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shows.add(new Show(
                            rs.getInt("show_id"),
                            rs.getInt("movie_id"),
                            rs.getTimestamp("show_time"),
                            rs.getInt("screen_no"),
                            rs.getDouble("price")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shows;
    }
}