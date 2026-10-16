package weather.ui;

/// Launcher entry point for the JavaFX Weather GUI.
///
/// ```bash
/// ./mvnw -q javafx:run
/// ```
public interface WeatherLauncher {

    /// Launches [WeatherGui].
    ///
    /// @param args command-line arguments
    static void main(String[] args) {
        WeatherGui.launch(WeatherGui.class, args);
    }
}
