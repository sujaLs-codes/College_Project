package ui;

import javax.swing.*;
import java.awt.*;

public class PaymentDialog extends JDialog {

    private String paidMethod = null; // stays null if the user cancels

    private final JComboBox<String> methodBox = new JComboBox<>(new String[]{"UPI", "Card", "Cash"});
    private final JLabel detailLabel = new JLabel("UPI ID (name@bank):");
    private final JTextField detailField = new JTextField(18);
    private final JLabel cvvLabel = new JLabel("CVV:");
    private final JPasswordField cvvField = new JPasswordField(4);

    public PaymentDialog(Frame owner, double amount) {
        super(owner, "Payment", true); // true = modal
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel amountLabel = new JLabel("Rs." + amount);
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        form.add(new JLabel("Amount to pay:")); form.add(amountLabel);
        form.add(new JLabel("Payment method:")); form.add(methodBox);
        form.add(detailLabel);                   form.add(detailField);
        form.add(cvvLabel);                      form.add(cvvField);
        cvvLabel.setVisible(false);
        cvvField.setVisible(false);

        JButton payBtn = new JButton("Pay Rs." + amount);
        JButton cancelBtn = new JButton("Cancel");
        JPanel buttons = new JPanel();
        buttons.add(payBtn);
        buttons.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        methodBox.addActionListener(e -> updateFields());
        payBtn.addActionListener(e -> pay());
        cancelBtn.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    private void updateFields() {
        String method = (String) methodBox.getSelectedItem();
        boolean card = "Card".equals(method);
        boolean cash = "Cash".equals(method);

        detailLabel.setText(card ? "Card number (16 digits):" : "UPI ID (name@bank):");
        detailField.setEnabled(!cash);
        detailLabel.setEnabled(!cash);
        cvvLabel.setVisible(card);
        cvvField.setVisible(card);
        revalidate();
        repaint();
    }

    private void pay() {
        String method = (String) methodBox.getSelectedItem();
        String detail = detailField.getText().trim();

        switch (method) {
            case "UPI" -> {
                if (!detail.matches("[\\w.\\-]{2,}@[a-zA-Z]{2,}")) {
                    JOptionPane.showMessageDialog(this, "Enter a valid UPI ID, e.g. suraj@okbank");
                    return;
                }
                paidMethod = "UPI (" + detail + ")";
            }
            case "Card" -> {
                String digits = detail.replace(" ", "");
                String cvv = new String(cvvField.getPassword());
                if (!digits.matches("\\d{16}")) {
                    JOptionPane.showMessageDialog(this, "Card number must be 16 digits.");
                    return;
                }
                if (!cvv.matches("\\d{3}")) {
                    JOptionPane.showMessageDialog(this, "CVV must be 3 digits.");
                    return;
                }
                // only the last 4 digits are kept, for the receipt
                paidMethod = "Card (**** " + digits.substring(12) + ")";
            }
            default -> paidMethod = "Cash at counter";
        }
        JOptionPane.showMessageDialog(this, "Payment successful!");
        dispose();
    }

    public String getPaidMethod() { return paidMethod; }
}