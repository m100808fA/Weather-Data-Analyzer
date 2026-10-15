package weather.io;

import org.junit.jupiter.api.io.TempDir;
import weather.model.WeatherRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/// Tests CSV parsing behavior in [WeatherCsvReader].
///
/// ```java
/// // Executed by JUnit 5
/// ```
public interface WeatherCsvReaderTest {

    /// Verifies valid rows are loaded and malformed rows are counted as skipped.
    ///
    /// ```java
    /// // Given a CSV with valid and invalid rows
    /// // WeatherCsvReader.read(...) returns Success with skipped count
    /// ```
    ///
    /// @param tempDir temporary directory supplied by JUnit
    static void readCountsSkippedLines(@TempDir Path tempDir) throws IOException {
        var csv = """
                Date,Temperature,Humidity,Precipitation
                2023-08-01,32.5,65,0.0
                bad,line

                2023-08-02,35.0,60,0.2
                2023-08-03,30.0,101,0.0
                """;

        var file = tempDir.resolve("weatherdata.csv");
        Files.writeString(file, csv);

        var result = WeatherCsvReader.read(file);
        var success = assertInstanceOf(Result.Success.class, result);

        @SuppressWarnings("unchecked")
        var typedSuccess = (Result.Success<List<WeatherRecord>>) success;
        assertEquals(2, typedSuccess.value().size());
        assertEquals(2, typedSuccess.skippedLines());
    }
}
