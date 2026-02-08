import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

/**
 * LoginDialog - Premium modern dark-themed authentication dialog
 */
public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private boolean authenticated = false;
    private int userId = -1;
    private String userRole = "";
    private DatabaseManager dbManager;

    // All colors now come from UITheme for centralized styling

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        this.dbManager = DatabaseManager.getInstance();
        initializeUI();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setUndecorated(true);
        setSize(450, 550);
        setBackground(new Color(0, 0, 0, 0));

        // Main panel with rounded corners
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, UITheme.CARD_BG,
                        0, getHeight(), UITheme.PANEL_BG);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Subtle inner shadow/border
                g2d.setColor(
                        new Color(UITheme.ACCENT.getRed(), UITheme.ACCENT.getGreen(), UITheme.ACCENT.getBlue(), 10));
                g2d.fillOval(-100, -200, getWidth() + 200, 300);

                g2d.dispose();
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        mainPanel.setOpaque(false);

        // Close button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JButton closeBtn = createIconButton("✕");
        closeBtn.addActionListener(e -> {
            authenticated = false;
            dispose();
        });
        topPanel.add(closeBtn);
        mainPanel.add(topPanel);

        mainPanel.add(Box.createVerticalStrut(20));

        // Logo/Icon
        JLabel iconLabel = new JLabel("🚑", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(iconLabel);

        mainPanel.add(Box.createVerticalStrut(15));

        // Title
        JLabel titleLabel = new JLabel("Accident Alert System");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Emergency Response Command Center");
        subtitleLabel.setFont(UITheme.FONT_SUBTITLE);
        subtitleLabel.setForeground(UITheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(40));

        // Username field
        mainPanel.add(createLabel("Username"));
        mainPanel.add(Box.createVerticalStrut(8));
        usernameField = createTextField("Enter your username");
        mainPanel.add(usernameField);

        mainPanel.add(Box.createVerticalStrut(20));

        // Password field
        mainPanel.add(createLabel("Password"));
        mainPanel.add(Box.createVerticalStrut(8));
        passwordField = createPasswordField("Enter your password");
        mainPanel.add(passwordField);

        mainPanel.add(Box.createVerticalStrut(35));

        // Login button
        loginButton = createPrimaryButton("Sign In");
        loginButton.addActionListener(e -> performLogin());
        mainPanel.add(loginButton);

        mainPanel.add(Box.createVerticalStrut(15));

        // Cancel button
        cancelButton = createSecondaryButton("Cancel");
        cancelButton.addActionListener(e -> {
            authenticated = false;
            dispose();
        });
        mainPanel.add(cancelButton);

        mainPanel.add(Box.createVerticalGlue());

        // Footer
        JLabel footerLabel = new JLabel("Default: admin / admin123");
        footerLabel.setFont(UITheme.FONT_XSMALL);
        footerLabel.setForeground(UITheme.TEXT_MUTED);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(footerLabel);

        // Enter key bindings
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // Make dialog draggable
        final Point[] dragPoint = { null };
        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }
        });
        mainPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Point location = getLocation();
                setLocation(location.x + e.getX() - dragPoint[0].x,
                        location.y + e.getY() - dragPoint[0].y);
            }
        });

        setContentPane(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
        label.setForeground(UITheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BORDER_RADIUS, UITheme.BORDER_RADIUS);
                super.paintComponent(g);

                if (getText().isEmpty() && !hasFocus()) {
                    g2d.setColor(UITheme.TEXT_MUTED);
                    g2d.setFont(getFont());
                    g2d.drawString(placeholder, 15, getHeight() / 2 + 5);
                }
                g2d.dispose();
            }
        };
        field.setOpaque(false);
        field.setFont(UITheme.FONT_BODY);
        field.setForeground(UITheme.TEXT_PRIMARY);
        field.setCaretColor(UITheme.ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 15, 12, 15)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        field.setPreferredSize(new Dimension(350, UITheme.INPUT_HEIGHT));
        return field;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BORDER_RADIUS, UITheme.BORDER_RADIUS);
                super.paintComponent(g);

                if (getPassword().length == 0 && !hasFocus()) {
                    g2d.setColor(UITheme.TEXT_MUTED);
                    g2d.setFont(getFont());
                    g2d.drawString(placeholder, 15, getHeight() / 2 + 5);
                }
                g2d.dispose();
            }
        };
        field.setOpaque(false);
        field.setFont(UITheme.FONT_BODY);
        field.setForeground(UITheme.TEXT_PRIMARY);
        field.setCaretColor(UITheme.ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 15, 12, 15)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        field.setPreferredSize(new Dimension(350, UITheme.INPUT_HEIGHT));
        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(UITheme.ACCENT);
                } else if (getModel().isRollover()) {
                    g2d.setColor(UITheme.ACCENT_HOVER);
                } else {
                    g2d.setColor(UITheme.ACCENT);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BORDER_RADIUS, UITheme.BORDER_RADIUS);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(UITheme.FONT_BUTTON);
        button.setForeground(UITheme.TEXT_PRIMARY);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.BUTTON_HEIGHT_LARGE));
        button.setPreferredSize(new Dimension(350, UITheme.BUTTON_HEIGHT_LARGE));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getModel().isRollover() ? UITheme.SURFACE_BG : new Color(0, 0, 0, 0));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.BORDER_RADIUS, UITheme.BORDER_RADIUS);
                g2d.setColor(UITheme.BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UITheme.BORDER_RADIUS, UITheme.BORDER_RADIUS);

                g2d.setColor(UITheme.TEXT_SECONDARY);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(UITheme.FONT_BODY);
        button.setForeground(UITheme.TEXT_SECONDARY);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(350, UITheme.BUTTON_HEIGHT));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private JButton createIconButton(String icon) {
        JButton button = new JButton(icon);
        button.setFont(UITheme.FONT_BODY.deriveFont(16f));
        button.setForeground(UITheme.TEXT_SECONDARY);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(30, 30));
        return button;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        try {
            String sql = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id");
                userRole = rs.getString("role");
                authenticated = true;
                logAudit(userId, "LOGIN", "User logged in successfully");
                dispose();
            } else {
                showError("Invalid username or password");
                passwordField.setText("");
                passwordField.requestFocus();
            }
        } catch (SQLException e) {
            System.err.println("[LoginDialog] Database error: " + e.getMessage());
            showError("Database connection error");
        }
    }

    private void logAudit(int userId, String action, String details) {
        try {
            String sql = "INSERT INTO audit_logs (user_id, action, details, ip_address) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.setString(4, "127.0.0.1");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[LoginDialog] Error logging audit: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public static LoginDialog showLogin(Frame parent) {
        LoginDialog dialog = new LoginDialog(parent);
        dialog.setVisible(true);
        return dialog;
    }
}


