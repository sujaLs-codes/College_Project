package ui;

import dao.BookingDAO;
import model.BookingDetail;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MyBookingsFrame extends JFrame {

    private final BookingDAO bookingDao = new BookingDAO();
    private final JTextField searchField = new JTextField(18);
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel totalLabel = new JLabel("Total: Rs.0.0");
    private List<BookingDetail> bookings = new ArrayList<>();

    public MyBookingsFrame() {
        setTitle("My Bookings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top: search
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Customer name:"));
        top.add(searchField);
        JButton searchBtn = new JButton("Search");
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        // Center: table
        model = new DefaultTableModel(
                new String[]{"Booking ID", "Movie", "Show Time", "Screen", "Seat", "Customer", "Price (Rs.)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom: total + cancel
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottom.add(totalLabel, BorderLayout.WEST);
        JButton cancelBtn = new JButton("Cancel Selected Booking(s)");
        bottom.add(cancelBtn, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> search());
        searchField.addActionListener(e -> search()); // Enter key
        cancelBtn.addActionListener(e -> cancelSelected());

        table.getColumnModel().getColumn(2).setPreferredWidth(180);
    }

    private void search() {
        String name = searchField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a customer name.");
            return;
        }
        bookings = bookingDao.getBookingsByCustomer(name);
        refreshTable();
        if (bookings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bookings found for \"" + name + "\".");
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        double total = 0;
        for (BookingDetail b : bookings) {
            model.addRow(new Object[]{
                    b.getBookingId(), b.getMovieTitle(), b.getShowTime(),
                    b.getScreenNo(), b.getSeatNo(), b.getCustomerName(), b.getPrice()});
            total += b.getPrice();
        }
        totalLabel.setText("Total: Rs." + total);
    }

    private void cancelSelected() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least one booking to cancel.");
            return;
        }

        // Rule: a show that has already started cannot be cancelled
        Timestamp now = new Timestamp(System.currentTimeMillis());
        for (int r : rows) {
            if (bookings.get(r).getShowTime().before(now)) {
                JOptionPane.showMessageDialog(this,
                        "Seat " + bookings.get(r).getSeatNo()
                                + " is for a show that has already started and cannot be cancelled.");
                return;
            }
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel " + rows.length + " booking(s)?", "Confirm Cancel",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        int cancelled = 0;
        for (int r : rows) {
            if (bookingDao.cancelBooking(bookings.get(r).getBookingId())) {
                cancelled++;
            }
        }
        JOptionPane.showMessageDialog(this, cancelled + " booking(s) cancelled.");
        search(); // reload the list
    }
}