package simulation.software.codebase;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;

public class SweepGeneratorController {
    @FXML private Slider frequencyStartSlider;
    @FXML private Slider frequencyEndSlider;
    @FXML private Slider amplitudeSlider;
    @FXML private Slider sweepTimeSlider;
    @FXML private Slider noiseAmplitudeSlider;
    @FXML private Slider modulationIndexSlider;
    @FXML private Slider modulationFrequencySlider;
    @FXML private Label frequencyStartLabel;
    @FXML private Label frequencyEndLabel;
    @FXML private Label amplitudeLabel;
    @FXML private Label sweepTimeLabel;
    @FXML private Label noiseAmplitudeLabel;
    @FXML private Label modulationIndexLabel;
    @FXML private Label modulationFrequencyLabel;
    @FXML private Button sineButton;
    @FXML private Button squareButton;
    @FXML private Button triangleButton;
    @FXML private Button startButton;
    @FXML private Button spectrumButton;
    @FXML private LineChart<Number, Number> waveformChart;
    @FXML private ComboBox<String> sweepTypeComboBox;
    @FXML private ComboBox<String> noiseTypeComboBox;
    @FXML private ComboBox<String> modulationTypeComboBox;
    @FXML private TextArea tableInputArea;

    private WaveformGenerator waveformGenerator;
    private FrequencySweep frequencySweep;
    private SteppedSweep steppedSweep;
    private TimeSweep timeSweep;
    private TableSweep tableSweep;
    private XYChart.Series<Number, Number> series;
    private AnimationTimer timer;
    private boolean isRunning;
    private double startTime;
    private String waveformType = "Sine";
    private String sweepType = "Linear";
    private String noiseType = "None";
    private String modulationType = "None";
    private SpectrumAnalyzer spectrumAnalyzer;

    @FXML
    public void initialize() {
        waveformGenerator = new WaveformGenerator();
        frequencySweep = new FrequencySweep();
        steppedSweep = new SteppedSweep();
        timeSweep = new TimeSweep();
        tableSweep = new TableSweep();
        series = new XYChart.Series<>();
        waveformChart.getData().add(series);
        waveformChart.setLegendVisible(false);
        waveformChart.setCreateSymbols(false);

        // Bind slider values to labels
        frequencyStartLabel.textProperty().bind(frequencyStartSlider.valueProperty().asString("%.0f Hz"));
        frequencyEndLabel.textProperty().bind(frequencyEndSlider.valueProperty().asString("%.0f Hz"));
        amplitudeLabel.textProperty().bind(amplitudeSlider.valueProperty().asString("%.2f V"));
        sweepTimeLabel.textProperty().bind(sweepTimeSlider.valueProperty().asString("%.1f s"));
        noiseAmplitudeLabel.textProperty().bind(noiseAmplitudeSlider.valueProperty().asString("%.2f V"));
        modulationIndexLabel.textProperty().bind(modulationIndexSlider.valueProperty().asString("%.2f"));
        modulationFrequencyLabel.textProperty().bind(modulationFrequencySlider.valueProperty().asString("%.0f Hz"));

        // Initialize sweep type combo box
        sweepTypeComboBox.setItems(FXCollections.observableArrayList(
                "Linear", "Logarithmic", "Bidirectional", "Stepped Linear", "Stepped Log", "Time", "Table"
        ));
        sweepTypeComboBox.setValue("Linear");
        sweepTypeComboBox.setOnAction(e -> {
            sweepType = sweepTypeComboBox.getValue();
            tableInputArea.setDisable(!sweepType.equals("Table"));
            if (sweepType.equals("Table")) {
                tableSweep.parseTable(tableInputArea.getText());
            }
            updateChartAxes();
        });

        // Initialize noise type combo box
        noiseTypeComboBox.setItems(FXCollections.observableArrayList(
                "None", "White", "Pink", "Brownian"
        ));
        noiseTypeComboBox.setValue("None");
        noiseTypeComboBox.setOnAction(e -> {
            noiseType = noiseTypeComboBox.getValue();
        });

        // Initialize modulation type combo box
        modulationTypeComboBox.setItems(FXCollections.observableArrayList(
                "None", "AM", "FM", "PM"
        ));
        modulationTypeComboBox.setValue("None");
        modulationTypeComboBox.setOnAction(e -> {
            modulationType = modulationTypeComboBox.getValue();
            updateModulationControls();
        });

        // Initialize button actions
        sineButton.setOnAction(e -> setWaveformType("Sine"));
        squareButton.setOnAction(e -> setWaveformType("Square"));
        triangleButton.setOnAction(e -> setWaveformType("Triangle"));
        startButton.setOnAction(e -> toggleSweep());
        spectrumButton.setOnAction(e -> showSpectrumAnalyzer());

        // Set retro button styles
        sineButton.getStyleClass().add("retro-button");
        squareButton.getStyleClass().add("retro-button");
        triangleButton.getStyleClass().add("retro-button");
        startButton.getStyleClass().add("retro-button");
        spectrumButton.getStyleClass().add("retro-button");
        sweepTypeComboBox.getStyleClass().add("retro-combo-box");
        noiseTypeComboBox.getStyleClass().add("retro-combo-box");
        modulationTypeComboBox.getStyleClass().add("retro-combo-box");
        tableInputArea.getStyleClass().add("retro-text-area");

        // Initialize animation timer
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateWaveform();
            }
        };

        // Initialize chart axes and modulation controls
        updateChartAxes();
        updateModulationControls();
    }

    private void setWaveformType(String type) {
        waveformType = type;
        sineButton.setDisable(type.equals("Sine"));
        squareButton.setDisable(type.equals("Square"));
        triangleButton.setDisable(type.equals("Triangle"));
    }

    private void toggleSweep() {
        if (isRunning) {
            timer.stop();
            startButton.setText("Start Sweep");
            isRunning = false;
            closeSpectrumAnalyzer();
        } else {
            startTime = System.nanoTime() / 1_000_000_000.0;
            if (sweepType.equals("Table")) {
                tableSweep.parseTable(tableInputArea.getText());
            }
            timer.start();
            startButton.setText("Stop Sweep");
            isRunning = true;
        }
    }

    private void showSpectrumAnalyzer() {
        if (spectrumAnalyzer == null) {
            spectrumAnalyzer = new SpectrumAnalyzer();
            spectrumAnalyzer.show();
            spectrumAnalyzer.startUpdating();
        }
    }

    public void closeSpectrumAnalyzer() {
        if (spectrumAnalyzer != null) {
            spectrumAnalyzer.close();
            spectrumAnalyzer = null;
        }
    }

    private void updateChartAxes() {
        if (sweepType.equals("Time")) {
            waveformChart.getXAxis().setLabel("Time (s)");
            ((javafx.scene.chart.NumberAxis) waveformChart.getXAxis()).setLowerBound(0);
            ((javafx.scene.chart.NumberAxis) waveformChart.getXAxis()).setUpperBound(sweepTimeSlider.getValue());
            ((javafx.scene.chart.NumberAxis) waveformChart.getXAxis()).setTickUnit(sweepTimeSlider.getValue() / 4);
        } else {
            waveformChart.getXAxis().setLabel("Time (ms)");
            ((javafx.scene.chart.NumberAxis) waveformChart.getXAxis()).setLowerBound(0);
            ((javafx.scene.chart.NumberAxis) waveformChart.getXAxis()).setUpperBound(20);
            ((javafx.scene.chart.NumberAxis) waveformChart.getXAxis()).setTickUnit(5);
        }
    }

    private void updateModulationControls() {
        boolean isModulationEnabled = !modulationType.equals("None");
        modulationIndexSlider.setDisable(!isModulationEnabled);
        modulationFrequencySlider.setDisable(!isModulationEnabled);
    }

    private void updateWaveform() {
        double currentTime = System.nanoTime() / 1_000_000_000.0 - startTime;
        double sweepTime = sweepTimeSlider.getValue();
        double startFreq = frequencyStartSlider.getValue();
        double endFreq = frequencyEndSlider.getValue();
        double amplitude = amplitudeSlider.getValue();
        double noiseAmplitude = noiseAmplitudeSlider.getValue();
        double modulationIndex = modulationIndexSlider.getValue();
        double modulationFrequency = modulationFrequencySlider.getValue();

        series.getData().clear();
        double duration = sweepType.equals("Time") ? sweepTime : 0.02;
        double[] time = waveformGenerator.generateTimeArray(duration);
        double currentFreq = startFreq;
        double currentAmplitude = amplitude;

        switch (sweepType) {
            case "Linear":
            case "Logarithmic":
            case "Bidirectional":
                currentFreq = frequencySweep.calculateFrequency(sweepType, startFreq, endFreq, currentTime, sweepTime);
                if (currentTime > sweepTime) {
                    startTime = System.nanoTime() / 1_000_000_000.0;
                }
                break;
            case "Stepped Linear":
            case "Stepped Log":
                currentFreq = steppedSweep.calculateFrequency(startFreq, endFreq, currentTime, sweepTime, sweepType.equals("Stepped Log"));
                if (currentTime > sweepTime) {
                    startTime = System.nanoTime() / 1_000_000_000.0;
                }
                break;
            case "Time":
                currentFreq = startFreq;
                currentAmplitude = timeSweep.calculateAmplitude(amplitude, currentTime, sweepTime);
                if (currentTime > sweepTime) {
                    startTime = System.nanoTime() / 1_000_000_000.0;
                }
                time = waveformGenerator.generateTimeArray(sweepTime);
                break;
            case "Table":
                currentFreq = tableSweep.getCurrentFrequency(currentTime, sweepTime);
                currentAmplitude = tableSweep.getCurrentAmplitude(currentTime, sweepTime);
                if (currentTime > sweepTime) {
                    startTime = System.nanoTime() / 1_000_000_000.0;
                }
                break;
        }

        double[] waveform = waveformGenerator.generateWaveform(waveformType, currentFreq, currentAmplitude, time, noiseType, noiseAmplitude, modulationType, modulationIndex, modulationFrequency);
        for (int i = 0; i < time.length; i++) {
            double xValue = sweepType.equals("Time") ? time[i] : time[i] * 1000;
            series.getData().add(new XYChart.Data<>(xValue, waveform[i]));
        }

        // Update spectrum analyzer if open
        if (spectrumAnalyzer != null) {
            spectrumAnalyzer.setWaveformData(waveform, sweepType.equals("Time") ? sweepTime : 0.02);
        }
    }
}