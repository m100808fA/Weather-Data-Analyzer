package weather.analysis;

import org.junit.jupiter.api.Test;
import weather.model.WeatherCategory;
import weather.model.WeatherRecord;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Tests weather categorization and description behavior.
///
/// ```java
/// // Executed by JUnit 5
/// ```
class CategorizerTest {

    /// Verifies category boundaries around threshold values.
    ///
    /// ```java
    /// var category = Categorizer.categorize(29.9);
    /// ```
    @Test
    void categorizeRespectsBoundaries() {
        assertEquals(WeatherCategory.WARM, Categorizer.categorize(29.9));
        assertEquals(WeatherCategory.HOT, Categorizer.categorize(30.0));

        assertEquals(WeatherCategory.MILD, Categorizer.categorize(21.9));
        assertEquals(WeatherCategory.WARM, Categorizer.categorize(22.0));

        assertEquals(WeatherCategory.COLD, Categorizer.categorize(14.9));
        assertEquals(WeatherCategory.MILD, Categorizer.categorize(15.0));

        assertEquals(WeatherCategory.FREEZING, Categorizer.categorize(4.9));
        assertEquals(WeatherCategory.COLD, Categorizer.categorize(5.0));
    }

    /// Verifies record-pattern-based description outputs.
    ///
    /// ```java
    /// var text = Categorizer.describe(record);
    /// ```
    @Test
    void describeUsesRecordPatternBranches() {
        var heavyRain = new WeatherRecord(LocalDate.of(2023, 8, 1), 25.0, 60, 12.5);
        var humidHeat = new WeatherRecord(LocalDate.of(2023, 8, 2), 33.0, 80, 0.0);
        var clearDry = new WeatherRecord(LocalDate.of(2023, 8, 3), 24.0, 50, 0.0);
        var rainy = new WeatherRecord(LocalDate.of(2023, 8, 4), 18.0, 65, 0.5);

        assertEquals("Heavy rain day", Categorizer.describe(heavyRain));
        assertEquals("Humid heat", Categorizer.describe(humidHeat));
        assertEquals("Clear and dry", Categorizer.describe(clearDry));
        assertEquals("Rainy day", Categorizer.describe(rainy));
    }

    /// Verifies grouping counts by weather category.
    ///
    /// ```java
    /// var counts = Categorizer.countByCategory(data);
    /// ```
    @Test
    void countByCategoryAggregatesCounts() {
        var data = List.of(
                new WeatherRecord(LocalDate.of(2023, 8, 1), 31.0, 60, 0.0),
                new WeatherRecord(LocalDate.of(2023, 8, 2), 30.0, 55, 0.0),
                new WeatherRecord(LocalDate.of(2023, 8, 3), 22.0, 50, 0.0),
                new WeatherRecord(LocalDate.of(2023, 8, 4), 10.0, 50, 0.0),
                new WeatherRecord(LocalDate.of(2023, 8, 5), 2.0, 50, 0.0)
        );

        var counts = Categorizer.countByCategory(data);
        assertEquals(2L, counts.get(WeatherCategory.HOT));
        assertEquals(1L, counts.get(WeatherCategory.WARM));
        assertEquals(1L, counts.get(WeatherCategory.COLD));
        assertEquals(1L, counts.get(WeatherCategory.FREEZING));
    }

    /// Verifies no records are lost or double counted.
    ///
    /// ```java
    /// var total = counts.values().stream().mapToLong(Long::longValue).sum();
    /// ```
    @Test
    void countByCategoryTotalMatchesInputSize() {
        var data = List.of(
                new WeatherRecord(LocalDate.of(2023, 8, 1), 31.0, 60, 0.0),
                new WeatherRecord(LocalDate.of(2023, 8, 2), 18.0, 55, 0.0),
                new WeatherRecord(LocalDate.of(2023, 8, 3), 10.0, 50, 0.0)
        );

        var counts = Categorizer.countByCategory(data);

        assertEquals(data.size(), counts.values().stream().mapToLong(Long::longValue).sum());
        assertEquals(1L, counts.get(WeatherCategory.MILD));
    }
}
