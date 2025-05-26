package simulation.software.codebase;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    private SweepGeneratorController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/simulation/software/codebase/SweepGenerator.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1300, 800);
        scene.getStylesheets().add(getClass().getResource("/simulation/software/codebase/Theme.css").toExternalForm());
        controller = fxmlLoader.getController();
        primaryStage.setTitle("Sweep Generator");
        primaryStage.setScene(scene);

        // Handle main window close to also close Spectrum Analyzer, DAC, Frequency Counter, THD, and Phase Analyzer
        primaryStage.setOnCloseRequest(event -> {
            if (controller != null) {
                //controller.closeSpectrumAnalyzer();
                controller.closeDac();
                controller.closeFrequencyCounter();
                controller.closeThd();
                controller.closePhaseAnalyzer();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}