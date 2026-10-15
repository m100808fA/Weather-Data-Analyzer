package weather;

import weather.io.Result;
import weather.io.WeatherCsvReader;

import java.nio.file.Path;

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
        var result = WeatherCsvReader.read(Path.of("weatherdata.csv"));
        var message = switch (result) {
            case Result.Success(var records, var skippedLines) ->
                    "Loaded %d records (%d skipped)".formatted(records.size(), skippedLines);
            case Result.Failure(var failureMessage) ->
                    "Failed to load weather data: %s".formatted(failureMessage);
        };
        System.out.println(message);
    }
}
