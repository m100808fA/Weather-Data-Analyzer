package weather.io;

/// Represents an operation outcome with either a successful value or a failure message.
///
/// ```java
/// Result<String> ok = new Result.Success<>("done", 0);
/// Result<String> error = new Result.Failure<>("file missing");
/// ```
///
/// @param <T> value type for successful outcomes
public sealed interface Result<T> permits Result.Success, Result.Failure {

    /// Successful operation with produced value and number of skipped malformed lines.
    ///
    /// ```java
    /// var success = new Result.Success<>(List.of(), 2);
    /// ```
    ///
    /// @param value parsed or computed value
    /// @param skippedLines malformed lines skipped during processing
    /// @param <T> value type
    record Success<T>(T value, int skippedLines) implements Result<T> {
    }

    /// Failed operation with an explanatory message.
    ///
    /// ```java
    /// var failure = new Result.Failure<List<String>>("Cannot read file");
    /// ```
    ///
    /// @param message human-readable failure reason
    /// @param <T> value type that would have been returned on success
    record Failure<T>(String message) implements Result<T> {
    }
}
