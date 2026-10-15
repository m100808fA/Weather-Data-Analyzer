package weather.analysis;

import weather.model.WeatherCategory;
import weather.model.WeatherRecord;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/// Provides category and descriptive views over weather records.
///
/// ```java
/// var category = Categorizer.categorize(30.0); // HOT
/// ```
public interface Categorizer {

    /// Categorizes a temperature using an enhanced switch expression with guards.
    ///
    /// The `double` is boxed first because primitive patterns are only a
    /// preview feature in JDK 23.
    ///
    /// ```java
    /// var category = Categorizer.categorize(22.0); // WARM
    /// ```
    ///
    /// @param temp temperature in Celsius
    /// @return a [WeatherCategory] based on threshold ranges
    static WeatherCategory categorize(double temp) {
        return switch (Double.valueOf(temp)) {
            case Double t when t >= 30 -> WeatherCategory.HOT;
            case Double t when t >= 22 -> WeatherCategory.WARM;
            case Double t when t >= 15 -> WeatherCategory.MILD;
            case Double t when t >= 5  -> WeatherCategory.COLD;
            default                    -> WeatherCategory.FREEZING;
        };
    }

    /// Returns a short human-readable weather description using record patterns
    /// and unnamed patterns (`_`, final since Java 22).
    ///
    /// ```java
    /// var text = Categorizer.describe(new WeatherRecord(LocalDate.now(), 31.0, 85, 0.0));
    /// // "Humid heat"
    /// ```
    ///
    /// @param weather weather record to describe
    /// @return description such as `Heavy rain day`, `Humid heat`, or `Clear and dry`
    static String describe(WeatherRecord weather) {
        return switch (weather) {
            case WeatherRecord(_, _, _, var precip) when precip > 10 -> "Heavy rain day";
            case WeatherRecord(_, var temp, var humidity, _)
                    when categorize(temp) == WeatherCategory.HOT && humidity >= 70 -> "Humid heat";
            case WeatherRecord(_, _, var humidity, var precip)
                    when precip == 0 && humidity < 70 -> "Clear and dry";
            case WeatherRecord(_, _, _, var precip) when precip > 0 -> "Rainy day";
            default -> "Moderate conditions";
        };
    }

    /// Counts records by [WeatherCategory], ordered from HOT to FREEZING.
    ///
    /// ```java
    /// var counts = Categorizer.countByCategory(data);
    /// counts.forEach((k, v) -> System.out.println(k + " = " + v));
    /// ```
    ///
    /// @param data weather records
    /// @return map with the number of records in each category
    static Map<WeatherCategory, Long> countByCategory(List<WeatherRecord> data) {
        return data.stream()
                .map(WeatherRecord::temperature)
                .map(Categorizer::categorize)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        () -> new EnumMap<>(WeatherCategory.class),
                        Collectors.counting()));
    }
}