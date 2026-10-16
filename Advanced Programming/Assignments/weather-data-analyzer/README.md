# 🌤️ Weather Data Analyzer

![Java 23](https://img.shields.io/badge/Java-23-orange?logo=openjdk)
![Maven](https://img.shields.io/badge/Build-Maven-blue?logo=apachemaven)
![JavaFX](https://img.shields.io/badge/GUI-JavaFX-green)
![JUnit 5](https://img.shields.io/badge/Tests-JUnit%205-brightgreen?logo=junit5)
![No explicit classes](https://img.shields.io/badge/Design-records%20%2B%20interfaces-purple)

A weather data analysis application written in **modern Java (15 → 23)**. It loads daily weather
measurements from a CSV file, analyzes them with **lambdas and streams**, categorizes days with an
**enhanced `switch`**, and presents the results in a **JavaFX GUI styled with CSS**. Text and HTML
report generation is also available through `ReportBuilder`.

The domain logic is built **without explicit classes**: only records, interfaces, enums, and sealed
types are used (the only exceptions are the JavaFX `Application` subclass and the JUnit test classes, which the frameworks require).

---

## ✨ Features

- 📄 **CSV parsing** of `Date,Temperature,Humidity,Precipitation` with malformed lines counted, not fatal
- 🌡️ **Average temperature** for a specific month
- 🔥 **Days above a temperature threshold**
- 🌧️ **Count of rainy days** (precipitation > 0)
- 🏷️ **Weather categories** (`HOT`, `WARM`, `MILD`, `COLD`, `FREEZING`) via an enhanced `switch`
- 📝 **Text-block reports** in plain text and HTML
- 🖥️ **JavaFX GUI** with a month selector, threshold slider/spinner, line chart, color-coded table, and CSS theme
- ⚡ **Non-blocking CSV loading** on virtual threads, with a *Reload CSV* button
- 📚 **Markdown Javadoc** (`///` comments) with code examples on every type and method

---

## 🧰 Modern Java Features Used

| Feature | Java version | Where it is used |
|---|---|---|
| **Records** | 16 | `WeatherRecord`, `Result.Success`, `Result.Failure`, `ReportBuilder.Stats` |
| **Compact canonical constructor** (validation) | 16 | `WeatherRecord` |
| **Sealed interfaces** | 17 | `Result` (permits `Success`, `Failure`) |
| **Enhanced `switch` expressions** (arrow labels) | 14 | `Categorizer.categorize`, `WeatherGui` row factory |
| **Pattern matching for `switch`** with `when` guards | 21 | `Categorizer.categorize`, `Categorizer.describe` |
| **Record patterns** (deconstruction) | 21 | `Categorizer.describe`, `WeatherGui.loadCsvVirtual` |
| **Unnamed patterns and variables (`_`)** | 22 | `Categorizer.describe`, lambdas in `WeatherGui` |
| **Text blocks** | 15 | `ReportBuilder.summary`, `ReportBuilder.toHtml` |
| **`String.stripIndent()` / `formatted()`** | 15 | `ReportBuilder` |
| **Virtual threads** | 21 | `WeatherGui.loadCsvVirtual` |
| **Lambdas, method references, Streams** | 8+ | `WeatherAnalyzer`, `WeatherCsvReader`, `Categorizer` |
| **`Stream.toList()`** | 16 | `WeatherAnalyzer`, `WeatherCsvReader` |
| **`var` (local type inference)** | 10+ | Throughout |
| **`Optional` / `OptionalDouble`** | 8+ | `WeatherRecord.fromCsvLine`, `WeatherAnalyzer` |
| **Static methods in interfaces** (incl. `main`) | 8+ | `App`, `WeatherLauncher`, `WeatherAnalyzer`, `Categorizer` |
| **Markdown Javadoc (`///`)** | 23 (JEP 467) | Every source file |

---

## 📁 Project Structure

```text
weather-data-analyzer/
├── pom.xml
├── weatherdata.csv                  # sample dataset (project root)
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/weather/
    │   │   ├── App.java                    # entry point (interface with main)
    │   │   ├── model/
    │   │   │   ├── WeatherRecord.java      # record: one CSV row
    │   │   │   └── WeatherCategory.java    # enum: HOT .. FREEZING
    │   │   ├── io/
    │   │   │   ├── Result.java             # sealed interface: Success | Failure
    │   │   │   └── WeatherCsvReader.java   # CSV parsing with streams
    │   │   ├── analysis/
    │   │   │   ├── WeatherAnalyzer.java    # averages, thresholds, rainy days
    │   │   │   └── Categorizer.java        # enhanced switch + record patterns
    │   │   ├── report/
    │   │   │   └── ReportBuilder.java      # text-block text/HTML reports
    │   │   └── ui/
        │   │       ├── WeatherLauncher.java    # launcher interface for JavaFX
    │   │       └── WeatherGui.java         # JavaFX Application (the only class)
    │   └── resources/
    │       └── style.css                   # GUI theme
    └── test/java/weather/
        ├── analysis/
        │   ├── CategorizerTest.java        # category boundaries, record-pattern descriptions
        │   └── WeatherAnalyzerTest.java    # averages, thresholds, rainy days, hottest day
        ├── io/
        │   └── WeatherCsvReaderTest.java   # valid/malformed rows, missing file
        └── report/
            └── ReportBuilderTest.java      # text report metrics
```

> Adjust the tree if your file names differ slightly.

---

## ✅ Requirements

- **JDK 23** (needed for Markdown Javadoc comments and unnamed patterns)
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- **JavaFX** is pulled in automatically through Maven (no manual install)

---

## 🚀 Build and Run

```bash
# Build and run the tests
./mvnw clean test

# Launch the JavaFX GUI
./mvnw -q javafx:run
```

In IntelliJ you can also click the green ▶ next to `main` in `App.java`.

> ⚠️ `weatherdata.csv` must be in the **project root**, next to `pom.xml`.

---

## 📄 Sample CSV Format

```csv
Date,Temperature,Humidity,Precipitation
2023-08-01,32.5,65,0.0
2023-08-02,35.0,60,0.2
```

| Column | Type | Rules |
|---|---|---|
| `Date` | ISO date (`yyyy-MM-dd`) | required |
| `Temperature` | decimal, °C | required |
| `Humidity` | integer, % | `0..100` |
| `Precipitation` | decimal, mm | `>= 0` |

The included dataset covers **2023-06-01 to 2023-11-30 (183 days)**.

---

## 🖨️ Sample Output

The application is GUI-based, so there is no console output. `ReportBuilder` can produce a text report
(and an HTML version) for any month and threshold. Example of its output for the included dataset.

Report for **August** with a threshold of **33.0 °C** (`ReportBuilder.summary(data, Month.AUGUST, 33.0)`):

```text
Weather Data Report
===================
Month: August
Threshold: 33.00 C
Average Temperature: 34.02 C
Days Above Threshold: 20
Rainy Days: 4
Category Breakdown: HOT=28, WARM=3
Hottest Day: 2023-08-06 | 43.80 C | Humidity 62% | Precip 0.00
```

Report for **November** with a threshold of **10.0 °C**:

```text
Weather Data Report
===================
Month: November
Threshold: 10.00 C
Average Temperature: 11.48 C
Days Above Threshold: 22
Rainy Days: 9
Category Breakdown: MILD=3, COLD=27
Hottest Day: 2023-11-19 | 15.20 C | Humidity 76% | Precip 0.00
```

---

## 🖼️ Screenshots

| GUI | Description |
|---|---|
| ![Main window](docs/screenshots/gui-main.png) | Main window with chart, table, and statistics |
| ![Threshold](docs/screenshots/gui-threshold.png) | Threshold slider and spinner in action |

> 📸 Replace the placeholders by saving your screenshots into `docs/screenshots/`.

---

## 🧪 Running the Tests

```bash
./mvnw test
```

The JUnit 5 tests cover:

| Test class | What it verifies |
|---|---|
| `CategorizerTest` | Category boundaries (`29.9` vs `30.0`, etc.), record-pattern descriptions, per-category counts |
| `WeatherAnalyzerTest` | Monthly average, empty month, strict "above threshold", rainy days, monthly grouping, hottest day |
| `WeatherCsvReaderTest` | Valid rows loaded, malformed/invalid rows counted as skipped (text-block CSV in a temp directory), missing file returns `Failure` |
| `ReportBuilderTest` | Report contains the expected average, days-above, and rainy-day values |

---

## 📚 Generating the Javadoc

The documentation uses **Markdown comments (`///`)**, introduced in JDK 23 (JEP 467).

```bash
./mvnw javadoc:javadoc
```

Open `target/reports/apidocs/index.html` (or `target/site/apidocs/index.html`, depending on the plugin
version). Use **maven-javadoc-plugin 3.11.0 or newer** and run Maven with **JDK 23**.

Example of the comment style used in the code:

```java
/// Counts days where precipitation is greater than zero.
///
/// ```java
/// var rainyCount = WeatherAnalyzer.countRainyDays(data);
/// ```
///
/// @param data weather records
/// @return number of rainy days
static long countRainyDays(List<WeatherRecord> data) {}
```

---

## 🧱 Design Notes: "No Explicit Classes"

The assignment requires a design without explicit classes. This is how it is achieved:

| Need | Solution |
|---|---|
| Data holders | **Records** (`WeatherRecord`, `Stats`, `Result.Success/Failure`) |
| Utility functions | **Interfaces with static methods** (`WeatherAnalyzer`, `Categorizer`, `ReportBuilder`, `WeatherCsvReader`) |
| Entry points | **Interfaces with a static `main`** (`App`, `WeatherLauncher`) |
| Fixed value sets | **Enum** (`WeatherCategory`) |
| Success/failure modelling | **Sealed interface** (`Result`) |
| Behavior as data | **Lambdas** and method references |

**The exceptions:** frameworks force two kinds of explicit class, and both are kept out of the domain logic:

- **`WeatherGui`**: JavaFX requires a subclass of `javafx.application.Application`. It is the only explicit
  class in `src/main`, and it delegates all logic to the records and interfaces above.
- **JUnit test classes** (`src/test`): JUnit 5 discovers tests in classes. They contain no application logic.

All application logic remains free of classes.

---

## 👤 Author and License

- **Author:** *MUHAMMAD FASEEH* (https://github.com/m100808fA/Weather-Data-Analyzer.git)
- **Course / Assignment:** *Modern Java Weather Data Analyzer*
- **License:** MIT — see `LICENSE` (add one via GitHub → *Add file → Create new file → LICENSE*)