import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Main extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Date", "Name", "Amount"}, 0);
    private final JTable table = new JTable(model);
    private final JTextField nameField = new JTextField(12);
    private final JTextField amountField = new JTextField(8);
    private final JTextField dateField = new JTextField(10);
    private final JLabel totalLabel = new JLabel("Total: ₹0.00");

    public Main() {
        super("Expense Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 420);
        setLocationRelativeTo(null);

        dateField.setText(LocalDate.now().toString());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Name:"));   top.add(nameField);
        top.add(new JLabel("Amount:")); top.add(amountField);
        top.add(new JLabel("Date:"));   top.add(dateField);

        JButton addBtn = new JButton("Add");
        JButton delBtn = new JButton("Delete Selected");
        JButton saveBtn = new JButton("Save CSV");
        top.add(addBtn); top.add(delBtn); top.add(saveBtn);

        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(totalLabel);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addExpense());
        delBtn.addActionListener(e -> deleteSelected());
        saveBtn.addActionListener(e -> saveCsv());

    }

    private void addExpense() {
        String name = nameField.getText().trim();
        String amountStr = amountField.getText().trim();
        String dateStr = dateField.getText().trim();

        if (name.isEmpty() || amountStr.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        try {
            double amt = Double.parseDouble(amountStr);
            if (amt < 0) throw new NumberFormatException();
            LocalDate.parse(dateStr);

            model.addRow(new Object[]{dateStr, name, String.format("%.2f", amt)});
            nameField.setText("");
            amountField.setText("");
            dateField.setText(LocalDate.now().toString());
            updateTotal();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a positive number.");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Date must be YYYY-MM-DD.");
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }
        model.removeRow(row);
        updateTotal();
    }

    private void updateTotal() {
        double sum = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            sum += Double.parseDouble(model.getValueAt(i, 2).toString());
        }
        totalLabel.setText("Total: ₹" + String.format("%.2f", sum));
    }

    private void saveCsv() {
        try (PrintWriter out = new PrintWriter(new FileWriter("expenses.csv"))) {
            out.println("date,name,amount");
            for (int i = 0; i < model.getRowCount(); i++) {
                String d = model.getValueAt(i, 0).toString();
                String n = escape(model.getValueAt(i, 1).toString());
                String a = model.getValueAt(i, 2).toString();
                out.println(d + "," + n + "," + a);
            }
            JOptionPane.showMessageDialog(this, "Saved to expenses.csv");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage());
        }
    }

    private String escape(String s) {
        return (s.contains(",") || s.contains("\"")) ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
