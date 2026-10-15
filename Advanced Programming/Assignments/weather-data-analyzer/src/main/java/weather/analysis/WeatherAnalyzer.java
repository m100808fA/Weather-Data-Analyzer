package weather.analysis;

import weather.model.WeatherRecord;

import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/// Provides stream-based weather analysis utilities.
///
/// ```java
/// var avg = WeatherAnalyzer.averageTemperature(data, Month.AUGUST);
/// ```
public interface WeatherAnalyzer {

    /// Computes the average temperature for one month.
    ///
    /// ```java
    /// var augustAverage = WeatherAnalyzer.averageTemperature(data, Month.AUGUST);
    /// ```
    ///
    /// @param data weather records
    /// @param month target month
    /// @return average temperature for that month, or empty when no records match
    static OptionalDouble averageTemperature(List<WeatherRecord> data, Month month) {
        return data.stream()
                .filter(record -> record.date().getMonth() == month)
                .mapToDouble(WeatherRecord::temperature)
                .average();
    }

    /// Returns all days with temperature strictly above the threshold.
    ///
    /// ```java
    /// var hotDays = WeatherAnalyzer.daysAbove(data, 35.0);
    /// ```
    ///
    /// @param data weather records
    /// @param threshold temperature threshold
    /// @return matching weather records
    static List<WeatherRecord> daysAbove(List<WeatherRecord> data, double threshold) {
        return data.stream()
                .filter(record -> record.temperature() > threshold)
                .toList();
    }

    /// Counts days where precipitation is greater than zero.
    ///
    /// ```java
    /// var rainyCount = WeatherAnalyzer.countRainyDays(data);
    /// ```
    ///
    /// @param data weather records
    /// @return number of rainy days
    static long countRainyDays(List<WeatherRecord> data) {
        return data.stream()
                .filter(WeatherRecord::isRainy)
                .count();
    }

    /// Computes average temperature grouped by month.
    ///
    /// ```java
    /// var byMonth = WeatherAnalyzer.averageTemperatureByMonth(data);
    /// ```
    ///
    /// @param data weather records
    /// @return map from month to average temperature
    static Map<Month, Double> averageTemperatureByMonth(List<WeatherRecord> data) {
        return data.stream()
                .collect(Collectors.groupingBy(
                        record -> record.date().getMonth(),
                        Collectors.averagingDouble(WeatherRecord::temperature)
                ));
    }

    /// Finds the hottest day in the dataset.
    ///
    /// ```java
    /// var hottest = WeatherAnalyzer.hottestDay(data);
    /// ```
    ///
    /// @param data weather records
    /// @return the record with the highest temperature, or empty when data is empty
    static Optional<WeatherRecord> hottestDay(List<WeatherRecord> data) {
        return data.stream()
                .max(Comparator.comparingDouble(WeatherRecord::temperature));
    }
}
