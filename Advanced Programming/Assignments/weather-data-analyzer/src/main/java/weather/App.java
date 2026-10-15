package weather;

/// Application entry point for the Weather Data Analyzer.
///
/// ```bash
/// mvn -q exec:java -Dexec.mainClass=weather.App
/// ```
public interface App {

    /// Starts the application.
    ///
    /// ```java
    /// App.main(new String[0]);
    /// ```
    ///
    /// @param args command-line arguments
    static void main(String[] args) {
        System.out.println("Hello from Weather Data Analyzer");
    }
}
