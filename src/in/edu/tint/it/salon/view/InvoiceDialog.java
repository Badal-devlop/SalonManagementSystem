package in.edu.tint.it.salon.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Dialog displaying a formatted printable text invoice.
 */
public class InvoiceDialog extends JDialog {

    public InvoiceDialog(Frame parent, String invoiceText, int bookingId) {
        super(parent, "Invoice / Bill - Booking #" + bookingId, true);
        setSize(540, 580);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel header = UIUtils.createHeaderBanner("Invoice / Bill", "Booking Reference #" + bookingId, null);
        add(header, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(invoiceText);
        textArea.setFont(UIUtils.FONT_MONO);
        textArea.setEditable(false);
        textArea.setBackground(new Color(250, 250, 250));
        textArea.setBorder(new EmptyBorder(12, 16, 12, 16));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIUtils.COLOR_BORDER));
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCopy = UIUtils.createSecondaryButton("Copy to Clipboard");
        btnCopy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(invoiceText), null);
            JOptionPane.showMessageDialog(this, "Invoice copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClose = UIUtils.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnCopy);
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);
    }
}
