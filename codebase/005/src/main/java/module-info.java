module simulation.software.codebase.sweepgenerationsoftware {
    requires javafx.controls;
    requires javafx.fxml;


    opens simulation.software.codebase to javafx.fxml;
    exports simulation.software.codebase;
}