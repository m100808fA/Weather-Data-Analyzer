package weather;

import weather.analysis.WeatherAnalyzer;
import weather.io.Result;
import weather.io.WeatherCsvReader;

import java.nio.file.Path;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

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

        switch (result) {
            case Result.Success(var records, var skippedLines) -> {
                System.out.println("Loaded %d records (%d skipped)".formatted(records.size(), skippedLines));

                System.out.println("Aug avg: " + formatOptionalDouble(WeatherAnalyzer.averageTemperature(records, Month.AUGUST)));
                System.out.println("Days > 33: " + WeatherAnalyzer.daysAbove(records, 33.0).size());
                System.out.println("Days > 35: " + WeatherAnalyzer.daysAbove(records, 35.0).size());
                System.out.println("Rainy days: " + WeatherAnalyzer.countRainyDays(records));
                System.out.println("By month: " + formatMonthlyAverages(WeatherAnalyzer.averageTemperatureByMonth(records)));
                System.out.println("Hottest: " + formatHottestDay(WeatherAnalyzer.hottestDay(records)));
            }
            case Result.Failure(var failureMessage) ->
                    System.out.println("Failed to load weather data: %s".formatted(failureMessage));
        }
    }

    /// Formats an [OptionalDouble] as a plain numeric string with two decimal places.
    ///
    /// ```java
    /// var label = App.formatOptionalDouble(OptionalDouble.of(34.019));
    /// ```
    ///
    /// @param value optional number
    /// @return formatted value like `34.02`, or `N/A` when empty
    private static String formatOptionalDouble(OptionalDouble value) {
        return value.isPresent() ? "%.2f".formatted(value.getAsDouble()) : "N/A";
    }

    /// Formats monthly averages in calendar order with two decimal places.
    ///
    /// ```java
    /// var text = App.formatMonthlyAverages(Map.of(Month.AUGUST, 34.019));
    /// ```
    ///
    /// @param averages map of monthly average temperatures
    /// @return ordered month-to-average representation
    private static String formatMonthlyAverages(Map<Month, Double> averages) {
        return averages.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "%s=%.2f".formatted(
                        entry.getKey().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase(Locale.ENGLISH),
                        entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /// Formats an optional hottest-day record for user-friendly output.
    ///
    /// ```java
    /// var formatted = App.formatHottestDay(Optional.empty());
    /// ```
    ///
    /// @param hottest optional hottest weather record
    /// @return formatted hottest day details or `N/A`
    private static String formatHottestDay(Optional<weather.model.WeatherRecord> hottest) {
        return hottest
                .map(record -> "%s (%.2f C, humidity %d%%, precipitation %.2f)".formatted(
                        record.date(),
                        record.temperature(),
                        record.humidity(),
                        record.precipitation()))
                .orElse("N/A");
    }
}
