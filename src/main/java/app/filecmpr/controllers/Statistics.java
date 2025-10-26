package app.filecmpr.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;

public class Statistics {

    @FXML private BarChart<String, Number> sizeChart;
    @FXML private PieChart ratioChart;
    @FXML private Label lblSummary;

    @FXML
    public void initialize() {
        // Datos de ejemplo — más adelante los llenarás con valores reales
        updateStatistics(500, 200, 0.6);  // original, comprimido, ratio
    }

    public void updateStatistics(double originalKB, double processedKB, double compressionRatio) {
        // --- BarChart ---
        sizeChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tamaño");
        series.getData().add(new XYChart.Data<>("Original", originalKB));
        series.getData().add(new XYChart.Data<>("Procesado", processedKB));

        sizeChart.getData().add(series);

        // --- PieChart ---
        ratioChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Ahorro", originalKB - processedKB),
                new PieChart.Data("Tamaño restante", processedKB)
        ));

        // --- Resumen textual ---
        lblSummary.setText(String.format(
                "Tamaño original: %.2f KB\n" +
                        "Tamaño procesado: %.2f KB\n" +
                        "Tasa de compresión: %.1f%%\n" +
                        "Reducción total: %.2f KB",
                originalKB, processedKB, (compressionRatio * 100), (originalKB - processedKB)
        ));
    }
}
