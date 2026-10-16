package weather.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import weather.analysis.Categorizer;
import weather.analysis.WeatherAnalyzer;
import weather.io.Result;
import weather.io.WeatherCsvReader;
import weather.model.WeatherCategory;
import weather.model.WeatherRecord;

import java.nio.file.Path;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/// JavaFX GUI for Weather Data Analyzer.
///
/// JavaFX requires an explicit class extending [Application].
/// This is the only explicit class in the project by requirement exception; domain logic remains in records/interfaces.
///
/// ```java
/// Application.launch(WeatherGui.class, args);
/// ```
public final class WeatherGui extends Application {

    private final ObservableList<WeatherRecord> masterData = FXCollections.observableArrayList();
    private final ObservableList<WeatherRecord> monthData = FXCollections.observableArrayList();

    private ComboBox<Month> monthCombo;
    private Slider thresholdSlider;
    private Spinner<Double> thresholdSpinner;
    private Label averageLabel;
    private Label rainyLabel;
    private Label statusLabel;
    private Label categoryLabel;
    private LineChart<Number, Number> lineChart;
    private TableView<WeatherRecord> table;

    @Override
    public void start(Stage stage) {
        monthCombo = new ComboBox<>();
        monthCombo.setItems(FXCollections.observableArrayList(Month.values()));
        monthCombo.getSelectionModel().select(Month.AUGUST);
        monthCombo.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Month item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            }
        });
        monthCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Month item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            }
        });

        thresholdSlider = new Slider(-10, 50, 33);
        thresholdSlider.setShowTickLabels(true);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.setMajorTickUnit(10);
        thresholdSlider.setMinorTickCount(4);

        thresholdSpinner = new Spinner<>();
        thresholdSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 50, 33, 0.5));
        thresholdSpinner.setEditable(true);

        thresholdSlider.valueProperty().addListener((_, _, newValue) ->
                thresholdSpinner.getValueFactory().setValue(round1(newValue.doubleValue())));
        thresholdSpinner.valueProperty().addListener((_, _, newValue) ->
                thresholdSlider.setValue(newValue));

        var refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("primary-button");

        var loadButton = new Button("Reload CSV");
        loadButton.getStyleClass().add("secondary-button");

        averageLabel = new Label("Average Temperature: N/A");
        rainyLabel = new Label("Rainy Days: N/A");
        statusLabel = new Label("Loading weather data...");
        categoryLabel = new Label("Category Mix: N/A");

        averageLabel.getStyleClass().add("metric");
        rainyLabel.getStyleClass().add("metric");
        statusLabel.getStyleClass().add("status");
        categoryLabel.getStyleClass().add("metric");

        table = createTable();
        lineChart = createChart();

        refreshButton.setOnAction(_ -> updateView());
        loadButton.setOnAction(_ -> loadCsvVirtual());
        monthCombo.setOnAction(_ -> updateView());

        var controls = new HBox(10,
                new Label("Month"), monthCombo,
                new Label("Threshold"), thresholdSlider,
                thresholdSpinner,
                refreshButton,
                loadButton);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(12));
        controls.getStyleClass().add("card");
        HBox.setHgrow(thresholdSlider, Priority.ALWAYS);

        var topStats = new HBox(12, averageLabel, rainyLabel, categoryLabel);
        topStats.setPadding(new Insets(12));
        topStats.getStyleClass().add("card");

        var chartBox = new VBox(8, new Label("Daily Temperature Trend"), lineChart);
        chartBox.setPadding(new Insets(12));
        chartBox.getStyleClass().add("card");
        VBox.setVgrow(lineChart, Priority.ALWAYS);

        var tableBox = new VBox(8, new Label("Daily Records"), table);
        tableBox.setPadding(new Insets(12));
        tableBox.getStyleClass().add("card");
        VBox.setVgrow(table, Priority.ALWAYS);

        var center = new VBox(12, topStats, chartBox, tableBox);
        center.setPadding(new Insets(12));
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        var footer = new StackPane(statusLabel);
        footer.setPadding(new Insets(8, 12, 12, 12));

        var root = new BorderPane();
        root.setTop(controls);
        root.setCenter(center);
        root.setBottom(footer);
        root.getStyleClass().add("root-pane");

        var scene = new Scene(root, 1100, 760);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Weather Data Analyzer");
        stage.setScene(scene);
        stage.show();

        loadCsvVirtual();
    }

    private TableView<WeatherRecord> createTable() {
        var tableView = new TableView<WeatherRecord>(monthData);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        var dateCol = new TableColumn<WeatherRecord, String>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().date().toString()));

        var tempCol = new TableColumn<WeatherRecord, Double>("Temp (C)");
        tempCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().temperature()));
        tempCol.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format(Locale.ROOT, "%.2f", item));
            }
        });

        var humidityCol = new TableColumn<WeatherRecord, Integer>("Humidity (%)");
        humidityCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().humidity()));

        var precipCol = new TableColumn<WeatherRecord, Double>("Precip (mm)");
        precipCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().precipitation()));
        precipCol.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format(Locale.ROOT, "%.2f", item));
            }
        });

        var categoryCol = new TableColumn<WeatherRecord, WeatherCategory>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleObjectProperty<>(Categorizer.categorize(data.getValue().temperature())));

        tableView.getColumns().setAll(dateCol, tempCol, humidityCol, precipCol, categoryCol);

        tableView.setRowFactory(_ -> new TableRow<>() {
            @Override
            protected void updateItem(WeatherRecord item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("cat-hot", "cat-warm", "cat-mild", "cat-cold", "cat-freezing");
                if (empty || item == null) {
                    return;
                }
                switch (Categorizer.categorize(item.temperature())) {
                    case HOT -> getStyleClass().add("cat-hot");
                    case WARM -> getStyleClass().add("cat-warm");
                    case MILD -> getStyleClass().add("cat-mild");
                    case COLD -> getStyleClass().add("cat-cold");
                    case FREEZING -> getStyleClass().add("cat-freezing");
                }
            }
        });

        return tableView;
    }

    private LineChart<Number, Number> createChart() {
        var xAxis = new NumberAxis();
        xAxis.setLabel("Day");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(32);
        xAxis.setTickUnit(5);                 // labels: 0, 5, 10, 15, 20, 25, 30
        xAxis.setMinorTickCount(0);           // no in-between ticks (the source of 2.5-style labels)
        xAxis.setMinorTickVisible(false);
        xAxis.setForceZeroInRange(false);

        var yAxis = new NumberAxis();
        yAxis.setLabel("Temperature (C)");

        var chart = new LineChart<>(xAxis, yAxis);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("temp-chart");
        return chart;
    }

    private void loadCsvVirtual() {
        statusLabel.setText("Loading weatherdata.csv on virtual thread...");

        AtomicReference<Result<List<WeatherRecord>>> resultRef = new AtomicReference<>();
        var loader = Thread.ofVirtual().start(() -> resultRef.set(WeatherCsvReader.read(Path.of("weatherdata.csv"))));

        Thread.ofVirtual().start(() -> {
            try {
                loader.join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> statusLabel.setText("Loading interrupted."));
                return;
            }

            Platform.runLater(() -> {
                var result = resultRef.get();
                switch (result) {
                    case Result.Success(var records, var skipped) -> {
                        masterData.setAll(records);
                        statusLabel.setText("Loaded %d records (%d skipped).".formatted(records.size(), skipped));
                        updateView();
                    }
                    case Result.Failure(var message) -> {
                        masterData.clear();
                        monthData.clear();
                        lineChart.getData().clear();
                        averageLabel.setText("Average Temperature: N/A");
                        rainyLabel.setText("Rainy Days: N/A");
                        categoryLabel.setText("Category Mix: N/A");
                        statusLabel.setText("Failed to load data: %s".formatted(message));
                    }
                }
            });
        });
    }

    private void updateView() {
        if (masterData.isEmpty()) {
            return;
        }

        var selectedMonth = monthCombo.getValue() == null ? Month.AUGUST : monthCombo.getValue();
        var threshold = thresholdSpinner.getValue();

        var filtered = masterData.stream()
                .filter(record -> record.date().getMonth() == selectedMonth)
                .sorted(Comparator.comparing(WeatherRecord::date))
                .toList();

        monthData.setAll(filtered);

        var average = WeatherAnalyzer.averageTemperature(filtered, selectedMonth);
        var rainy = WeatherAnalyzer.countRainyDays(filtered);
        var categorySummary = Categorizer.countByCategory(filtered).entrySet().stream()
                .map(entry -> "%s=%d".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        var daysAbove = WeatherAnalyzer.daysAbove(filtered, threshold).size();

        averageLabel.setText("Average Temperature: %s".formatted(formatAverage(average)));
        rainyLabel.setText("Rainy Days: %d  |  Days Above %.1f C: %d".formatted(rainy, threshold, daysAbove));
        categoryLabel.setText("Category Mix: %s".formatted(categorySummary.isBlank() ? "none" : categorySummary));

        var series = new XYChart.Series<Number, Number>();
        for (var record : filtered) {
            series.getData().add(new XYChart.Data<>(record.date().getDayOfMonth(), record.temperature()));
        }
        lineChart.getData().setAll(series);

        statusLabel.setText("Showing %s (%d days).".formatted(
                selectedMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                filtered.size()));
    }

    private static String formatAverage(OptionalDouble average) {
        return average.isPresent()
                ? String.format(Locale.ROOT, "%.2f C", average.getAsDouble())
                : "N/A";
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
