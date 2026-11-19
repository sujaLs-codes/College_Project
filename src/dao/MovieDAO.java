package dao;

import db.DBConnection;
import model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT movie_id, title, genre, duration, language FROM movies ORDER BY movie_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movies.add(new Movie(
                        rs.getInt("movie_id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getString("language")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
}