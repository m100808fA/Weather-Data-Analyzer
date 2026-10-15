package weather.io;

import weather.model.WeatherRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/// Reads weather records from a CSV file.
///
/// ```java
/// var result = WeatherCsvReader.read(Path.of("weatherdata.csv"));
/// ```
public interface WeatherCsvReader {

    /// Loads weather records from a CSV path.
    ///
    /// The reader skips the header line, ignores blank lines, and counts malformed lines
    /// instead of failing the entire read.
    ///
    /// ```java
    /// var result = WeatherCsvReader.read(Path.of("weatherdata.csv"));
    /// ```
    ///
    /// @param path CSV file path
    /// @return a [Result] containing parsed records or a failure message
    static Result<List<WeatherRecord>> read(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            var parsedRows = lines
                    .skip(1)
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .map(WeatherRecord::fromCsvLine)
                    .toList();

            var records = parsedRows.stream()
                    .flatMap(Optional::stream)
                    .toList();

            var skippedLines = (int) parsedRows.stream()
                    .filter(Optional::isEmpty)
                    .count();

            return new Result.Success<>(records, skippedLines);
        } catch (IOException exception) {
            return new Result.Failure<>("Failed to read CSV '%s': %s".formatted(path, exception.getMessage()));
        }
    }
}
