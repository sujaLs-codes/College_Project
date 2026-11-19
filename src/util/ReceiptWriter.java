package util;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ReceiptWriter {

    private static final String DIR = "receipts";

    public static String newReceiptId() {
        return "RCPT" + new SimpleDateFormat("yyMMddHHmmss").format(new Date());
    }

    public static String buildReceipt(String receiptId, String customer, String movieTitle,
                                      Timestamp showTime, int screenNo,
                                      Map<String, Double> seatPrices, double total, String method) {
        String line = "=".repeat(38);
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        StringBuilder sb = new StringBuilder();
        sb.append(line).append("\n");
        sb.append("        MOVIE TICKET RECEIPT\n");
        sb.append(line).append("\n");
        sb.append("Receipt ID : ").append(receiptId).append("\n");
        sb.append("Booked on  : ").append(fmt.format(new Date())).append("\n");
        sb.append("Customer   : ").append(customer).append("\n");
        sb.append("Movie      : ").append(movieTitle).append("\n");
        sb.append("Show time  : ").append(fmt.format(showTime)).append("\n");
        sb.append("Screen     : ").append(screenNo).append("\n");
        sb.append("-".repeat(38)).append("\n");
        for (Map.Entry<String, Double> e : seatPrices.entrySet()) {
            sb.append(String.format("  Seat %-5s        Rs.%8.2f%n", e.getKey(), e.getValue()));
        }
        sb.append("-".repeat(38)).append("\n");
        sb.append(String.format("  TOTAL PAID        Rs.%8.2f%n", total));
        sb.append("Payment    : ").append(method).append("\n");
        sb.append(line).append("\n");
        sb.append("     Thank you! Enjoy the movie.\n");
        return sb.toString();
    }

    // Saves the receipt to receipts/<id>.txt and returns the file path
    public static String save(String receiptId, String text) {
        File dir = new File(DIR);
        dir.mkdirs();
        File file = new File(dir, receiptId + ".txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(text);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file.getAbsolutePath();
    }
}