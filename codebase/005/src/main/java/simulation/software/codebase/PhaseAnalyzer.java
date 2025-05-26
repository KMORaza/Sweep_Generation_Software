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

public class PhaseAnalyzer {
    private final Stage stage;
    private final LineChart<Number, Number> phaseChart;
    private final XYChart.Series<Number, Number> phaseSeries;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final Label phaseLabel;
    private final Label fundamentalLabel;
    private final Slider windowSizeSlider;
    private final ComboBox<String> channelComboBox;
    private double[] leftWaveform;
    private double[] rightWaveform;
    private double duration;
    private double lastPhaseDifference;
    private double lastFundamentalFreq;
    private FFTCalculator fftCalculator;

    public PhaseAnalyzer() {
        stage = new Stage();
        stage.setTitle("Phase Analyzer");

        // Chart setup
        xAxis = new NumberAxis(0, 20, 5);
        xAxis.setLabel("Time (ms)");
        xAxis.setAutoRanging(false);

        yAxis = new NumberAxis(-180, 180, 45);
        yAxis.setLabel("Phase Difference (°)");
        yAxis.setAutoRanging(false);

        phaseChart = new LineChart<>(xAxis, yAxis);
        phaseChart.setCreateSymbols(false);
        phaseChart.setTitle("Phase");
        phaseSeries = new XYChart.Series<>();
        phaseChart.getData().add(phaseSeries);

        // Labels
        phaseLabel = new Label("Phase: 0.0°");
        fundamentalLabel = new Label("Fundamental: 0.0 Hz");
        for (Label label : new Label[]{phaseLabel, fundamentalLabel}) {
            label.setStyle(
                    "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-color: #000000;" +
                            "-fx-padding: 5;"
            );
        }

        // Export button
        Button exportButton = new Button("Export Phase Data");
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
        HBox topBar = new HBox(20, phaseLabel, fundamentalLabel, exportButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #000000;");

        // Controls
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

        channelComboBox = new ComboBox<>(FXCollections.observableArrayList("Left vs. Right"));
        channelComboBox.setValue("Left vs. Right");
        channelComboBox.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: #000000;" +
                        "-fx-text-fill: #FFFFCC;"
        );

        Label windowSizeLabel = new Label("Window Size (ms):");
        Label channelLabel = new Label("Channel Pair:");
        Label controlsHeader = new Label("Phase Settings");
        for (Label label : new Label[]{windowSizeLabel, channelLabel}) {
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
                new HBox(10, windowSizeLabel, windowSizeSlider),
                new HBox(10, channelLabel, channelComboBox)
        );
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setPrefWidth(200);
        controlPanel.setStyle("-fx-background-color: #000000;");

        // Main layout
        BorderPane root = new BorderPane();
        root.setCenter(phaseChart);
        root.setTop(topBar);
        root.setRight(controlPanel);

        Scene scene = new Scene(root, 800, 600);

        // Embedded CSS
        phaseChart.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: #666666 #333333 #333333 #666666;" +
                        "-fx-border-width: 2;"
        );
        phaseChart.lookup(".chart-plot-background").setStyle("-fx-background-color: #000000;");
        phaseChart.lookupAll(".chart-series-line").forEach(node ->
                node.setStyle("-fx-stroke: #FFFFCC; -fx-stroke-width: 2;")
        );
        phaseChart.lookup(".chart-title").setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 14px;"
        );
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

        fftCalculator = new FFTCalculator();

        // Update on control changes
        windowSizeSlider.valueProperty().addListener((obs, old, newVal) -> updatePhase());
        channelComboBox.valueProperty().addListener((obs, old, newVal) -> updatePhase());
    }

    public void show() {
        stage.show();
    }

    public void setWaveformData(double[] waveform, double duration) {
        if (waveform == null || waveform.length == 0 || duration <= 0) {
            leftWaveform = null;
            rightWaveform = null;
            phaseSeries.getData().clear();
            phaseLabel.setText("Phase: N/A");
            fundamentalLabel.setText("Fundamental: N/A");
            return;
        }

        // Simulate two channels: left is input, right has a phase shift
        leftWaveform = waveform;
        rightWaveform = new double[waveform.length];
        double phaseShift = Math.toRadians(45); // Example 45° shift for right channel
        for (int i = 0; i < waveform.length; i++) {
            rightWaveform[i] = waveform[i] * Math.cos(phaseShift);
        }
        this.duration = duration;
        updatePhase();
    }

    public void startUpdating() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (leftWaveform != null && rightWaveform != null) {
                    updatePhase();
                }
            }
        };
        timer.start();
    }

    private void updatePhase() {
        phaseSeries.getData().clear();
        if (leftWaveform == null || rightWaveform == null) return;

        double windowSize = windowSizeSlider.getValue() / 1000; // ms to s
        int sampleRate = 44100; // Matches SpectrumAnalyzer

        // Extract windowed data
        int windowSamples = (int) (windowSize * sampleRate);
        if (windowSamples > leftWaveform.length) windowSamples = leftWaveform.length;
        if (windowSamples < 2) {
            phaseLabel.setText("Phase: N/A");
            fundamentalLabel.setText("Fundamental: N/A");
            return;
        }

        double[] leftWindow = new double[windowSamples];
        double[] rightWindow = new double[windowSamples];
        for (int i = 0; i < windowSamples; i++) {
            leftWindow[i] = leftWaveform[i % leftWaveform.length];
            rightWindow[i] = rightWaveform[i % rightWaveform.length];
        }

        // Compute FFT for phase
        double[] leftMagnitude = fftCalculator.computeFFTMagnitude(leftWindow);
        double[] rightMagnitude = fftCalculator.computeFFTMagnitude(rightWindow);
        double[] leftPhase = fftCalculator.computeFFTPhase(leftWindow);
        double[] rightPhase = fftCalculator.computeFFTPhase(rightWindow);

        if (leftMagnitude.length == 0 || rightMagnitude.length == 0) {
            phaseLabel.setText("Phase: N/A");
            fundamentalLabel.setText("Fundamental: N/A");
            return;
        }

        // Find fundamental frequency
        int fundamentalIndex = 0;
        double maxMagnitude = 0;
        for (int i = 1; i < leftMagnitude.length; i++) {
            if (leftMagnitude[i] > maxMagnitude) {
                maxMagnitude = leftMagnitude[i];
                fundamentalIndex = i;
            }
        }
        double frequencyResolution = sampleRate / (double) leftMagnitude.length / 2;
        double fundamentalFreq = fundamentalIndex * frequencyResolution;

        // Compute phase difference
        double phaseDifference = 0;
        if (fundamentalIndex < leftPhase.length && fundamentalIndex < rightPhase.length) {
            phaseDifference = rightPhase[fundamentalIndex] - leftPhase[fundamentalIndex];
            phaseDifference = Math.toDegrees(phaseDifference);
            // Normalize to [-180, 180]
            while (phaseDifference > 180) phaseDifference -= 360;
            while (phaseDifference < -180) phaseDifference += 360;
        }

        lastPhaseDifference = phaseDifference;
        lastFundamentalFreq = fundamentalFreq;

        // Update labels
        phaseLabel.setText(String.format("Phase: %.1f°", phaseDifference));
        fundamentalLabel.setText(String.format("Fundamental: %.1f Hz", fundamentalFreq));

        // Update phase chart (simulate over 20ms)
        double chartDuration = 0.02; // 20ms
        int chartSamples = (int) (chartDuration * sampleRate);
        for (int i = 0; i < chartSamples && i < leftWaveform.length; i++) {
            double timeMs = (i / (double) sampleRate) * 1000;
            phaseSeries.getData().add(new XYChart.Data<>(timeMs, phaseDifference)); // Constant for demo
        }
    }

    private void exportData() {
        if (leftWaveform == null || rightWaveform == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Phase Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("phase_data.csv");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Timestamp,PhaseDifference,FundamentalFrequency\n");
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.write(String.format("%s,%.6f,%.6f\n", timestamp, lastPhaseDifference, lastFundamentalFreq));
            } catch (IOException e) {
                System.err.println("Error writing CSV file: " + e.getMessage());
            }
        }
    }

    public void close() {
        stage.close();
    }
}