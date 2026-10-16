package weather;

import weather.ui.WeatherLauncher;

/// Application entry point for the Weather Data Analyzer.
///
/// The JavaFX API requires an explicit Application subclass, so the entry point delegates to
/// [weather.ui.WeatherLauncher], which launches the minimal GUI class.
///
/// ```bash
/// ./mvnw -q javafx:run
/// ```
public interface App {

    /// Launches the GUI application.
    ///
    /// @param args command-line args
    static void main(String[] args) {
        WeatherLauncher.main(args);
    }
}

