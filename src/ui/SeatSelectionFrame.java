package ui;

import dao.BookingDAO;
import model.Show;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import ui.PaymentDialog;   // not needed, same package; skip this line
import util.ReceiptWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class SeatSelectionFrame extends JFrame {

    private static final String[] ROWS = {"A", "B", "C", "D", "E"};
    private static final int SEATS_PER_ROW = 8;

    private final BookingDAO bookingDao = new BookingDAO();
    private final Show show;
    private final List<String> selectedSeats = new ArrayList<>();
    private final JLabel summaryLabel = new JLabel("No seats selected");
    private final JTextField nameField = new JTextField(15);
    private final String movieTitle;

    public SeatSelectionFrame(Show show, String movieTitle) {
        this.show = show;
        this.movieTitle = movieTitle;
        setTitle("Select Seats - Show " + show.getShowId());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel screenLabel = new JLabel("SCREEN THIS WAY   |   A-B: Rs.150   C-D: Rs.200   E: Rs.300", SwingConstants.CENTER);
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.LIGHT_GRAY);
        screenLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(screenLabel, BorderLayout.NORTH);

        add(buildSeatGrid(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildSeatGrid() {
        JPanel grid = new JPanel(new GridLayout(ROWS.length, SEATS_PER_ROW, 8, 8));
        grid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<String> booked = bookingDao.getBookedSeats(show.getShowId());

        for (String row : ROWS) {
            for (int i = 1; i <= SEATS_PER_ROW; i++) {
                String seatNo = row + i;
                JToggleButton btn = new JToggleButton(
                        "<html><center>" + seatNo + "<br><small>Rs." + (int) getSeatPrice(seatNo) + "</small></center></html>");

                if (booked.contains(seatNo)) {
                    btn.setEnabled(false);
                    btn.setBackground(Color.RED);
                    btn.setOpaque(true);
                } else {
                    btn.addActionListener(e -> {
                        if (btn.isSelected()) {
                            selectedSeats.add(seatNo);
                            btn.setBackground(Color.GREEN);
                            btn.setOpaque(true);
                        } else {
                            selectedSeats.remove(seatNo);
                            btn.setBackground(null);
                        }
                        updateSummary();
                    });
                }
                grid.add(btn);
            }
        }
        return grid;
    }

    //Price depends on the row letter: A,B = 150 | C,D = 200 | E = 300
    private double getSeatPrice(String seatNo) {
        char row = seatNo.charAt(0);
        if (row == 'A' || row == 'B') return 150;
        if (row == 'C' || row == 'D') return 200;
        return 300; // row E
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));

        panel.add(summaryLabel);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Your name:"));
        namePanel.add(nameField);
        panel.add(namePanel);

        JButton confirmBtn = new JButton("Confirm Booking");
        confirmBtn.addActionListener(e -> confirmBooking());
        panel.add(confirmBtn);

        return panel;
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            summaryLabel.setText("No seats selected");
        } else {
            summaryLabel.setText("Seats: " + selectedSeats + "  |  Total: Rs." + calculateTotal());
        }
    }

    private double calculateTotal() {
        double total = 0;
        for (String seat : selectedSeats) {
            total += getSeatPrice(seat);
        }
        return total;
    }

    private void confirmBooking() {
        String name = nameField.getText().trim();

        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one seat.");
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name.");
            return;
        }

        // Re-check availability just before payment
        List<String> nowBooked = bookingDao.getBookedSeats(show.getShowId());
        List<String> clash = new ArrayList<>();
        for (String s : selectedSeats) {
            if (nowBooked.contains(s)) clash.add(s);
        }
        if (!clash.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "These seats were just taken by someone else: " + clash + "\nPlease reopen and choose again.");
            dispose();
            return;
        }

        // Mock payment
        PaymentDialog payment = new PaymentDialog(this, calculateTotal());
        payment.setVisible(true);          // blocks until the dialog closes
        String method = payment.getPaidMethod();
        if (method == null) {
            JOptionPane.showMessageDialog(this, "Payment cancelled. No seats were booked.");
            return;
        }

        // Save the bookings (only after successful payment)
        Map<String, Double> booked = new LinkedHashMap<>();
        List<String> failed = new ArrayList<>();
        double paidTotal = 0;
        for (String seat : selectedSeats) {
            double price = getSeatPrice(seat);
            if (bookingDao.bookSeat(show.getShowId(), seat, name, price)) {
                booked.put(seat, price);
                paidTotal += price;
            } else {
                failed.add(seat);
            }
        }

        if (booked.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Booking failed. Please try again.");
            dispose();
            return;
        }

        // Receipt: show on screen and save to a file
        String receiptId = ReceiptWriter.newReceiptId();
        String text = ReceiptWriter.buildReceipt(receiptId, name, movieTitle,
                show.getShowTime(), show.getScreenNo(), booked, paidTotal, method);
        String path = ReceiptWriter.save(receiptId, text);

        JTextArea area = new JTextArea(text);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, area, "Booking Confirmed", JOptionPane.PLAIN_MESSAGE);

        if (!failed.isEmpty()) {
            JOptionPane.showMessageDialog(this, "These seats could not be booked: " + failed);
        }
        if (path != null) {
            System.out.println("Receipt saved: " + path);
        }
        dispose();
    }
}