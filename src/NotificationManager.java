import javax.swing.*;
import java.awt.*;

/**
 * NotificationManager - Displays toast-style notifications in the bottom-right
 * corner
 * Supports different priority levels with color coding
 */
public class NotificationManager {
    private static NotificationManager instance;
    private ConfigManager config;

    // Notification priorities and colors
    public enum Priority {
        LOW(new Color(100, 181, 246)), // Light blue
        MEDIUM(new Color(255, 183, 77)), // Orange
        HIGH(new Color(255, 138, 101)), // Deep orange
        URGENT(new Color(239, 83, 80)); // Red

        public final Color color;

        Priority(Color color) {
            this.color = color;
        }
    }

    private NotificationManager() {
        config = ConfigManager.getInstance();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Show notification toast
     */
    public void showNotification(String title, String message, Priority priority) {
        SwingUtilities.invokeLater(() -> {
            createNotificationWindow(title, message, priority);
        });
    }

    /**
     * Show info notification
     */
    public void showInfo(String title, String message) {
        showNotification(title, message, Priority.LOW);
    }

    /**
     * Show warning notification
     */
    public void showWarning(String title, String message) {
        showNotification(title, message, Priority.MEDIUM);
    }

    /**
     * Show error notification
     */
    public void showError(String title, String message) {
        showNotification(title, message, Priority.HIGH);
    }

    /**
     * Show urgent notification
     */
    public void showUrgent(String title, String message) {
        showNotification(title, message, Priority.URGENT);

        // Play sound for urgent notifications if enabled
        if (config.isNotificationSoundEnabled()) {
            playNotificationSound();
        }
    }

    /**
     * Create notification window
     */
    private void createNotificationWindow(String title, String message, Priority priority) {
        JWindow notification = new JWindow();
        notification.setAlwaysOnTop(true);

        // Create main panel with gradient background
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                Color color1 = priority.color;
                Color color2 = new Color(
                        Math.max(0, color1.getRed() - 30),
                        Math.max(0, color1.getGreen() - 30),
                        Math.max(0, color1.getBlue() - 30));
                GradientPaint gradient = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };

        panel.setLayout(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setOpaque(false);

        // Title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        // Message label
        JLabel messageLabel = new JLabel("<html><body style='width: 250px'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(Color.WHITE);

        // Close button
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> notification.dispose());

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(closeBtn, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(messageLabel, BorderLayout.CENTER);

        notification.add(panel);
        notification.setSize(320, 100);

        // Position at bottom-right corner
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - notification.getWidth() - 20;
        int y = screenSize.height - notification.getHeight() - 60;
        notification.setLocation(x, y);

        notification.setVisible(true);

        // Auto-close after duration
        int duration = config.getNotificationDisplayDuration();
        Timer closeTimer = new Timer(duration, e -> {
            notification.dispose();
        });
        closeTimer.setRepeats(false);
        closeTimer.start();

        // Fade in animation (optional)
        animateFadeIn(notification);
    }

    /**
     * Animate fade-in effect
     */
    private void animateFadeIn(Window window) {
        Timer timer = new Timer(20, null);
        final float[] opacity = { 0.0f };

        timer.addActionListener(e -> {
            opacity[0] += 0.05f;
            if (opacity[0] >= 1.0f) {
                opacity[0] = 1.0f;
                timer.stop();
            }
            try {
                window.setOpacity(opacity[0]);
            } catch (Exception ex) {
                // Opacity not supported on this platform
                timer.stop();
            }
        });

        timer.start();
    }

    /**
     * Play notification sound
     */
    private void playNotificationSound() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("[NotificationManager] Error playing sound: " + e.getMessage());
        }
    }
}

