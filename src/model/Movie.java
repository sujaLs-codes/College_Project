package model;

public class Movie {
    private int movieId;
    private String title;
    private String genre;
    private int duration;
    private String language;

    public Movie(int movieId, String title, String genre, int duration, String language) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.language = language;
    }

    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public String getLanguage() { return language; }

    @Override
    public String toString() {
        return movieId + " | " + title + " | " + genre + " | " + duration + " min | " + language;
    }
}