package simulation.software.codebase;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
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
import java.util.Random;

public class DigitalToAnalogConversion {
    private final Stage stage;
    private final LineChart<Number, Number> dacChart;
    private final XYChart.Series<Number, Number> series;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final Label snrLabel;
    private final Slider bitDepthSlider;
    private final Slider samplingRateSlider;
    private final Slider nonlinearitySlider;
    private final Slider thermalNoiseSlider;
    private double[] waveformData;
    private double duration;
    private double[] timeData;
    private double[] dacOutput;

    public DigitalToAnalogConversion() {
        stage = new Stage();
        stage.setTitle("DAC Simulation");

        // Chart setup
        xAxis = new NumberAxis(0, 20, 5);
        xAxis.setLabel("Time (ms)");
        xAxis.setAutoRanging(false);

        yAxis = new NumberAxis(-5, 5, 1);
        yAxis.setLabel("Amplitude (V)");
        yAxis.setAutoRanging(false);

        dacChart = new LineChart<>(xAxis, yAxis);
        dacChart.setCreateSymbols(false);
        dacChart.setLegendVisible(false);
        dacChart.setPrefWidth(600);

        series = new XYChart.Series<>();
        dacChart.getData().add(series);

        // SNR label
        snrLabel = new Label("SNR: 0.0 dB");
        snrLabel.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: #000000;" +
                        "-fx-padding: 5;"
        );

        // Sliders
        bitDepthSlider = new Slider(4, 16, 12);
        bitDepthSlider.setMajorTickUnit(4);
        bitDepthSlider.setMinorTickCount(0);
        bitDepthSlider.setShowTickMarks(true);
        bitDepthSlider.setShowTickLabels(true);
        bitDepthSlider.setSnapToTicks(true);

        samplingRateSlider = new Slider(1000, 20000, 10000);
        samplingRateSlider.setMajorTickUnit(5000);
        samplingRateSlider.setMinorTickCount(4);
        samplingRateSlider.setShowTickMarks(true);
        samplingRateSlider.setShowTickLabels(true);

        nonlinearitySlider = new Slider(0, 0.1, 0);
        nonlinearitySlider.setMajorTickUnit(0.05);
        nonlinearitySlider.setMinorTickCount(4);
        nonlinearitySlider.setShowTickMarks(true);
        nonlinearitySlider.setShowTickLabels(true);

        thermalNoiseSlider = new Slider(0, 0.1, 0);
        thermalNoiseSlider.setMajorTickUnit(0.05);
        thermalNoiseSlider.setMinorTickCount(4);
        thermalNoiseSlider.setShowTickMarks(true);
        thermalNoiseSlider.setShowTickLabels(true);

        // Control labels
        Label bitDepthLabel = new Label("Bit Depth:");
        Label samplingRateLabel = new Label("Sampling Rate (Hz):");
        Label nonlinearityLabel = new Label("Non-linearity:");
        Label thermalNoiseLabel = new Label("Thermal Noise (V):");
        Label quantizationHeader = new Label("Quantization");
        Label samplingHeader = new Label("Sampling");
        Label imperfectionsHeader = new Label("Analog Imperfections");
        for (Label label : new Label[]{bitDepthLabel, samplingRateLabel, nonlinearityLabel, thermalNoiseLabel}) {
            label.setStyle(
                    "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 12px;"
            );
        }
        for (Label header : new Label[]{quantizationHeader, samplingHeader, imperfectionsHeader}) {
            header.setStyle(
                    "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;"
            );
        }

        // Export button
        Button exportButton = new Button("Export DAC Data");
        exportButton.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-border-color: #666666 #333333 #333333 #666666;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 5;"
        );
        exportButton.setOnAction(e -> exportDacData());

        // Control panel layout
        VBox controlPanel = new VBox(15,
                quantizationHeader,
                new HBox(10, bitDepthLabel, bitDepthSlider),
                samplingHeader,
                new HBox(10, samplingRateLabel, samplingRateSlider),
                imperfectionsHeader,
                new HBox(10, nonlinearityLabel, nonlinearitySlider),
                new HBox(10, thermalNoiseLabel, thermalNoiseSlider)
        );
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setPrefWidth(200);
        controlPanel.setStyle("-fx-background-color: #000000;");

        // Top bar
        HBox topBar = new HBox(20, snrLabel, exportButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #000000;");

        // Main layout
        BorderPane root = new BorderPane();
        root.setCenter(dacChart);
        root.setTop(topBar);
        root.setRight(controlPanel);

        Scene scene = new Scene(root, 800, 600);

        // Embedded CSS styling
        dacChart.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: #666666 #333333 #333333 #666666;" +
                        "-fx-border-width: 2;"
        );
        dacChart.lookup(".chart-plot-background").setStyle("-fx-background-color: #000000;");
        dacChart.lookup(".chart-series-line").setStyle("-fx-stroke: #FFFFCC; -fx-stroke-width: 2;");
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
        bitDepthSlider.setStyle(
                "-fx-control-inner-background: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-tick-label-fill: #FFFFFF;"
        );
        samplingRateSlider.setStyle(
                "-fx-control-inner-background: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-tick-label-fill: #FFFFFF;"
        );
        nonlinearitySlider.setStyle(
                "-fx-control-inner-background: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-tick-label-fill: #FFFFFF;"
        );
        thermalNoiseSlider.setStyle(
                "-fx-control-inner-background: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-tick-label-fill: #FFFFFF;"
        );

        stage.setScene(scene);

        // Update on slider value change
        for (Slider slider : new Slider[]{bitDepthSlider, samplingRateSlider, nonlinearitySlider, thermalNoiseSlider}) {
            slider.valueProperty().addListener((obs, old, newVal) -> updateDacChart());
        }
    }

    public void show() {
        stage.show();
    }

    public void setWaveformData(double[] waveform, double duration) {
        if (waveform == null || waveform.length == 0 || duration <= 0) {
            waveformData = null;
            timeData = null;
            dacOutput = null;
            series.getData().clear();
            snrLabel.setText("SNR: N/A");
            return;
        }
        this.waveformData = waveform;
        this.duration = duration;
        updateWaveformData();
        updateDacChart();
    }

    public void startUpdating() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (waveformData != null) {
                    updateDacChart();
                }
            }
        };
        timer.start();
    }

    private void updateWaveformData() {
        // Generate time array matching waveform duration
        int numSamples = waveformData.length;
        timeData = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            timeData[i] = i * duration / (numSamples - 1);
        }
        xAxis.setUpperBound(duration * 1000); // Convert to ms
        xAxis.setTickUnit(duration * 1000 / 4);
    }

    private void updateDacChart() {
        series.getData().clear();
        if (waveformData == null || timeData == null) return;

        int bitDepth = (int) bitDepthSlider.getValue();
        double samplingRate = samplingRateSlider.getValue();
        double nonlinearity = nonlinearitySlider.getValue();
        double thermalNoiseAmp = thermalNoiseSlider.getValue();
        Random rand = new Random();

        // Quantization levels
        int levels = 1 << bitDepth;
        double maxAmp = 5.0; // Matches waveform amplitude range
        double quantizationStep = 2 * maxAmp / levels;

        // Resample based on sampling rate
        int newSamples = (int) (duration * samplingRate);
        double[] sampledTime = new double[newSamples];
        double[] sampledOutput = new double[newSamples];
        dacOutput = new double[newSamples];

        // Linear interpolation for resampling
        for (int i = 0; i < newSamples; i++) {
            double t = i / samplingRate;
            sampledTime[i] = t;
            // Find interpolation points
            int idx = (int) (t / duration * (waveformData.length - 1));
            if (idx >= waveformData.length - 1) idx = waveformData.length - 2;
            double frac = (t - timeData[idx]) / (timeData[idx + 1] - timeData[idx]);
            sampledOutput[i] = waveformData[idx] + frac * (waveformData[idx + 1] - waveformData[idx]);
        }

        // Apply DAC effects
        double signalPower = 0;
        double noisePower = 0;
        for (int i = 0; i < newSamples; i++) {
            // Quantization
            double value = sampledOutput[i];
            int quantLevel = (int) Math.round(value / quantizationStep);
            value = quantLevel * quantizationStep;
            value = Math.max(-maxAmp, Math.min(maxAmp, value)); // Clip

            // Non-linearity: y = x + k*x^3
            double nonlinear = value + nonlinearity * Math.pow(value, 3);
            value = Math.max(-maxAmp, Math.min(maxAmp, nonlinear));

            // Thermal noise
            double noise = thermalNoiseAmp * (2 * rand.nextDouble() - 1);
            value += noise;

            dacOutput[i] = value;
            // For SNR calculation
            signalPower += Math.pow(sampledOutput[i], 2);
            noisePower += Math.pow(value - sampledOutput[i], 2);

            // Update chart
            series.getData().add(new XYChart.Data<>(sampledTime[i] * 1000, dacOutput[i]));
        }

        // Compute SNR
        if (signalPower > 0 && noisePower > 0) {
            double snr = 10 * Math.log10(signalPower / noisePower);
            snrLabel.setText(String.format("SNR: %.1f dB", snr));
        } else {
            snrLabel.setText("SNR: N/A");
        }
    }

    private void exportDacData() {
        if (dacOutput == null || timeData == null) {
            return; // No data to export
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save DAC Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("dac_data.csv");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Time,Amplitude\n");
                for (int i = 0; i < dacOutput.length; i++) {
                    writer.write(String.format("%.6f,%.6f\n", timeData[i] * 1000, dacOutput[i]));
                }
            } catch (IOException e) {
                System.err.println("Error writing CSV file: " + e.getMessage());
            }
        }
    }

    public void close() {
        stage.close();
    }
}