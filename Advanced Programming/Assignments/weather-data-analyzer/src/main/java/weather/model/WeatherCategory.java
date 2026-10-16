package weather.model;

/// Represents temperature-based weather buckets.
///
/// Thresholds are applied by [weather.analysis.Categorizer#categorize(double)].
///
/// ```java
/// var category = WeatherCategory.HOT;
/// ```
public enum WeatherCategory {

    /// Temperatures of `30 °C` and above.
    HOT,

    /// Temperatures from `22 °C` up to (but excluding) `30 °C`.
    WARM,

    /// Temperatures from `15 °C` up to (but excluding) `22 °C`.
    MILD,

    /// Temperatures from `5 °C` up to (but excluding) `15 °C`.
    COLD,

    /// Temperatures below `5 °C`.
    FREEZING
}