package app.filecmpr.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import java.io.File;
import java.text.DecimalFormat;
import app.filecmpr.utils.AppState;

public class Statistics {
    @FXML private BarChart<String, Number> sizeChart;
    @FXML private PieChart ratioChart;
    @FXML private Label lblSummary;

    private final DecimalFormat df = new DecimalFormat("0.00");

    @FXML
    public void initialize() {
        lblSummary.setText("Esperando datos...");

        if (AppState.lastOriginal != null && AppState.lastProcessed != null) {
            updateStatistics(AppState.lastOriginal, AppState.lastProcessed, AppState.lastTime);
        }
    }

    public void updateStatistics(File originalFile, File processedFile, long timeMs) {
        if (!originalFile.exists() || !processedFile.exists()) {
            lblSummary.setText("Archivos no válidos.");
            return;
        }

        double originalKB = originalFile.length() / 1024.0;
        double processedKB = processedFile.length() / 1024.0;
        double ratio = processedKB / originalKB;
        double saving = originalKB - processedKB;

        sizeChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Tamaño (KB)");
        s.getData().add(new XYChart.Data<>("Original", originalKB));
        s.getData().add(new XYChart.Data<>("Procesado", processedKB));
        sizeChart.getData().add(s);

        ratioChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Ahorro", Math.max(0, saving)),
                new PieChart.Data("Restante", processedKB)
        ));

        lblSummary.setText(
                "Original: " + df.format(originalKB) + " KB\n" +
                        "Procesado: " + df.format(processedKB) + " KB\n" +
                        "Tasa: " + df.format((1 - ratio) * 100) + "%\n" +
                        "Tiempo: " + timeMs + " ms"
        );
    }
}
