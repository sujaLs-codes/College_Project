package model;

import java.sql.Timestamp;

public class Show {
    private int showId;
    private int movieId;
    private Timestamp showTime;
    private int screenNo;
    private double price;

    public Show(int showId, int movieId, Timestamp showTime, int screenNo, double price) {
        this.showId = showId;
        this.movieId = movieId;
        this.showTime = showTime;
        this.screenNo = screenNo;
        this.price = price;
    }

    public int getShowId() {
        return showId;
    }
    public int getMovieId() {
        return movieId;
    }
    public Timestamp getShowTime() {
        return showTime;
    }
    public int getScreenNo() {
        return screenNo;
    }
    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Show " + showId + " | Screen " + screenNo + " | " + showTime + " | Rs." + price;
    }
}
