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
import java.text.SimpleDateFormat;
import java.util.Date;

public class TotalHarmonicDistortion {
    private final Stage stage;
    private final LineChart<Number, Number> spectrumChart;
    private final XYChart.Series<Number, Number> spectrumSeries;
    private final XYChart.Series<Number, Number> harmonicSeries;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final Label thdLabel;
    private final Label fundamentalLabel;
    private final Slider windowSizeSlider;
    private final Slider harmonicCountSlider;
    private double[] waveformData;
    private double duration;
    private double lastTHD;
    private double lastFundamentalFreq;
    private double[] lastHarmonicAmplitudes;
    private FFTCalculator fftCalculator;

    public TotalHarmonicDistortion() {
        stage = new Stage();
        stage.setTitle("THD Simulation");

        // Chart setup
        xAxis = new NumberAxis(0, 10000, 1000);
        xAxis.setLabel("Frequency (Hz)");
        xAxis.setAutoRanging(false);

        yAxis = new NumberAxis(0, 10, 1);
        yAxis.setLabel("Magnitude");
        yAxis.setAutoRanging(false);

        spectrumChart = new LineChart<>(xAxis, yAxis);
        spectrumChart.setCreateSymbols(false);
        spectrumChart.setLegendVisible(false);
        spectrumChart.setPrefWidth(600);

        spectrumSeries = new XYChart.Series<>();
        harmonicSeries = new XYChart.Series<>();
        spectrumChart.getData().addAll(spectrumSeries, harmonicSeries);

        // Labels
        thdLabel = new Label("THD: 0.0%");
        fundamentalLabel = new Label("Fundamental: 0.0 Hz");
        for (Label label : new Label[]{thdLabel, fundamentalLabel}) {
            label.setStyle(
                    "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-color: #000000;" +
                            "-fx-padding: 5;"
            );
        }

        // Export button
        Button exportButton = new Button("Export THD Data");
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
        HBox topBar = new HBox(20, thdLabel, fundamentalLabel, exportButton);
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

        harmonicCountSlider = new Slider(2, 10, 5);
        harmonicCountSlider.setMajorTickUnit(2);
        harmonicCountSlider.setMinorTickCount(0);
        harmonicCountSlider.setShowTickMarks(true);
        harmonicCountSlider.setShowTickLabels(true);
        harmonicCountSlider.setSnapToTicks(true);
        harmonicCountSlider.setStyle(
                "-fx-control-inner-background: #000000;" +
                        "-fx-text-fill: #FFFFCC;" +
                        "-fx-tick-label-fill: #FFFFFF;"
        );

        Label windowSizeLabel = new Label("Window Size (ms):");
        Label harmonicCountLabel = new Label("Harmonic Count:");
        Label controlsHeader = new Label("THD Settings");
        for (Label label : new Label[]{windowSizeLabel, harmonicCountLabel}) {
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
                new HBox(10, harmonicCountLabel, harmonicCountSlider)
        );
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setPrefWidth(200);
        controlPanel.setStyle("-fx-background-color: #000000;");

        // Main layout
        BorderPane root = new BorderPane();
        root.setCenter(spectrumChart);
        root.setTop(topBar);
        root.setRight(controlPanel);

        Scene scene = new Scene(root, 800, 600);

        // Embedded CSS
        spectrumChart.setStyle(
                "-fx-background-color: #000000;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: #666666 #333333 #333333 #666666;" +
                        "-fx-border-width: 2;"
        );
        spectrumChart.lookup(".chart-plot-background").setStyle("-fx-background-color: #000000;");
        spectrumChart.lookupAll(".chart-series-line").forEach(node -> {
            String stroke = node.getStyle().contains("harmonic") ? "#FF0000" : "#FFFFCC";
            node.setStyle("-fx-stroke: " + stroke + "; -fx-stroke-width: 2;");
        });
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
        windowSizeSlider.valueProperty().addListener((obs, old, newVal) -> updateTHD());
        harmonicCountSlider.valueProperty().addListener((obs, old, newVal) -> updateTHD());
    }

    public void show() {
        stage.show();
    }

    public void setWaveformData(double[] waveform, double duration) {
        if (waveform == null || waveform.length == 0 || duration <= 0) {
            waveformData = null;
            spectrumSeries.getData().clear();
            harmonicSeries.getData().clear();
            thdLabel.setText("THD: N/A");
            fundamentalLabel.setText("Fundamental: N/A");
            return;
        }
        this.waveformData = waveform;
        this.duration = duration;
        updateTHD();
    }

    public void startUpdating() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (waveformData != null) {
                    updateTHD();
                }
            }
        };
        timer.start();
    }

    private void updateTHD() {
        spectrumSeries.getData().clear();
        harmonicSeries.getData().clear();
        if (waveformData == null) return;

        double windowSize = windowSizeSlider.getValue() / 1000; // ms to s
        int harmonicCount = (int) harmonicCountSlider.getValue();
        int sampleRate = 44100; // Default, matches SpectrumAnalyzer

        // Extract windowed data
        int windowSamples = (int) (windowSize * sampleRate);
        if (windowSamples > waveformData.length) windowSamples = waveformData.length;
        if (windowSamples < 2) {
            thdLabel.setText("THD: N/A");
            fundamentalLabel.setText("Fundamental: N/A");
            return;
        }

        double[] windowedWaveform = new double[windowSamples];
        for (int i = 0; i < windowSamples; i++) {
            windowedWaveform[i] = waveformData[i % waveformData.length];
        }

        // Compute FFT
        double[] fftMagnitude = fftCalculator.computeFFTMagnitude(windowedWaveform);
        if (fftMagnitude.length == 0) {
            thdLabel.setText("THD: N/A");
            fundamentalLabel.setText("Fundamental: N/A");
            return;
        }

        // Frequency resolution
        double frequencyResolution = sampleRate / (double) fftMagnitude.length / 2;

        // Find fundamental frequency
        int fundamentalIndex = 0;
        double maxMagnitude = 0;
        for (int i = 1; i < fftMagnitude.length; i++) {
            if (fftMagnitude[i] > maxMagnitude) {
                maxMagnitude = fftMagnitude[i];
                fundamentalIndex = i;
            }
        }
        double fundamentalFreq = fundamentalIndex * frequencyResolution;

        // Compute harmonic amplitudes
        double fundamentalPower = fftMagnitude[fundamentalIndex] * fftMagnitude[fundamentalIndex];
        double harmonicPower = 0;
        lastHarmonicAmplitudes = new double[harmonicCount];
        for (int n = 2; n <= harmonicCount + 1; n++) {
            int harmonicIndex = n * fundamentalIndex;
            if (harmonicIndex < fftMagnitude.length) {
                lastHarmonicAmplitudes[n - 2] = fftMagnitude[harmonicIndex];
                harmonicPower += fftMagnitude[harmonicIndex] * fftMagnitude[harmonicIndex];
                harmonicSeries.getData().add(new XYChart.Data<>(harmonicIndex * frequencyResolution, fftMagnitude[harmonicIndex]));
            }
        }

        // Compute THD
        double thd = fundamentalPower > 0 ? Math.sqrt(harmonicPower / fundamentalPower) * 100 : 0;
        lastTHD = thd;
        lastFundamentalFreq = fundamentalFreq;

        // Update labels
        thdLabel.setText(String.format("THD: %.2f%%", thd));
        fundamentalLabel.setText(String.format("Fundamental: %.1f Hz", fundamentalFreq));

        // Update spectrum chart
        for (int i = 0; i < fftMagnitude.length; i++) {
            double frequency = i * frequencyResolution;
            if (frequency > 10000) break;
            spectrumSeries.getData().add(new XYChart.Data<>(frequency, fftMagnitude[i]));
        }
        harmonicSeries.getData().add(new XYChart.Data<>(fundamentalFreq, fftMagnitude[fundamentalIndex]));
    }

    private void exportData() {
        if (waveformData == null || lastHarmonicAmplitudes == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save THD Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("thd_data.csv");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Timestamp,THD,FundamentalFrequency");
                for (int i = 2; i <= lastHarmonicAmplitudes.length + 1; i++) {
                    writer.write(",Harmonic" + i);
                }
                writer.write("\n");
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.write(String.format("%s,%.6f,%.6f", timestamp, lastTHD, lastFundamentalFreq));
                for (double amp : lastHarmonicAmplitudes) {
                    writer.write(String.format(",%.6f", amp));
                }
                writer.write("\n");
            } catch (IOException e) {
                System.err.println("Error writing CSV file: " + e.getMessage());
            }
        }
    }

    public void close() {
        stage.close();
    }
}