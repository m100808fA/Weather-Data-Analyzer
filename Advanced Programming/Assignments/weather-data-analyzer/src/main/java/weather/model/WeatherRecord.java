package weather.model;

import java.time.LocalDate;
import java.util.Optional;

/// Represents one immutable weather measurement row parsed from the CSV dataset.
///
/// ```java
/// var maybeRecord = WeatherRecord.fromCsvLine("2023-08-01,32.5,65,0.0");
/// maybeRecord.ifPresent(System.out::println);
/// ```
public record WeatherRecord(LocalDate date, double temperature, int humidity, double precipitation) {

    /// Creates a validated [WeatherRecord].
    ///
    /// - `humidity` must be in the inclusive range `0..100`.
    /// - `precipitation` must be greater than or equal to `0`.
    ///
    /// ```java
    /// var record = new WeatherRecord(LocalDate.parse("2023-08-01"), 32.5, 65, 0.0);
    /// ```
    public WeatherRecord {
        if (humidity < 0 || humidity > 100) {
            throw new IllegalArgumentException("humidity must be between 0 and 100");
        }
        if (precipitation < 0) {
            throw new IllegalArgumentException("precipitation must be >= 0");
        }
    }

    /// Returns whether this day is rainy.
    ///
    /// A day is considered rainy when precipitation is strictly greater than `0`.
    ///
    /// ```java
    /// var rainy = new WeatherRecord(LocalDate.parse("2023-08-02"), 35.0, 60, 0.2).isRainy();
    /// ```
    ///
    /// @return `true` when precipitation is greater than `0`, otherwise `false`
    public boolean isRainy() {
        return precipitation > 0;
    }

    /// Parses one CSV data line into a validated [WeatherRecord].
    ///
    /// The expected shape is `Date,Temperature,Humidity,Precipitation` without the header.
    /// Invalid or malformed lines produce [Optional#empty()].
    ///
    /// ```java
    /// var record = WeatherRecord.fromCsvLine("2023-08-02,35.0,60,0.2");
    /// var missing = WeatherRecord.fromCsvLine("bad,line");
    /// ```
    ///
    /// @param line a single CSV line
    /// @return an [Optional] containing a parsed record, or empty for invalid input
    public static Optional<WeatherRecord> fromCsvLine(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        var parts = line.split(",", -1);
        if (parts.length != 4) {
            return Optional.empty();
        }

        try {
            var date = LocalDate.parse(parts[0].trim());
            var temperature = Double.parseDouble(parts[1].trim());
            var humidity = Integer.parseInt(parts[2].trim());
            var precipitation = Double.parseDouble(parts[3].trim());

            return Optional.of(new WeatherRecord(date, temperature, humidity, precipitation));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }
}
