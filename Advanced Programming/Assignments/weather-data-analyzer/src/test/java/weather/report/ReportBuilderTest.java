package weather.report;

import org.junit.jupiter.api.Test;
import weather.model.WeatherRecord;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests report summary output from [ReportBuilder].
///
/// ```java
/// // Executed by JUnit 5
/// ```
class ReportBuilderTest {

    /// Verifies summary includes exact monthly average, days-above, and rainy counts.
    ///
    /// ```java
    /// var report = ReportBuilder.summary(data, Month.AUGUST, 33.0);
    /// ```
    @Test
    void summaryContainsExpectedAugustMetrics() {
        var data = List.of(
                new WeatherRecord(LocalDate.of(2023, 8, 1), 32.0, 60, 0.0),
                new WeatherRecord(LocalDate.of(2023, 8, 2), 34.0, 65, 1.2),
                new WeatherRecord(LocalDate.of(2023, 8, 3), 30.0, 50, 0.0)
        );

        var report = ReportBuilder.summary(data, Month.AUGUST, 33.0);

        assertTrue(report.contains("Average Temperature: 32.00 C"));
        assertTrue(report.contains("Days Above Threshold: 1"));
        assertTrue(report.contains("Rainy Days: 1"));
    }
}
