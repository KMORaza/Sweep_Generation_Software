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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SpectrumAnalyzer {
    private final Stage stage;
    private final LineChart<Number, Number> spectrumChart;
    private final XYChart.Series<Number, Number> series;
    private final FFTCalculator fftCalculator;
    private final NumberAxis yAxis;
    private final NumberAxis xAxis;
    private final Label peakLabel;
    private double[] waveformData;
    private double sampleRate;
    private boolean isLogarithmic = false;

    public SpectrumAnalyzer() {
        fftCalculator = new FFTCalculator();
        stage = new Stage();
        stage.setTitle("Spectrum Analyzer");

        xAxis = new NumberAxis();
        xAxis.setLabel("Frequency (Hz)");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(5000); // Initial value, adjusted dynamically
        xAxis.setTickUnit(1000);

        yAxis = new NumberAxis();
        yAxis.setLabel("Magnitude (Linear)");
        yAxis.setAutoRanging(true);

        spectrumChart = new LineChart<>(xAxis, yAxis);
        spectrumChart.setCreateSymbols(false);
        spectrumChart.setLegendVisible(false);

        series = new XYChart.Series<>();
        spectrumChart.getData().add(series);

        peakLabel = new Label("Peak: 0 Hz");
        peakLabel.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: #000000;" +
                        "-fx-padding: 5;"
        );

        Button toggleScaleButton = new Button("Switch to Log");
        toggleScaleButton.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-border-color: #666666 #333333 #333333 #666666;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 5;"
        );
        toggleScaleButton.setOnAction(e -> {
            isLogarithmic = !isLogarithmic;
            toggleScaleButton.setText(isLogarithmic ? "Switch to Linear" : "Switch to Log");
            yAxis.setLabel(isLogarithmic ? "Magnitude (dB)" : "Magnitude (Linear)");
            updateSpectrum();
        });

        HBox controls = new HBox(10, toggleScaleButton);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setCenter(spectrumChart);
        root.setTop(peakLabel);
        root.setBottom(controls);
        BorderPane.setAlignment(peakLabel, Pos.CENTER);

        Scene scene = new Scene(root, 800, 600);

        // Embedded CSS styling
        spectrumChart.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: #666666 #333333 #333333 #666666;" +
                        "-fx-border-width: 2;"
        );
        spectrumChart.lookup(".chart-plot-background").setStyle("-fx-background-color: #000000;");
        spectrumChart.lookup(".chart-series-line").setStyle("-fx-stroke: #FFFFCC; -fx-stroke-width: 2;");
        xAxis.setStyle(
                "-fx-tick-label-fill: #FFFFCC;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 10px;" +
                        "-fx-minor-tick-visible: false;" +
                        "-fx-major-tick-visible: true;" +
                        "-fx-tick-mark-visible: true;" +
                        "-fx-grid-lines-visible: true;" +
                        "-fx-tick-length: 8;"
        );
        yAxis.setStyle(
                "-fx-tick-label-fill: #FFFFCC;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 10px;"
        );

        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }

    public void setWaveformData(double[] waveform, double duration) {
        if (waveform == null || waveform.length == 0 || duration <= 0) {
            waveformData = null;
            return;
        }
        this.waveformData = waveform;
        this.sampleRate = 1024 / duration; // Adjusted for 1024-sample FFT
        xAxis.setUpperBound(sampleRate / 2); // Dynamic Nyquist frequency
        xAxis.setTickUnit(sampleRate / 10); // Adjust tick spacing
        updateSpectrum();
    }

    public void startUpdating() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (waveformData != null) {
                    updateSpectrum();
                }
            }
        };
        timer.start();
    }

    private void updateSpectrum() {
        series.getData().clear();
        if (waveformData == null) return;

        double[] magnitude = fftCalculator.computeFFTMagnitude(waveformData);
        double freqStep = sampleRate / 1024; // Adjusted for 1024-sample FFT
        double maxMagnitude = 0;
        double peakFrequency = 0;

        for (int i = 0; i < magnitude.length; i++) {
            double frequency = i * freqStep;
            if (frequency > sampleRate / 2) break;
            double value = isLogarithmic ? (magnitude[i] > 0 ? 20 * Math.log10(magnitude[i]) : -100) : magnitude[i];
            series.getData().add(new XYChart.Data<>(frequency, value));
            if (magnitude[i] > maxMagnitude) {
                maxMagnitude = magnitude[i];
                peakFrequency = frequency;
            }
        }

        peakLabel.setText(String.format("Peak: %.0f Hz", peakFrequency));
    }

    public void close() {
        stage.close();
    }
}