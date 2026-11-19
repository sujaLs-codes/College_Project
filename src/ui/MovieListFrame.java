package ui;

import dao.BookingDAO;
import dao.MovieDAO;
import dao.ShowDAO;
import model.Movie;
import model.Show;
import util.SeatMapFile;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MovieListFrame extends JFrame {

    private final MovieDAO movieDao = new MovieDAO();
    private final ShowDAO showDao = new ShowDAO();

    private final DefaultTableModel movieModel;
    private final DefaultTableModel showModel;
    private final JTable movieTable;
    private final JTable showTable;
    private List<Movie> movies;
    private List<Show> shows;

    public MovieListFrame() {
        setTitle("Movie Ticket Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(750, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("Now Showing", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        // Movies table
        movieModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Genre", "Duration (min)", "Language"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        movieTable = new JTable(movieModel);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Shows table
        showModel = new DefaultTableModel(
                new String[]{"Show ID", "Screen", "Show Time", "Price (Rs.)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        showTable = new JTable(showModel);
        showTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new GridLayout(2, 1, 5, 5));
        JScrollPane moviePane = new JScrollPane(movieTable);
        moviePane.setBorder(BorderFactory.createTitledBorder("Movies"));
        JScrollPane showPane = new JScrollPane(showTable);
        showPane.setBorder(BorderFactory.createTitledBorder("Shows (select a movie)"));
        center.add(moviePane);
        center.add(showPane);
        add(center, BorderLayout.CENTER);

        // Bottom buttons (each created once)
        JButton bookBtn = new JButton("Select Seats");
        JButton myBookingsBtn = new JButton("My Bookings");
        JButton mapBtn = new JButton("View Seat Map File");

        JPanel bottom = new JPanel();
        bottom.add(bookBtn);
        bottom.add(myBookingsBtn);
        bottom.add(mapBtn);
        add(bottom, BorderLayout.SOUTH);

        // When a movie row is clicked, load its shows
        movieTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadShows();
        });

        // Select Seats
        bookBtn.addActionListener(e -> {
            int row = showTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a show first.");
                return;
            }
            Show s = shows.get(row);
            if (s.getShowTime().before(new java.sql.Timestamp(System.currentTimeMillis()))) {
                JOptionPane.showMessageDialog(this, "This show has already started. Please pick another show.");
                return;
            }
            String title = movies.get(movieTable.getSelectedRow()).getTitle();
            new SeatSelectionFrame(s, title).setVisible(true);
        });

        // My Bookings (this listener was missing before)
        myBookingsBtn.addActionListener(e -> new MyBookingsFrame().setVisible(true));

        // View Seat Map File
        mapBtn.addActionListener(e -> {
            int row = showTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a show first.");
                return;
            }
            int showId = shows.get(row).getShowId();
            SeatMapFile.write(showId, new BookingDAO().getBookedSeats(showId)); // sync first
            JTextArea area = new JTextArea(SeatMapFile.read(showId));
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, area, "Seat map - Show " + showId, JOptionPane.PLAIN_MESSAGE);
        });

        loadMovies();
    }

    private void loadMovies() {
        movieModel.setRowCount(0);
        movies = movieDao.getAllMovies();
        for (Movie m : movies) {
            movieModel.addRow(new Object[]{
                    m.getMovieId(), m.getTitle(), m.getGenre(), m.getDuration(), m.getLanguage()});
        }
    }

    private void loadShows() {
        showModel.setRowCount(0);
        int row = movieTable.getSelectedRow();
        if (row < 0) return;
        shows = showDao.getShowsByMovie(movies.get(row).getMovieId());
        for (Show s : shows) {
            showModel.addRow(new Object[]{
                    s.getShowId(), s.getScreenNo(), s.getShowTime(), s.getPrice()});
        }
    }
}