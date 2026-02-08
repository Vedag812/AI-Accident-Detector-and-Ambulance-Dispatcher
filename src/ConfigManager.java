import java.io.*;
import java.util.Properties;

/**
 * ConfigManager - Handles loading and accessing configuration from
 * config.properties
 * Follows Singleton pattern to ensure single instance throughout the
 * application
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;
    private static final String CONFIG_FILE = "config.properties";

    private ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    /**
     * Get singleton instance of ConfigManager
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Load configuration from config.properties file
     */
    private void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            System.out.println("[ConfigManager] Configuration loaded successfully");
        } catch (IOException e) {
            System.err.println("[ConfigManager] Error loading config file: " + e.getMessage());
            // Set default values if config file not found
            setDefaultConfig();
        }
    }

    /**
     * Set default configuration values
     */
    private void setDefaultConfig() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/accident_alert_system");
        properties.setProperty("db.user", "root");
        properties.setProperty("db.password", "YOUR_PASSWORD_HERE");
        properties.setProperty("google.maps.api.key", "YOUR_API_KEY_HERE");
        properties.setProperty("weather.update.interval", "300000");
        properties.setProperty("notification.sound.enabled", "true");
        properties.setProperty("notification.display.duration", "5000");
    }

    /**
     * Get configuration property by key
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get configuration property with default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get database URL
     */
    public String getDbUrl() {
        return getProperty("db.url");
    }

    /**
     * Get database username
     */
    public String getDbUser() {
        return getProperty("db.user");
    }

    /**
     * Get database password
     */
    public String getDbPassword() {
        return getProperty("db.password");
    }

    /**
     * Get Google Maps API key
     */
    public String getGoogleMapsApiKey() {
        return getProperty("google.maps.api.key");
    }

    /**
     * Get weather update interval in milliseconds
     */
    public int getWeatherUpdateInterval() {
        return Integer.parseInt(getProperty("weather.update.interval", "300000"));
    }

    /**
     * Check if notification sound is enabled
     */
    public boolean isNotificationSoundEnabled() {
        return Boolean.parseBoolean(getProperty("notification.sound.enabled", "true"));
    }

    /**
     * Get notification display duration in milliseconds
     */
    public int getNotificationDisplayDuration() {
        return Integer.parseInt(getProperty("notification.display.duration", "5000"));
    }
}
