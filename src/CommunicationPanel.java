import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.Map;

/**
 * CommunicationPanel - Messaging system for communication between dispatchers,
 * hospitals, and ambulance drivers
 */
public class CommunicationPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTextArea messageDisplay;
    private JTextField messageInput;
    private JComboBox<String> prioritySelector;
    private JButton sendButton;
    private Timer refreshTimer;
    private int currentUserId = 1; // Default admin user

    public CommunicationPanel() {
        this.dbManager = DatabaseManager.getInstance();
        initializeUI();
        startMessageRefresh();
    }

    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(UITheme.PANEL_BG);

        // Header
        JLabel header = new JLabel("📨 Communication Center");
        header.setFont(UITheme.FONT_HEADER);
        header.setForeground(UITheme.TEXT_PRIMARY);
        add(header, BorderLayout.NORTH);

        // Message display area
        messageDisplay = new JTextArea();
        messageDisplay.setEditable(false);
        messageDisplay.setFont(new Font("Consolas", Font.PLAIN, 12));
        messageDisplay.setBackground(UITheme.CARD_BG);
        messageDisplay.setLineWrap(true);
        messageDisplay.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(messageDisplay);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(UITheme.PANEL_BG);

        // Message input
        messageInput = new JTextField();
        messageInput.setFont(UITheme.FONT_BODY);
        messageInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(8, 10, 8, 10)));

        // Priority selector
        String[] priorities = { "LOW", "MEDIUM", "HIGH", "URGENT" };
        prioritySelector = new JComboBox<>(priorities);
        prioritySelector.setSelectedIndex(1); // Default to MEDIUM
        prioritySelector.setFont(UITheme.FONT_SMALL);

        // Send button
        sendButton = new JButton("Send");
        sendButton.setFont(UITheme.FONT_BUTTON);
        sendButton.setBackground(UITheme.ACCENT);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());

        // Enter key to send
        messageInput.addActionListener(e -> sendMessage());

        // Layout input panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setBackground(UITheme.PANEL_BG);
        controlPanel.add(new JLabel("Priority:"));
        controlPanel.add(prioritySelector);
        controlPanel.add(sendButton);

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(controlPanel, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Send message to database
     */
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        String priority = (String) prioritySelector.getSelectedItem();

        try {
            String sql = "INSERT INTO messages (sender_id, receiver_id, message, priority) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, 0); // Broadcast to all
            pstmt.setString(3, message);
            pstmt.setString(4, priority);
            pstmt.executeUpdate();

            messageInput.setText("");
            refreshMessages();

            NotificationManager.getInstance().showInfo("Message Sent", "Your message has been delivered");
        } catch (SQLException e) {
            System.err.println("[CommunicationPanel] Error sending message: " + e.getMessage());
            NotificationManager.getInstance().showError("Error", "Failed to send message");
        }
    }

    /**
     * Refresh and display all messages
     */
    private void refreshMessages() {
        try {
            String sql = "SELECT m.*, u.username FROM messages m " +
                    "LEFT JOIN users u ON m.sender_id = u.id " +
                    "ORDER BY m.sent_at DESC LIMIT 50";
            ResultSet rs = dbManager.executeQuery(sql);

            StringBuilder messages = new StringBuilder();
            while (rs.next()) {
                String username = rs.getString("username");
                String msg = rs.getString("message");
                String priority = rs.getString("priority");
                String time = rs.getTimestamp("sent_at").toString();

                String priorityIcon = getPriorityIcon(priority);
                messages.append(String.format("[%s] %s %s: %s\n",
                        time.substring(0, 19), priorityIcon, username, msg));
            }

            messageDisplay.setText(messages.toString());
        } catch (SQLException e) {
            System.err.println("[CommunicationPanel] Error refreshing messages: " + e.getMessage());
        }
    }

    /**
     * Get icon for priority level
     */
    private String getPriorityIcon(String priority) {
        switch (priority) {
            case "LOW":
                return "ℹ️";
            case "MEDIUM":
                return "⚠️";
            case "HIGH":
                return "🔴";
            case "URGENT":
                return "🚨";
            default:
                return "📌";
        }
    }

    /**
     * Start automatic message refresh
     */
    private void startMessageRefresh() {
        refreshMessages();
        refreshTimer = new Timer(5000, e -> refreshMessages());
        refreshTimer.start();
    }

    /**
     * Stop message refresh
     */
    public void stopRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    /**
     * Set current user ID for sending messages
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
}

