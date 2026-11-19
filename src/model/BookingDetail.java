package model;

import java.sql.Timestamp;

public class BookingDetail {
    private int bookingId;
    private String movieTitle;
    private Timestamp showTime;
    private int screenNo;
    private String seatNo;
    private String customerName;
    private double price;

    public BookingDetail(int bookingId, String movieTitle, Timestamp showTime,
                         int screenNo, String seatNo, String customerName, double price) {
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.showTime = showTime;
        this.screenNo = screenNo;
        this.seatNo = seatNo;
        this.customerName = customerName;
        this.price = price;
    }

    public int getBookingId() { return bookingId; }
    public String getMovieTitle() { return movieTitle; }
    public Timestamp getShowTime() { return showTime; }
    public int getScreenNo() { return screenNo; }
    public String getSeatNo() { return seatNo; }
    public String getCustomerName() { return customerName; }
    public double getPrice() { return price; }
}