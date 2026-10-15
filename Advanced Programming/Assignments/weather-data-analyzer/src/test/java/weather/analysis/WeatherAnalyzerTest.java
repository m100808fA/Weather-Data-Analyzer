package weather.analysis;

import org.junit.jupiter.api.Test;
import weather.model.WeatherRecord;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests stream-based analysis methods in [WeatherAnalyzer].
///
/// ```java
/// // Executed by JUnit 5
/// ```
class WeatherAnalyzerTest {

    /// Provides a small in-memory sample dataset for analysis tests.
    ///
    /// ```java
    /// var data = sampleData();
    /// ```
    ///
    /// @return sample weather records
    private static List<WeatherRecord> sampleData() {
        return List.of(
                new WeatherRecord(LocalDate.of(2023, 8, 1), 32.5, 65, 0.0),
                new WeatherRecord(LocalDate.of(2023, 8, 2), 35.0, 60, 0.2),
                new WeatherRecord(LocalDate.of(2023, 8, 3), 30.0, 55, 0.0),
                new WeatherRecord(LocalDate.of(2023, 9, 1), 26.4, 72, 0.0)
        );
    }

    /// Verifies monthly average temperature.
    ///
    /// ```java
    /// var avg = WeatherAnalyzer.averageTemperature(data, Month.AUGUST);
    /// ```
    @Test
    void averageTemperatureByMonthValue() {
        var average = WeatherAnalyzer.averageTemperature(sampleData(), Month.AUGUST);
        assertTrue(average.isPresent());
        assertEquals(32.5, average.getAsDouble(), 0.0001);
    }

    /// Verifies that a month with no data gives an empty result.
    ///
    /// ```java
    /// var avg = WeatherAnalyzer.averageTemperature(data, Month.JANUARY);
    /// ```
    @Test
    void averageTemperatureForMissingMonthIsEmpty() {
        var average = WeatherAnalyzer.averageTemperature(sampleData(), Month.JANUARY);
        assertTrue(average.isEmpty());
    }

    /// Verifies filtering days above a threshold.
    ///
    /// ```java
    /// var hotDays = WeatherAnalyzer.daysAbove(data, 31.0);
    /// ```
    @Test
    void daysAboveReturnsMatchingRecords() {
        var hotDays = WeatherAnalyzer.daysAbove(sampleData(), 31.0);
        assertEquals(2, hotDays.size());
    }

    /// Verifies that "above" is strictly greater than the threshold.
    ///
    /// ```java
    /// var days = WeatherAnalyzer.daysAbove(data, 35.0);
    /// ```
    @Test
    void daysAboveExcludesExactThreshold() {
        var days = WeatherAnalyzer.daysAbove(sampleData(), 35.0);
        assertTrue(days.isEmpty());
    }

    /// Verifies rainy day counting.
    ///
    /// ```java
    /// var rainy = WeatherAnalyzer.countRainyDays(data);
    /// ```
    @Test
    void countRainyDaysCountsOnlyRain() {
        var rainyCount = WeatherAnalyzer.countRainyDays(sampleData());
        assertEquals(1, rainyCount);
    }

    /// Verifies grouped monthly averages.
    ///
    /// ```java
    /// var byMonth = WeatherAnalyzer.averageTemperatureByMonth(data);
    /// ```
    @Test
    void averageTemperatureByMonthGroupsCorrectly() {
        var byMonth = WeatherAnalyzer.averageTemperatureByMonth(sampleData());
        assertEquals(2, byMonth.size());
        assertEquals(32.5, byMonth.get(Month.AUGUST), 0.0001);
        assertEquals(26.4, byMonth.get(Month.SEPTEMBER), 0.0001);
    }

    /// Verifies hottest day selection.
    ///
    /// ```java
    /// var hottest = WeatherAnalyzer.hottestDay(data);
    /// ```
    @Test
    void hottestDayReturnsMaxTemperatureRecord() {
        var hottest = WeatherAnalyzer.hottestDay(sampleData());
        assertTrue(hottest.isPresent());
        assertEquals(LocalDate.of(2023, 8, 2), hottest.orElseThrow().date());
    }

    /// Verifies that an empty list has no hottest day.
    ///
    /// ```java
    /// var hottest = WeatherAnalyzer.hottestDay(List.of());
    /// ```
    @Test
    void hottestDayOfEmptyListIsEmpty() {
        assertTrue(WeatherAnalyzer.hottestDay(List.of()).isEmpty());
    }
}