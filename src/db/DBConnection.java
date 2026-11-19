package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/movie_ticket_db";
    private static final String USER = "postgres";
    private static final String PASS = "r00t";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            System.out.println("Connected to postgreSQL!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
