package weather;

import weather.io.Result;
import weather.io.WeatherCsvReader;
import weather.report.ReportBuilder;

import java.nio.file.Path;
import java.time.Month;
import java.util.Locale;
import java.util.Optional;

/// Application entry point for the Weather Data Analyzer.
///
/// ```bash
/// mvn -q exec:java -Dexec.mainClass=weather.App
/// ```
public interface App {

    /// Starts the application.
    ///
    /// @param args command-line arguments
    static void main(String[] args) {
        var monthInput = args.length > 0 ? args[0].trim() : "";
        var monthOption = parseMonth(monthInput);
        var month = monthOption.orElse(Month.AUGUST);
        var threshold = parseThreshold(args);

        if (!monthInput.isBlank() && monthOption.isEmpty()) {
            System.out.println("Unknown month '%s', using AUGUST".formatted(monthInput));
        }

        var result = WeatherCsvReader.read(Path.of("weatherdata.csv"));

        switch (result) {
            case Result.Success(var records, var skippedLines) -> {
                System.out.println("Loaded %d records (%d skipped)".formatted(records.size(), skippedLines));
                System.out.println(ReportBuilder.summary(records, month, threshold));
            }
            case Result.Failure(var failureMessage) ->
                    System.out.println("Failed to load weather data: %s".formatted(failureMessage));
        }
    }

    /// Parses a month token from the CLI.
    ///
    /// @param token raw month token
    /// @return parsed month or empty when invalid
    private static Optional<Month> parseMonth(String token) {
        if (token == null || token.isBlank()) {
            return Optional.of(Month.AUGUST);
        }

        try {
            return Optional.of(Month.valueOf(token.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    /// Parses the temperature threshold argument from CLI input.
    ///
    /// @param args command-line arguments
    /// @return threshold value or `33.0` when invalid or missing
    private static double parseThreshold(String[] args) {
        if (args.length < 2) {
            return 33.0;
        }

        try {
            return Double.parseDouble(args[1].trim());
        } catch (NumberFormatException exception) {
            System.out.println("Unknown threshold '%s', using 33.0".formatted(args[1]));
            return 33.0;
        }
    }
}
