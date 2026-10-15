package weather.report;

import weather.analysis.Categorizer;
import weather.analysis.WeatherAnalyzer;
import weather.model.WeatherCategory;
import weather.model.WeatherRecord;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/// Builds rich textual and HTML summaries for a weather dataset.
///
/// ```java
/// var report = ReportBuilder.summary(data, Month.AUGUST, 33.0);
/// System.out.println(report);
/// ```
public interface ReportBuilder {

    /// Captures report statistics shared by text and HTML rendering.
    ///
    /// ```java
    /// var stats = ReportBuilder.Stats.of(data, Month.AUGUST, 33.0);
    /// ```
    ///
    /// @param average optional month average temperature
    /// @param daysAbove number of days above the threshold
    /// @param rainy number of rainy days
    /// @param categoryCounts raw category totals for the month
    /// @param hottest hottest weather record for the month
    record Stats(OptionalDouble average, int daysAbove, long rainy,
                 Map<WeatherCategory, Long> categoryCounts,
                 Optional<WeatherRecord> hottest) {

        /// Computes statistics for the selected month and threshold.
        ///
        /// ```java
        /// var stats = ReportBuilder.Stats.of(data, Month.NOVEMBER, 10.0);
        /// ```
        ///
        /// @param data full weather dataset
        /// @param month selected month
        /// @param threshold selected threshold
        /// @return calculated [Stats]
        public static Stats of(List<WeatherRecord> data, Month month, double threshold) {
            var monthData = data.stream()
                    .filter(record -> record.date().getMonth() == month)
                    .toList();

            var categoryCounts = Categorizer.countByCategory(monthData);

            return new Stats(
                    WeatherAnalyzer.averageTemperature(monthData, month),
                    WeatherAnalyzer.daysAbove(monthData, threshold).size(),
                    WeatherAnalyzer.countRainyDays(monthData),
                    categoryCounts,
                    WeatherAnalyzer.hottestDay(monthData)
            );
        }
    }

    /// Formats an [OptionalDouble] for display.
    ///
    /// @param value optional average temperature
    /// @return formatted temperature string or `N/A`
    private static String formatAverage(OptionalDouble value) {
        return value.isPresent() ? String.format(Locale.ROOT, "%.2f C", value.getAsDouble()) : "N/A";
    }

    /// Formats category counts for display.
    ///
    /// @param categoryCounts counts by weather category
    /// @return text like `HOT=28, WARM=3` or `none`
    private static String formatCategories(Map<WeatherCategory, Long> categoryCounts) {
        if (categoryCounts == null || categoryCounts.isEmpty()) {
            return "none";
        }

        return categoryCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format(Locale.ROOT, "%s=%d", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    /// Formats the hottest-day value for display.
    ///
    /// @param hottest optional hottest weather record
    /// @return human-readable hottest-day text or `N/A`
    private static String formatHottest(Optional<WeatherRecord> hottest) {
        return hottest.map(record -> String.format(
                Locale.ROOT,
                "%s | %.2f C | Humidity %d%% | Precip %.2f",
                record.date(),
                record.temperature(),
                record.humidity(),
                record.precipitation())).orElse("N/A");
    }

    /// Generates a full multiline text report for one month and threshold.
    ///
    /// ```java
    /// var summary = ReportBuilder.summary(data, Month.AUGUST, 33.0);
    /// ```
    ///
    /// @param data weather records
    /// @param month target month
    /// @param threshold temperature threshold for "above" count
    /// @return a formatted text report
    static String summary(List<WeatherRecord> data, Month month, double threshold) {
        var stats = Stats.of(data, month, threshold);

        return """
                Weather Data Report
                ===================
                Month: %s
                Threshold: %.2f C
                Average Temperature: %s
                Days Above Threshold: %d
                Rainy Days: %d
                Category Breakdown: %s
                Hottest Day: %s
                """.stripIndent().formatted(
                month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                threshold,
                formatAverage(stats.average()),
                stats.daysAbove(),
                stats.rainy(),
                formatCategories(stats.categoryCounts()),
                formatHottest(stats.hottest())
        );
    }

    /// Converts a weather summary into valid HTML for GUI/export usage.
    ///
    /// ```java
    /// var html = ReportBuilder.toHtml(data, Month.AUGUST, 33.0);
    /// ```
    ///
    /// @param data weather records
    /// @param month target month
    /// @param threshold temperature threshold
    /// @return an HTML representation of the report
    static String toHtml(List<WeatherRecord> data, Month month, double threshold) {
        var stats = Stats.of(data, month, threshold);
        var categoryRows = stats.categoryCounts().isEmpty()
                ? "<tr><td>none</td><td>0</td></tr>"
                : stats.categoryCounts().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> String.format(
                                Locale.ROOT,
                                "<tr><td>%s</td><td>%d</td></tr>",
                                entry.getKey(),
                                entry.getValue()))
                        .collect(Collectors.joining());

        return """
                <html>
                  <head>
                    <title>Weather Report</title>
                  </head>
                  <body>
                    <h1>Weather Data Report</h1>
                    <p><strong>Month:</strong> %s</p>
                    <p><strong>Threshold:</strong> %.2f C</p>
                    <p><strong>Average Temperature:</strong> %s</p>
                    <p><strong>Days Above Threshold:</strong> %d</p>
                    <p><strong>Rainy Days:</strong> %d</p>
                    <table>
                      <thead><tr><th>Category</th><th>Count</th></tr></thead>
                      <tbody>%s</tbody>
                    </table>
                    <p><strong>Hottest Day:</strong> %s</p>
                  </body>
                </html>
                """.stripIndent().formatted(
                month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                threshold,
                formatAverage(stats.average()),
                stats.daysAbove(),
                stats.rainy(),
                categoryRows,
                formatHottest(stats.hottest())
        );
    }
}
