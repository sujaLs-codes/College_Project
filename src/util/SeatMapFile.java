package util;

import java.io.*;
import java.util.List;

public class SeatMapFile {

    private static final String DIR = "seatmaps";
    private static final String[] ROWS = {"A", "B", "C", "D", "E"};
    private static final int SEATS_PER_ROW = 8;

    // Writes the seat map of a show to seatmaps/show_<id>.txt
    public static void write(int showId, List<String> bookedSeats) {
        File dir = new File(DIR);
        dir.mkdirs();
        File file = new File(dir, "show_" + showId + ".txt");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("SEAT MAP - SHOW " + showId);
            bw.newLine();
            bw.write("[X] = booked    [ ] = free");
            bw.newLine();
            bw.newLine();

            for (String row : ROWS) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= SEATS_PER_ROW; i++) {
                    String seat = row + i;
                    sb.append(seat).append(bookedSeats.contains(seat) ? "[X] " : "[ ] ");
                }
                bw.write(sb.toString().trim());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reads the seat map file back as text
    public static String read(int showId) {
        File file = new File(DIR, "show_" + showId + ".txt");
        if (!file.exists()) {
            return "No seat map file for this show yet.";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Could not read the seat map file.";
        }
        return sb.toString();
    }
}