import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * WeatherService - Simulates or fetches weather data for the accident location
 * Can be extended to use real weather APIs like OpenWeatherMap
 */
public class WeatherService {
    private Random random;
    private ConfigManager config;
    private Timer updateTimer;
    private String currentWeather;
    private double currentTemperature;

    // Weather conditions
    private static final String[] WEATHER_CONDITIONS = {
            "Clear", "Partly Cloudy", "Cloudy", "Rainy", "Stormy", "Foggy"
    };

    public WeatherService() {
        this.random = new Random();
        this.config = ConfigManager.getInstance();
        this.currentWeather = "Clear";
        this.currentTemperature = 28.0;
        startPeriodicUpdate();
    }

    /**
     * Start periodic weather updates
     */
    private void startPeriodicUpdate() {
        int interval = config.getWeatherUpdateInterval();
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateWeather();
            }
        }, 0, interval);
    }

    /**
     * Update weather information (simulated)
     */
    private void updateWeather() {
        // Simulate weather changes
        currentWeather = WEATHER_CONDITIONS[random.nextInt(WEATHER_CONDITIONS.length)];
        currentTemperature = 20.0 + random.nextDouble() * 20.0; // 20-40°C

        System.out.println("[WeatherService] Weather updated: " + currentWeather +
                ", Temp: " + String.format("%.1f°C", currentTemperature));
    }

    /**
     * Get current weather condition
     */
    public String getCurrentWeather() {
        return currentWeather;
    }

    /**
     * Get current temperature
     */
    public double getCurrentTemperature() {
        return currentTemperature;
    }

    /**
     * Get weather for specific location (simulated)
     * In production, this would make API call to weather service
     */
    public String getWeatherForLocation(double latitude, double longitude) {
        // Simulate location-based weather
        // In production, use OpenWeatherMap API or similar
        return currentWeather;
    }

    /**
     * Get weather severity (for accident dispatching priority)
     * 
     * @return Severity level: 1 (clear) to 5 (severe)
     */
    public int getWeatherSeverity() {
        switch (currentWeather) {
            case "Clear":
                return 1;
            case "Partly Cloudy":
                return 2;
            case "Cloudy":
                return 2;
            case "Rainy":
                return 3;
            case "Stormy":
                return 5;
            case "Foggy":
                return 4;
            default:
                return 2;
        }
    }

    /**
     * Check if weather conditions are dangerous for driving
     */
    public boolean isDangerousWeather() {
        return getWeatherSeverity() >= 4;
    }

    /**
     * Get weather icon/emoji representation
     */
    public String getWeatherIcon() {
        switch (currentWeather) {
            case "Clear":
                return "☀️";
            case "Partly Cloudy":
                return "⛅";
            case "Cloudy":
                return "☁️";
            case "Rainy":
                return "🌧️";
            case "Stormy":
                return "⛈️";
            case "Foggy":
                return "🌫️";
            default:
                return "🌤️";
        }
    }

    /**
     * Get formatted weather display string
     */
    public String getWeatherDisplay() {
        return String.format("%s %s, %.1f°C",
                getWeatherIcon(),
                currentWeather,
                currentTemperature);
    }

    /**
     * Get weather impact on response time (multiplier)
     */
    public double getWeatherImpactMultiplier() {
        switch (currentWeather) {
            case "Clear":
                return 1.0;
            case "Partly Cloudy":
                return 1.05;
            case "Cloudy":
                return 1.1;
            case "Rainy":
                return 1.3;
            case "Stormy":
                return 1.6;
            case "Foggy":
                return 1.4;
            default:
                return 1.0;
        }
    }

    /**
     * Stop weather updates
     */
    public void stop() {
        if (updateTimer != null) {
            updateTimer.cancel();
            System.out.println("[WeatherService] Weather updates stopped");
        }
    }
}

