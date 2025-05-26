package simulation.software.codebase;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class FrequencyCounter {
    private final Stage stage;
    private final LineChart<Number, Number> waveformChart;
    private final XYChart.Series<Number, Number> waveformSeries;
    private final XYChart.Series<Number, Number> zeroCrossingSeries;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final Label frequencyLabel;
    private final Label periodLabel;
    private final ComboBox<String> methodComboBox;
    private final Slider windowSizeSlider;
    private double[] waveformData;
    private double duration;
    private double[] timeData;
    private double lastFrequency;
    private double lastPeriod;

    public FrequencyCounter() {
        stage = new Stage();
        stage.setTitle("Frequency Counter");

        // Chart setup
        xAxis = new NumberAxis(0, 20, 5);
        xAxis.setLabel("Time (ms)");
        xAxis.setAutoRanging(false);

        yAxis = new NumberAxis(-5, 5, 1);
        yAxis.setLabel("Amplitude (V)");
        yAxis.setAutoRanging(false);

        waveformChart = new LineChart<>(xAxis, yAxis);
        waveformChart.setCreateSymbols(false);
        waveformChart.setLegendVisible(false);
        waveformChart.setPrefWidth(600);

        waveformSeries = new XYChart.Series<>();
        zeroCrossingSeries = new XYChart.Series<>();
        waveformChart.getData().addAll(waveformSeries, zeroCrossingSeries);

        // Labels
        frequencyLabel = new Label("Frequency: 0.0 Hz");
        periodLabel = new Label("Period: 0.0 ms");
        for (Label label : new Label[]{frequencyLabel, periodLabel}) {
            label.setStyle(
                    "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-color: #000000;" +
                            "-fx-padding: 5;"
            );
        }

        // Export button
        Button exportButton = new Button("Export Data");
        exportButton.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-border-color: #666666 #333333 #333333 #666666;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 5;"
        );
        exportButton.setOnAction(e -> exportData());

        // Top bar
        HBox topBar = new HBox(20, frequencyLabel, periodLabel, exportButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #000000;");

        // Controls
        methodComboBox = new ComboBox<>(FXCollections.observableArrayList("Zero Crossing", "FFT-based"));
        methodComboBox.setValue("Zero Crossing");
        methodComboBox.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 12px;"
        );

        windowSizeSlider = new Slider(5, 50, 20);
        windowSizeSlider.setMajorTickUnit(15);
        windowSizeSlider.setMinorTickCount(4);
        windowSizeSlider.setShowTickMarks(true);
        windowSizeSlider.setShowTickLabels(true);
        windowSizeSlider.setStyle(
                "-fx-control-inner-background: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-tick-label-fill: #FFFFFF;"
        );

        Label methodLabel = new Label("Method:");
        Label windowSizeLabel = new Label("Window Size (ms):");
        Label controlsHeader = new Label("Measurement Settings");
        for (Label label : new Label[]{methodLabel, windowSizeLabel}) {
            label.setStyle(
                    "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 12px;"
            );
        }
        controlsHeader.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        // Control panel
        VBox controlPanel = new VBox(15,
                controlsHeader,
                new HBox(10, methodLabel, methodComboBox),
                new HBox(10, windowSizeLabel, windowSizeSlider)
        );
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setPrefWidth(200);
        controlPanel.setStyle("-fx-background-color: #000000;");

        // Main layout
        BorderPane root = new BorderPane();
        root.setCenter(waveformChart);
        root.setTop(topBar);
        root.setRight(controlPanel);

        Scene scene = new Scene(root, 800, 600);

        // Embedded CSS
        waveformChart.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: #666666 #333333 #333333 #666666;" +
                        "-fx-border-width: 2;"
        );
        waveformChart.lookup(".chart-plot-background").setStyle("-fx-background-color: #000000;");
        waveformChart.lookup(".chart-series-line").setStyle("-fx-stroke: #FFFFCC; -fx-stroke-width: 2;");
        xAxis.setStyle(
                "-fx-tick-label-fill: #FFFFFF;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 10px;" +
                        "-fx-minor-tick-visible: false;" +
                        "-fx-tick-mark-visible: true;" +
                        "-fx-grid-lines-visible: true;" +
                        "-fx-grid-line-stroke: #FFFFCC;"
        );
        yAxis.setStyle(
                "-fx-tick-label-fill: #FFFFFF;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 10px;" +
                        "-fx-grid-line-stroke: #FFFFCC;"
        );

        stage.setScene(scene);

        // Update on control changes
        methodComboBox.setOnAction(e -> updateMeasurements());
        windowSizeSlider.valueProperty().addListener((obs, old, newVal) -> updateMeasurements());
    }

    public void show() {
        stage.show();
    }

    public void setWaveformData(double[] waveform, double duration) {
        if (waveform == null || waveform.length == 0 || duration <= 0) {
            waveformData = null;
            timeData = null;
            waveformSeries.getData().clear();
            zeroCrossingSeries.getData().clear();
            frequencyLabel.setText("Frequency: N/A");
            periodLabel.setText("Period: N/A");
            return;
        }
        this.waveformData = waveform;
        this.duration = duration;
        updateWaveformData();
        updateMeasurements();
    }

    public void startUpdating() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (waveformData != null) {
                    updateMeasurements();
                }
            }
        };
        timer.start();
    }

    private void updateWaveformData() {
        int numSamples = waveformData.length;
        timeData = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            timeData[i] = i * duration / (numSamples - 1);
        }
    }

    private void updateMeasurements() {
        waveformSeries.getData().clear();
        zeroCrossingSeries.getData().clear();
        if (waveformData == null || timeData == null) return;

        double windowSize = windowSizeSlider.getValue() / 1000; // Convert ms to s
        String method = methodComboBox.getValue();

        // Extract windowed data
        int windowSamples = (int) (windowSize / duration * waveformData.length);
        if (windowSamples > waveformData.length) windowSamples = waveformData.length;
        if (windowSamples < 2) {
            frequencyLabel.setText("Frequency: N/A");
            periodLabel.setText("Period: N/A");
            return;
        }

        double[] windowedTime = new double[windowSamples];
        double[] windowedWaveform = new double[windowSamples];
        for (int i = 0; i < windowSamples; i++) {
            windowedTime[i] = timeData[i];
            windowedWaveform[i] = waveformData[i];
        }

        // Update chart
        xAxis.setUpperBound(windowSize * 1000);
        xAxis.setTickUnit(windowSize * 1000 / 4);
        for (int i = 0; i < windowSamples; i++) {
            waveformSeries.getData().add(new XYChart.Data<>(windowedTime[i] * 1000, windowedWaveform[i]));
        }

        // Calculate frequency
        double frequency = 0;
        if (method.equals("Zero Crossing")) {
            int crossings = 0;
            for (int i = 1; i < windowSamples; i++) {
                if (windowedWaveform[i - 1] <= 0 && windowedWaveform[i] > 0) {
                    crossings++;
                    zeroCrossingSeries.getData().add(new XYChart.Data<>(windowedTime[i] * 1000, 0));
                }
            }
            if (crossings > 0) {
                frequency = crossings / (2 * windowSize); // Half-cycles to full cycles
            }
        } else {
            FFTCalculator fft = new FFTCalculator();
            double[] spectrum = fft.calculateMagnitudeSpectrum(windowedWaveform, (int) (1 / (windowedTime[1] - windowedTime[0])));
            int maxIndex = 0;
            double maxMagnitude = 0;
            for (int i = 1; i < spectrum.length / 2; i++) {
                if (spectrum[i] > maxMagnitude) {
                    maxMagnitude = spectrum[i];
                    maxIndex = i;
                }
            }
            double frequencyResolution = 1 / windowSize;
            frequency = maxIndex * frequencyResolution;
        }

        // Update labels
        lastFrequency = frequency;
        lastPeriod = frequency > 0 ? 1000 / frequency : 0;
        frequencyLabel.setText(String.format("Frequency: %.1f Hz", frequency));
        periodLabel.setText(String.format("Period: %.1f ms", lastPeriod));
    }

    private void exportData() {
        if (waveformData == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Frequency Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("frequency_data.csv");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Timestamp,Frequency,Period\n");
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.write(String.format("%s,%.6f,%.6f\n", timestamp, lastFrequency, lastPeriod));
            } catch (IOException e) {
                System.err.println("Error writing CSV file: " + e.getMessage());
            }
        }
    }

    public void close() {
        stage.close();
    }
}