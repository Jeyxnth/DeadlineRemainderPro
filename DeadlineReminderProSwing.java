import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DeadlineReminderProSwing extends JFrame {

    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField taskField, dateField;
    private static final String FILE_NAME = "tasks.txt";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DeadlineReminderProSwing() {
        // Frame setup
        setTitle("📅 Deadline Reminder Pro");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Colors
        Color primaryBlue = new Color(52, 152, 219);
        Color successGreen = new Color(39, 174, 96);
        Color dangerRed = new Color(231, 76, 60);
        Color warningYellow = new Color(241, 196, 15);
        Color bgGray = new Color(245, 247, 250);

        // Header
        JLabel header = new JLabel("Deadline Reminder System", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(primaryBlue);
        header.setBorder(new EmptyBorder(15, 10, 15, 10));
        add(header, BorderLayout.NORTH);

        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inputPanel.setBackground(bgGray);

        taskField = new JTextField(15);
        dateField = new JTextField(10);

        JButton addBtn = createButton("➕ Add Task", primaryBlue, Color.WHITE);
        JButton deleteBtn = createButton("🗑 Delete", dangerRed, Color.WHITE);
        JButton saveBtn = createButton("💾 Save", successGreen, Color.WHITE);
        JButton showBtn = createButton("📆 Upcoming", warningYellow, Color.DARK_GRAY);

        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(taskField);
        inputPanel.add(new JLabel("Due Date (yyyy-MM-dd):"));
        inputPanel.add(dateField);
        inputPanel.add(addBtn);
        inputPanel.add(deleteBtn);
        inputPanel.add(showBtn);
        inputPanel.add(saveBtn);

        add(inputPanel, BorderLayout.SOUTH);

        // Table setup
        String[] columns = {"Task", "Due Date", "Days Left"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(primaryBlue);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(220, 240, 255));

        // Center align days-left column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Load any saved tasks
        loadTasks();

        // Button actions
        addBtn.addActionListener(e -> addTask());
        deleteBtn.addActionListener(e -> deleteTask());
        saveBtn.addActionListener(e -> saveTasks());
        showBtn.addActionListener(e -> showUpcoming());

        setVisible(true);
    }

    // Helper: create stylized button
    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(110, 35));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Rounded effect
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return btn;
    }

    private void addTask() {
        String task = taskField.getText().trim();
        String dateStr = dateField.getText().trim();

        if (task.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Please enter both task and date.");
            return;
        }

        try {
            LocalDate dueDate = LocalDate.parse(dateStr, formatter);
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
            model.addRow(new Object[]{task, dueDate, daysLeft});
            taskField.setText("");
            dateField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Invalid date format! Use yyyy-MM-dd");
        }
    }

    private void deleteTask() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            model.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Select a task to delete.");
        }
    }

    private void showUpcoming() {
        LocalDate today = LocalDate.now();
        DefaultTableModel filtered = new DefaultTableModel(new String[]{"Task", "Due Date", "Days Left"}, 0);

        for (int i = 0; i < model.getRowCount(); i++) {
            LocalDate date = LocalDate.parse(model.getValueAt(i, 1).toString());
            long daysLeft = ChronoUnit.DAYS.between(today, date);
            if (!date.isBefore(today) && daysLeft <= 7) {
                filtered.addRow(new Object[]{model.getValueAt(i, 0), date, daysLeft});
            }
        }

        JTable filteredTable = new JTable(filtered);
        filteredTable.setRowHeight(25);
        filteredTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filteredTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        filteredTable.getTableHeader().setBackground(new Color(52, 152, 219));
        filteredTable.getTableHeader().setForeground(Color.WHITE);

        JOptionPane.showMessageDialog(this, new JScrollPane(filteredTable),
                "📅 Upcoming Tasks (Next 7 Days)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveTasks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                bw.write(model.getValueAt(i, 0) + "," + model.getValueAt(i, 1));
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "✅ Tasks saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage());
        }
    }

    private void loadTasks() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            LocalDate today = LocalDate.now();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String task = parts[0];
                LocalDate dueDate = LocalDate.parse(parts[1]);
                long daysLeft = ChronoUnit.DAYS.between(today, dueDate);
                model.addRow(new Object[]{task, dueDate, daysLeft});
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DeadlineReminderProSwing::new);
    }
}
